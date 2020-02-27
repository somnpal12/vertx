package com.sample.employee;

import com.sample.employee.verticles.EmployeeApiServiceVerticle;
import com.sample.employee.verticles.EmployeeDataServiceVerticle;
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
    //List<AbstractVerticle> verticleList = Arrays.asList(new WebControllerVerticle(), new DataAccessVerticle());
    List<AbstractVerticle> verticleList = Arrays.asList(new EmployeeApiServiceVerticle(), new EmployeeDataServiceVerticle());

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
