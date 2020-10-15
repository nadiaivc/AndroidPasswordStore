package com.example.android.lab2;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptOrDecrypt {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static byte[] encrypt(String str, String password) throws Exception {
        byte[] iv_byte ={-10,127,13,4,-8,-34,67,99,105,-97,33,56,-23,87,-67,7};
        byte[] source = str.getBytes();
        // Превращаем пароль (произвольной длины) в ключ (256 бит)
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8); // Превращаем пароль из строки в байты UTF-8
        byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(passwordBytes); // Хэшируем байты пароля, на выходе имеем всегда 32 байта
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES"); // Так как это джава, тут любая вещь должна быть превращена в объект :/

        Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv_byte)); // Инициализируем шифратор с заданным ключом и nonce
        byte[] encryptedBytes = cipher.doFinal(source); // В общем-то, шифруем!

        return encryptedBytes;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String decrypt(byte[] encrypted, String password) throws Exception {
        //byte[] encrypted = strEncrypted.getBytes();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(passwordBytes);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        byte[] iv_byte ={-10,127,13,4,-8,-34,67,99,105,-97,33,56,-23,87,-67,7};
        //String iv = iv_byte.toString();
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv_byte));
        byte[] decryptedBytes = cipher.doFinal(encrypted);
        return new String(decryptedBytes);
    }

}
