package com.chat.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookProfileResponse(
        String email,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName
) {
}
