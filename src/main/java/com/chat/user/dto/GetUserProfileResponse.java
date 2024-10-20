package com.chat.user.dto;

public record GetUserProfileResponse(
        String firstName,
        String lastName,
        boolean noPassword
) {
}
