package com.sample.crud;

import com.sample.poc.MessagingVerticle;
import com.sample.poc.ReceiverVerticle;
import com.sample.poc.SenderVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {




  @Override
  public void start() throws Exception {
    List<AbstractVerticle> verticleList = Arrays.asList(new WebControllerVerticle(), new DataAccessVerticle());

    final AtomicInteger serviceCount = new AtomicInteger();

    verticleList.stream().forEach(verticle -> vertx.deployVerticle(verticle, response -> {
      if (response.failed()) {
        System.out.println("unable to deploy verticle :: " + verticle.getClass().getSimpleName() + " : " + response.cause());
      } else {
        serviceCount.incrementAndGet();
        System.out.println("All verticles deployed ::" + serviceCount.get());
      }
    }));
  }

  public static void main(final String... args) {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
