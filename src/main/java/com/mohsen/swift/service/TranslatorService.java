package com.mohsen.swift.service;

import com.mohsen.swift.dao.erp.entity.SwiftPayments;
import com.mohsen.swift.model.SharepointRespVO;
import com.mohsen.swift.model.SharepointVO;
import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.AppUtil;
import com.mohsen.swift.util.StatusType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TranslatorService {

  private static final Logger LOG = LoggerFactory.getLogger(TranslatorService.class);

  @Autowired
  private DAOService daoService;

  @Autowired
  private SharepointService sharepointService;

  @Autowired
  private ValidationService validationService;

  @Value("${swift.message-partner}")
  private String messagePartner;

  @Value("${swift.spv.message-partner}")
  private String spvMessagePartner;

  @Value("${swift.file.location}")
  private String fileLocation;

  @Value("${swift.file.active}")
  private boolean fileActive;

  @Value("${swift.sender.name}")
  private String senderName;

  @Value("${swift.sender.x1}")
  private String senderX1;

  @Value("${swift.sender.bic12}")
  private String senderBIC12;

  @Value("${swift.receiver.active}")
  private boolean receiverActive;

  @Value("${swift.receiver.name}")
  private String receiverName;

  @Value("${swift.receiver.x1}")
  private String receiverX1;

  @Value("${swift.receiver.bic12}")
  private String receiverBIC12;

  public List<SwiftMessageVO> getSwiftMessages() {

    LOG.info("getSwiftMessages - BEGIN");
    List<SwiftMessageVO> messages = new ArrayList<>();

    if (fileActive) {
      retrieveDataFromFile(messages);
    } else {
      retrieveDataFromERP(messages);
    }

    LOG.info("getSwiftMessages - END");
    return messages;
  }

  public List<SwiftMessageVO> getSpvSwiftMessages(StatusType statusType) {
    LOG.info("getSpvSwiftMessages - BEGIN");
    List<SwiftMessageVO> messages = new ArrayList<>();
    retrieveDataFromSharepoint(statusType, messages);
    LOG.info("getSpvSwiftMessages - END");
    return messages;
  }

  public List<SwiftMessageVO> getMessagesBasedOnStatus(List<SwiftMessageVO> messageList, StatusType status) {
    List<SwiftMessageVO> statusList = new ArrayList<>();
    for (SwiftMessageVO messageVO : messageList) {
      if (status.getValue().equalsIgnoreCase(messageVO.getStatus()) && !messageVO.isInvalid()) {
        statusList.add(messageVO);
      }
    }
    return statusList;
  }

  public List<SwiftMessageVO> getInvalidMessages(List<SwiftMessageVO> messageList) {
    List<SwiftMessageVO> statusList = new ArrayList<>();
    for (SwiftMessageVO messageVO : messageList) {
      if (messageVO.isInvalid()) {
        statusList.add(messageVO);
      }
    }
    return statusList;
  }

  public void updateSpvSwiftStatus(List<SwiftMessageVO> messageList) {

    LOG.info("updateSpvSwiftStatus - BEGIN");

    if (messageList == null || messageList.isEmpty()) {
      LOG.info("updateSpvSwiftStatus - Message list is empty. No data to update.");
      LOG.info("updateSpvSwiftStatus - END");
      return;
    } else {
      for (SwiftMessageVO messageVO : messageList) {
        if (messageVO.isStatusChange()) {
          SharepointVO sharepointVO = sharepointService.getSharepointItemById(String.valueOf(messageVO.getTaskId()));
          if (messageVO.getStatus().contains(StatusType.INVALID.getValue())) {
            sharepointVO.setSwiftStatus(StatusType.INVALID.getValue());
          } else {
            sharepointVO.setSwiftStatus(messageVO.getStatus());
          }
          sharepointService.updateSharepointItem(sharepointVO);
          LOG.info("updateSpvSwiftStatus - SWIFT status updated for TaskId: '{}' with Status: '{}'",
              messageVO.getTaskId(), messageVO.getStatus());
        }
      }
    }

    daoService.updateSharepointStatusToDB(messageList);

    LOG.info("updateSpvSwiftStatus - END");
  }

  public List<SwiftMessageVO> filterSwiftMessages(List<SwiftMessageVO> messageList,
                                                  List<SwiftMessageVO> processedList,
                                                  List<SwiftMessageVO> dbList) {

    List<SwiftMessageVO> finalFilterList = new ArrayList<>();
    List<SwiftMessageVO> initialFilterList = new ArrayList<>();
    boolean matchFound;

    for (SwiftMessageVO message : messageList) {
      matchFound = false;
      for (SwiftMessageVO processed : processedList) {
        if (message.getPaymentId().equalsIgnoreCase(processed.getPaymentId()) && !processed.isInvalid() && !processed.isError()) {
          matchFound = true;
          break;
        }
      }
      if (!matchFound) {
        if (message.isInvalid()) {
          message.setStatus(message.getStatus());
        } else {
          message.setStatus(StatusType.PICKED.getValue());
        }
        message.setStatusChange(true);
        initialFilterList.add(message);
      }
    }

    Set<String> referenceSet = dbList.stream().map(SwiftMessageVO::getReference).collect(Collectors.toSet());

    for (SwiftMessageVO messageVO : initialFilterList) {
      if (!referenceSet.contains(messageVO.getReference())) {
        finalFilterList.add(messageVO);
      }
    }

    return finalFilterList;
  }

  public List<SwiftMessageVO> filterSpvSwiftMessages(List<SwiftMessageVO> messageList,
                                                     List<SwiftMessageVO> processedList,
                                                     List<SwiftMessageVO> dbList) {

    List<SwiftMessageVO> finalFilterList = new ArrayList<>();
    List<SwiftMessageVO> initialFilterList = new ArrayList<>();
    boolean matchFound;

    for (SwiftMessageVO message : messageList) {
      matchFound = false;
      for (SwiftMessageVO processed : processedList) {
        if (message.getReference().equalsIgnoreCase(processed.getReference()) && !processed.isInvalid() && !processed.isError()) {
          matchFound = true;
          break;
        }
      }
      if (!matchFound) {
        if (message.isInvalid()) {
          message.setStatus(StatusType.INVALID.getValue());
        } else {
          message.setStatus(StatusType.PICKED.getValue());
        }
        message.setStatusChange(true);
        initialFilterList.add(message);
      }
    }

    Set<String> referenceSet = dbList.stream().map(SwiftMessageVO::getReference).collect(Collectors.toSet());

    for (SwiftMessageVO messageVO : initialFilterList) {
      if (!referenceSet.contains(messageVO.getReference())) {
        finalFilterList.add(messageVO);
      }
    }

    return finalFilterList;
  }

  private void retrieveDataFromFile(List<SwiftMessageVO> messages) {

    StringBuilder sb = new StringBuilder();
    sb.append(AppConstants.CRLF);
    try (BufferedReader br = Files.newBufferedReader(Paths.get(fileLocation))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append(AppConstants.CRLF);
      }
    } catch (IOException e) {
      LOG.error("Error while reading the file", e);
    }

    String messageBody = sb.toString();
    int beginIndex = messageBody.indexOf(AppConstants.TAG_20);
    int endIndex = messageBody.indexOf(AppConstants.TAG_28D);
    String reference = messageBody.substring(beginIndex + 4, endIndex - 2);

    SwiftMessageVO swiftMessageVO = new SwiftMessageVO();
    swiftMessageVO.setSenderX1(senderX1);
    swiftMessageVO.setSenderBIC12(senderBIC12);
    swiftMessageVO.setReceiverX1(receiverX1);
    swiftMessageVO.setReceiverBIC12(receiverBIC12);
    swiftMessageVO.setReference(reference);
    swiftMessageVO.setPaymentId(reference);
    swiftMessageVO.setMessagePartner(messagePartner);
    swiftMessageVO.setMessageBody(messageBody);

    messages.add(swiftMessageVO);
  }

  private void retrieveDataFromERP(List<SwiftMessageVO> messages) {

    LOG.info("retrieveDataFromERP - BEGIN");

    List<SwiftPayments> paymentMessages = daoService.getAllMessages();
    LOG.info("retrieveDataFromERP - No.of messages found in ERP DB: {}", paymentMessages.size());

    for (SwiftPayments swiftPayments : paymentMessages) {

      Date paymentDate = AppUtil.getDate(AppConstants.FORMAT_DDMMYYYY, swiftPayments.getPaymentDate());

      StringBuilder sb = new StringBuilder();
      sb.append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_20).append(swiftPayments.getSwiftRef()).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_28D).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_50H).append(swiftPayments.getMohsenDebitAcctNo()).append(AppConstants.CRLF);
      sb.append(swiftPayments.getLegalName()).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_30).append(AppUtil.getDateString(AppConstants.FORMAT_YYMMDD, paymentDate)).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_21).append(swiftPayments.getSwiftRef()).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_32B).append(swiftPayments.getCurrencyCode()).append(swiftPayments.getAmount()).append(",").append(AppConstants.CRLF);
      //sb.append(AppConstants.TAG_52A).append("").append(AppConstants.CRLF);
      //sb.append(AppConstants.TAG_56A).append("").append(AppConstants.CRLF);
      if (!StringUtils.isBlank(swiftPayments.getVendorSwiftCode())) {
        sb.append(AppConstants.TAG_57A).append(swiftPayments.getVendorSwiftCode()).append(AppConstants.CRLF);
      }
      if (!StringUtils.isBlank(swiftPayments.getBeneficiaryIban())) {
        sb.append(AppConstants.TAG_59).append(swiftPayments.getBeneficiaryIban()).append(AppConstants.CRLF);
      }
      if (!StringUtils.isBlank(swiftPayments.getBeneficiaryName())) {
        if (isArabic(swiftPayments.getBeneficiaryName())) {
          sb.append(receiverName).append(AppConstants.CRLF);
        } else {
          String beneficiaryName = swiftPayments.getBeneficiaryName();
          if (beneficiaryName.length() > 35) {
            beneficiaryName = beneficiaryName.substring(0, 34);
          }
          sb.append(beneficiaryName).append(AppConstants.CRLF);
        }
      }
      if (!StringUtils.isBlank(swiftPayments.getBeneficiaryAddr()) && !isArabic(swiftPayments.getBeneficiaryAddr())) {
        int length = swiftPayments.getBeneficiaryAddr().length();
        if (length <= 35) {
          sb.append(swiftPayments.getBeneficiaryAddr()).append(AppConstants.CRLF);
        } else if (length <= 70) {
          String part1 = swiftPayments.getBeneficiaryAddr().substring(0, 35);
          String part2 = swiftPayments.getBeneficiaryAddr().substring(35, length);
          sb.append(part1).append(AppConstants.CRLF);
          sb.append(part2).append(AppConstants.CRLF);
        } else {
          String part1 = swiftPayments.getBeneficiaryAddr().substring(0, 35);
          String part2 = swiftPayments.getBeneficiaryAddr().substring(35, 70);
          sb.append(part1).append(AppConstants.CRLF);
          sb.append(part2).append(AppConstants.CRLF);
        }
      }
      sb.append(AppConstants.TAG_70).append(swiftPayments.getDocumentNumber()).append(AppConstants.COMMA).append(AppConstants.REF_INV).append(swiftPayments.getInvoiceNumbers()).append(AppConstants.CRLF);
      //sb.append(AppConstants.TAG_77B).append("").append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_71A).append(swiftPayments.getBankCharges()).append(AppConstants.CRLF);
      // OUR - Charges will be for you (Mohsen)
      // BEN - Charges will be for beneficiary
      // SHA - Charges will be shared between Mohsen and beneficiary

      String messageBody = sb.toString();

      SwiftMessageVO swiftMessageVO = new SwiftMessageVO();
      String formattedReceiverX1, formattedReceiverBIC12;
      String receiverBic = swiftPayments.getReceiverBic();

      if (receiverBic != null) {
        formattedReceiverBIC12 = getFormattedData(receiverBic, 12);
        formattedReceiverX1 = getFormattedData(receiverBic, 11);
      } else {
        formattedReceiverBIC12 = "";
        formattedReceiverX1 = "";
      }

      swiftMessageVO.setSenderX1(senderX1);
      swiftMessageVO.setSenderBIC12(senderBIC12);
      swiftMessageVO.setReceiverX1(formattedReceiverX1);
      swiftMessageVO.setReceiverBIC12(formattedReceiverBIC12);
      swiftMessageVO.setReference(swiftPayments.getSwiftRef());
      swiftMessageVO.setPaymentId(swiftPayments.getPaymentId().toString());
      swiftMessageVO.setMessagePartner(messagePartner);
      swiftMessageVO.setMessageBody(messageBody);

      String errorMsg = validationService.validateSwiftPayment(swiftPayments);
      if (errorMsg != null) {
        swiftMessageVO.setInvalid(true);
        swiftMessageVO.setStatusChange(true);
        swiftMessageVO.setStatus(errorMsg);
      }

      messages.add(swiftMessageVO);
      sleep();
    }

    LOG.info("retrieveDataFromERP - END");
  }

  private void retrieveDataFromSharepoint(StatusType statusType, List<SwiftMessageVO> messages) {

    LOG.info("retrieveDataFromSharepoint - BEGIN");

    SharepointRespVO sharepointRespVO = sharepointService.getSharepointItems(statusType);

    if (sharepointRespVO == null || sharepointRespVO.getSharepointVOList() == null || sharepointRespVO.getSharepointVOList().isEmpty()) {
      return;
    }

    LOG.info("retrieveDataFromSharepoint - No.of messages found in Sharepoint: {}",
        sharepointRespVO.getSharepointVOList().size());

    for (SharepointVO sharepointVO : sharepointRespVO.getSharepointVOList()) {

      Date paymentDate = AppUtil.getDate(AppConstants.FORMAT_YYYYMMDD_T_HHMMSS, sharepointVO.getPaymentDate());

      StringBuilder sb = new StringBuilder();
      sb.append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_20).append(sharepointVO.getReferenceNumber()).append(AppConstants.CRLF);
      //sb.append(AppConstants.TAG_20).append(AppConstants.REF_SPV).append(AppUtil.getDateString(AppConstants.FORMAT_YYMMDDHHMM, new Date())).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_28D).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_50H).append(sharepointVO.getMohsenAccountName()).append(AppConstants.CRLF);
      sb.append(sharepointVO.getMohsenBankName()).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_30).append(AppUtil.getDateString(AppConstants.FORMAT_YYMMDD, paymentDate)).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_21).append(sharepointVO.getReferenceNumber()).append(AppConstants.CRLF);
      sb.append(AppConstants.TAG_32B).append(sharepointVO.getPaymentCurrency()).append(sharepointVO.getPaymentAmount()).append(",").append(AppConstants.CRLF);
      if (!StringUtils.isBlank(sharepointVO.getVendorSwiftCode())) {
        sb.append(AppConstants.TAG_57A).append(sharepointVO.getVendorSwiftCode()).append(AppConstants.CRLF);
      }
      if (!StringUtils.isBlank(sharepointVO.getSupplierIBAN())) {
        sb.append(AppConstants.TAG_59).append(sharepointVO.getSupplierIBAN()).append(AppConstants.CRLF);
      }
      if (!StringUtils.isBlank(sharepointVO.getSupBankAcctName())) {
        if (isArabic(sharepointVO.getSupBankAcctName())) {
          sb.append(receiverName).append(AppConstants.CRLF);
        } else {
          String beneficiaryName = sharepointVO.getSupBankAcctName();
          if (beneficiaryName.length() > 35) {
            beneficiaryName = beneficiaryName.substring(0, 34);
          }
          sb.append(beneficiaryName).append(AppConstants.CRLF);
        }
      }
      if (!StringUtils.isBlank(sharepointVO.getAddressLine()) && !isArabic(sharepointVO.getAddressLine())) {
        int length = sharepointVO.getAddressLine().length();
        if (length <= 35) {
          sb.append(sharepointVO.getAddressLine()).append(AppConstants.CRLF);
        } else if (length <= 70) {
          String part1 = sharepointVO.getAddressLine().substring(0, 35);
          String part2 = sharepointVO.getAddressLine().substring(35, length);
          sb.append(part1).append(AppConstants.CRLF);
          sb.append(part2).append(AppConstants.CRLF);
        } else {
          String part1 = sharepointVO.getAddressLine().substring(0, 35);
          String part2 = sharepointVO.getAddressLine().substring(35, 70);
          sb.append(part1).append(AppConstants.CRLF);
          sb.append(part2).append(AppConstants.CRLF);
        }
      }
      if (sharepointVO.getVoucherNumber() != null) {
        sb.append(AppConstants.TAG_70).append(sharepointVO.getVoucherNumber()).append(AppConstants.COMMA).append(AppConstants.REF_INV).append(sharepointVO.getVoucherNumber()).append(AppConstants.CRLF);
      }
      sb.append(AppConstants.TAG_71A).append("OUR").append(AppConstants.CRLF);
      // OUR - Charges will be for you (Mohsen)
      // BEN - Charges will be for beneficiary
      // SHA - Charges will be shared between Mohsen and beneficiary

      String messageBody = sb.toString();

      SwiftMessageVO swiftMessageVO = new SwiftMessageVO();
      String formattedReceiverX1, formattedReceiverBIC12;
      String receiverBic = sharepointVO.getMohsenBankName();

      if (receiverBic != null) {
        receiverBic = receiverBic.replaceAll(" ", "");
        receiverBic = receiverBic.toUpperCase();
        formattedReceiverBIC12 = getFormattedData(receiverBic, 12);
        formattedReceiverX1 = getFormattedData(receiverBic, 11);
      } else {
        formattedReceiverBIC12 = "";
        formattedReceiverX1 = "";
      }

      swiftMessageVO.setTaskId(sharepointVO.getId());
      swiftMessageVO.setSenderX1(senderX1);
      swiftMessageVO.setSenderBIC12(senderBIC12);
      swiftMessageVO.setReceiverX1(formattedReceiverX1);
      swiftMessageVO.setReceiverBIC12(formattedReceiverBIC12);
      swiftMessageVO.setReference(sharepointVO.getReferenceNumber());
      swiftMessageVO.setPaymentId(sharepointVO.getReferenceNumber());
      swiftMessageVO.setMessagePartner(spvMessagePartner);
      swiftMessageVO.setMessageBody(messageBody);

      String errorMsg = validationService.validateSharepointData(sharepointVO);
      if (errorMsg != null) {
        LOG.error("INVALID error message for ReferenceNumber '{}': {}", sharepointVO.getReferenceNumber(), errorMsg);
        swiftMessageVO.setInvalid(true);
        swiftMessageVO.setStatusChange(true);
        swiftMessageVO.setStatus(errorMsg);
      }

      messages.add(swiftMessageVO);
      sleep();
    }

    LOG.info("retrieveDataFromSharepoint - END");
  }

  private String getRightPaddedString(String data, String length) {
    return String.format("%-" + length + "s", data).replace(' ', 'X');
  }

  private String getFormattedData(String data, int length) {
    String formattedData;
    if (data.length() > length) {
      formattedData = data.substring(0, (length + 1));
    } else if (data.length() == length) {
      formattedData = data;
    } else {
      formattedData = getRightPaddedString(data, String.valueOf(length));
    }
    return formattedData;
  }

  private void sleep() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      LOG.error("Error while putting the thread to sleep for 1000ms");
    }
  }

  private static boolean isArabic(String s) {
    if (s != null) {
      for (int i = 0; i < s.length(); ) {
        int c = s.codePointAt(i);
        if (c >= 0x0600 && c <= 0x06E0)
          return true;
        i += Character.charCount(c);
      }
    }
    return false;
  }
}
