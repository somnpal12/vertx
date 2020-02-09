package com.sample.poc;

import com.sample.model.Article;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class HelloVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Router router = Router.router(vertx);
    router.get("/api/articles/article/:id")
      .handler(this::article);

    System.out.println("Welcome to Vertx");
    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(9090,asyncResult -> {
            if(asyncResult.succeeded()){
              startFuture.complete();
            }else {
              startFuture.fail(asyncResult.cause());
            }
        });
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    System.out.println("Shutting down application");
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new HelloVerticle());
  }

  private void article(RoutingContext routingContext){
    String articleId = routingContext.request().getParam("id");
    Article article = new Article(articleId, "This is an intro to vertx", "Somnath");

    routingContext.response().putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(Json.encodePrettily(article));
  }
}
