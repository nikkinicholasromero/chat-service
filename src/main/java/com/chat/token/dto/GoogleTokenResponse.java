package com.chat.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("id_token")
        String idToken
) {
}
