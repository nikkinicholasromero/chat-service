package com.chat.token.dto;

public record MicrosoftProfileResponse(
        String mail,
        String givenName,
        String surname
) {
}
