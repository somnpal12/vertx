package com.sample.employee.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface DatabaseService {

     /*static DatabaseServiceImpl create(Vertx vertx){
         return  new DatabaseServiceImpl(vertx);
     }*/

    static  DatabaseService createProxy(Vertx vertx,String address){
        return new DatabaseServiceVertxEBProxy(vertx,address);
    }

    void save(String collection, JsonObject document,   Handler<AsyncResult<Void>> resultHandler);
    void fetchAllEmployee( Handler<AsyncResult<JsonArray>> resultHandler);
    void fetchEmployeeByById(String employeeId,  Handler<AsyncResult<JsonObject>> resultHandler);
}
