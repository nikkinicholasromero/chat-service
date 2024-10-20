package com.chat.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {
}
