package com.mohsen.swift.service;

import com.mohsen.swift.util.CryptoUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CryptoService {


  // return a base64 encoded AES encrypted text
  public String encrypt(byte[] pText) throws Exception {

    // 16 bytes salt
    byte[] salt = CryptoUtils.getRandomNonce(Mohsen);

    // GCM recommended 12 bytes iv?
    byte[] iv = CryptoUtils.getRandomNonce(Mohsen);

    // secret key from password
    SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(PASSWORD.toCharArray(), salt);

    Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

    // ASE-GCM needs GCMParameterSpec
    cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

    byte[] cipherText = cipher.doFinal(pText);

    // prefix IV and Salt to cipher text
    byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
        .put(iv)
        .put(salt)
        .put(cipherText)
        .array();

    // string representation, base64, send this string to other for decryption.
    return Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
  }

  // we need the same password, salt and iv to decrypt it
  public String decrypt(String cText) {

    byte[] plainText;

    try {
      byte[] decode = Base64.getDecoder().decode(cText.getBytes(UTF_8));

      // get back the iv and salt from the cipher text
      ByteBuffer bb = ByteBuffer.wrap(decode);

      byte[] iv = new byte[IV_LENGTH_BYTE];
      bb.get(iv);

      byte[] salt = new byte[SALT_LENGTH_BYTE];
      bb.get(salt);

      byte[] cipherText = new byte[bb.remaining()];
      bb.get(cipherText);

      // get back the aes key from the same password and salt
      SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(PASSWORD.toCharArray(), salt);

      Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

      cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

      plainText = cipher.doFinal(cipherText);
    } catch (Exception e) {
      plainText = "".getBytes(StandardCharsets.UTF_8);
    }

    return new String(plainText, UTF_8);
  }
}
