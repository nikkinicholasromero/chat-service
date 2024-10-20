package com.chat.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;

public record RegistrationRequest(
        @NotBlank(message = "errors.email.required")
        @Size(message = "errors.email.length", max = 320)
        @Email(message = "errors.email.format")
        String email,
        @NotBlank(message = "errors.password.required")
        @Size(message = "errors.password.length", min = 8)
        String password,
        @NotBlank(message = "errors.firstName.required")
        @Size(message = "errors.firstName.length", max = 100)
        String firstName,
        @NotBlank(message = "errors.lastName.required")
        @Size(message = "errors.lastName.length", max = 100)
        String lastName,
        @JsonIgnore
        boolean social
) {
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("email", email)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("password", "********")
                .append("social", social)
                .toString();
    }
}
