package com.mohsen.swift.util;

public enum StatusType {
  READY_TO_PICK("READY_TO_PICK"),
  PICKED("PICKED"),
  DELIVERED("DELIVERED"),
  INVALID("INVALID"),
  SWIFT_ERROR("SWIFT_ERROR"),
  NETWORK_ERROR("NETWORK_ERROR"),
  ACK("ACK"),
  NACK("NACK");

  private final String value;

  StatusType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
