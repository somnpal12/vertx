package com.sample.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreFirstVerticle extends AbstractVerticle {
  private final Logger log = LoggerFactory.getLogger(CoreFirstVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start......");



    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        log.info("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
    log.info("started......");
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new CoreFirstVerticle());
  }
}
