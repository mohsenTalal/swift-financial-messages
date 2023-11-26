package com.mohsen.swift.service;

import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.util.AppConstants;
import com.mohsen.swift.util.AppUtil;
import com.mohsen.swift.util.MessageType;
import com.mohsen.swift.util.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import swift.saa.wsdl.soapha.SAAFault;
import swift.saa.wsdl.soapha.SessionFault;
import swift.saa.wsdl.soapha.Soapha;
import swift.saa.wsdl.soapha.Soapha_Service;
import swift.saa.xsd.saa_2.*;
import swift.saa.xsd.soapha.ObjectFactory;
import swift.saa.xsd.soapha.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.util.List;

@Service
public class SwiftService {

  private static final Logger LOG = LoggerFactory.getLogger(SwiftService.class);

  @Autowired
  private HeaderHandler handler;

  @Autowired
  private SpvHeaderHandler spvHandler;

  @Value("${swift.url}")
  private String swiftUrl;

  private Soapha getSwiftService() throws MalformedURLException {
    LOG.info("getSwiftService - BEGIN");
    LOG.info("getSwiftService - SWIFT URL: {}", swiftUrl);
    Soapha_Service serviceImpl = new Soapha_Service(SwiftService.class.getResource("/wsdl/soapha.wsdl"));
    Soapha service = serviceImpl.getSoaphaSOAP();
    BindingProvider binding = (BindingProvider) service;
    binding.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, swiftUrl);
    List<Handler> handlerChain = binding.getBinding().getHandlerChain();
    handlerChain.add(handler);
    binding.getBinding().setHandlerChain(handlerChain);
    LOG.info("getSwiftService - END");
    return service;
  }

  private Soapha getSpvSwiftService() throws MalformedURLException {
    LOG.info("getSwiftService - BEGIN");
    LOG.info("getSwiftService - SWIFT URL: {}", swiftUrl);
    Soapha_Service serviceImpl = new Soapha_Service(SwiftService.class.getResource("/wsdl/soapha.wsdl"));
    Soapha service = serviceImpl.getSoaphaSOAP();
    BindingProvider binding = (BindingProvider) service;
    binding.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, swiftUrl);
    List<Handler> handlerChain = binding.getBinding().getHandlerChain();
    handlerChain.add(spvHandler);
    binding.getBinding().setHandlerChain(handlerChain);
    LOG.info("getSwiftService - END");
    return service;
  }

  public boolean open(MessageType messageType, String messagePartner) {
    LOG.info("open - BEGIN");
    boolean isSuccess = false;
    try {
      Soapha service;
      if (MessageType.SPV.equals(messageType)) {
        service = getSpvSwiftService();
      } else {
        service = getSwiftService();
      }
      LOG.info("open - SOAP Service initialized");
      OpenResponseDetails response = service.open(messagePartner, 1L, 10L, Direction.TO_AND_FROM_MESSAGE_PARTNER, RoutingMode.IMMEDIATE, null);
      LOG.info("open - Service invocation completed");
      LOG.info("open - Response Sequence Number: {}, Response Window Size: {}",
          response.getSequenceNumberFromSAA(), response.getWindowSize());
      //AppConstants.SAA_HEADER.setSequenceNumber(response.getSequenceNumberFromSAA());
      AppConstants.SAA_HEADER.setSequenceNumber(1L);
      isSuccess = true;
    } catch (SAAFault | SessionFault | MalformedURLException e) {
      LOG.error("Error while invoking the SWIFT OPEN WS", e);
    }
    LOG.info("open - isSuccess: {}", isSuccess);
    LOG.info("open - END");
    return isSuccess;
  }

  public boolean close(MessageType messageType) {
    LOG.info("close - BEGIN");
    boolean isSuccess = false;
    try {
      Soapha service;
      if (MessageType.SPV.equals(messageType)) {
        service = getSpvSwiftService();
      } else {
        service = getSwiftService();
      }
      LOG.info("close - SOAP Service initialized");
      ObjectFactory factory = new ObjectFactory();
      Close close = factory.createClose();
      close.setRoutingAction(RoutingAction.COMMIT);
      CloseResponse response = service.close(close);

      if (response != null) {
        JAXBContext context = JAXBContext.newInstance(DataPDU.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        DataPDU data = unmarshaller.unmarshal(response.getAny(), DataPDU.class).getValue();
        isSuccess = data.getHeader().getSessionStatus().isIsSuccess();
      }
      LOG.info("close - Service invocation completed");
    } catch (SAAFault | SessionFault | MalformedURLException | JAXBException e) {
      LOG.error("Error while invoking the SWIFT CLOSE WS", e);
    }
    LOG.info("close - isSuccess: {}", isSuccess);
    LOG.info("close - END");
    return isSuccess;
  }

  public boolean put(MessageType messageType, SwiftMessageVO swiftMessageVO) {
    LOG.info("put - BEGIN");
    boolean isSuccess = false;
    try {
      swift.saa.xsd.saa_2.ObjectFactory xsdFactory = new swift.saa.xsd.saa_2.ObjectFactory();
      Soapha service;
      if (MessageType.SPV.equals(messageType)) {
        service = getSpvSwiftService();
      } else {
        service = getSwiftService();
      }
      LOG.info("put - SOAP Service initialized");

      String messageBody = swiftMessageVO.getMessageBody();
      int length = messageBody.length();
      String bodyContent = messageBody.substring(0, length - 2);
      LOG.info("BODY CONTENT: {}", bodyContent);

      AddressFullName senderAddress = xsdFactory.createAddressFullName();
      senderAddress.setX1(swiftMessageVO.getSenderX1());

      AddressFullName receiverAddress = xsdFactory.createAddressFullName();
      receiverAddress.setX1(swiftMessageVO.getReceiverX1());

      AddressInfo sender = xsdFactory.createAddressInfo();
      sender.setBIC12(swiftMessageVO.getSenderBIC12());
      sender.setFullName(senderAddress);

      AddressInfo receiver = xsdFactory.createAddressInfo();
      receiver.setBIC12(swiftMessageVO.getReceiverBIC12());
      receiver.setFullName(receiverAddress);

      InterfaceInfo interfaceInfo = xsdFactory.createInterfaceInfo();
      interfaceInfo.setUserReference(swiftMessageVO.getReference());

      Message message = xsdFactory.createMessage();
      message.setSenderReference(swiftMessageVO.getReference());
      message.setMessageIdentifier("fin.101");
      message.setFormat("MT");
      message.setSender(sender);
      message.setReceiver(receiver);
      message.setInterfaceInfo(interfaceInfo);

      Header header = xsdFactory.createHeader();
      header.setMessage(message);

      SwAny body = xsdFactory.createSwAny();
      body.getContent().add(AppUtil.encodeWithBase64(bodyContent));

      DataPDU dataPDU = xsdFactory.createDataPDU();
      dataPDU.setBody(body);
      dataPDU.setHeader(header);
      dataPDU.setRevision("2.0.2");

      JAXBContext context = JAXBContext.newInstance(DataPDU.class);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document document = dbf.newDocumentBuilder().newDocument();
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(xsdFactory.createDataPDU(dataPDU), document);

      ObjectFactory factory = new ObjectFactory();
      Put put = factory.createPut();
      put.setAny(document.getDocumentElement());
      PutResponse response = service.put(put);

      // after getting response from SAA
      Unmarshaller unmarshaller = context.createUnmarshaller(); // extract maseage
      DataPDU data = unmarshaller.unmarshal(response.getAny(), DataPDU.class).getValue(); // data format
      isSuccess = data.getHeader().getMessageStatus().isIsSuccess(); // in the header we will ind the stats
      swiftMessageVO.setSuccess(isSuccess);
      swiftMessageVO.setStatusChange(true);
      if (isSuccess) {
        swiftMessageVO.setStatus(StatusType.DELIVERED.getValue());
      } else {
        swiftMessageVO.setStatus(StatusType.SWIFT_ERROR.getValue());
      }

      long seqNum = AppConstants.SAA_HEADER.getSequenceNumber();
      AppConstants.SAA_HEADER.setSequenceNumber(++seqNum);
      LOG.info("put - Service invocation completed");
    } catch (SAAFault | SessionFault | MalformedURLException e) {
      LOG.error("Error while invoking the SWIFT PUT WS", e);
      swiftMessageVO.setStatusChange(true);
      swiftMessageVO.setStatus(StatusType.NETWORK_ERROR.getValue());
    } catch (JAXBException | ParserConfigurationException e) {
      LOG.error("Error during JAXB processing for SWIFT PUT WS", e);
      swiftMessageVO.setStatusChange(true);
      swiftMessageVO.setStatus(StatusType.NETWORK_ERROR.getValue());
    } catch (Exception e) {
      LOG.error("Unknown error during processing for SWIFT PUT WS", e);
      swiftMessageVO.setStatusChange(true);
      swiftMessageVO.setStatus(StatusType.NETWORK_ERROR.getValue());
    }
    LOG.info("put - isSuccess: {}", isSuccess);
    LOG.info("put - END");
    return isSuccess;
  }

  public String getAck(MessageType messageType, String clientRef) {
    String ackResp = null;
    LOG.info("getAck - BEGIN");
    try {
      Soapha service;
      if (MessageType.SPV.equals(messageType)) {
        service = getSpvSwiftService();
      } else {
        service = getSwiftService();
      }
      LOG.info("getAck - SOAP Service initialized");
      AppConstants.SAA_HEADER.setClientRef(clientRef);
      ObjectFactory factory = new ObjectFactory();
      GetAck getAck = factory.createGetAck();
      getAck.setTimeout(100L);
      GetAckResponse response = service.getAck(getAck);
      if (response != null) {
        JAXBContext context = JAXBContext.newInstance(DataPDU.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        DataPDU data = unmarshaller.unmarshal(response.getAny(), DataPDU.class).getValue();
        ackResp = data.getHeader().getTransmissionReport().getNetworkDeliveryStatus().value();
      }
      LOG.info("getAck - Service invocation completed");
      AppConstants.SAA_HEADER.setClientRef(null);
    } catch (SAAFault | SessionFault | MalformedURLException e) {
      LOG.error("Error while invoking the SWIFT GETACK WS", e);
    } catch (JAXBException e) {
      LOG.error("Error during JAXB processing for SWIFT GETACK WS", e);
    } catch (Exception e) {
      LOG.error("Unknown error during processing for SWIFT GETACK WS", e);
    }
    LOG.info("getAck - END");
    return ackResp;
  }
}
