package com.chat.token.dto;

public record GoogleTokenRequest(
        String state,
        String code,
        String scope
) {
}
