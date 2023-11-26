package com.mohsen.swift.util;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class CryptoUtils {

  public static byte[] getRandomNonce(int numBytes) {
    byte[] nonce = new byte[numBytes];
    new SecureRandom().nextBytes(nonce);
    return nonce;
  }

  // Password derived AES 256 bits secret key
  public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {

    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // iterationCount = 65536
    // keyLength = 256
    KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
  }
}
