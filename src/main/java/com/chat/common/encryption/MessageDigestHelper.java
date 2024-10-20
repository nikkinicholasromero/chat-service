package com.chat.common.encryption;

import com.chat.common.config.Generated;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Generated
@Component
public class MessageDigestHelper {
    public MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException();
        }
    }
}
