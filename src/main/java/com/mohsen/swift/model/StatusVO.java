package com.mohsen.swift.model;

import java.util.Date;

public class StatusVO {

  private Date processingDate;
  private String reference;
  private String paymentId;
  private String senderBIC;
  private String receiverBIC;
  private String status;
  private String statusMessage;
  private Date lastUpdateDate;

  public Date getProcessingDate() {
    return processingDate;
  }

  public void setProcessingDate(Date processingDate) {
    this.processingDate = processingDate;
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

  public String getSenderBIC() {
    return senderBIC;
  }

  public void setSenderBIC(String senderBIC) {
    this.senderBIC = senderBIC;
  }

  public String getReceiverBIC() {
    return receiverBIC;
  }

  public void setReceiverBIC(String receiverBIC) {
    this.receiverBIC = receiverBIC;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }

  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  public void setLastUpdateDate(Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
  }
}
