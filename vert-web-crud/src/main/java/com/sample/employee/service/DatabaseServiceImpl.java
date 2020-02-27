package com.sample.employee.service;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
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

import static com.sample.employee.utils.DatabaseQueries.QUERY_FETCH_ALL_EMPLOYEE;

public class DatabaseServiceImpl  implements DatabaseService {
    private final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    PgPool client = null;
    Vertx vertx;

    public DatabaseServiceImpl(Vertx vertx1) {
        this.vertx = vertx1;
        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(json -> {
            logger.debug("json");
            JsonObject config = json.result();
            logger.debug(config.getJsonObject("datasource").toString());

            client = dbInit(config.getJsonObject("datasource"));

        });
    }

    @Override
    public void save(String collection, JsonObject document, Handler<AsyncResult<Void>> resultHandler) {

    }

    @Override
    public void fetchAllEmployee(Handler<AsyncResult<JsonArray>> resultHandler) {
        client.getConnection(sqlConnectionAsyncResult -> {
            if (sqlConnectionAsyncResult.succeeded()) {
                logger.info("db connection succeed  {}", QUERY_FETCH_ALL_EMPLOYEE);
                SqlConnection conn = sqlConnectionAsyncResult.result();
                conn.query(QUERY_FETCH_ALL_EMPLOYEE, ar -> {
                    if (ar.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(getResult(ar)));

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

    @Override
    public void fetchEmployeeByById(String employeeId, Handler<AsyncResult<JsonObject>> resultHandler) {

    }

    private JsonArray getResult(AsyncResult<RowSet<Row>> rowSetAsyncResult) {
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
        return  jsonArray;

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
