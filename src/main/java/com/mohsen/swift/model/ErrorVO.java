package com.mohsen.swift.model;

public class ErrorVO {

  private String code;
  private MessageVO message;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public MessageVO getMessage() {
    return message;
  }

  public void setMessage(MessageVO message) {
    this.message = message;
  }
}
