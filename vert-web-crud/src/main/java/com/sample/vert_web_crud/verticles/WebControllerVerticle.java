package com.sample.vert_web_crud.verticles;


import com.sample.vert_web_crud.MainVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebControllerVerticle extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(WebControllerVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route("/api/employee*").handler(BodyHandler.create());
    router.get("/api/employee").handler(this::getEmployees);


    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8181, httpServerAsyncResult -> {
        if (httpServerAsyncResult.succeeded()) {
          logger.info("server running at 8181");
        } else {
          logger.warn("", httpServerAsyncResult.cause());
        }
      });
  }

  //https://github.com/vert-x3/vertx-service-proxy/blob/master/src/main/java/examples/Examples.java
  private void getEmployees(RoutingContext routingContext) {
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "employees");

    vertx.eventBus().request("ShowAllEmployee", null,options, reply -> {
        if (reply.succeeded()) {
          logger.info(reply.result().body().toString());
          JsonObject jsonObject = (JsonObject) reply.result().body();
          routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
            .end(jsonObject.toString());
        } else {
          // routingContext.response().setStatusCode(500).end("No records found...");
          logger.warn("", reply.cause());
        }
    });


  }
}
