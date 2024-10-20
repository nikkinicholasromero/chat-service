package com.chat.user;

import com.chat.BaseControllerUnitTest;
import com.chat.user.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest extends BaseControllerUnitTest {
    @MockBean
    private UserProfileService userProfileService;

    @Test
    void getEmailStatus() throws Exception {
        when(userProfileService.getEmailStatus(any())).thenReturn(EmailStatus.CONFIRMED);

        MvcResult result = mockMvc.perform(get("/user?email=nikki@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "status": "CONFIRMED"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verify(userProfileService).getEmailStatus("nikki@gmail.com");
    }

    @Test
    void registerUser_nullEmail() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                null,
                "some password",
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Email address is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.email.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_blankEmail() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                " ",
                "some password",
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Invalid email address. Email address is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.email.format",
                        "errors.email.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_longEmail() throws Exception {
        String longEmail = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@really_long_email_address.com";

        RegistrationRequest request = new RegistrationRequest(
                longEmail,
                "some password",
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Invalid email address. Email length should not exceed 320 characters",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.email.format",
                        "errors.email.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_invalidEmail() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "invalid_email",
                "some password",
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Invalid email address",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.email.format"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_nullPassword() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                null,
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.password.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_blankPassword() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                " ",
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password must be at least 8 characters long. Password is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.password.length",
                        "errors.password.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_shortPassword() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "1234567",
                "Nikki Nicholas",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password must be at least 8 characters long",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.password.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_nullFirstName() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                null,
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "First name is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.firstName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_blankFirstName() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                " ",
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "First name is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.firstName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_longFirstName() throws Exception {
        String longFirstName = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901";

        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                longFirstName,
                "Romero",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "First name length should not exceed 100 characters",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.firstName.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_nullLastName() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                "Nikki Nicholas",
                null,
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Last name is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.lastName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_blankLastName() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                "Nikki Nicholas",
                " ",
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Last name is required",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.lastName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser_longLastName() throws Exception {
        String longLastName = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901";

        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                "Nikki Nicholas",
                longLastName,
                false);

        MvcResult result = mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Last name length should not exceed 100 characters",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.lastName.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void registerUser() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                "Nikki Nicholas",
                "Romero",
                false);

        mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProfileService).registerUser(request);
    }

    @Test
    void sendConfirmationCode() throws Exception {
        mockMvc.perform(post("/user/registration/confirmation/nikki@gmail.com"))
                .andExpect(status().isOk());

        verify(userProfileService).sendConfirmationCode("nikki@gmail.com");
    }

    @Test
    void confirmRegistration_nullConfirmationCode() throws Exception {
        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                "nikki@gmail.com",
                null,
                false);

        MvcResult result = mockMvc.perform(post("/user/registration/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Confirmation code is required",
                    "instance": "/user/registration/confirmation",
                    "error_codes": [
                        "errors.confirmationCode.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void confirmRegistration() throws Exception {
        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                "nikki@gmail.com",
                "some confirmation code",
                false);

        mockMvc.perform(post("/user/registration/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProfileService).confirmRegistration(request);
    }

    @Test
    void sendPasswordResetCode() throws Exception {
        mockMvc.perform(post("/user/password/reset/nikki@gmail.com"))
                .andExpect(status().isOk());

        verify(userProfileService).sendPasswordResetCode("nikki@gmail.com");
    }

    @Test
    void resetPassword_nullPasswordResetCode() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                null,
                "some new password");

        MvcResult result = mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password reset code is required",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.passwordResetCode.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void resetPassword_blankPasswordResetCode() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                " ",
                "some new password");

        MvcResult result = mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password reset code is required",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.passwordResetCode.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void resetPassword_nullPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                "some password reset code",
                null);

        MvcResult result = mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password is required",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.password.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void resetPassword_blankPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                "some password reset code",
                " ");

        MvcResult result = mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password must be at least 8 characters long. Password is required",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.password.length",
                        "errors.password.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void resetPassword_shortPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                "some password reset code",
                "1234567");

        MvcResult result = mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password must be at least 8 characters long",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.password.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    void resetPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                "some password reset code",
                "some new password");

        mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProfileService).resetPassword(request);
    }

    @Test
    @WithAnonymousUser
    void getUserProfile_anonymous() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getUserProfile_authenticated() throws Exception {
        when(userProfileService.getUserProfile()).thenReturn(
                new GetUserProfileResponse("Nikki Nicholas", "Romero", true));

        MvcResult result = mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "firstName": "Nikki Nicholas",
                    "lastName": "Romero",
                    "noPassword": true
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verify(userProfileService).getUserProfile();
    }

    @Test
    @WithAnonymousUser
    void updateUserProfile_anonymous() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nikki Nicholas",
                "Romero");

        mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile_nullFirstName() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                null,
                "Romero");

        MvcResult result = mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "First name is required",
                    "instance": "/user/profile",
                    "error_codes": [
                        "errors.firstName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile_blankFirstName() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                " ",
                "Romero");

        MvcResult result = mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "First name is required",
                    "instance": "/user/profile",
                    "error_codes": [
                        "errors.firstName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile_longFirstName() throws Exception {
        String longFirstName = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901";

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                longFirstName,
                "Romero");

        MvcResult result = mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "First name length should not exceed 100 characters",
                    "instance": "/user/profile",
                    "error_codes": [
                        "errors.firstName.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile_nullLastName() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nikki Nicholas",
                null);

        MvcResult result = mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Last name is required",
                    "instance": "/user/profile",
                    "error_codes": [
                        "errors.lastName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile_blankLastName() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nikki Nicholas",
                " ");

        MvcResult result = mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Last name is required",
                    "instance": "/user/profile",
                    "error_codes": [
                        "errors.lastName.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile_longLastName() throws Exception {
        String longLastName = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901";

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nikki Nicholas",
                longLastName);

        MvcResult result = mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Last name length should not exceed 100 characters",
                    "instance": "/user/profile",
                    "error_codes": [
                        "errors.lastName.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updateUserProfile() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nikki Nicholas",
                "Romero");

        mockMvc.perform(post("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProfileService).updateUserProfile(request);
    }

    @Test
    @WithAnonymousUser
    void updatePassword_anonymous() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                "some new password");

        mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updatePassword_nullNewPassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                null);

        MvcResult result = mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "New password is required",
                    "instance": "/user/password",
                    "error_codes": [
                        "errors.newPassword.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updatePassword_blankNewPassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                " ");

        MvcResult result = mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "New password is required. Password must be at least 8 characters long",
                    "instance": "/user/password",
                    "error_codes": [
                        "errors.newPassword.required",
                        "errors.password.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updatePassword_shortNewPassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                "1234567");

        MvcResult result = mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Password must be at least 8 characters long",
                    "instance": "/user/password",
                    "error_codes": [
                        "errors.password.length"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(userProfileService);
    }

    @Test
    @WithMockUser
    void updatePassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                "some new password");

        mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProfileService).updatePassword(request);
    }
}
