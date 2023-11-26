package com.mohsen.swift.dao.erp.repository;

import com.mohsen.swift.dao.erp.entity.SwiftPayments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PaymentsRepository {

  @Autowired
  @Qualifier("erpJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  @Value("${swift.limit-data}")
  private boolean limitData;

  @Value("${swift.limit-size}")
  private int limitSize;

  private static final String SQL_FIND_ALL = "SELECT * FROM APPS.XXMohsen_PAYMENTS_SWIFT_V";

  private static final String SQL_FIND_LIMIT = "SELECT * FROM APPS.XXMohsen_PAYMENTS_SWIFT_V WHERE ROWNUM <= ";

  public List<SwiftPayments> findAll() {
    if (limitData) {
      return jdbcTemplate.query(SQL_FIND_LIMIT + limitSize, (rs, rowNum) -> getSwiftPayments(rs));
    } else {
      return jdbcTemplate.query(SQL_FIND_ALL, (rs, rowNum) -> getSwiftPayments(rs));
    }
  }

  private SwiftPayments getSwiftPayments(ResultSet rs) throws SQLException {
    SwiftPayments payments = new SwiftPayments();
    payments.setMohsenDebitAcctNo(rs.getString("Mohsen_DEBIT_ACCOUNT_NUMBER"));
    payments.setPaymentDate(rs.getString("PAYMENT_DATE"));
    payments.setCurrencyCode(rs.getString("CURRENCY_CODE"));
    payments.setBeneficiaryIban(rs.getString("BENEFICIARY_IBAN"));
    payments.setVendorSwiftCode(rs.getString("VENDOR_SWIFT_CODE"));
    payments.setBeneficiaryName(rs.getString("BENEFICIARY_NAME"));
    payments.setBeneficiaryAddr(rs.getString("BEN_ADDRESS"));
    payments.setVoucherNumber(rs.getLong("CHECK_VOUCHER_NUM"));
    payments.setDocumentNumber(rs.getLong("DOCUMENT_NUMBER"));
    payments.setAmount(rs.getLong("AMOUNT"));
    payments.setPaymentId(rs.getLong("PAYMENT_ID"));
    payments.setBankCharges(rs.getString("BANK_CHARGES"));
    payments.setReceiverBic(rs.getString("Mohsen_DEBIT_RECEIVER_BIC"));
    payments.setInvoiceNumbers(rs.getString("INVOICE_NUMBERS"));
    payments.setTrxType(rs.getString("TRX_TYPE"));
    payments.setLegalName(rs.getString("LEGAL_ENTITY_NAME"));
    payments.setBeneficiaryAcctNo(rs.getString("BENEFICIARY_ACCOUNT_NUM"));
    payments.setSwiftRef(rs.getString("SWIFT_REF"));
    return payments;
  }
}
