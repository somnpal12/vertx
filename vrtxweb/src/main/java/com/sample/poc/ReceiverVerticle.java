package com.sample.poc;

import com.sample.utils.ApplicationConstant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;

public class ReceiverVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer(ApplicationConstant.ADDRESS, message -> {
      dispatchMessage(message);
    });
  }
  private void dispatchMessage(final Message<Object> message) {
    System.out.println("message.body() :: " + message.body());
    try {
      String operation = message.body().toString();
      switch (operation) {
        case ApplicationConstant.OPERATION:
          message.reply("HELLO WORLD");
          break;
        default:
          System.out.println("Unable to handle operation "+ operation);
          message.reply("Unsupported operation");
      }
    }catch (final Exception ex) {
      System.out.println("Unable to handle operation due to exception" + message.body());
    }
  }
}
