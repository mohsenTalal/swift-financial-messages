package com.mohsen.swift.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateRequest {

  @JsonProperty("SwiftStatus")
  private String swiftStatus;

  public String getSwiftStatus() {
    return swiftStatus;
  }

  public void setSwiftStatus(String swiftStatus) {
    this.swiftStatus = swiftStatus;
  }
}
