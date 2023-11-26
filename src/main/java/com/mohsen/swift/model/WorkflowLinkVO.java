package com.mohsen.swift.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowLinkVO {

  @JsonProperty("Url")
  private String url;

  @JsonProperty("Description")
  private String description;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
