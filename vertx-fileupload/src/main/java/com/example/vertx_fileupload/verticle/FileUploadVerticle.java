package com.example.vertx_fileupload.verticle;


import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

public class FileUploadVerticle extends AbstractVerticle {
  WebClient webClient = null;

  private final Logger log = LoggerFactory.getLogger(FileUploadVerticle.class);

  String base = "/kie-server/services/rest";
  String containerId  = "FileUploadProcess_1.0.0-SNAPSHOT";
  String processId = "FileUploadProcess.FileUpload" ;

  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    router.route("/api/upload*").handler(BodyHandler.create());
    router.post("/api/upload-process/initiate").handler(this::initiateProcess);
    router.post("/api/upload-process/start/:process").handler(this::startUploadingProcess);
    router.post("/api/upload-process/upload/:process").handler(this::uploadFileByTaskId);


    //These api's are used by JBPM processes only
    router.route("/api/employee*").handler(BodyHandler.create());
    router.get("/api/employee").handler(this::getEmployee);
    router.post("/api/employee").handler(this::saveEmployee);
    //router.put("/api/employee/:empid").handler(this::updateEmployee);
    router.post("/api/employee/:empid").handler(this::updateEmployee);


    vertx.createHttpServer().requestHandler(router).listen(8888, httpServerAsyncResult -> {
      if (httpServerAsyncResult.succeeded()) {
        log.info("server running at 8888");
      } else {
        log.warn("", httpServerAsyncResult.cause());
      }
    });
  }

  private void updateEmployee(RoutingContext routingContext){
    log.debug(routingContext.request().getParam("empid"));
    log.debug("file :" + routingContext.fileUploads());

    JsonObject jsonObject = routingContext.getBodyAsJson();
    log.debug("incoming payload .. " + jsonObject.toString());
    routingContext.response().putHeader("content-type", "application/json").setStatusCode(201).end("employee record updated.....");
  }

  private void saveEmployee(RoutingContext routingContext){

   JsonObject jsonObject = routingContext.getBodyAsJson();
    log.debug("incoming payload .. " + jsonObject.toString());
    routingContext.response().putHeader("content-type", "application/json").setStatusCode(201).end("employee created.....");
  }

  private void getEmployee(RoutingContext routingContext){
    log.info("starting get employee service");
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("name" , "somnath");
    //jsonObject.put("time", LocalDateTime.now().format(new DateTimeFormatter()));
   /* JsonArray jsonArray = new JsonArray();
    jsonArray.add(jsonObject);*/
    routingContext.response().putHeader("content-type", "application/json").setStatusCode(200).end(jsonObject.toString());
  }

  private void initiateProcess(RoutingContext routingContext){
    JsonObject jsonObject = new JsonObject();
   String url =  getUrl(base,"/server/containers/",containerId,"/processes/",processId,"/instances");
    //StringBuilder url = new StringBuilder(base).append("/server/containers/").append(containerId).append("/processes/").append(processId).append("/instances");
    log.info("initiate Url : " + url);
   // WebClient webClient = WebClient.create(vertx);
    webClient = WebClient.create(vertx);
    webClient.post(8080,"localhost",url.toString())
      .putHeader("Content-Type", "application/json")
      .putHeader("Accept", "application/json")
      .putHeader("Authorization", "Basic " + getToken("katy","katy"))
      .as(BodyCodec.string())
      .ssl(false)
      .sendJsonObject(jsonObject, httpResponseAsyncResult -> {
        if (httpResponseAsyncResult.succeeded()) {
          log.info(httpResponseAsyncResult.result().body());
          jsonObject.put("process-id",httpResponseAsyncResult.result().body());
          routingContext.response().setStatusCode(200).end(jsonObject.toString());
        } else {
          routingContext.response().setStatusCode(500).end(httpResponseAsyncResult.cause().getMessage());
        }
      });
  }

  private void startUploadingProcess(RoutingContext routingContext){
    String processNo = routingContext.request().getParam("process");
    fetchTaskId(processNo, response ->  {
        if(response.succeeded()){
          JsonObject jsonObject = response.result();
          String taskId= String.valueOf(jsonObject.getInteger("task-id"));
          log.info(taskId);
          startTaskByTaskId(taskId, response2 ->{
            if(response2.succeeded()){
              routingContext.response().setStatusCode(200).end();
            }else{
              routingContext.response().setStatusCode(500).end();
            }
          });

          //routingContext.response().setStatusCode(200).end(jsonObject.toString());
        }else{
          routingContext.response().setStatusCode(500).end();
        }
    });
  }

  private void startTaskByTaskId(String taskId, Handler<AsyncResult<JsonObject>> handler){
    String url = getUrl(base,"/server/containers/",containerId,"/tasks/",taskId,"/states/started");
    log.info("starting task details url ::" + url);
    webClient = WebClient.create(vertx);

    webClient.put(8080,"localhost",url)
      .putHeader("Content-Type", "application/json")
      .putHeader("Accept", "application/json")
      .putHeader("Authorization", "Basic " + getToken("katy","katy"))
      .as(BodyCodec.string()).ssl(false)
      .send(resp ->{
        if(resp.succeeded()){
          handler.handle(Future.succeededFuture());
        }else{
          handler.handle(Future.failedFuture(resp.cause()));
        }
      });
  }


  private void uploadFileByTaskId(RoutingContext routingContext){
    String taskId = routingContext.request().getParam("process");
    String url = getUrl(base,"/server/containers/",containerId,"/tasks/",taskId,"/states/completed");
    log.info("upload task details url ::" + url);
    JsonObject jsonObject = null;
    webClient = WebClient.create(vertx);
    try {
       jsonObject = getProcessedDataObject(taskId,routingContext.fileUploads());
    }catch (Exception e){
      e.printStackTrace();
    }

    webClient.put(8080,"localhost",url)
      .putHeader("Content-Type", "application/json")
      .putHeader("Accept", "application/json")
      .putHeader("Authorization", "Basic " + getToken("katy","katy"))
      .as(BodyCodec.string()).ssl(false)
      .sendJsonObject(jsonObject, resp ->{
        if(resp.succeeded()){
          routingContext.response().setStatusCode(200).end();
        }else{
          routingContext.response().setStatusCode(500).end();
        }
      });
  }


  private void fetchTaskId(String process, Handler<AsyncResult<JsonObject>> handler) {
    String url = getUrl(base,"/server/queries/tasks/instances/process/",process);
    log.info("fetching task details url ::" + url);
    webClient = WebClient.create(vertx);
    webClient.get(8080,"localhost",url)
      .putHeader("Content-Type", "application/json")
      .putHeader("Accept", "application/json")
      .putHeader("Authorization", "Basic " + getToken("katy","katy"))
      .as(BodyCodec.string())
      .ssl(false).send(response -> {
        if(response.succeeded()){

          JsonObject jsonObject = new JsonObject(response.result().body());
          JsonArray jsonArray = jsonObject.getJsonArray("task-summary");
          handler.handle(Future.succeededFuture(jsonArray.getJsonObject(0)));
        }else{
          log.error(response.cause().getMessage());
          handler.handle(Future.failedFuture(response.cause()));
        }
    });

  }


  private void uploadFile(RoutingContext routingContext) {

    String request = routingContext.request().getParam("process");
    String url = getUrl(base,"/server/containers/",containerId,"/tasks/",request,"/states/completed");
    log.info(" user ::" + request);
    log.info("url :" + url);

    try {
      JsonObject jsonObject = getProcessedDataObject(request, routingContext.fileUploads());
      WebClient webClient = WebClient.create(vertx);
      log.debug(jsonObject.encodePrettily());
      webClient.put(8080, "localhost", "url")
        .putHeader("Content-Type", "application/json")
        .putHeader("Accept", "application/json")
        .putHeader("Authorization", "Basic " + getToken("katy","katy"))
        .as(BodyCodec.string())
        .ssl(false)
        .sendJsonObject(jsonObject, httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded()) {
            routingContext.response().setStatusCode(200).end();
          } else {
            routingContext.response().setStatusCode(500).end(httpResponseAsyncResult.cause().getMessage());
          }
        });


    } catch (IOException e) {
      e.printStackTrace();
      routingContext.response().setStatusCode(500).end(e.getMessage());
    }

  }

  private String getUrl(String... args){
    StringBuilder stringBuilder = new StringBuilder();
    Arrays.stream(args).forEach(e -> stringBuilder.append(e));
    //log.info(stringBuilder.toString());
    return  stringBuilder.toString();
  }


  private JsonObject getProcessedDataObject(String requestId, Set<FileUpload> fileUploads) throws IOException {



    JsonObject jsonObject1 = new JsonObject();
    JsonObject jsonObject2 = new JsonObject();
    JsonObject innerJsonObject = null;
    for (FileUpload fileupload : fileUploads) {

      File f = getUploadFile(fileupload);

      innerJsonObject = new JsonObject();
      innerJsonObject.put("identifier", requestId);
      innerJsonObject.put("name", fileupload.fileName());
      innerJsonObject.put("size", fileupload.size());
      innerJsonObject.put("lastModified", f.lastModified());
      innerJsonObject.put("content", getBase64EncodedFileContent(f));


      jsonObject2.put("org.jbpm.document.service.impl.DocumentImpl", innerJsonObject);
    }
    jsonObject1 = new JsonObject();
    jsonObject1.put("request_no", requestId);
    jsonObject1.put("taskdoc_out", jsonObject2);

    return jsonObject1;
  }

  private File getUploadFile(FileUpload fileupload) {
    return new File(fileupload.uploadedFileName());
  }

  private String getBase64EncodedFileContent(File f) throws IOException {
    byte[] fileContent = FileUtils.readFileToByteArray(f);
    String encodedString = Base64.getEncoder().encodeToString(fileContent);
    return encodedString;
  }

  private String getToken(String user, String passoword){
    return  Base64.getEncoder().encodeToString(new StringBuilder(user)
      .append(":").append(passoword).toString().getBytes());
  }
}

