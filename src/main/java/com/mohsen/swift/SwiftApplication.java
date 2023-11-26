package com.mohsen.swift;

import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.service.*;
import com.mohsen.swift.util.MessageType;
import com.mohsen.swift.util.SourceType;
import com.mohsen.swift.util.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SpringBootApplication
public class SwiftApplication implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SwiftApplication.class);

  @Autowired
  private SwiftService swiftService;

  @Autowired
  private DAOService daoService;

  @Autowired
  private TranslatorService translatorService;

  @Autowired
  private EmailService emailService;

  @Autowired
  private SftpService sftpService;

  @Value("${swift.message-partner}")
  private String messagePartner;

  @Value("${swift.spv.message-partner}")
  private String spvMessagePartner;

  @Value("${swift.file.active}")
  private boolean fileActive;

  public static void main(String[] args) {
    SpringApplication.run(SwiftApplication.class, args).close();
  }

  @Override
  public void run(String... args) {

    trustAllCerts();
    LOG.info("SwiftApplication.run - BEGIN");
    processERPSwiftMessages();
    processSharepointSPVMessages();
    LOG.info("SwiftApplication.run - END");
  }

  private void processERPSwiftMessages() {

    LOG.info("processERPSwiftMessages - BEGIN");
    List<SwiftMessageVO> messageList = translatorService.getSwiftMessages();
    LOG.info("processERPSwiftMessages - No.of SWIFT messages to process: {}", messageList.size());

    List<SwiftMessageVO> processedList = daoService.getSwiftPaymentsLog();
    LOG.info("processERPSwiftMessages - No.of SWIFT messages processed: {}", processedList.size());

    List<SwiftMessageVO> dbList = daoService.getAllStatusFromDB();
    LOG.info("processERPSwiftMessages - No.of SWIFT messages in DB: {}", dbList.size());

    List<SwiftMessageVO> filteredList = translatorService.filterSwiftMessages(messageList, processedList, dbList);
    LOG.info("processERPSwiftMessages - Filtered SWIFT messages to process: {}", filteredList.size());

    boolean isOpen = swiftService.open(MessageType.OTHERS, messagePartner);
    LOG.info("processERPSwiftMessages - SWIFT open connection status: {}", isOpen);

    if (isOpen) {
      for (SwiftMessageVO processedVO : processedList) {
        if (StatusType.DELIVERED.getValue().equalsIgnoreCase(processedVO.getStatus())) {
          String status = swiftService.getAck(MessageType.OTHERS, processedVO.getPaymentId());
          if (NetworkDeliveryStatus.NETWORK_ACKED.value().equalsIgnoreCase(status)) {
            processedVO.setStatus(StatusType.ACK.getValue());
            processedVO.setStatusChange(true);
          } else if (NetworkDeliveryStatus.NETWORK_NACKED.value().equalsIgnoreCase(status)) {
            processedVO.setStatus(StatusType.NACK.getValue());
            processedVO.setStatusChange(true);
          }
          LOG.info("processERPSwiftMessages - SWIFT GetAck paymentId: {}, status: {}", processedVO.getPaymentId(), status);
        }
      }
      LOG.info("processERPSwiftMessages - SWIFT GetAck completed");

      List<SwiftMessageVO> invalidList = translatorService.getInvalidMessages(filteredList);
      List<SwiftMessageVO> validList = translatorService.getMessagesBasedOnStatus(filteredList, StatusType.PICKED);
      LOG.info("processERPSwiftMessages - Invalid SWIFT messages count: {}", invalidList.size());
      LOG.info("processERPSwiftMessages - Valid SWIFT messages count: {}", validList.size());

      if (!fileActive) {
        daoService.updateSwiftStatus(validList);
      }

      for (SwiftMessageVO message : validList) {
        boolean isSuccess = swiftService.put(MessageType.OTHERS, message);
        LOG.info("processERPSwiftMessages - SWIFT put message reference: {}, status: {}", message.getReference(),
            isSuccess);
      }

      boolean isClose = swiftService.close(MessageType.OTHERS);
      LOG.info("processERPSwiftMessages - SWIFT close connection status: {}", isClose);

      emailService.sendHtmlEmailMessage(SourceType.ERP, filteredList);
      LOG.info("processERPSwiftMessages - Email notification sent");

      if (!fileActive) {
        LOG.info("processERPSwiftMessages - Updating Swift Status for processed list");
        daoService.updateSwiftStatus(processedList);
        LOG.info("processERPSwiftMessages - Updating Swift Status for valid list");
        daoService.updateSwiftStatus(validList);
        LOG.info("processERPSwiftMessages - Updating Swift Status for invalid list");
        daoService.updateSwiftStatus(invalidList);
      }
    }

    if (!fileActive) {
      // Process MT940 messages
      sftpService.uploadFilesToERP();
    }

    LOG.info("processERPSwiftMessages - END");
  }

  private void processSharepointSPVMessages() {

    LOG.info("processSharepointSPVMessages - BEGIN");

    List<SwiftMessageVO> messageList = translatorService.getSpvSwiftMessages(StatusType.READY_TO_PICK);
    LOG.info("processSharepointSPVMessages - No.of SWIFT messages to process: {}", messageList.size());

    List<SwiftMessageVO> processedList = translatorService.getSpvSwiftMessages(StatusType.DELIVERED);
    LOG.info("processSharepointSPVMessages - No.of SWIFT messages processed: {}", processedList.size());

    List<SwiftMessageVO> dbList = daoService.getAllStatusFromDB();
    LOG.info("processSharepointSPVMessages - No.of SWIFT messages in DB: {}", dbList.size());

    List<SwiftMessageVO> filteredList = translatorService.filterSpvSwiftMessages(messageList, processedList, dbList);
    LOG.info("processSharepointSPVMessages - Filtered SWIFT messages to process: {}", filteredList.size());

    boolean isOpen = swiftService.open(MessageType.SPV, spvMessagePartner);
    LOG.info("processSharepointSPVMessages - SWIFT open connection status: {}", isOpen);

    if (isOpen) {
      for (SwiftMessageVO processedVO : processedList) {
        if (StatusType.DELIVERED.getValue().equalsIgnoreCase(processedVO.getStatus())) {
          String status = swiftService.getAck(MessageType.SPV, processedVO.getReference());
          if (NetworkDeliveryStatus.NETWORK_ACKED.value().equalsIgnoreCase(status)) {
            processedVO.setStatus(StatusType.ACK.getValue());
            processedVO.setStatusChange(true);
          } else if (NetworkDeliveryStatus.NETWORK_NACKED.value().equalsIgnoreCase(status)) {
            processedVO.setStatus(StatusType.NACK.getValue());
            processedVO.setStatusChange(true);
          }
          LOG.info("processSharepointSPVMessages - SWIFT GetAck paymentId: {}, status: {}", processedVO.getPaymentId(), status);
        }
      }
      LOG.info("processSharepointSPVMessages - SWIFT GetAck completed");

      List<SwiftMessageVO> invalidList = translatorService.getInvalidMessages(filteredList);
      List<SwiftMessageVO> validList = translatorService.getMessagesBasedOnStatus(filteredList, StatusType.PICKED);
      LOG.info("processSharepointSPVMessages - Invalid SWIFT messages count: {}", invalidList.size());
      LOG.info("processSharepointSPVMessages - Valid SWIFT messages count: {}", validList.size());

      if (!fileActive) {
        translatorService.updateSpvSwiftStatus(validList);
      }

      for (SwiftMessageVO message : validList) {
        boolean isSuccess = swiftService.put(MessageType.SPV, message);
        LOG.info("processSharepointSPVMessages - SWIFT put message reference: {}, status: {}", message.getReference(),
            isSuccess);
      }

      boolean isClose = swiftService.close(MessageType.SPV);
      LOG.info("processSharepointSPVMessages - SWIFT close connection status: {}", isClose);

      emailService.sendHtmlEmailMessage(SourceType.SHAREPOINT, filteredList);
      LOG.info("processSharepointSPVMessages - Email notification sent");

      if (!fileActive) {
        LOG.info("processSharepointSPVMessages - Updating Swift Status for processed list");
        translatorService.updateSpvSwiftStatus(processedList);
        LOG.info("processSharepointSPVMessages - Updating Swift Status for valid list");
        translatorService.updateSpvSwiftStatus(validList);
        LOG.info("processSharepointSPVMessages - Updating Swift Status for invalid list");
        translatorService.updateSpvSwiftStatus(invalidList);
      }
    }

    LOG.info("processSharepointSPVMessages - END");
  }

  private void trustAllCerts() {
    try {
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, trustManagers, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      LOG.error("Error while setting SSL Context to trust all certificates");
    }
  }

  TrustManager[] trustManagers = new TrustManager[]{
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
      }
  };
}