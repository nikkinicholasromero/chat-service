package com.chat.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;

public record PasswordResetRequest(
        String email,
        @NotBlank(message = "errors.passwordResetCode.required")
        String passwordResetCode,
        @NotBlank(message = "errors.password.required")
        @Size(message = "errors.password.length", min = 8)
        String newPassword
) {
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("email", email)
                .append("passwordResetCode", "********")
                .append("newPassword", "********")
                .toString();
    }
}
