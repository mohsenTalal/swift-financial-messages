package com.mohsen.swift.service;

import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.AppUtil;
import com.mohsen.swift.util.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringWriter;
import java.util.*;

@Component
public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOG = LoggerFactory.getLogger(HeaderHandler.class);

  @Value("${swift.key-left}")
  private String swiftKeyLeft;

  @Value("${swift.key-right}")
  private String swiftKeyRight;

  @Override
  public Set<QName> getHeaders() {
    return new HashSet<>();
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

    LOG.info("handleMessage - outboundProperty: {}", outboundProperty);
    try {
      SOAPEnvelope soapEnvelope = context.getMessage().getSOAPPart().getEnvelope();
      //soapEnvelope.addNamespaceDeclaration(AppConstants.PREFIX_WSU, AppConstants.URI_SECURITY_UTIL);
      soapEnvelope.addNamespaceDeclaration(AppConstants.PREFIX_URN, AppConstants.URI_SOAP_HA);
      soapEnvelope.addNamespaceDeclaration(AppConstants.PREFIX_SAA, AppConstants.URI_SOAP_SAA);
      soapEnvelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      soapEnvelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
      SOAPHeader soapHeader = soapEnvelope.getHeader();
      SOAPBody soapBody = soapEnvelope.getBody();
      SOAPElement saaHeaderElement = null;

      if (soapHeader == null) {
        soapHeader = soapEnvelope.addHeader();
      }

//      soapEnvelope.setPrefix(AppConstants.PREFIX_SOAP_ENV);
//      soapHeader.setPrefix(AppConstants.PREFIX_SOAP_ENV);
//      soapBody.setPrefix(AppConstants.PREFIX_SOAP_ENV);
//      soapEnvelope.removeNamespaceDeclaration("S");
//      soapEnvelope.removeNamespaceDeclaration("SOAP-ENV");

//      LOG.info("SOAP MESSAGE (BEFORE) ::: {}", getSOAPMsgCtxAsString(context));
//      LOG.info("SOAP BODY - NodeName: {}, LocalName: {}", soapBody.getFirstChild().getNodeName(),
//          soapBody.getFirstChild().getLocalName());

      if (outboundProperty) {
        boolean isSaaHeaderPresent = false;
        if (OperationType.OPEN.getValue().equalsIgnoreCase(soapBody.getFirstChild().getLocalName())) {
          LOG.info("Operation type is OPEN");
          String messagePartner = getMessagePartner(soapBody);
          AppConstants.OPEN_OBJ.setMessagePartnerName(messagePartner);
        } else {
          LOG.info("Operation type is Not OPEN");
          saaHeaderElement = marshalSOAPHeader();
          soapHeader.addChildElement(saaHeaderElement);
          isSaaHeaderPresent = true;
          LOG.info("SOAP MESSAGE (SAA HEADER) ::: {}", getSOAPMsgCtxAsString(context));
        }

        Name securityName = soapEnvelope.createName("Security", "wsse",
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        Name attributeName = soapEnvelope.createName("Id");
        signMessage(soapHeader, soapBody, saaHeaderElement, securityName, attributeName, isSaaHeaderPresent);
      } else {
        Iterator<Node> iterator = soapHeader.getChildElements();
        while (iterator.hasNext()) {
          Node node = iterator.next();
          if ("SAAHeader".equalsIgnoreCase(node.getLocalName())) {
            unmarshalSOAPHeader(node);
          }
        }
      }
      LOG.info("SOAP MESSAGE (AFTER) ::: {}", getSOAPMsgCtxAsString(context));
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
      Detail detail = fault.getDetail();
      LOG.info("Fault Code: {}, Fault String: {}", fault.getFaultCode(), fault.getFaultString());

      if (detail != null) {
        for (Iterator<DetailEntry> itEntry = detail.getDetailEntries(); itEntry.hasNext(); ) {
          DetailEntry entry = itEntry.next();
          LOG.info("Entry NodeName: {}", entry.getNodeName());
          if ("SAAFault".equalsIgnoreCase(entry.getNodeName())) {
            unmarshallToSAAFault(entry);
          }
          for (Iterator<Node> itNode = entry.getChildElements(); itNode.hasNext(); ) {
            Node node = itNode.next();
            LOG.info("NodeName: {}, NodeValue: {}", node.getNodeName(), node.getTextContent());
          }
        }
      }
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
          new DOMSource(message.getSOAPPart()), new StreamResult(sw));
    } catch (TransformerException e) {
      LOG.error("Unable to convert the SOAPMessageContext as string");
    }

    return sw.toString();
  }

  private void signMessage(SOAPHeader soapHeader, SOAPBody soapBody, SOAPElement saaHeader, Name securityName,
                           Name attributeName, boolean isSaaHeaderPresent) {
    LOG.info("signMessage - Begin");
    try {
      SOAPHeaderElement securityElement = soapHeader.addHeaderElement(securityName);
      securityElement.addNamespaceDeclaration(AppConstants.PREFIX_SOAP_ENV, AppConstants.URI_SOAP_ENVELOPE);
      securityElement.addAttribute(new QName("soapenv:actor"), "urn:swift:saa");
      soapHeader.addChildElement(securityElement);

      //soapBody.addAttribute(new QName(AppConstants.URI_SECURITY_UTIL, "id", AppConstants.PREFIX_WSU), "SAAPayload");
      soapBody.addAttribute(attributeName, "SAAPayload");
      org.w3c.dom.Node firstNode = soapBody.getFirstChild();
      firstNode.setPrefix(AppConstants.PREFIX_URN);
      org.w3c.dom.Node secondNode = firstNode.getFirstChild();
      if ("DataPDU".equalsIgnoreCase(secondNode.getNodeName())) {
        secondNode.setPrefix(AppConstants.PREFIX_SAA);
      }

      XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance();
      Transform transform = sigFactory.newTransform(AppConstants.URI_CANONICAL_EXCLUSIVE, (TransformParameterSpec) null);
      Reference refBody = sigFactory.newReference(
          "#SAAPayload", sigFactory.newDigestMethod(DigestMethod.SHA256, null),
          Collections.singletonList(transform), null, null);
      Reference refSaaHeader = sigFactory.newReference(
          "#SAAHeader", sigFactory.newDigestMethod(DigestMethod.SHA256, null),
          Collections.singletonList(transform), null, null);
      Reference refKeyInfo = sigFactory.newReference(
          "#KeyInfo", sigFactory.newDigestMethod(DigestMethod.SHA256, null),
          Collections.singletonList(transform), null, null);

      List<Reference> references = new ArrayList<>();
      if (isSaaHeaderPresent) {
        references.add(refSaaHeader);
      }
      references.add(refBody);
      references.add(refKeyInfo);

      SignatureMethod sigMethod = sigFactory.newSignatureMethod(AppConstants.URI_SIGNATURE_METHOD, null);
      CanonicalizationMethod sigCanonicalization = sigFactory.newCanonicalizationMethod(
          CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);

      SignedInfo signedInfo = sigFactory.newSignedInfo(sigCanonicalization, sigMethod, references);
      KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
      KeyName kn = kif.newKeyName(AppConstants.OPEN_OBJ.getMessagePartnerName());
      KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(kn), "KeyInfo");

      XMLSignature sig = sigFactory.newXMLSignature(signedInfo, keyInfo);

      DOMSignContext sigContext = new DOMSignContext(
          AppUtil.getSecretKeySpec(swiftKeyLeft, swiftKeyRight), securityElement);
      sigContext.putNamespacePrefix(XMLSignature.XMLNS, AppConstants.PREFIX_DS);
      sigContext.setIdAttributeNS(soapBody, null, "Id");
      if (isSaaHeaderPresent) {
        sigContext.setIdAttributeNS(saaHeader, null, "Id");
      }
      sig.sign(sigContext);
    } catch (Exception e) {
      LOG.error("Error while signing the message", e);
    }
    LOG.info("signMessage - End");
  }

  private SOAPElement marshalSOAPHeader() {
    SOAPElement headerElement = null;
    try {
      SOAPFactory factory = SOAPFactory.newInstance();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      Document document = dbf.newDocumentBuilder().newDocument();
      ObjectFactory objFactory = new ObjectFactory();
      objFactory.createSAAHeader(AppConstants.SAA_HEADER);
      Marshaller marshaller = JAXBContext.newInstance(SAAHeader.class).createMarshaller();
      marshaller.marshal(objFactory.createSAAHeader(AppConstants.SAA_HEADER), document);
      headerElement = factory.createElement(document.getDocumentElement());
    } catch (Exception e) {
      LOG.error("Error while marshalling SOAP Header", e);
    }
    return headerElement;
  }

  private void unmarshalSOAPHeader(org.w3c.dom.Node soapNode) {
    try {
      Unmarshaller marshaller = JAXBContext.newInstance(SAAHeader.class).createUnmarshaller();
      JAXBElement<SAAHeader> unmarshalElement = marshaller.unmarshal(soapNode, SAAHeader.class);
      if (unmarshalElement != null) {
        SAAHeader header = unmarshalElement.getValue();
        AppConstants.SAA_HEADER.setId("SAAHeader");
        AppConstants.SAA_HEADER.setSessionToken(header.getSessionToken());
        if (header.getClientRef() != null) {
          AppConstants.SAA_HEADER.setClientRef(header.getClientRef());
        }
        if (header.getAckClientRef() != null) {
          AppConstants.SAA_HEADER.setAckClientRef(header.getAckClientRef());
        }
        AppConstants.SAA_HEADER.setAckNack(header.getAckNack());

        LOG.info("unmarshalSOAPHeader - Id: {}", AppConstants.SAA_HEADER.getId());
        LOG.info("unmarshalSOAPHeader - Session Token: {}", AppConstants.SAA_HEADER.getSessionToken());
        LOG.info("unmarshalSOAPHeader - Sequence Number: {}", AppConstants.SAA_HEADER.getSequenceNumber());
        LOG.info("unmarshalSOAPHeader - Client Ref: {}", AppConstants.SAA_HEADER.getClientRef());
        LOG.info("unmarshalSOAPHeader - Ack Client Ref: {}", AppConstants.SAA_HEADER.getAckClientRef());
      }
    } catch (Exception e) {
      LOG.error("Error while unmarshalling SOAP Header", e);
    }
  }

  private void unmarshallToSAAFault(DetailEntry entry) {
    try {
      Unmarshaller marshaller = JAXBContext.newInstance(FaultDetails.class).createUnmarshaller();
      JAXBElement<FaultDetails> unmarshalElement = marshaller.unmarshal(entry, FaultDetails.class);
      if (unmarshalElement != null) {
        FaultDetails saaFault = unmarshalElement.getValue();
        LOG.info("SAAFault : Context: {}", saaFault.getContext());
        LOG.info("SAAFault - Details: {}", saaFault.getDetails());
        LOG.info("SAAFault - Reason: {}", saaFault.getReason());
        LOG.info("SAAFault - Severity: {}", saaFault.getSeverity());
        AppConstants.SAA_FAULT.setContext(saaFault.getContext());
        AppConstants.SAA_FAULT.setDetails(saaFault.getDetails());
        AppConstants.SAA_FAULT.setReason(saaFault.getReason());
        AppConstants.SAA_FAULT.setSeverity(saaFault.getSeverity());
      }
    } catch (Exception e) {
      LOG.error("Error while unmarshalling SOAP Fault", e);
    }
  }

  private String getMessagePartner(SOAPBody soapBody) {
    String messagePartner = null;
    LOG.info("getMessagePartner - First Child NodeName: {}", soapBody.getFirstChild().getNodeName());
    NodeList nodeList = soapBody.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      NodeList childNodeList = nodeList.item(i).getChildNodes();
      for (int j = 0; j < childNodeList.getLength(); j++) {
        LOG.info("getMessagePartner - ChildNode name: {}, value: {}", childNodeList.item(j).getNodeName(),
            childNodeList.item(j).getTextContent());
        if ("MessagePartnerName".equalsIgnoreCase(childNodeList.item(j).getNodeName())) {
          messagePartner = childNodeList.item(j).getTextContent();
          break;
        }
      }
    }
    LOG.info("getMessagePartner ::: {}", messagePartner);
    return messagePartner;
  }
}
