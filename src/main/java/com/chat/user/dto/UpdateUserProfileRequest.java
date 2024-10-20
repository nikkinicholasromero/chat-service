package com.chat.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "errors.firstName.required")
        @Size(message = "errors.firstName.length", max = 100)
        String firstName,
        @NotBlank(message = "errors.lastName.required")
        @Size(message = "errors.lastName.length", max = 100)
        String lastName
) {
}
