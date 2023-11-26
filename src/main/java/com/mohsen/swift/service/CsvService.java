package com.mohsen.swift.service;

import com.mohsen.swift.model.StatusVO;
import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.AppUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CsvService {

  private static final Logger LOG = LoggerFactory.getLogger(CsvService.class);

  @Value("${tracker.csv}")
  private String csvFile;

  public List<StatusVO> getCsvData() {

    LOG.info("getCsvData - BEGIN");
    List<StatusVO> statusList = new ArrayList<>();

    try (CSVParser parser = CSVFormat.DEFAULT.parse(new FileReader(csvFile))) {
      for (CSVRecord record : parser) {
        StatusVO statusVO = new StatusVO();
        statusVO.setProcessingDate(AppUtil.getDate(AppConstants.FORMAT_YYYYMMDDHHMMSS, record.get(0)));
        statusVO.setReference(record.get(1));
        statusVO.setPaymentId(record.get(2));
        statusVO.setSenderBIC(record.get(3));
        statusVO.setReceiverBIC(record.get(4));
        statusVO.setStatus(record.get(5));
        statusVO.setStatusMessage(record.get(6));
        statusVO.setLastUpdateDate(AppUtil.getDate(AppConstants.FORMAT_YYYYMMDDHHMMSS, record.get(7)));
        statusList.add(statusVO);
      }
    } catch (IOException e) {
      LOG.error("Error while parsing the CSV file : {}", csvFile, e);
    }

    LOG.info("getCsvData - No.of processed data in CSV file: {}", statusList.size());
    LOG.info("getCsvData - END");
    return statusList;
  }

  public void updateCsvData(List<StatusVO> statusList, List<SwiftMessageVO> messageList, boolean isStatusUpdated) {

    LOG.info("updateCsvData - BEGIN");

    if ((messageList == null || messageList.isEmpty()) && !isStatusUpdated) {
      LOG.info("updateCsvData - Message list is empty or no status updates. No data to update in CSV.");
      LOG.info("updateCsvData - END");
      return;
    }

    if (messageList != null && !messageList.isEmpty()) {
      List<StatusVO> processList = new ArrayList<>();
      Date currentDate = new Date();

      for (SwiftMessageVO message : messageList) {
        StatusVO statusVO = new StatusVO();
        statusVO.setReference(message.getReference());
        statusVO.setPaymentId(message.getPaymentId());
        statusVO.setSenderBIC(message.getSenderBIC12());
        statusVO.setReceiverBIC(message.getReceiverBIC12());
        if (message.isSuccess()) {
          statusVO.setStatus(AppConstants.SUCCESS);
        } else {
          statusVO.setStatus(AppConstants.ERROR);
        }
        statusVO.setStatusMessage(message.getStatus());
        statusVO.setProcessingDate(currentDate);
        statusVO.setLastUpdateDate(currentDate);
        processList.add(statusVO);
      }

      LOG.info("updateCsvData - New processed records to update in CSV file: {}", processList.size());
      statusList.addAll(processList);
    }

    try (CSVPrinter csvPrinter = new CSVPrinter(new BufferedWriter(new FileWriter(csvFile)),
        CSVFormat.DEFAULT)) {
      for (StatusVO statusVO : statusList) {
        csvPrinter.printRecord(AppUtil.getDateString(AppConstants.FORMAT_YYYYMMDDHHMMSS, statusVO.getProcessingDate()),
            statusVO.getReference(), statusVO.getPaymentId(), statusVO.getSenderBIC(), statusVO.getReceiverBIC(),
            statusVO.getStatus(), statusVO.getStatusMessage(),
            AppUtil.getDateString(AppConstants.FORMAT_YYYYMMDDHHMMSS, statusVO.getLastUpdateDate()));
      }
    } catch (IOException e) {
      LOG.error("Error while writing SWIFT processing status to CSV", e);
    }

    LOG.info("updateCsvData - Total processed records in CSV file: {}", statusList.size());
    LOG.info("updateCsvData - END");
  }

  public List<SwiftMessageVO> filterProcessedMessages(List<StatusVO> statusList, List<SwiftMessageVO> messageList) {

    List<SwiftMessageVO> filteredList = new ArrayList<>();
    boolean matchFound;

    for (SwiftMessageVO message : messageList) {
      matchFound = false;
      for (StatusVO status : statusList) {
        if (message.getPaymentId().equalsIgnoreCase(status.getPaymentId()) &&
            message.getSenderBIC12().equalsIgnoreCase(status.getSenderBIC()) &&
            message.getReceiverBIC12().equalsIgnoreCase(status.getReceiverBIC())) {
          matchFound = true;
          break;
        }
      }
      if (!matchFound) {
        filteredList.add(message);
      }
    }

    return filteredList;
  }
}
