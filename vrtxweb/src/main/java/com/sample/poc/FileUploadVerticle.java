package com.sample.poc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;

public class FileUploadVerticle extends AbstractVerticle {
  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new FileUploadVerticle(), res -> {
      if (res.succeeded()) {
        System.out.println("Deployment id is: " + res.result());
      } else {
        System.out.println("Deployment failed!");
      }
    });
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setUploadsDirectory("my-uploads"));

    router.post("/upload").handler(getCorsHandler()).handler(this::handleUploadFile);

    vertx.createHttpServer().requestHandler(router).listen(8081);
  }

  private CorsHandler getCorsHandler(){
    return  CorsHandler.create("*").allowedHeaders(allowedHeaders()).allowedMethods(allowMethods());
  }

  private Set<String> allowedHeaders(){
    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");
    allowedHeaders.add("X-PINGARUNER");

    return allowedHeaders;
  }

  private Set<HttpMethod> allowMethods(){
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
    return allowedMethods;
  }

  private void handleUploadFile(RoutingContext ctx){

      System.out.println("##################");
      for (FileUpload f : ctx.fileUploads()) {
        Buffer uploadedFile = vertx.fileSystem().readFileBlocking(f.uploadedFileName());

        System.out.println("Filename: " + f.fileName());
        System.out.println("Size: " + f.size());
      }

      ctx.response().end();

  }
}
