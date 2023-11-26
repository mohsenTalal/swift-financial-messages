package com.mohsen.swift.model;

import java.util.Date;

public class SwiftMessageVO {

  private String messagePartner;
  private String senderX1;
  private String senderBIC12;
  private String receiverX1;
  private String receiverBIC12;
  private String reference;
  private String paymentId;
  private String messageBody;
  private boolean success;
  private String status;
  private long seq;
  private int taskId;
  private Date actionDate;
  private boolean statusChange;
  private boolean invalid;
  private boolean error;

  public String getMessagePartner() {
    return messagePartner;
  }

  public void setMessagePartner(String messagePartner) {
    this.messagePartner = messagePartner;
  }

  public String getSenderX1() {
    return senderX1;
  }

  public void setSenderX1(String senderX1) {
    this.senderX1 = senderX1;
  }

  public String getSenderBIC12() {
    return senderBIC12;
  }

  public void setSenderBIC12(String senderBIC12) {
    this.senderBIC12 = senderBIC12;
  }

  public String getReceiverX1() {
    return receiverX1;
  }

  public void setReceiverX1(String receiverX1) {
    this.receiverX1 = receiverX1;
  }

  public String getReceiverBIC12() {
    return receiverBIC12;
  }

  public void setReceiverBIC12(String receiverBIC12) {
    this.receiverBIC12 = receiverBIC12;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public String getMessageBody() {
    return messageBody;
  }

  public void setMessageBody(String messageBody) {
    this.messageBody = messageBody;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }

  public int getTaskId() {
    return taskId;
  }

  public void setTaskId(int taskId) {
    this.taskId = taskId;
  }

  public Date getActionDate() {
    return actionDate;
  }

  public void setActionDate(Date actionDate) {
    this.actionDate = actionDate;
  }

  public boolean isStatusChange() {
    return statusChange;
  }

  public void setStatusChange(boolean statusChange) {
    this.statusChange = statusChange;
  }

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid(boolean invalid) {
    this.invalid = invalid;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }
}
