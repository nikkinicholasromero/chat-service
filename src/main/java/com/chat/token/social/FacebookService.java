package com.chat.token.social;

import com.chat.common.config.WebClientLogger;
import com.chat.common.exception.AuthorizationException;
import com.chat.token.dto.FacebookAccessTokenResponse;
import com.chat.token.dto.FacebookProfileResponse;
import com.chat.token.dto.FacebookTokenRequest;
import com.chat.token.dto.SocialProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class FacebookService {
    private final String host;
    private final String tokenEndpoint;
    private final String profileEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String fields;
    private final WebClientLogger webClientLogger;

    public FacebookService(
            @Value("${facebook.service.host}") String host,
            @Value("${facebook.service.token.endpoint}") String tokenEndpoint,
            @Value("${facebook.service.profile.endpoint}") String profileEndpoint,
            @Value("${facebook.service.client.id}") String clientId,
            @Value("${facebook.service.client.secret}") String clientSecret,
            @Value("${facebook.service.redirect.uri}") String redirectUri,
            @Value("${facebook.service.fields}") String fields,
            WebClientLogger webClientLogger) {
        this.host = host;
        this.tokenEndpoint = tokenEndpoint;
        this.profileEndpoint = profileEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.fields = fields;
        this.webClientLogger = webClientLogger;
    }

    public SocialProfile getFacebookProfile(FacebookTokenRequest request) {
        WebClient webClient = WebClient.builder()
                .baseUrl(host)
                .filter(webClientLogger.logRequest())
                .filter(webClientLogger.logResponse())
                .build();

        FacebookAccessTokenResponse token = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(tokenEndpoint)
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", request.code())
                        .build())
                .exchangeToMono(clientResponse -> clientResponse.statusCode().isError() ?
                        Mono.error(new AuthorizationException("Something went wrong while trying to get facebook token")) :
                        clientResponse.bodyToMono(FacebookAccessTokenResponse.class))
                .block();

        if (Objects.isNull(token)) {
            throw new AuthorizationException("Something went wrong while trying to get facebook token");
        }

        FacebookProfileResponse profile = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(profileEndpoint)
                        .queryParam("fields", fields)
                        .queryParam("access_token", token.accessToken())
                        .build())
                .exchangeToMono(clientResponse -> clientResponse.statusCode().isError() ?
                        Mono.error(new AuthorizationException("Something went wrong while trying to get facebook profile")) :
                        clientResponse.bodyToMono(FacebookProfileResponse.class))
                .block();

        if (Objects.isNull(profile)) {
            throw new AuthorizationException("Something went wrong while trying to get facebook profile");
        }

        return new SocialProfile(
                profile.email(),
                profile.firstName(),
                profile.lastName());
    }
}
