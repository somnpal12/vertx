package com.sample.vert_web_crud.verticles;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
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
    router.get("/api/employee/:id").handler(this::getEmployeeById);
    router.post("/api/employee").handler(this::saveEmployee);
    router.put("/api/employee").handler(this::updateEmployee);
    router.delete("/api/employee/:id").handler(this::deleteEmployee);

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

  private  void deleteEmployee(RoutingContext routingContext){
    String employeeId = routingContext.request().getParam("id");
    logger.debug(" id : {}", employeeId );

    routingContext.response().setStatusCode(200).end(employeeId);
  }

  private  void updateEmployee(RoutingContext routingContext){
    JsonObject jsonObject =  routingContext.getBodyAsJson();
    logger.debug(jsonObject.toString());
    routingContext.response().setStatusCode(200).end(jsonObject.toString());
  }

  private void saveEmployee(RoutingContext routingContext){
    JsonObject jsonObject =  routingContext.getBodyAsJson();
    DeliveryOptions options = new DeliveryOptions();
    logger.debug(jsonObject.toString());
    vertx.eventBus().request("SaveEmployee",jsonObject,options,reply ->{
        if(reply.succeeded()){
          JsonArray jsonArray = (JsonArray) reply.result().body();
          routingContext.response().putHeader("content-type", "application/json").setStatusCode(201).end(jsonArray.toString());
        }else {
          ReplyException  replyException = (ReplyException) reply.cause();
          routingContext.response().putHeader("content-type", "application/json").setStatusCode(replyException.failureCode()).end(replyException.getMessage());
        }
    });


  }

  private void getEmployeeById(RoutingContext routingContext){
    String employeeId = routingContext.request().getParam("id");
    logger.debug(" id : {}", employeeId );
    DeliveryOptions options = new DeliveryOptions().addHeader("employeeId",employeeId);

    vertx.eventBus().request("SearchEmployeeById", null,options, reply -> {
      if (reply.succeeded()) {
        logger.info(reply.result().body().toString());
        JsonArray jsonArray = (JsonArray) reply.result().body();
        routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
          .end(jsonArray.toString());
      } else {
         logger.warn("", reply.cause());
      }
    });
  }

  //https://github.com/vert-x3/vertx-service-proxy/blob/master/src/main/java/examples/Examples.java
  private void getEmployees(RoutingContext routingContext) {
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "employees");

    vertx.eventBus().request("ShowAllEmployee", null,options, reply -> {
        if (reply.succeeded()) {
          logger.info(reply.result().body().toString());
          JsonArray jsonArray = (JsonArray) reply.result().body();
          routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
            .end(jsonArray.toString());
        } else {
          // routingContext.response().setStatusCode(500).end("No records found...");
          logger.warn("", reply.cause());
        }
    });


  }
}
