package com.sample.vert_web_crud.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessVerticle extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(DataAccessVerticle.class);
  PgPool client = null;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    logger.debug("--1--");
    client = dbInit();
    vertx.eventBus().consumer("ShowAllEmployee", this::fetchEmployeeListHandler);
    vertx.eventBus().consumer("SearchEmployeeById", this::findEmployeeByIdHandler);
  }

  private void findEmployeeByIdHandler(Message msg){
    logger.debug("employee id :: {} -- {} ", msg.headers().get("employeeId"),QUERY_FIND_EMPLOYEE_BY_ID);
    JsonArray params = new JsonArray().add(msg.headers().get("employeeId"));
    client.preparedQuery(QUERY_FIND_EMPLOYEE_BY_ID, Tuple.of(Integer.parseInt(msg.headers().get("employeeId"))),rowSetAsyncResult -> {
      if(rowSetAsyncResult.succeeded()){
        RowSet<Row> rows = rowSetAsyncResult.result();
        JsonArray jsonArray = new JsonArray();
        for (Row row : rows) {
          JsonObject jsonObject = new JsonObject();
          jsonObject.put(row.getColumnName(0).toLowerCase(), row.getValue(0));
          jsonObject.put(row.getColumnName(1).toLowerCase(), row.getValue(1));
          jsonObject.put(row.getColumnName(2).toLowerCase(), row.getValue(2));
          jsonObject.put(row.getColumnName(3).toLowerCase(), row.getValue(3));
          jsonObject.put(row.getColumnName(4).toLowerCase(), row.getValue(4));
          jsonArray.add(jsonObject);

        }
        logger.debug(jsonArray.toString());
        msg.reply(jsonArray);
      }else{
        logger.error("failed :: ",rowSetAsyncResult.cause());
      }
      client.close();
    });

  }

  public void fetchEmployeeListHandler(Message msg) {

    logger.debug("action :" + msg.headers().get("action"));

    getEmployeeList(msg);
  }


  private void getEmployeeList(Message msg) {

    client.getConnection(sqlConnectionAsyncResult -> {
      if (sqlConnectionAsyncResult.succeeded()) {
        logger.info("db connection succeed  {}",QUERY_FETCH_ALL_EMPLOYEE);
        SqlConnection conn = sqlConnectionAsyncResult.result();
        conn.query(QUERY_FETCH_ALL_EMPLOYEE, ar -> {
          if (ar.succeeded()) {
            RowSet<Row> rows = ar.result();
            JsonArray jsonArray = new JsonArray();
            for (Row row : rows) {
              JsonObject jsonObject = new JsonObject();
              jsonObject.put(row.getColumnName(0).toLowerCase(), row.getValue(0));
              jsonObject.put(row.getColumnName(1).toLowerCase(), row.getValue(1));
              jsonObject.put(row.getColumnName(2).toLowerCase(), row.getValue(2));
              jsonObject.put(row.getColumnName(3).toLowerCase(), row.getValue(3));
              jsonObject.put(row.getColumnName(4).toLowerCase(), row.getValue(4));
              jsonArray.add(jsonObject);

            }
            logger.debug(jsonArray.toString());
            //msg.reply(new JsonObject().put("employee",jsonArray));
            msg.reply(jsonArray);
          } else {
            logger.error("", ar.cause());
          }
          conn.close();
        });
      } else {
        logger.warn("unable to connect database. ");
      }
    });
  }

  private PgPool dbInit() {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres").setLogActivity(true)/*.addProperty("search_path","SAMPLE")*/;

    // Pool options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the client pool
    return PgPool.pool(vertx, connectOptions, poolOptions);

  }

  private static String QUERY_FETCH_ALL_EMPLOYEE = "SELECT * FROM sample.\"EMPLOYEE\"";
  private static String QUERY_FIND_EMPLOYEE_BY_ID = "SELECT * FROM sample.\"EMPLOYEE\" WHERE \"ID\" = $1";
  private static String QUERY_INSERT_EMPLOYEE = "INSERT INTO sample.\"EMPLOYEE\"(\"ID\", \"NAME\", \"AGE\", \"ADDRESS\", \"SALARY\") VALUES($1,$2,$3,$4)";
  private static String QUERY_UPDATE_EMPLOYEE = "UPDATE sample.\"EMPLOYEE\" SET \"SALARY\"=$1 WHERE \"ID\" = $2 " ;
  private static String QUERY_DELETE_EMPLOYEE = "DELETE FROM sample.\"EMPLOYEE\" WHERE \"ID\" = $1" ;


}

