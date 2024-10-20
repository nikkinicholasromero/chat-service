package com.chat.token.dto;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;

public record TokenRequest(
        @NotBlank(message = "errors.email.required")
        String email,
        @NotBlank(message = "errors.password.required")
        String password
) {
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("email", email)
                .append("password", "********")
                .toString();
    }
}
