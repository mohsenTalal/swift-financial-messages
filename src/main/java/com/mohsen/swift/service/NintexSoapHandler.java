package com.mohsen.swift.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringWriter;
import java.util.Set;

public class NintexSoapHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOG = LoggerFactory.getLogger(NintexSoapHandler.class);

  private final String formDigest;

  public NintexSoapHandler(String formDigest) {
    this.formDigest = formDigest;
  }

  @Override
  public Set<QName> getHeaders() {
    return null;
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    LOG.info("handleMessage - outboundProperty: {}", outboundProperty);
    try {
      SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
      SOAPHeader header = envelope.getHeader();
      if (header == null && formDigest != null) {
        header = envelope.addHeader();
      }
      if (outboundProperty) {
        if (header != null) {
          header.addChildElement(getHeaderElement(formDigest));
        }
        LOG.info("OUTBOUND SOAP MESSAGE ::: {}", getSOAPMsgCtxAsString(context));
      } else {
        LOG.info("INBOUND SOAP MESSAGE ::: {}", getSOAPMsgCtxAsString(context));
      }
    } catch (Exception e) {
      LOG.error("Error while processing SOAP Header", e);
    }
    return true;
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    LOG.error("SOAP MESSAGE FAULT ::: {}", getSOAPMsgCtxAsString(context));
    try {
      SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
      SOAPFault fault = envelope.getBody().getFault();
      LOG.info("Fault Code: {}, Fault String: {}", fault.getFaultCode(), fault.getFaultString());
    } catch (Exception e) {
      LOG.error("Error while processing SOAP Fault Message", e);
    }
    return false;
  }

  @Override
  public void close(MessageContext messageContext) {

  }

  private String getSOAPMsgCtxAsString(SOAPMessageContext messageContext) {
    final SOAPMessage message = messageContext.getMessage();
    final StringWriter sw = new StringWriter();

    try {
      TransformerFactory.newInstance().newTransformer().transform(
          new DOMSource(message.getSOAPPart()),
          new StreamResult(sw));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }

    return sw.toString();
  }

  private SOAPElement getHeaderElement(String formDigest) {
    SOAPElement headerElement = null;
    try {
      SOAPFactory factory = SOAPFactory.newInstance();
      headerElement = factory.createElement("X-RequestDigest");
      headerElement.addTextNode(formDigest);
    } catch (SOAPException e) {
      LOG.error("Error while generating SOAP Header", e);
    }
    return headerElement;
  }
}
