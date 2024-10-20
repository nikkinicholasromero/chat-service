package com.chat.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;

public record UpdatePasswordRequest(
        String currentPassword,
        @NotBlank(message = "errors.newPassword.required")
        @Size(message = "errors.password.length", min = 8)
        String newPassword
) {
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("currentPassword", "********")
                .append("newPassword", "********")
                .toString();
    }
}
