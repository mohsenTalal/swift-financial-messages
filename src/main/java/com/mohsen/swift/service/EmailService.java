package com.mohsen.swift.service;

import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Component
public class EmailService {

  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String sender;

  @Value("${swift.email.receivers}")
  private String[] receivers;

  @Value("${swift.email.subject}")
  private String subject;

  @Value("${swift.email.active}")
  private boolean active;

  public void sendHtmlEmailMessage(SourceType sourceType, List<SwiftMessageVO> swiftMessages) {
    if (!active || swiftMessages.isEmpty()) {
      return;
    }
    try {
      LOG.info("sendHtmlEmailMessage - BEGIN");
      String sub = subject.replaceAll(AppConstants.SOURCE, sourceType.name());
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
      helper.setFrom(sender);
      helper.setTo(receivers);
      helper.setSubject(sub);
      helper.setText(getHtmlEmailContent(sourceType, swiftMessages), true);
      mailSender.send(mimeMessage);
      LOG.info("sendHtmlEmailMessage - END");
    } catch (MessagingException e) {
      LOG.error("Error while sending HTML email");
    }
  }

  private String getHtmlEmailContent(SourceType sourceType, List<SwiftMessageVO> swiftMessages) {

    int count = 1;
    StringBuilder sb = new StringBuilder();

    sb.append("<html>");
    sb.append("<head><style>");
    sb.append("table, th, td { border: 1px solid black; border-collapse: collapse; }");
    sb.append("th, td { padding: 20px; }");
    sb.append("</style></head>");
    sb.append("<body>");
    sb.append("<p>Dears,</p>");
    sb.append("<br/>");
    sb.append("<p>Please find below the status of SWIFT-").append(sourceType.name()).append(" integration.</p>");

    sb.append("<table><thead><tr>");
    sb.append("<th>SNO</th>");
    sb.append("<th>REFERENCE</th>");
    sb.append("<th>PAYMENT ID</th>");
    sb.append("<th>STATUS</th>");
    sb.append("<th>STATUS MESSAGE</th>");
    sb.append("</tr></thead><tbody>");

    for (SwiftMessageVO message : swiftMessages) {
      sb.append("<tr>");
      sb.append("<td>").append(count).append("</td>");
      sb.append("<td>").append(message.getReference()).append("</td>");
      sb.append("<td>").append(message.getPaymentId()).append("</td>");
      if (message.isSuccess()) {
        sb.append("<td style=\"color:green;\">").append("SUCCESS").append("</td>");
        sb.append("<td style=\"color:green;\">").append("SUCCESS").append("</td>");
      } else {
        sb.append("<td style=\"color:red;\">").append("ERROR").append("</td>");
        sb.append("<td style=\"color:red;\">").append(message.getStatus()).append("</td>");
      }
      sb.append("</tr>");
    }

    sb.append("</tbody></table>");
    sb.append("<br/><br/>");
    sb.append("<div>Regards,</div><br/>");
    sb.append("<div>IT Integration Team.</div>");
    sb.append("</body>");
    sb.append("</html>");

    LOG.debug("HTML Email Content: {}", sb);
    return sb.toString();
  }
}
