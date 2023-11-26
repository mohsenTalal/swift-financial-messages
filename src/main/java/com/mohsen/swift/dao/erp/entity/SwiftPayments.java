package com.mohsen.swift.dao.erp.entity;

import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Immutable
@Table(name = "XXMohsen_PAYMENTS_SWIFT_V", schema = "APPS")
public class SwiftPayments implements Serializable {

  @Column(name = "Mohsen_DEBIT_ACCOUNT_NUMBER")
  private String mohsenDebitAcctNo;

  @Column(name = "PAYMENT_DATE")
  private String paymentDate;

  @Column(name = "CURRENCY_CODE")
  private String currencyCode;

  @Column(name = "BENEFICIARY_IBAN")
  private String beneficiaryIban;

  @Column(name = "VENDOR_SWIFT_CODE")
  private String vendorSwiftCode;

  @Column(name = "BENEFICIARY_NAME")
  private String beneficiaryName;

  @Column(name = "BEN_ADDRESS")
  private String beneficiaryAddr;

  @Column(name = "CHECK_VOUCHER_NUM")
  private Long voucherNumber;

  @Column(name = "DOCUMENT_NUMBER")
  private Long documentNumber;

  @Column(name = "AMOUNT")
  private Long amount;

  @Id
  @Column(name = "PAYMENT_ID")
  private Long paymentId;

  @Column(name = "BANK_CHARGES")
  private String bankCharges;

  @Column(name = "Mohsen_DEBIT_RECEIVER_BIC")
  private String receiverBic;

  @Column(name = "INVOICE_NUMBERS")
  private String invoiceNumbers;

  @Column(name = "TRX_TYPE")
  private String trxType;

  @Column(name = "LEGAL_ENTITY_NAME")
  private String legalName;

  @Column(name = "BENEFICIARY_ACCOUNT_NUM")
  private String beneficiaryAcctNo;

  @Column(name = "SWIFT_REF")
  private String swiftRef;

  public String getMohsenDebitAcctNo() {
    return mohsenDebitAcctNo;
  }

  public void setMohsenDebitAcctNo(String mohsenDebitAcctNo) {
    this.mohsenDebitAcctNo = mohsenDebitAcctNo;
  }

  public String getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(String paymentDate) {
    this.paymentDate = paymentDate;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getBeneficiaryIban() {
    return beneficiaryIban;
  }

  public void setBeneficiaryIban(String beneficiaryIban) {
    this.beneficiaryIban = beneficiaryIban;
  }

  public String getVendorSwiftCode() {
    return vendorSwiftCode;
  }

  public void setVendorSwiftCode(String vendorSwiftCode) {
    this.vendorSwiftCode = vendorSwiftCode;
  }

  public String getBeneficiaryName() {
    return beneficiaryName;
  }

  public void setBeneficiaryName(String beneficiaryName) {
    this.beneficiaryName = beneficiaryName;
  }

  public String getBeneficiaryAddr() {
    return beneficiaryAddr;
  }

  public void setBeneficiaryAddr(String beneficiaryAddr) {
    this.beneficiaryAddr = beneficiaryAddr;
  }

  public Long getVoucherNumber() {
    return voucherNumber;
  }

  public void setVoucherNumber(Long voucherNumber) {
    this.voucherNumber = voucherNumber;
  }

  public Long getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(Long documentNumber) {
    this.documentNumber = documentNumber;
  }

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public Long getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(Long paymentId) {
    this.paymentId = paymentId;
  }

  public String getBankCharges() {
    return bankCharges;
  }

  public void setBankCharges(String bankCharges) {
    this.bankCharges = bankCharges;
  }

  public String getReceiverBic() {
    return receiverBic;
  }

  public void setReceiverBic(String receiverBic) {
    this.receiverBic = receiverBic;
  }

  public String getInvoiceNumbers() {
    return invoiceNumbers;
  }

  public void setInvoiceNumbers(String invoiceNumbers) {
    this.invoiceNumbers = invoiceNumbers;
  }

  public String getTrxType() {
    return trxType;
  }

  public void setTrxType(String trxType) {
    this.trxType = trxType;
  }

  public String getLegalName() {
    return legalName;
  }

  public void setLegalName(String legalName) {
    this.legalName = legalName;
  }

  public String getBeneficiaryAcctNo() {
    return beneficiaryAcctNo;
  }

  public void setBeneficiaryAcctNo(String beneficiaryAcctNo) {
    this.beneficiaryAcctNo = beneficiaryAcctNo;
  }

  public String getSwiftRef() {
    return swiftRef;
  }

  public void setSwiftRef(String swiftRef) {
    this.swiftRef = swiftRef;
  }

  @Override
  public String toString() {
    return "SwiftPayments{" +
        "mohsenDebitAcctNo='" + mohsenDebitAcctNo + '\'' +
        ", paymentDate='" + paymentDate + '\'' +
        ", currencyCode='" + currencyCode + '\'' +
        ", beneficiaryIban='" + beneficiaryIban + '\'' +
        ", vendorSwiftCode='" + vendorSwiftCode + '\'' +
        ", beneficiaryName='" + beneficiaryName + '\'' +
        ", beneficiaryAddr='" + beneficiaryAddr + '\'' +
        ", voucherNumber=" + voucherNumber +
        ", documentNumber=" + documentNumber +
        ", amount=" + amount +
        ", paymentId=" + paymentId +
        ", bankCharges='" + bankCharges + '\'' +
        ", receiverBic='" + receiverBic + '\'' +
        ", invoiceNumbers='" + invoiceNumbers + '\'' +
        ", trxType='" + trxType + '\'' +
        ", legalName='" + legalName + '\'' +
        ", beneficiaryAcctNo='" + beneficiaryAcctNo + '\'' +
        ", swiftRef='" + swiftRef + '\'' +
        '}';
  }
}
