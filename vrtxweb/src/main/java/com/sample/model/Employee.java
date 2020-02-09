package com.sample.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Employee {

  private static final AtomicInteger COUNTER = new AtomicInteger();

  private Integer empId;
  private String firstName;
  private String lastName;

  public Employee(String firstName, String lastName) {
    this.empId = COUNTER.getAndIncrement();
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Employee(){
    this.empId = COUNTER.getAndIncrement();
  }

  public Integer getEmpId() {
    return empId;
  }

  public void setEmpId(Integer empId) {
    this.empId = empId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
}
