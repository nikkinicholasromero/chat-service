package com.chat.common.encryption;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Service
public class HashService {
    private final MessageDigestHelper messageDigestHelper;

    public HashService(MessageDigestHelper messageDigestHelper) {
        this.messageDigestHelper = messageDigestHelper;
    }

    public String hash(String clear, String salt) {
        String text = salt + clear + salt;
        MessageDigest messageDigest = messageDigestHelper.messageDigest();
        messageDigest.update(text.getBytes());
        byte[] byteData = messageDigest.digest();

        StringBuilder stringBuilder = new StringBuilder();
        for (byte byteDatum : byteData) {
            stringBuilder.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuilder.toString();
    }

    public String generateRandomSalt() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
