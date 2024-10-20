package com.chat.common.encryption;

import com.chat.common.exception.AuthorizationException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private JwtService target;

    @BeforeEach
    public void setup() {
        target = new JwtService(
                "Some JWT Secret Key",
                "chat.com",
                86400L);
    }

    @Test
    void createJWT_and_decodeJWT() {
        String email = "nikki@gmail.com";
        String sessionId = UUID.randomUUID().toString();

        String jwt = target.createJWT(email, sessionId);

        assertThat(jwt).isNotBlank();

        Claims claims = target.decodeJWT(jwt);

        assertThat(claims).isNotNull();
        assertThat(claims.get(Claims.ID, String.class)).isNotBlank();
        assertThat(claims.get(Claims.ISSUER, String.class)).isEqualTo("chat.com");
        assertThat(claims.get(Claims.ISSUED_AT, Date.class)).isNotNull();
        assertThat(claims.get(Claims.SUBJECT, String.class)).isEqualTo(email);
        assertThat(claims.get(JwtService.CLAIMS_SESSION_ID, String.class)).isEqualTo(sessionId);

        assertThrows(AuthorizationException.class, () -> target.decodeJWT(""));
    }
}
