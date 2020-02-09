package com.sample.employee.verticles;


import io.vertx.config.ConfigRetriever;
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
    private final Logger log = LoggerFactory.getLogger(WebControllerVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);

        router.route("/api/employee*").handler(BodyHandler.create());
        router.get("/api/employee").handler(this::getEmployees);
        router.get("/api/employee/:id").handler(this::getEmployeeById);
        router.post("/api/employee").handler(this::saveEmployee);
        router.put("/api/employee/:id").handler(this::updateEmployee);
        router.delete("/api/employee/:id").handler(this::deleteEmployee);

        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(json -> {
            JsonObject config = json.result();
            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(config.getInteger("http.port"), httpServerAsyncResult -> {
                        if (httpServerAsyncResult.succeeded()) {
                            log.info("server running at {}",config.getInteger("http.port"));
                        } else {
                            log.warn("", httpServerAsyncResult.cause());
                        }
                    });
        });



    }

    private void deleteEmployee(RoutingContext routingContext) {
        String employeeId = routingContext.request().getParam("id");
        log.debug(" id : {}", employeeId);
        DeliveryOptions options = new DeliveryOptions().addHeader("employeeId", employeeId);
        vertx.eventBus().request("DeleteEmployee", null, options, ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).end("Record Deleted : " + employeeId);
            } else {
                ReplyException replyException = (ReplyException) ar.cause();
                routingContext.response().putHeader("content-type", "application/json").setStatusCode(replyException.failureCode()).end(replyException.getMessage());
            }
        });

    }

    private void updateEmployee(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext.getBodyAsJson();
        String employeeId = routingContext.request().getParam("id");
        DeliveryOptions options = new DeliveryOptions().addHeader("employeeId", employeeId);
        log.debug("id : {} , json body : {}", employeeId, jsonObject.toString());
        vertx.eventBus().request("UpdateEmployee", jsonObject, options, ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).end("Record updated : " + employeeId);
            } else {
                ReplyException replyException = (ReplyException) ar.cause();
                routingContext.response().putHeader("content-type", "application/json").setStatusCode(replyException.failureCode()).end(replyException.getMessage());
            }
        });

    }

    private void saveEmployee(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext.getBodyAsJson();
        DeliveryOptions options = new DeliveryOptions();
        log.debug(jsonObject.toString());
        vertx.eventBus().request("SaveEmployee", jsonObject, options, reply -> {
            if (reply.succeeded()) {
                JsonArray jsonArray = (JsonArray) reply.result().body();
                routingContext.response().putHeader("content-type", "application/json").setStatusCode(201).end(jsonArray.toString());
            } else {
                ReplyException replyException = (ReplyException) reply.cause();
                routingContext.response().putHeader("content-type", "application/json").setStatusCode(replyException.failureCode()).end(replyException.getMessage());
            }
        });


    }

    private void getEmployeeById(RoutingContext routingContext) {
        String employeeId = routingContext.request().getParam("id");
        log.debug(" id : {}", employeeId);
        DeliveryOptions options = new DeliveryOptions().addHeader("employeeId", employeeId);

        vertx.eventBus().request("SearchEmployeeById", null, options, reply -> {
            if (reply.succeeded()) {
                log.info(reply.result().body().toString());
                JsonArray jsonArray = (JsonArray) reply.result().body();
                routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
                        .end(jsonArray.toString());
            } else {
                log.warn("", reply.cause());
            }
        });
    }

    //https://github.com/vert-x3/vertx-service-proxy/blob/master/src/main/java/examples/Examples.java
    private void getEmployees(RoutingContext routingContext) {
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "employees");

        vertx.eventBus().request("ShowAllEmployee", null, options, reply -> {
            if (reply.succeeded()) {
                log.info(reply.result().body().toString());
                JsonArray jsonArray = (JsonArray) reply.result().body();
                routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
                        .end(jsonArray.toString());
            } else {
                // routingContext.response().setStatusCode(500).end("No records found...");
                log.warn("", reply.cause());
            }
        });


    }
}
