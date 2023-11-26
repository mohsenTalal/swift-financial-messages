package com.mohsen.swift.dao.erp.entity;

import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Immutable
@Table(name = "XXMohsen_SWIFT_PAYMENT_LOG_V", schema = "APPS")
public class SwiftPaymentsLog implements Serializable {

  @Id
  @Column(name = "SEQ")
  private Long seq;

  @Column(name = "SWIFT_STATUS")
  private String status;

  @Column(name = "PAYMENT_ID")
  private Long paymentId;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ACTION_DATE")
  private Date actionDate;

  public Long getSeq() {
    return seq;
  }

  public void setSeq(Long seq) {
    this.seq = seq;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(Long paymentId) {
    this.paymentId = paymentId;
  }

  public Date getActionDate() {
    return actionDate;
  }

  public void setActionDate(Date actionDate) {
    this.actionDate = actionDate;
  }
}
