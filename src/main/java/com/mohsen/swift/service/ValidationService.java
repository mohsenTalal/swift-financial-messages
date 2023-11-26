package com.mohsen.swift.service;

import com.mohsen.swift.dao.erp.entity.SwiftPayments;
import com.mohsen.swift.model.SharepointVO;
import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.AppUtil;
import com.mohsen.swift.util.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.regex.Pattern;

@Service
public class ValidationService {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

  private static final Pattern ALPHANUMERIC = Pattern.compile("[0-9a-zA-Z/\\-\\?:\\(\\)\\.,'\\+ ]{1,16}");
  private static final Pattern ALPHANUMERIC_34 = Pattern.compile("[0-9a-zA-Z/\\-\\?:\\(\\)\\.,'\\+ ]{1,34}");
  private static final Pattern ALPHANUMERIC_35 = Pattern.compile("[0-9a-zA-Z/\\-\\?:\\(\\)\\.,'\\+ ]{1,35}");
  private static final Pattern NUMERIC = Pattern.compile("[0-9]{6}");
  private static final Pattern ALPHA = Pattern.compile("[A-Z]{3}");
  private static final Pattern SWIFT_CODE = Pattern.compile("[A-Z]{4}[A-Z]{2}[0-9A-Z]{2}([0-9A-Z]{3})?");

  public String validateSwiftPayment(SwiftPayments swiftPayments) {

    boolean isInvalid;
    StringBuilder errorMsg = new StringBuilder();

    isInvalid = isFieldInvalid(swiftPayments.getSwiftRef(), ALPHANUMERIC, 1, 16);
    if (isInvalid) {
      errorMsg.append("SWIFT_REF,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getMohsenDebitAcctNo(), ALPHANUMERIC_34, 1, 34);
    if (isInvalid) {
      errorMsg.append("DEBIT_ACCT_NO,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getLegalName(), ALPHANUMERIC_35, 1, 35);
    if (isInvalid) {
      errorMsg.append("LEGAL_NAME,");
    }

    Date paymentDate = AppUtil.getDate(AppConstants.FORMAT_DDMMYYYY, swiftPayments.getPaymentDate());
    isInvalid = isFieldInvalid(AppUtil.getDateString(AppConstants.FORMAT_YYMMDD, paymentDate), NUMERIC, 6, 6);
    if (isInvalid) {
      errorMsg.append("PAYMENT_DATE,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getCurrencyCode(), ALPHA, 3, 3);
    if (isInvalid) {
      errorMsg.append("CURRENCY,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getAmount());
    if (isInvalid) {
      errorMsg.append("AMOUNT,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getVendorSwiftCode(), SWIFT_CODE, 8, 11);
    if (isInvalid) {
      errorMsg.append("SWIFT_CODE,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getBeneficiaryIban(), ALPHANUMERIC_34, 1, 34);
    if (isInvalid) {
      errorMsg.append("BENEFICIARY_IBAN,");
    }

    isInvalid = isFieldInvalid(swiftPayments.getBankCharges(), ALPHA, 3, 3);
    if (isInvalid) {
      errorMsg.append("BANK_CHARGES,");
    }

    LOG.info("validateSwiftPayment - errorMsg: {}", errorMsg);
    LOG.info("validateSwiftPayment - Data Object: {}", swiftPayments);

    if (errorMsg.toString().trim().length() > 0) {
      String finalMsg = StatusType.INVALID.getValue() + AppConstants.HYPHEN + errorMsg;
      if (finalMsg.length() > 30) {
        return finalMsg.substring(0, 29);
      } else {
        return finalMsg;
      }
    } else {
      return null;
    }
  }

  public String validateSharepointData(SharepointVO sharepointVO) {

    boolean isInvalid;
    StringBuilder errorMsg = new StringBuilder();

    isInvalid = isFieldInvalid(sharepointVO.getReferenceNumber(), ALPHANUMERIC, 1, 16);
    if (isInvalid) {
      errorMsg.append("SWIFT_REF,");
    }

    isInvalid = isFieldInvalid(sharepointVO.getMohsenAccountName(), ALPHANUMERIC_34, 1, 34);
    if (isInvalid) {
      errorMsg.append("DEBIT_ACCT_NO,");
    }

    Date paymentDate = AppUtil.getDate(AppConstants.FORMAT_YYYYMMDD_T_HHMMSS, sharepointVO.getPaymentDate());
    isInvalid = isFieldInvalid(AppUtil.getDateString(AppConstants.FORMAT_YYMMDD, paymentDate), NUMERIC, 6, 6);
    if (isInvalid) {
      errorMsg.append("PAYMENT_DATE,");
    }

    isInvalid = isFieldInvalid(sharepointVO.getPaymentCurrency(), ALPHA, 3, 3);
    if (isInvalid) {
      errorMsg.append("CURRENCY,");
    }

    isInvalid = isFieldInvalid(sharepointVO.getPaymentAmount());
    if (isInvalid) {
      errorMsg.append("AMOUNT,");
    }

    isInvalid = isFieldInvalid(sharepointVO.getVendorSwiftCode(), SWIFT_CODE, 8, 11);
    if (isInvalid) {
      errorMsg.append("SWIFT_CODE,");
    }

    isInvalid = isFieldInvalid(sharepointVO.getSupplierIBAN(), ALPHANUMERIC_34, 1, 34);
    if (isInvalid) {
      errorMsg.append("BENEFICIARY_IBAN,");
    }

    LOG.info("validateSharepointData - errorMsg: {}", errorMsg);
    LOG.info("validateSwiftPayment - Data Object: {}", sharepointVO);

    if (errorMsg.toString().trim().length() > 0) {
      return StatusType.INVALID.getValue() + AppConstants.HYPHEN + errorMsg;
    } else {
      return null;
    }
  }

  private boolean isFieldInvalid(String value, Pattern pattern, int minLength, int maxLength) {
    if (value == null || value.trim().length() == 0 || value.trim().length() > maxLength || value.trim().length() < minLength) {
      return true;
    }
    return !pattern.matcher(value).matches();
  }

  private boolean isFieldInvalid(Long value) {
    return value == null || value <= 0;
  }
}
