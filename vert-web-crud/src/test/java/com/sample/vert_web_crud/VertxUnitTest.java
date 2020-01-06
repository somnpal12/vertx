package com.sample.vert_web_crud;

import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import org.junit.jupiter.api.Test;

public class VertxUnitTest {
  @Test
  public void shouldConnectSuccessfully() throws InterruptedException {
    TestSuite suite = TestSuite.create(" 'The Test Suite '");

    suite.before(testContext -> {
      System.out.println(">> Before");
    }).after(testContext -> {
        System.out.println(">> After");
      }
    ).test("'My Test Case 1'", context -> {
      String s = "value";
      context.assertEquals("value", s);
    }).test("'My Test Case 2'", context -> {
      String s = "value";
      context.assertEquals("value", s);
    }).beforeEach(testContext -> {
      System.out.println(">> Before Each");
    }).afterEach(testContext -> {
      System.out.println(">> After Each");
    });
    ReportOptions consoleReport =  new ReportOptions().setTo("console");
    /*ReportOptions junitReport =  new ReportOptions().setTo("file:.").setFormat("junit");*/

    suite.run(new TestOptions().addReporter(consoleReport));
  }
}
