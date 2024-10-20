package com.chat;

import com.chat.user.repository.UserProfile;
import com.chat.user.repository.UserProfileRepository;
import com.chat.token.dto.TokenRequest;
import com.chat.token.dto.TokenResponse;
import com.chat.user.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DirtiesContext
    void registration() throws Exception {
        getEmailStatus_unregistered();
        sendConfirmationCode_unregistered();
        confirmRegistration_unregistered();
        sendPasswordResetCode_unregistered();
        resetPassword_unregistered();
        getToken_unregistered();
        registerUser_unregistered("Nikki Nicholas", "Romero");
        getEmailStatus_unconfirmed();
        registerUser_unconfirmed();
        sendConfirmationCode_unconfirmed();
        resetPassword_unconfirmed_blankPasswordResetCode();
        sendPasswordResetCode_unconfirmed();
        resetPassword_unconfirmed();
        resetPassword_unconfirmed_blankPasswordResetCode();
        getToken_unconfirmed();
        confirmRegistration_unconfirmed_invalidConfirmationCode();
        confirmRegistration_unconfirmed();
        getEmailStatus_confirmed();
        registerUser_confirmed();
        sendConfirmationCode_confirmed();
        confirmRegistration_confirmed();
        getToken_confirmed("some new password");
        sendPasswordResetCode_confirmed();
        resetPassword_confirmed();
        getToken_confirmed_newPassword();
        getToken_confirmed_invalidPassword();
    }

    @Test
    @DirtiesContext
    void profileManagement() throws Exception {
        registerUser_unregistered("Nikki Nicholas", "Romero");
        confirmRegistration_unconfirmed();
        String token = getToken_confirmed("some password");
        getUserProfile(token, new GetUserProfileResponse("Nikki Nicholas", "Romero", false));
        updateUserProfile(token, new UpdateUserProfileRequest("New First Name", "New Last Name"));
        getUserProfile(token, new GetUserProfileResponse("New First Name", "New Last Name", false));
        updatePassword(token, new UpdatePasswordRequest("some password", "some new password"));
        getToken_confirmed("some new password");
    }

    // TODO: Write tests for social login flow

    private void getEmailStatus_unregistered() throws Exception {
        MvcResult result = mockMvc.perform(get("/user?email=nikki@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "status": "UNREGISTERED"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void sendConfirmationCode_unregistered() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/registration/confirmation/nikki@gmail.com"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Email not registered",
                    "instance": "/user/registration/confirmation/nikki@gmail.com",
                    "error_codes": [
                        "errors.email.unregistered"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void confirmRegistration_unregistered() throws Exception {
        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                "nikki@gmail.com",
                "some confirmation code",
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
                    "detail": "Email not registered",
                    "instance": "/user/registration/confirmation",
                    "error_codes": [
                        "errors.email.unregistered"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void sendPasswordResetCode_unregistered() throws Exception {
        mockMvc.perform(post("/user/password/reset/nikki@gmail.com"))
                .andExpect(status().isOk());
    }

    private void resetPassword_unregistered() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                "some password reset code",
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
                    "detail": "Email not registered",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.email.unregistered"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void getToken_unregistered() throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                "some password");

        MvcResult result = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Email not registered",
                    "instance": "/token",
                    "error_codes": [
                        "errors.email.unregistered"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void registerUser_unregistered(String firstName, String lastName) throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
                "some password",
                firstName,
                lastName,
                false);

        mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void getEmailStatus_unconfirmed() throws Exception {
        MvcResult result = mockMvc.perform(get("/user?email=nikki@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "status": "UNCONFIRMED"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void registerUser_unconfirmed() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
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
                    "detail": "Email already registered",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.email.registered"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void sendConfirmationCode_unconfirmed() throws Exception {
        mockMvc.perform(post("/user/registration/confirmation/nikki@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void resetPassword_unconfirmed_blankPasswordResetCode() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                "some password reset code",
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
                    "detail": "Invalid password reset code",
                    "instance": "/user/password/reset",
                    "error_codes": [
                        "errors.invalidPasswordResetCode"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void sendPasswordResetCode_unconfirmed() throws Exception {
        mockMvc.perform(post("/user/password/reset/nikki@gmail.com"))
                .andExpect(status().isOk());
    }

    private void resetPassword_unconfirmed() throws Exception {
        UserProfile userProfile = userProfileRepository.findByEmail("nikki@gmail.com").orElseThrow();

        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                userProfile.passwordResetCode(),
                "some new password");

        mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void getToken_unconfirmed() throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                "some password");

        MvcResult result = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
            {
                "type": "about:blank",
                "title": "Bad Request",
                "status": 400,
                "detail": "Account not confirmed yet",
                "instance": "/token",
                "error_codes": [
                    "errors.email.unconfirmed"
                ]
            }
            """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void confirmRegistration_unconfirmed_invalidConfirmationCode() throws Exception {
        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                "nikki@gmail.com",
                "some invalid confirmation code",
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
                    "detail": "Invalid confirmation code",
                    "instance": "/user/registration/confirmation",
                    "error_codes": [
                        "errors.invalidConfirmationCode"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void confirmRegistration_unconfirmed() throws Exception {
        UserProfile userProfile = userProfileRepository.findByEmail("nikki@gmail.com").orElseThrow();

        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                "nikki@gmail.com",
                userProfile.confirmationCode(),
                false);

        mockMvc.perform(post("/user/registration/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void getEmailStatus_confirmed() throws Exception {
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
    }

    private void registerUser_confirmed() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "nikki@gmail.com",
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
                    "detail": "Email already registered",
                    "instance": "/user/registration",
                    "error_codes": [
                        "errors.email.registered"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void sendConfirmationCode_confirmed() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/registration/confirmation/nikki@gmail.com"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Email already confirmed",
                    "instance": "/user/registration/confirmation/nikki@gmail.com",
                    "error_codes": [
                        "errors.email.alreadyConfirmed"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void confirmRegistration_confirmed() throws Exception {
        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                "nikki@gmail.com",
                "some confirmation code",
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
                    "detail": "Email already confirmed",
                    "instance": "/user/registration/confirmation",
                    "error_codes": [
                        "errors.email.alreadyConfirmed"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private String getToken_confirmed(String password) throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                password);

        MvcResult result = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class).accessToken().toString();
    }

    private void sendPasswordResetCode_confirmed() throws Exception {
        mockMvc.perform(post("/user/password/reset/nikki@gmail.com"))
                .andExpect(status().isOk());
    }

    private void resetPassword_confirmed() throws Exception {
        UserProfile userProfile = userProfileRepository.findByEmail("nikki@gmail.com").orElseThrow();

        PasswordResetRequest request = new PasswordResetRequest(
                "nikki@gmail.com",
                userProfile.passwordResetCode(),
                "some password");

        mockMvc.perform(post("/user/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void getToken_confirmed_newPassword() throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                "some password");

        mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    private void getToken_confirmed_invalidPassword() throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                "some invalid password");

        MvcResult result = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expected = """
            {
                "type": "about:blank",
                "title": "Bad Request",
                "status": 400,
                "detail": "Incorrect credentials",
                "instance": "/token",
                "error_codes": [
                    "errors.credentials.incorrect"
                ]
            }
            """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);
    }

    private void getUserProfile(String token, GetUserProfileResponse expected) throws Exception {
        MvcResult result = mockMvc.perform(get("/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String actual = result.getResponse().getContentAsString();
        assertEquals(objectMapper.writeValueAsString(expected), actual, false);
    }

    private void updateUserProfile(String token, UpdateUserProfileRequest request) throws Exception {
        mockMvc.perform(post("/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void updatePassword(String token, UpdatePasswordRequest request) throws Exception {
        mockMvc.perform(post("/user/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
    }
}
