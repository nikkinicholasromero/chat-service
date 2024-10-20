package com.chat.common.encryption;

import com.chat.common.exception.AuthorizationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    public static final String CLAIMS_SESSION_ID = "sessionId";

    private final String secretKey;
    private final String issuer;
    private final long expire;

    public JwtService(
            @Value("${jwt.secret.key}") String secretKey,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expire}") long expire) {
        this.secretKey = secretKey;
        this.issuer = issuer;
        this.expire = expire;
    }

    public String createJWT(String subject, String sessionId) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        SecretKeySpec signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.ID, UUID.randomUUID().toString());
        claims.put(Claims.ISSUER, issuer);
        claims.put(Claims.ISSUED_AT, now);
        claims.put(Claims.SUBJECT, StringUtils.trimToEmpty(subject).toLowerCase());
        claims.put(CLAIMS_SESSION_ID, sessionId);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm, signingKey);

        long ttlMillis = expire * 1000;
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
        return builder.compact();
    }

    public Claims decodeJWT(String jwt) {
        try {
            return Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            throw new AuthorizationException("errors.session.expired");
        }
    }
}
