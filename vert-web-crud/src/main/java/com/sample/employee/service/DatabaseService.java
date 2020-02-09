package com.sample.employee.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@VertxGen
@ProxyGen
public interface DatabaseService {



    static  DatabaseService createProxy(Vertx vertx,String address){
        return new DatabaseServiceVertxEBProxy(vertx,address);
    }
}
