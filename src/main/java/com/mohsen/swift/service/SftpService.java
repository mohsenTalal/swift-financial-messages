package com.mohsen.swift.service;

import com.mohsen.swift.dao.erp.repository.PaymentsLogRepository;
import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.AppUtil;
import com.jcraft.jsch.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SftpService {

  private static final Logger LOG = LoggerFactory.getLogger(SftpService.class);

  @Value("${sftp.host}")
  String host;

  @Value("${sftp.port}")
  int port;

  @Value("${sftp.username}")
  String username;

  @Value("${sftp.password}")
  String password;

  @Value("${sftp.directory}")
  String erpFolder;

  @Value("${sftp.host-file}")
  String hostFile;

  @Value("${swift.shared-folder}")
  String swiftFolder;

  @Value("${swift.local-folder}")
  String localFolder;

  @Value("${sftp.time.hour}")
  int timeHour;

  @Value("${sftp.time.minute}")
  int timeMinute;

  @Autowired
  CryptoService cryptoService;

  @Autowired
  PaymentsLogRepository repository;

  private Session jschSession;

  private ChannelSftp setupJsch() throws JSchException {
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    JSch jsch = new JSch();
    jsch.setKnownHosts(hostFile);
    jschSession = jsch.getSession(cryptoService.decrypt(username), host, port);
    jschSession.setConfig(config);
    jschSession.setPassword(cryptoService.decrypt(password));
    jschSession.connect();
    return (ChannelSftp) jschSession.openChannel("sftp");
  }

  public void uploadFilesToERP() {

    LOG.info("uploadFilesToERP - BEGIN");
    ChannelSftp channelSftp = null;
    List<String> fileList = new ArrayList<>();

    try {
      channelSftp = setupJsch();
      channelSftp.connect();
      channelSftp.cd(erpFolder);

      String currentDate = AppUtil.getDateString(AppConstants.FORMAT_YYYYMMDD, Calendar.getInstance().getTime());
      String localDir = localFolder + currentDate + File.separator;
      String erpDir = erpFolder + currentDate + File.separator;
      LOG.info("Local SWIFT Folder: {}", localDir);
      LOG.info("ERP Shared Folder: {}", erpDir);
      LOG.info("SWIFT Shared Folder: {}", swiftFolder);

      Files.createDirectories(Paths.get(localDir));
      try {
        channelSftp.cd(currentDate);
      } catch (SftpException se) {
        channelSftp.mkdir(currentDate);
        channelSftp.cd(currentDate);
      }

      Set<String> swiftFiles = getFilesName(swiftFolder);
      Set<String> localFiles = getFilesName(localDir);

      if (localFiles != null) {
        LOG.info("Total files present in Local SWIFT Folder: {}", localFiles.size());
      }

      if (swiftFiles != null) {
        LOG.info("Total files present in SWIFT Shared Folder: {}", swiftFiles.size());
        boolean matchFound;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        for (String swiftFile : swiftFiles) {
          File fileSwift = new File(swiftFolder + swiftFile);
          matchFound = false;

          if (localFiles == null || localFiles.size() == 0) {
            if (hour >= timeHour && minute >= timeMinute) {
              fileList.add(swiftFile);
            }
          } else {
            for (String localFile : localFiles) {
              File fileLocal = new File(localDir + localFile);
              if (swiftFile.equalsIgnoreCase(localFile) && fileLocal.lastModified() >= fileSwift.lastModified()) {
                matchFound = true;
                break;
              }
            }
            if (!matchFound) {
              fileList.add(swiftFile);
            }
          }
        }

        LOG.info("Total no.of files to upload to ERP shared folder: {}", fileList.size());

        for (String file : fileList) {
          LOG.info("Uploading file '{}' to ERP Shared Folder", file);
          channelSftp.put(swiftFolder + file, erpDir + file);
          LOG.info("Completed uploading of file '{}' to ERP Shared Folder", file);

          FileUtils.copyFile(new File(swiftFolder + file), new File(localDir + file));
          repository.updateMT940FileDetails(file, erpDir);
          LOG.info("File '{}' copied to local directory and data updated in ERP", file);
        }
      }
    } catch (Exception e) {
      LOG.error("Error while uploading the files to ERP Shared Folder", e);
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
        channelSftp.exit();
        jschSession.disconnect();
      }
    }

    LOG.info("uploadFilesToERP - END");
  }

  private Set<String> getFilesName(String dir) throws IOException {
    try (Stream<Path> stream = Files.list(Paths.get(dir))) {
      return stream
          .filter(file -> !Files.isDirectory(file))
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toSet());
    }
  }
}
