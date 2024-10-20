package com.chat.common.security;

public record UserPrincipal(
        String email,
        String token
) {
}
