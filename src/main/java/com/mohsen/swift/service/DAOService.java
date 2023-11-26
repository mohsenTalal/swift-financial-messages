package com.mohsen.swift.service;

import com.mohsen.swift.dao.erp.entity.SwiftPayments;
import com.mohsen.swift.dao.erp.entity.SwiftPaymentsLog;
import com.mohsen.swift.dao.erp.repository.PaymentsLogRepository;
import com.mohsen.swift.dao.erp.repository.PaymentsRepository;
import com.mohsen.swift.dao.integration.entity.SwiftStatus;
import com.mohsen.swift.dao.integration.repository.SwiftStatusRepository;
import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.util.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DAOService {

  private static final Logger LOG = LoggerFactory.getLogger(DAOService.class);

  @Autowired
  private PaymentsRepository paymentsRepository;

  @Autowired
  private PaymentsLogRepository logRepository;

  @Autowired
  private SwiftStatusRepository statusRepository;

  public List<SwiftPayments> getAllMessages() {
    return paymentsRepository.findAll();
  }

  public List<SwiftMessageVO> getSwiftPaymentsLog() {

    LOG.info("getSwiftPaymentsLog - BEGIN");
    List<SwiftMessageVO> messages = new ArrayList<>();

    List<SwiftPaymentsLog> logMessages = logRepository.findByLatestDate();
    for (SwiftPaymentsLog logMessage : logMessages) {
      SwiftMessageVO messageVO = new SwiftMessageVO();
      messageVO.setSeq(logMessage.getSeq());
      messageVO.setPaymentId(logMessage.getPaymentId().toString());
      messageVO.setStatus(logMessage.getStatus());
      messageVO.setInvalid(logMessage.getStatus().contains(StatusType.INVALID.getValue()));
      if (logMessage.getStatus().contains(StatusType.SWIFT_ERROR.getValue()) ||
          logMessage.getStatus().contains(StatusType.NETWORK_ERROR.getValue())) {
        messageVO.setError(Boolean.TRUE);
      } else {
        messageVO.setError(Boolean.FALSE);
      }
      messageVO.setActionDate(logMessage.getActionDate());
      messages.add(messageVO);
    }

    LOG.info("getSwiftPaymentsLog - END");
    return messages;
  }

  public void updateSwiftStatus(List<SwiftMessageVO> messageList) {

    LOG.info("updateSwiftStatus - BEGIN");

    if (messageList == null || messageList.isEmpty()) {
      LOG.info("updateSwiftStatus - Message list is empty. No data to update.");
      LOG.info("updateSwiftStatus - END");
      return;
    } else {
      for (SwiftMessageVO messageVO : messageList) {
        if (messageVO.isStatusChange()) {
          logRepository.updatePaymentStatus(messageVO);
          LOG.info("updateSwiftStatus - SWIFT status updated for PaymentId: '{}' with Status: '{}'",
              messageVO.getPaymentId(), messageVO.getStatus());
        }
      }
    }

    updateStatusToDB(messageList);

    LOG.info("updateSwiftStatus - END");
  }

  public List<SwiftMessageVO> getAllStatusFromDB() {
    List<SwiftStatus> statusList = statusRepository.findAll();
    List<SwiftMessageVO> messageList = new ArrayList<>();
    for (SwiftStatus status : statusList) {
      SwiftMessageVO messageVO = new SwiftMessageVO();
      messageVO.setReference(status.getReference());
      messageVO.setStatus(status.getStatus());
      messageList.add(messageVO);
    }
    return messageList;
  }

  private void updateStatusToDB(List<SwiftMessageVO> messageList) {

    LOG.info("updateStatusToDB - BEGIN");

    for (SwiftMessageVO messageVO : messageList) {
      if (messageVO.isStatusChange()) {
        List<SwiftStatus> swiftStatuses = statusRepository.findByPaymentId(messageVO.getPaymentId());
        if (swiftStatuses == null || swiftStatuses.isEmpty()) {
          statusRepository.insertStatus(messageVO.getReference(), messageVO.getPaymentId(), messageVO.getStatus());
          LOG.info("updateStatusToDB - SWIFT status inserted for Reference: '{}' and PaymentId: '{}' with Status: '{}'",
              messageVO.getReference(), messageVO.getPaymentId(), messageVO.getStatus());
        } else if (swiftStatuses.size() == 1) {
          statusRepository.updateStatusByPaymentId(messageVO.getPaymentId(), messageVO.getStatus());
          LOG.info("updateStatusToDB - SWIFT status updated for PaymentId: '{}' with Status: '{}'",
              messageVO.getPaymentId(), messageVO.getStatus());
        }
      }
    }

    LOG.info("updateStatusToDB - END");
  }

  public void updateSharepointStatusToDB(List<SwiftMessageVO> messageList) {

    LOG.info("updateSharepointStatusToDB - BEGIN");

    for (SwiftMessageVO messageVO : messageList) {
      if (messageVO.isStatusChange()) {
        List<SwiftStatus> swiftStatuses = statusRepository.findByReference(messageVO.getReference());
        if (swiftStatuses == null || swiftStatuses.isEmpty()) {
          statusRepository.insertStatus(messageVO.getReference(), messageVO.getReference(), messageVO.getStatus());
          LOG.info("updateSharepointStatusToDB - SWIFT status inserted for Reference: '{}' with Status: '{}'",
              messageVO.getReference(), messageVO.getStatus());
        } else if (swiftStatuses.size() == 1) {
          statusRepository.updateStatusByReference(messageVO.getReference(), messageVO.getStatus());
          LOG.info("updateSharepointStatusToDB - SWIFT status updated for Reference: '{}' with Status: '{}'",
              messageVO.getReference(), messageVO.getStatus());
        }
      }
    }

    LOG.info("updateSharepointStatusToDB - END");
  }
}
