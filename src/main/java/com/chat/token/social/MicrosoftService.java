package com.chat.token.social;

import com.chat.common.config.WebClientLogger;
import com.chat.common.exception.AuthorizationException;
import com.chat.token.dto.MicrosoftAccessTokenResponse;
import com.chat.token.dto.MicrosoftProfileResponse;
import com.chat.token.dto.MicrosoftTokenRequest;
import com.chat.token.dto.SocialProfile;
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
public class MicrosoftService {
    private final String tokenHost;
    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String grantType;
    private final String profileHost;
    private final String profileEndpoint;
    private final WebClientLogger webClientLogger;

    public MicrosoftService(
            @Value("${microsoft.service.token.host}") String tokenHost,
            @Value("${microsoft.service.token.endpoint}") String tokenEndpoint,
            @Value("${microsoft.service.client.id}") String clientId,
            @Value("${microsoft.service.client.secret}") String clientSecret,
            @Value("${microsoft.service.redirect.uri}") String redirectUri,
            @Value("${microsoft.service.grant.type}") String grantType,
            @Value("${microsoft.service.profile.host}") String profileHost,
            @Value("${microsoft.service.profile.endpoint}") String profileEndpoint,
            WebClientLogger webClientLogger) {
        this.tokenHost = tokenHost;
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.grantType = grantType;
        this.profileHost = profileHost;
        this.profileEndpoint = profileEndpoint;
        this.webClientLogger = webClientLogger;
    }

    public SocialProfile getMicrosoftProfile(MicrosoftTokenRequest request) {
        MicrosoftAccessTokenResponse token = WebClient.builder()
                .baseUrl(tokenHost)
                .filter(webClientLogger.logRequest())
                .filter(webClientLogger.logResponse())
                .build()
                .post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body(request)))
                .exchangeToMono(clientResponse -> clientResponse.statusCode().isError() ?
                        Mono.error(new AuthorizationException("Something went wrong while trying to get microsoft token")) :
                        clientResponse.bodyToMono(MicrosoftAccessTokenResponse.class))
                .block();

        if (Objects.isNull(token)) {
            throw new AuthorizationException("Something went wrong while trying to get microsoft token");
        }

        MicrosoftProfileResponse profile = WebClient.builder()
                .baseUrl(profileHost)
                .filter(webClientLogger.logRequest())
                .filter(webClientLogger.logResponse())
                .build()
                .get()
                .uri(profileEndpoint)
                .headers(h -> h.setBearerAuth(token.accessToken()))
                .exchangeToMono(clientResponse -> clientResponse.statusCode().isError() ?
                        Mono.error(new AuthorizationException("Something went wrong while trying to get microsoft profile")) :
                        clientResponse.bodyToMono(MicrosoftProfileResponse.class))
                .block();

        if (Objects.isNull(profile)) {
            throw new AuthorizationException("Something went wrong while trying to get microsoft profile");
        }

        return new SocialProfile(
                profile.mail(),
                profile.givenName(),
                profile.surname());
    }

    private MultiValueMap<String, String> body(MicrosoftTokenRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", grantType);
        body.add("code", request.code());
        return body;
    }
}
