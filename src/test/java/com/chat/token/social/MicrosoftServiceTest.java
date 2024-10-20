package com.chat.token.social;

import com.chat.BaseUnitTest;
import com.chat.common.config.WebClientLogger;
import com.chat.common.exception.AuthorizationException;
import com.chat.token.dto.MicrosoftAccessTokenResponse;
import com.chat.token.dto.MicrosoftProfileResponse;
import com.chat.token.dto.MicrosoftTokenRequest;
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

class MicrosoftServiceTest extends BaseUnitTest {
    private MockWebServer mockBackEnd;
    private ObjectMapper objectMapper;

    @InjectMocks
    private MicrosoftService target;

    @Mock
    private WebClientLogger webClientLogger;

    @BeforeEach
    public void setup() throws Exception {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(target, "tokenHost", "http://localhost:" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "tokenEndpoint", "/consumers/oauth2/v2.0/token");
        ReflectionTestUtils.setField(target, "clientId", "6e5ee64a-b7da-42ca-ad8b-ffceac8fe699");
        ReflectionTestUtils.setField(target, "clientSecret", "some client secret");
        ReflectionTestUtils.setField(target, "redirectUri", "http://localhost:4200/auth/microsoft");
        ReflectionTestUtils.setField(target, "grantType", "authorization_code");
        ReflectionTestUtils.setField(target, "profileHost", "http://localhost:" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "profileEndpoint", "/v1.0/me");

        when(webClientLogger.logRequest()).thenCallRealMethod();
        when(webClientLogger.logResponse()).thenCallRealMethod();
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void getMicrosoftProfile_token_error() throws InterruptedException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");
        assertThrows(RuntimeException.class, () -> target.getMicrosoftProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/consumers/oauth2/v2.0/token");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("client_id=6e5ee64a-b7da-42ca-ad8b-ffceac8fe699&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fmicrosoft&grant_type=authorization_code&code=someCode");
    }

    @Test
    void getMicrosoftProfile_token_null() throws InterruptedException {
        mockBackEnd.enqueue(new MockResponse());

        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");
        assertThrows(RuntimeException.class, () -> target.getMicrosoftProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/consumers/oauth2/v2.0/token");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("client_id=6e5ee64a-b7da-42ca-ad8b-ffceac8fe699&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fmicrosoft&grant_type=authorization_code&code=someCode");
    }

    @Test
    void getGoogleProfile_profile_error() throws JsonProcessingException, InterruptedException {
        MicrosoftAccessTokenResponse token = new MicrosoftAccessTokenResponse("some access token");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(token))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");
        assertThrows(RuntimeException.class, () -> target.getMicrosoftProfile(request));
        assertEquals(2, mockBackEnd.getRequestCount());

        RecordedRequest recordedRequest1 = mockBackEnd.takeRequest();
        assertThat(recordedRequest1.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest1.getPath()).isEqualTo("/consumers/oauth2/v2.0/token");
        assertThat(recordedRequest1.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest1.getBody().readUtf8()).isEqualTo("client_id=6e5ee64a-b7da-42ca-ad8b-ffceac8fe699&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fmicrosoft&grant_type=authorization_code&code=someCode");

        RecordedRequest recordedRequest2 = mockBackEnd.takeRequest();
        assertThat(recordedRequest2.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest2.getPath()).isEqualTo("/v1.0/me");
        assertThat(recordedRequest2.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer some access token");
    }

    @Test
    void getGoogleProfile_profile_null() throws JsonProcessingException, InterruptedException {
        MicrosoftAccessTokenResponse token = new MicrosoftAccessTokenResponse("some access token");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(token))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockBackEnd.enqueue(new MockResponse());

        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");
        assertThrows(AuthorizationException.class, () -> target.getMicrosoftProfile(request));
        assertEquals(2, mockBackEnd.getRequestCount());

        RecordedRequest recordedRequest1 = mockBackEnd.takeRequest();
        assertThat(recordedRequest1.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest1.getPath()).isEqualTo("/consumers/oauth2/v2.0/token");
        assertThat(recordedRequest1.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest1.getBody().readUtf8()).isEqualTo("client_id=6e5ee64a-b7da-42ca-ad8b-ffceac8fe699&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fmicrosoft&grant_type=authorization_code&code=someCode");

        RecordedRequest recordedRequest2 = mockBackEnd.takeRequest();
        assertThat(recordedRequest2.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest2.getPath()).isEqualTo("/v1.0/me");
        assertThat(recordedRequest2.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer some access token");
    }

    @Test
    void getGoogleProfile() throws JsonProcessingException, InterruptedException {
        MicrosoftAccessTokenResponse token = new MicrosoftAccessTokenResponse("some access token");
        MicrosoftProfileResponse profile = new MicrosoftProfileResponse(
                "nikkinicholas.romero@gmail.com",
                "Nikki Nicholas",
                "Romero");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(token))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(profile))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");
        SocialProfile expected = new SocialProfile(
                "nikkinicholas.romero@gmail.com",
                "Nikki Nicholas",
                "Romero");
        assertEquals(expected, target.getMicrosoftProfile(request));
        assertEquals(2, mockBackEnd.getRequestCount());

        RecordedRequest recordedRequest1 = mockBackEnd.takeRequest();
        assertThat(recordedRequest1.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest1.getPath()).isEqualTo("/consumers/oauth2/v2.0/token");
        assertThat(recordedRequest1.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        assertThat(recordedRequest1.getBody().readUtf8()).isEqualTo("client_id=6e5ee64a-b7da-42ca-ad8b-ffceac8fe699&client_secret=some+client+secret&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fmicrosoft&grant_type=authorization_code&code=someCode");

        RecordedRequest recordedRequest2 = mockBackEnd.takeRequest();
        assertThat(recordedRequest2.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest2.getPath()).isEqualTo("/v1.0/me");
        assertThat(recordedRequest2.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer some access token");
    }
}
