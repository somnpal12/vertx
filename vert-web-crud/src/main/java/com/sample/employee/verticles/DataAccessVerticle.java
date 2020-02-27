package com.sample.employee.verticles;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sample.employee.utils.DatabaseQueries.*;

public class DataAccessVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(DataAccessVerticle.class);
    PgPool client = null;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        logger.debug("--1--");

        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(json -> {
            logger.debug("json");
            JsonObject config = json.result();
            logger.debug(config.getJsonObject("datasource").toString());

            client = dbInit(config.getJsonObject("datasource"));
            vertx.eventBus().consumer("ShowAllEmployee", this::fetchEmployeeListHandler);
            vertx.eventBus().consumer("SearchEmployeeById", this::findEmployeeByIdHandler);
            vertx.eventBus().consumer("SaveEmployee", this::saveEmployeeHandler);
            vertx.eventBus().consumer("UpdateEmployee", this::updateEmployeeHandler);
            vertx.eventBus().consumer("DeleteEmployee", this::deleteEmployeeHandler);

        });





    }

    private void updateEmployeeHandler(Message message) {
        JsonObject jsonObject = (JsonObject) message.body();
        Tuple tuple = Tuple.of(jsonObject.getDouble("salary"), Integer.parseInt(message.headers().get("employeeId")));
        client.preparedQuery(QUERY_UPDATE_EMPLOYEE, tuple, ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonArray().add("Employee Data Updated !"));
            } else {
                logger.error("failed :: ", ar.cause());
                message.reply(ar.cause().getMessage());
            }
        });
    }

    private void deleteEmployeeHandler(Message message) {
        Tuple tuple = Tuple.of(Integer.parseInt(message.headers().get("employeeId")));
        client.preparedQuery(QUERY_DELETE_EMPLOYEE, tuple, ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonArray().add("Employee Data Deleted !"));
            } else {
                logger.error("failed :: ", ar.cause());
                message.reply(ar.cause().getMessage());
            }
        });
    }

    private void saveEmployeeHandler(Message msg) {
        JsonObject jsonObject = (JsonObject) msg.body();
        Tuple tuple = Tuple.of(jsonObject.getString("name"), jsonObject.getInteger("age"), jsonObject.getString("address"), jsonObject.getDouble("salary"));
        logger.debug(jsonObject.toString());
        logger.debug("Insert Query :: {}", QUERY_INSERT_EMPLOYEE);
        client.preparedQuery(QUERY_INSERT_EMPLOYEE, tuple, rowSetAsyncResult -> {
            if (rowSetAsyncResult.succeeded()) {
                msg.reply(new JsonArray().add("New Employee Created !"));
            } else {
                logger.error("failed :: ", rowSetAsyncResult.cause());
                msg.fail(500, rowSetAsyncResult.cause().getMessage());
            }
        });
        //client.close();
    }

    private void findEmployeeByIdHandler(Message msg) {
        logger.debug("employee id :: {} -- {} ", msg.headers().get("employeeId"), QUERY_FIND_EMPLOYEE_BY_ID);
        JsonArray params = new JsonArray().add(msg.headers().get("employeeId"));
        client.preparedQuery(QUERY_FIND_EMPLOYEE_BY_ID, Tuple.of(Integer.parseInt(msg.headers().get("employeeId"))), rowSetAsyncResult -> {
            if (rowSetAsyncResult.succeeded()) {
                getResult(msg, rowSetAsyncResult);
            } else {
                logger.error("failed :: ", rowSetAsyncResult.cause());
                msg.reply(rowSetAsyncResult.cause().getMessage());
            }
            //client.close();
        });

    }

    private void getResult(Message msg, AsyncResult<RowSet<Row>> rowSetAsyncResult) {
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
    }

    public void fetchEmployeeListHandler(Message msg) {

        logger.debug("action :" + msg.headers().get("action"));

        getEmployeeList(msg);
    }


    private void getEmployeeList(Message msg) {

        client.getConnection(sqlConnectionAsyncResult -> {
            if (sqlConnectionAsyncResult.succeeded()) {
                logger.info("db connection succeed  {}", QUERY_FETCH_ALL_EMPLOYEE);
                SqlConnection conn = sqlConnectionAsyncResult.result();
                conn.query(QUERY_FETCH_ALL_EMPLOYEE, ar -> {
                    if (ar.succeeded()) {
                        getResult(msg, ar);
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

    private PgPool dbInit(JsonObject dataSourceConfig) {
        logger.debug(">>> db init");
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(dataSourceConfig.getInteger("port"))
                .setHost(dataSourceConfig.getString("host"))
                .setDatabase(dataSourceConfig.getString("schema"))
                .setUser(dataSourceConfig.getString("user"))
                .setPassword(dataSourceConfig.getString("password")).setLogActivity(true)/*.addProperty("search_path","SAMPLE")*/;

        // Pool options
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        // Create the client pool
        return PgPool.pool(vertx, connectOptions, poolOptions);


    }



}

