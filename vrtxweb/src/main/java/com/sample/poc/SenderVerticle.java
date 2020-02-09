package com.sample.poc;

import com.sample.utils.ApplicationConstant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class SenderVerticle  extends AbstractVerticle {
  @Override
  public void start(Promise promise) throws Exception {
    Router router = Router.router(vertx);

    router.get("/api/publish").handler(this::sendMessage);


    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8181);
  }

  private void sendMessage(RoutingContext routingContext){
    vertx.eventBus().send(ApplicationConstant.ADDRESS, ApplicationConstant.OPERATION, response -> {
      if (response.succeeded()) {
        System.out.println("Received reply: " + response.result().body());
        routingContext.response().putHeader("content-type", "text/plain").end(response.result().body().toString());
      } else {
        System.out.println("No reply");
        routingContext.response().setStatusCode(500).end(response.cause().getMessage());
      }
    });


  }

/*  private void handleHttpRequest(HttpServerRequest httpRequest) {
    System.out.println(">>>> handleHttpRequest <<<<");
    vertx.eventBus().send(ApplicationConstant.ADDRESS, ApplicationConstant.OPERATION, response -> {
      if (response.succeeded()) {
        System.out.println("Received reply: " + response.result().body());
        httpRequest.response().end(response.result().body().toString());
      } else {
        System.out.println("No reply");
        httpRequest.response().setStatusCode(500).end(response.cause().getMessage());
      }
    });
  }*/
}
