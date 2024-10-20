package com.chat.token;

import com.chat.token.dto.FacebookTokenRequest;
import com.chat.token.dto.GoogleTokenRequest;
import com.chat.token.dto.MicrosoftTokenRequest;
import com.chat.token.dto.TokenRequest;
import com.chat.BaseControllerUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TokenController.class)
class TokenControllerTest extends BaseControllerUnitTest {
    @MockBean
    private TokenService tokenService;

    @Test
    void getToken_nullEmail() throws Exception {
        TokenRequest request = new TokenRequest(
                null,
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
                    "detail": "Email address is required",
                    "instance": "/token",
                    "error_codes": [
                        "errors.email.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(tokenService);
    }

    @Test
    void getToken_blankEmail() throws Exception {
        TokenRequest request = new TokenRequest(
                " ",
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
                    "detail": "Email address is required",
                    "instance": "/token",
                    "error_codes": [
                        "errors.email.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(tokenService);
    }

    @Test
    void getToken_nullPassword() throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                null);

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
                    "detail": "Password is required",
                    "instance": "/token",
                    "error_codes": [
                        "errors.password.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(tokenService);
    }

    @Test
    void getToken_blankPassword() throws Exception {
        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                " ");

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
                    "detail": "Password is required",
                    "instance": "/token",
                    "error_codes": [
                        "errors.password.required"
                    ]
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verifyNoInteractions(tokenService);
    }

    @Test
    void getToken() throws Exception {
        when(tokenService.getToken(any())).thenReturn("someToken");

        TokenRequest request = new TokenRequest(
                "nikki@gmail.com",
                "some password");

        MvcResult result = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "accessToken": "someToken"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verify(tokenService).getToken(request);
    }

    @Test
    void getGoogleToken() throws Exception {
        when(tokenService.getGoogleToken(any())).thenReturn("someToken");

        GoogleTokenRequest request = new GoogleTokenRequest(
                "someState",
                "someCode",
                "someScope");

        MvcResult result = mockMvc.perform(post("/token/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "accessToken": "someToken"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verify(tokenService).getGoogleToken(request);
    }

    @Test
    void getFacebookToken() throws Exception {
        when(tokenService.getFacebookToken(any())).thenReturn("someToken");

        FacebookTokenRequest request = new FacebookTokenRequest("someCode");

        MvcResult result = mockMvc.perform(post("/token/facebook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "accessToken": "someToken"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verify(tokenService).getFacebookToken(request);
    }

    @Test
    void getMicrosoftToken() throws Exception {
        when(tokenService.getMicrosoftToken(any())).thenReturn("someToken");

        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");

        MvcResult result = mockMvc.perform(post("/token/microsoft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String expected = """
                {
                    "accessToken": "someToken"
                }
                """;

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected, actual, false);

        verify(tokenService).getMicrosoftToken(request);
    }
}
