package com.sample.poc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;

public class Server extends AbstractVerticle {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new Server(), res -> {
      if (res.succeeded()) {
        System.out.println("Deployment id is: " + res.result());
      } else {
        System.out.println("Deployment failed!");
      }
    });
  }

  @Override
  public void start() throws Exception {

    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");
    allowedHeaders.add("X-PINGARUNER");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);
    /*
     * these methods aren't necessary for this sample,
     * but you may need them for your projects
     */
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);

    //
    CorsHandler corsHandler = CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods);
    Router router = Router.router(vertx);

    // Enable multipart form data parsing
    router.route().handler(corsHandler).handler(BodyHandler.create().setUploadsDirectory("uploads"));

    router.route("/").handler(routingContext -> {
      routingContext.response().putHeader("content-type", "text/html").end(
        "<form action=\"/form\" method=\"post\" enctype=\"multipart/form-data\">\n" +
          "    <div>\n" +
          "        <label for=\"name\">Select a file:</label>\n" +
          "        <input type=\"file\" name=\"file\" />\n" +
          "    </div>\n" +
          "    <div class=\"button\">\n" +
          "        <button type=\"submit\">Send</button>\n" +
          "    </div>" +
          "</form>"
      );
    });

    // handle the form
    router.post("/form").handler(ctx -> {
      ctx.response().putHeader("Content-Type", "text/plain");

      ctx.response().setChunked(true);

      for (FileUpload f : ctx.fileUploads()) {
        System.out.println("fileName()" + f.fileName());
        ctx.response().write("Filename: " + f.fileName());
        ctx.response().write("\n");
        ctx.response().write("Size: " + f.size());
        ctx.response().write("\n");
        ctx.response().write("Type: " + f.contentType());
        System.out.println("Type: " + f.contentType());
      }

      ctx.response().end();
    });

    vertx.createHttpServer().requestHandler(router).listen(8080);
  }
}
