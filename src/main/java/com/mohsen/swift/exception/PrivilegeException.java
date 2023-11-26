package com.mohsen.swift.exception;

public class PrivilegeException extends Exception {

  public PrivilegeException(String message) {
    super(message);
  }

  public PrivilegeException(String message, Throwable cause) {
    super(message, cause);
  }
}
