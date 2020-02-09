package com.sample.model;

public class Article {
  private String id;
  private String details;
  private String author;

  public Article(String id, String details, String author) {
    this.id = id;
    this.details = details;
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
