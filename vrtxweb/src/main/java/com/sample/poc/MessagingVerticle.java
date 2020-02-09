package com.sample.poc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagingVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    List<AbstractVerticle> verticleList = Arrays.asList(new ReceiverVerticle(),new SenderVerticle());
    final AtomicInteger serviceCount = new AtomicInteger();

    verticleList.stream().forEach(verticle -> vertx.deployVerticle(verticle,response -> {
      if(response.failed()){
        System.out.println("unable to deploy verticle :: " + verticle.getClass().getSimpleName() + " : " + response.cause());
      }else{
        serviceCount.incrementAndGet();
      }
    } ));
  }

  public static void main(final String... args) {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MessagingVerticle());
  }
}
