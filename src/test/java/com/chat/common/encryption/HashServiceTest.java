package com.chat.common.encryption;

import com.chat.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class HashServiceTest extends BaseUnitTest {
    @InjectMocks
    private HashService target;

    @Mock
    private MessageDigestHelper messageDigestHelper;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException {
        when(messageDigestHelper.messageDigest()).thenReturn(MessageDigest.getInstance("SHA-256"));
    }

    @Test
    void generateRandomSalt_hash() {
        String salt = target.generateRandomSalt();
        String clear = UUID.randomUUID().toString();
        String hashed1 = target.hash(clear, salt);
        String hashed2 = target.hash(clear, salt);
        assertThat(hashed1).isEqualTo(hashed2);
    }
}
