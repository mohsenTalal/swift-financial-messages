package com.mohsen.swift.util;

public enum OperationType {
  OPEN("Open"),
  CLOSE("Close"),
  PUT("Put"),
  GET_ACK("GetAck"),
  ACK("Ack");

  private final String value;

  OperationType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
