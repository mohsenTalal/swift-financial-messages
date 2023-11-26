package com.mohsen.swift.dao.integration.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SWIFT_STATUS")
public class SwiftStatus {

  @Id
  @Column(name = "ID")
  private Integer id;

  @Column(name = "SWIFT_REF")
  private String reference;

  @Column(name = "SWIFT_STATUS")
  private String status;

  @Column(name = "PAYMENT_ID")
  private String paymentId;

  @Column(name = "IS_ENABLED")
  private Integer isEnabled;

  @Temporal(TemporalType.TIMESTAMP)
  @CreationTimestamp
  @Column(name = "CREATED_ON")
  private Date createdOn;

  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  @Column(name = "UPDATED_ON")
  private Date updatedOn;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public Integer getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(Integer isEnabled) {
    this.isEnabled = isEnabled;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public Date getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }
}
