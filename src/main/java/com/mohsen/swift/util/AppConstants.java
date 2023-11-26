package com.mohsen.swift.util;

import swift.saa.xsd.soapha.FaultDetails;
import swift.saa.xsd.soapha.ObjectFactory;
import swift.saa.xsd.soapha.Open;
import swift.saa.xsd.soapha.SAAHeader;

public class AppConstants {

  public static final String CRLF = "\r\n";
  public static final String HYPHEN = "-";
  public static final String COMMA = ",";

  public static final String EMPTY = "";
  public static final String ITEM_ID = "<ITEM_ID>";
  public static final String SOURCE = "<SOURCE>";
  public static final String SUCCESS = "SUCCESS";
  public static final String ERROR = "ERROR";

  public static final String REF_INV = "/INV/";
  public static final String REF_IPI = "/IPI/";
  public static final String REF_RFB = "/RFB/";
  public static final String REF_ROC = "/ROC/";
  public static final String REF_SPV = "SPV";

  public static final String TAG_20 = ":20:";
  public static final String TAG_28D = ":28D:1/1";
  public static final String TAG_50H = ":50H:/";
  public static final String TAG_30 = ":30:";
  public static final String TAG_21 = ":21:";
  public static final String TAG_32B = ":32B:";
  public static final String TAG_52A = ":52A:";
  public static final String TAG_56A = ":56A:";
  public static final String TAG_57A = ":57A:";
  public static final String TAG_59 = ":59:/";
  public static final String TAG_70 = ":70:";
  public static final String TAG_77B = ":77B:/";
  public static final String TAG_71A = ":71A:";

  public static final String FORMAT_YYYYMMDD = "yyyy-MM-dd";
  public static final String FORMAT_YYMMDD = "yyMMdd";
  public static final String FORMAT_YYMMDDHHMMSS = "yyMMddHHmmss";
  public static final String FORMAT_YYMMDDHHMM = "yyMMddHHmm";
  public static final String FORMAT_DDMMYYYY = "dd/MM/yyyy";
  public static final String FORMAT_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
  public static final String FORMAT_YYYYMMDD_T_HHMMSS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static final String KEY_LEFT = "5sKelyfjcc6NZ692";
  public static final String KEY_RIGHT = "5sKelyfjcc6NZ691";

  public static final String ALGO_SHA256 = "SHA-256";
  public static final String ALGO_HMAC_SHA256 = "HmacSHA256";

  public static final String PREFIX_URN = "urn";
  public static final String PREFIX_SOAP_ENV = "soapenv";
  public static final String PREFIX_DS = "ds";
  public static final String PREFIX_WSU = "wsu";
  public static final String PREFIX_SAA = "saa";

  public static final String URI_SOAP_HA = "urn:swift:saa:xsd:soapha";
  public static final String URI_SOAP_SAA = "urn:swift:saa:xsd:saa.2.0";
  public static final String URI_SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
  public static final String URI_CANONICAL_EXCLUSIVE = "http://www.w3.org/2001/10/xml-exc-c14n#";
  public static final String URI_SIGNATURE_METHOD = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";
  public static final String URI_SECURITY_UTIL = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurityutility-1.0.xsd";

  public static final SAAHeader SAA_HEADER = (new ObjectFactory()).createSAAHeader();

  public static final FaultDetails SAA_FAULT = (new ObjectFactory()).createFaultDetails();

  public static final Open OPEN_OBJ = (new ObjectFactory()).createOpen();
}
