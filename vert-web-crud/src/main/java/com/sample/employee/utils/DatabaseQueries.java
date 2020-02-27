package com.sample.employee.utils;

public interface DatabaseQueries {

    static String QUERY_FETCH_ALL_EMPLOYEE = "SELECT * FROM sample.\"EMPLOYEE\"";
    static String QUERY_FIND_EMPLOYEE_BY_ID = "SELECT * FROM sample.\"EMPLOYEE\" WHERE \"ID\" = $1";
    static String QUERY_INSERT_EMPLOYEE = "INSERT INTO sample.\"EMPLOYEE\"(\"NAME\", \"AGE\", \"ADDRESS\", \"SALARY\") VALUES($1,$2,$3,$4)";
    static String QUERY_UPDATE_EMPLOYEE = "UPDATE sample.\"EMPLOYEE\" SET \"SALARY\"=$1 WHERE \"ID\" = $2 ";
    static  String QUERY_DELETE_EMPLOYEE = "DELETE FROM sample.\"EMPLOYEE\" WHERE \"ID\" = $1";

}
