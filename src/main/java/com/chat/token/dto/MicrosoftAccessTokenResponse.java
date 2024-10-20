package com.chat.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MicrosoftAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {
}
