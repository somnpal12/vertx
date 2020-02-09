package com.sample.poc;

import com.sample.model.Employee;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {
  private int port = 8080;
  private Map<Integer, Employee> employeeMap = new LinkedHashMap<>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    init();
    Router router = Router.router(vertx);

    router.route("/api/employee*").handler(BodyHandler.create());

    router.get("/api/employee").handler(this::getEmployees);
    router.get("/api/employee/:empid").handler(this::getEmployeesById);
    router.post("/api/employee").handler(this::createEmployee);
    router.put("/api/employee/:empid").handler(this::updateEmployee);
    router.delete("/api/employee/:empid").handler(this::deleteEmployee);


    vertx.createHttpServer().requestHandler(router).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }




  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new MainVerticle(), res -> {
      if (res.succeeded()) {
        System.out.println("Deployment id is: " + res.result());
      } else {
        System.out.println("Deployment failed!");
      }
    });
  }


  private void deleteEmployee(RoutingContext routingContext) {
    String id = routingContext.request().getParam("empid");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      Integer idAsInteger = Integer.valueOf(id);
      employeeMap.remove(idAsInteger);
    }
    routingContext.response().setStatusCode(204).end();
  }


  private void updateEmployee(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("empid");
    JsonObject json = routingContext.getBodyAsJson();
    if (id == null || json == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      final Integer idAsInteger = Integer.valueOf(id);
      Employee employee = employeeMap.get(idAsInteger);
      if (employee == null) {
        routingContext.response().setStatusCode(404).end();
      } else {
        employee.setFirstName(json.getString("firstName"));
        employee.setLastName(json.getString("lastName"));
        routingContext.response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(employee));
      }

    }
  }


  private void createEmployee(RoutingContext routingContext) {
    System.out.println(routingContext.getBodyAsString());
    Employee e = Json.decodeValue(routingContext.getBodyAsString(), Employee.class);
    employeeMap.put(e.getEmpId(), e);
    routingContext.response()
      .setStatusCode(201)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(employeeMap));
  }

  private void getEmployeesById(RoutingContext routingContext) {
    Integer empid = Integer.parseInt(routingContext.request().getParam("empid"));
    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "application/json")
      .end(Json.encodePrettily(employeeMap.get(empid)));

  }

  private void getEmployees(RoutingContext routingContext) {

    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "application/json")
      .end(Json.encodePrettily(employeeMap));

  }


  public void init() {
    Employee e1 = new Employee("A", "AA");
    Employee e2 = new Employee("B", "BB");
    Employee e3 = new Employee("C", "CC");

    employeeMap.put(e1.getEmpId(), e1);
    employeeMap.put(e2.getEmpId(), e2);
    employeeMap.put(e3.getEmpId(), e3);
  }
}
