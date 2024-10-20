package com.chat.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;

public record ConfirmationRegistrationRequest (
    String email,
    @NotBlank(message = "errors.confirmationCode.required")
    String confirmationCode,
    @JsonIgnore
    boolean social
) {
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("email", email)
                .append("confirmationCode", "********")
                .append("social", social)
                .toString();
    }
}
