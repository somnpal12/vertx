package com.sample.poc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseVerticle extends AbstractVerticle {

  private static Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    System.out.println("Connecting database");
    dbInit();

  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {

  }

  public void dbInit() {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres").setLogActivity(true)/*.addProperty("search_path","SAMPLE")*/;

    // Pool options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the client pool
    PgPool client = PgPool.pool(vertx, connectOptions, poolOptions);


    client.getConnection(sqlConnectionAsyncResult -> {
      if (sqlConnectionAsyncResult.succeeded()) {
        System.out.println("db connection succeed . ");
        SqlConnection conn = sqlConnectionAsyncResult.result();
        conn.query("SELECT * FROM sample.\"EMPLOYEE\"", ar -> {
          if (ar.succeeded()) {
            RowSet<Row> rows = ar.result();
            JsonArray jsonArray = new JsonArray();
            for (Row row: rows) {
              JsonObject jsonObject = new JsonObject();
              jsonObject.put(row.getColumnName(0).toLowerCase(),row.getValue(0));
              jsonObject.put(row.getColumnName(1).toLowerCase(),row.getValue(1));
              jsonObject.put(row.getColumnName(2).toLowerCase(),row.getValue(2));
              jsonObject.put(row.getColumnName(3).toLowerCase(),row.getValue(3));
              jsonObject.put(row.getColumnName(4).toLowerCase(),row.getValue(4));
              jsonArray.add(jsonObject);

            }
            System.out.println(jsonArray);

          } else {
            System.out.println(ar.cause());
          }
          conn.close();
        });
      } else {
        System.out.println("unable to connect database. ");
      }
    });


  }

  public static void main(String[] args) {
    //System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new DatabaseVerticle(), asyncResult -> {
      if (asyncResult.succeeded()) {
        System.out.println("deployed successfully");
      } else {
        System.out.println("Unable to deployed : " + asyncResult.cause());
      }
    });
  }
}
