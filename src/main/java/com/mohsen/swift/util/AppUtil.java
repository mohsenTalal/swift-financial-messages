package com.mohsen.swift.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.xml.security.c14n.Canonicalizer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class AppUtil {

  public static Date getDate(String format, String dateString) {
    Date date = null;
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try {
      date = sdf.parse(dateString);
    } catch (Exception ignored) {

    }
    return date;
  }

  public static String getDateString(String format, Date date) {
    if (date == null) {
      return AppConstants.EMPTY;
    }
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(date);
  }

  public static byte[] getSHA256Bytes(String data) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance(AppConstants.ALGO_SHA256);
    return digest.digest(data.getBytes(StandardCharsets.UTF_8));
  }

  public static String getSHA256Data(String data) throws NoSuchAlgorithmException {
    return bytesToHex(getSHA256Bytes(data));
  }

  public static String getHashedContent(byte[] data) throws Exception {
    return encodeWithHMAC(canonicalizeXML(new String(data)));
  }

  public static String encodeWithBase64(String data) {
    if (data == null || data.trim().length() == 0) {
      return "";
    } else {
      return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }
  }

  public static SecretKeySpec getSecretKeySpec() {
    String key = AppConstants.KEY_LEFT + AppConstants.KEY_RIGHT;
    return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AppConstants.ALGO_HMAC_SHA256);
  }

  public static SecretKeySpec getSecretKeySpec(String keyLeft, String keyRight) {
    String key = keyLeft + keyRight;
    return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AppConstants.ALGO_HMAC_SHA256);
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static String encodeWithHMAC(String data) throws Exception {
    Mac hmacSha256 = Mac.getInstance(AppConstants.ALGO_HMAC_SHA256);
    hmacSha256.init(getSecretKeySpec());
    return Hex.encodeHexString(hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8)));
  }

  public static String canonicalizeXML(String xmlData) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      org.apache.xml.security.Init.init();
      Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
      canon.canonicalize(xmlData.getBytes(StandardCharsets.UTF_8), byteStream, true);
    } catch (Exception ignored) {

    }
    return byteStream.toString();
  }

}
