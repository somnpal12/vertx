package com.sample.vert_web_crud.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessVerticle extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(DataAccessVerticle.class);
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    logger.debug("--1--");
    vertx.eventBus().consumer("ShowAllEmployee", this::responseHandler);
  }


  public void responseHandler(Message msg){
    logger.debug(msg.toString());
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("name","somnath");

    msg.reply(jsonObject);
  }

}
