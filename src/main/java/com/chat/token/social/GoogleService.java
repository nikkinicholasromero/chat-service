package com.chat.token.social;

import com.chat.common.config.WebClientLogger;
import com.chat.common.exception.AuthorizationException;
import com.chat.token.dto.GoogleTokenRequest;
import com.chat.token.dto.GoogleTokenResponse;
import com.chat.token.dto.SocialProfile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class GoogleService {
    private final String host;
    private final String endpoint;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String grantType;
    private final WebClientLogger webClientLogger;

    public GoogleService(
            @Value("${google.service.host}") String host,
            @Value("${google.service.endpoint}") String endpoint,
            @Value("${google.service.client.id}") String clientId,
            @Value("${google.service.client.secret}") String clientSecret,
            @Value("${google.service.redirect.uri}") String redirectUri,
            @Value("${google.service.grant.type}") String grantType,
            WebClientLogger webClientLogger) {
        this.host = host;
        this.endpoint = endpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.grantType = grantType;
        this.webClientLogger = webClientLogger;
    }

    public SocialProfile getGoogleProfile(GoogleTokenRequest request) {
        GoogleTokenResponse token = WebClient.builder()
                .baseUrl(host)
                .filter(webClientLogger.logRequest())
                .filter(webClientLogger.logResponse())
                .build()
                .post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body(request)))
                .exchangeToMono(clientResponse -> clientResponse.statusCode().isError() ?
                        Mono.error(new AuthorizationException("Something went wrong while trying to get google token")) :
                        clientResponse.bodyToMono(GoogleTokenResponse.class))
                .block();

        if (Objects.isNull(token)) {
            throw new AuthorizationException("Something went wrong while trying to get google token");
        }

        Claims claims = getUntrustedClaims(token.idToken());

        return new SocialProfile(
                claims.get("email", String.class),
                claims.get("given_name", String.class),
                claims.get("family_name", String.class));
    }

    private MultiValueMap<String, String> body(GoogleTokenRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", request.code());
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", grantType);
        return body;
    }

    private Claims getUntrustedClaims(String token) {
        String withoutSignature = token.substring(0, token.lastIndexOf('.') + 1);
        return Jwts.parser()
                .setAllowedClockSkewSeconds(Integer.MAX_VALUE)
                .parseClaimsJwt(withoutSignature)
                .getBody();
    }
}
