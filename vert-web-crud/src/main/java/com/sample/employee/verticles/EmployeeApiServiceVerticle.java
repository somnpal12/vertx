package com.sample.employee.verticles;

import com.sample.employee.service.DatabaseService;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeApiServiceVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(EmployeeApiServiceVerticle.class);
    DatabaseService databaseService;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        databaseService = DatabaseService.createProxy(vertx, "employee.service");

        Router router = Router.router(vertx);

        router.route("/api/v2/employee*").handler(BodyHandler.create());
        router.get("/api/v2/employee").handler(this::getEmployees);


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
    private void getEmployees(RoutingContext routingContext) {
        databaseService.fetchAllEmployee( res -> {
            if(res.succeeded()){
                JsonArray jsonArray = res.result();
                log.debug(">>>>>>>>>>>>"  + jsonArray.toString());
                routingContext.response().setStatusCode(200).end(jsonArray.toString());
            }else{
                routingContext.response().setStatusCode(500).end("Internal Server Error : " + res.cause().getMessage());
            }
        });
    }

}
