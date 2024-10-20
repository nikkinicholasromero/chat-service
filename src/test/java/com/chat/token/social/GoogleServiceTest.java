package com.chat.token.social;

import com.chat.BaseUnitTest;
import com.chat.common.config.WebClientLogger;
import com.chat.common.exception.AuthorizationException;
import com.chat.token.dto.GoogleTokenRequest;
import com.chat.token.dto.GoogleTokenResponse;
import com.chat.token.dto.SocialProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class GoogleServiceTest extends BaseUnitTest {
    private static final String GOOGLE_TOKEN = """
            eyJhbGciOiJSUzI1NiIsImtpZCI6IjJhZjkwZTg3YmUxNDBjMjAwMzg4OThhNmVmYTExMjgzZGFiNjAzMWQiLCJ0eXAiOiJKV1QifQ
            .
            eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1NDYwNDM0OTUwODItZ3FsY3JidGZjcmNmY21rNWM1dG\
            FnMzNyM2dtdGQ5ZTMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1NDYwNDM0OTUwODItZ3FsY3JidGZjcmNmY21r\
            NWM1dGFnMzNyM2dtdGQ5ZTMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDUwMDMyNzQxODc4NjkzMTAxMDYiLC\
            JlbWFpbCI6Im5pa2tpbmljaG9sYXMucm9tZXJvQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoibU5F\
            bGJoMGpGZXlhcVBFTEZMX1E3USIsIm5hbWUiOiJOaWtraSBOaWNob2xhcyBSb21lcm8iLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ2\
            9vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jTE85c0FIaGc5eHEzSE93Nm13Uy1ncUN4bllha3R4R3hDbGIxY2k1Q3pSaW9rdkRT\
            Nkk9czk2LWMiLCJnaXZlbl9uYW1lIjoiTmlra2kgTmljaG9sYXMiLCJmYW1pbHlfbmFtZSI6IlJvbWVybyIsImlhdCI6MTcxOTQ4OT\
            k3MywiZXhwIjoxNzE5NDkzNTczfQ
            .
            JrsYQM8Na_L6De1oD1_NdnmsuKOrsGBJ9DbmwIRGe_wtKyo8pVg-DM0Obmn5yfsI_NDErROyEqHOTbILSYoMGvO7V3GZKiFBaWobDg\
            ZaTkZsWdGhyMHYHUbLODQACBUX_Y4FMy5EMA7fXdxM5LNweHD-JoLRzeJ5ZEmvylILqs1oalqTc2JpvqIS6b6veOjhcFsRy-eXbOaO\
            uA_G_kVZ4dF-XQs9yl0tmGiRyfngMsvyuN3_6QZxlzhQj3RPLSPeKpksYrJPDWanzjvi_NWPYM7q8wcrmKeO5mshD9mIqzkCXZA9-z\
            qJayFU6Zl7MWbsN_v58siWjA5963V8BWiDHg
            """;

    private MockWebServer mockBackEnd;
    private ObjectMapper objectMapper;

    @InjectMocks
    private GoogleService target;

    @Mock
    private WebClientLogger webClientLogger;

    @BeforeEach
    public void setup() throws Exception {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(target, "host", "http://localhost:" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "endpoint", "/token");
        ReflectionTestUtils.setField(target, "clientId", "546043495082-gqlcrbtfcrcfcmk5c5tag33r3gmtd9e3.apps.googleusercontent.com");
        ReflectionTestUtils.setField(target, "clientSecret", "some client secret");
        ReflectionTestUtils.setField(target, "redirectUri", "http://localhost:4200/auth/google");
        ReflectionTestUtils.setField(target, "grantType", "authorization_code");

        when(webClientLogger.logRequest()).thenCallRealMethod();
        when(webClientLogger.logResponse()).thenCallRealMethod();
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void getGoogleProfile_error() throws InterruptedException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        GoogleTokenRequest request = new GoogleTokenRequest(
                "someState",
                "someCode",
                "someScope");
        assertThrows(RuntimeException.class, () -> target.getGoogleProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/token");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("code=someCode&client_id=546043495082-gqlcrbtfcrcfcmk5c5tag33r3gmtd9e3.apps.googleusercontent.com&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fgoogle&grant_type=authorization_code");
    }

    @Test
    void getGoogleProfile_null() throws InterruptedException {
        mockBackEnd.enqueue(new MockResponse());

        GoogleTokenRequest request = new GoogleTokenRequest(
                "someState",
                "someCode",
                "someScope");
        assertThrows(AuthorizationException.class, () -> target.getGoogleProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/token");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("code=someCode&client_id=546043495082-gqlcrbtfcrcfcmk5c5tag33r3gmtd9e3.apps.googleusercontent.com&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fgoogle&grant_type=authorization_code");
    }

    @Test
    void getGoogleProfile() throws JsonProcessingException, InterruptedException {
        GoogleTokenResponse response = new GoogleTokenResponse(GOOGLE_TOKEN);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        GoogleTokenRequest request = new GoogleTokenRequest(
                "someState",
                "someCode",
                "someScope");
        SocialProfile expected = new SocialProfile(
                "nikkinicholas.romero@gmail.com",
                "Nikki Nicholas",
                "Romero");
        assertEquals(expected, target.getGoogleProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/token");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("code=someCode&client_id=546043495082-gqlcrbtfcrcfcmk5c5tag33r3gmtd9e3.apps.googleusercontent.com&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fgoogle&grant_type=authorization_code");
    }
}
