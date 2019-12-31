package com.sample.vert_web_crud;

import com.sample.vert_web_crud.verticles.DataAccessVerticle;
import com.sample.vert_web_crud.verticles.WebControllerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
  List<AbstractVerticle> verticleList = Arrays.asList(new WebControllerVerticle(), new DataAccessVerticle());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    final AtomicInteger serviceCount = new AtomicInteger();
    verticleList.stream().forEach(verticle -> vertx.deployVerticle(verticle, response -> {
      if (response.failed()) {
        logger.info("unable to deploy verticle :: " + verticle.getClass().getSimpleName() + " : " + response.cause());
      } else {
        serviceCount.incrementAndGet();
        logger.info("All verticles deployed ::" + serviceCount.get());
      }
    }));
  }

  public static void main(final String... args) {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
