package com.sample.crud;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class WebControllerVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route("/api/employee*").handler(BodyHandler.create());
    router.get("/api/employee").handler(this::getEmployees);


    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8181,httpServerAsyncResult -> {
          if(httpServerAsyncResult.succeeded()){
            System.out.println("server running at 8181");
          }else{
            System.out.println(httpServerAsyncResult.cause().getStackTrace());
          }
      });
  }
//https://github.com/vert-x3/vertx-service-proxy/blob/master/src/main/java/examples/Examples.java
  private void getEmployees(RoutingContext routingContext){
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "employees");
    vertx.eventBus().request("ShowAllEmployee",options,reply -> {
      if(reply.succeeded()){
        JsonArray jsonArray = (JsonArray) reply.result().body();
        routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
          .end();
      }else{
        routingContext.response().setStatusCode(500).end("No records found...");
      }
    });



  }
}
