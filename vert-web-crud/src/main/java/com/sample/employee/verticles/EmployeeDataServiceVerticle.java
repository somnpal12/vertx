package com.sample.employee.verticles;

import com.sample.employee.service.DatabaseService;
import com.sample.employee.service.DatabaseServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeDataServiceVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(EmployeeDataServiceVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("Registering DatabaseService");
        DatabaseService service = new DatabaseServiceImpl(vertx);
        new ServiceBinder(vertx).setAddress("employee.service").register(DatabaseService.class, service);


    }

}
