package com.chat.token.social;

import com.chat.BaseUnitTest;
import com.chat.common.config.WebClientLogger;
import com.chat.common.exception.AuthorizationException;
import com.chat.token.dto.FacebookAccessTokenResponse;
import com.chat.token.dto.FacebookProfileResponse;
import com.chat.token.dto.FacebookTokenRequest;
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

class FacebookServiceTest extends BaseUnitTest {
    private MockWebServer mockBackEnd;
    private ObjectMapper objectMapper;

    @InjectMocks
    private FacebookService target;

    @Mock
    private WebClientLogger webClientLogger;

    @BeforeEach
    public void setup() throws Exception {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(target, "host", "http://localhost:" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "tokenEndpoint", "/v20.0/oauth/access_token");
        ReflectionTestUtils.setField(target, "profileEndpoint", "/me");
        ReflectionTestUtils.setField(target, "clientId", "441766668623384");
        ReflectionTestUtils.setField(target, "clientSecret", "some client secret");
        ReflectionTestUtils.setField(target, "redirectUri", "http://localhost:4200/auth/facebook");
        ReflectionTestUtils.setField(target, "fields", "email,first_name,last_name");

        when(webClientLogger.logRequest()).thenCallRealMethod();
        when(webClientLogger.logResponse()).thenCallRealMethod();
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void getFacebookProfile_token_error() throws JsonProcessingException, InterruptedException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        FacebookAccessTokenResponse response = new FacebookAccessTokenResponse("some access token");
        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        FacebookTokenRequest request = new FacebookTokenRequest("someCode");
        assertThrows(RuntimeException.class, () -> target.getFacebookProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/v20.0/oauth/access_token?client_id=441766668623384&client_secret=some%20client%20secret&redirect_uri=http://localhost:4200/auth/facebook&code=someCode");
    }

    @Test
    void getFacebookProfile_token_null() throws InterruptedException {
        mockBackEnd.enqueue(new MockResponse());

        FacebookTokenRequest request = new FacebookTokenRequest("someCode");
        assertThrows(AuthorizationException.class, () -> target.getFacebookProfile(request));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/v20.0/oauth/access_token?client_id=441766668623384&client_secret=some%20client%20secret&redirect_uri=http://localhost:4200/auth/facebook&code=someCode");
    }

    @Test
    void getFacebookProfile_profile_error() throws JsonProcessingException, InterruptedException {
        FacebookAccessTokenResponse token = new FacebookAccessTokenResponse("some access token");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(token))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        FacebookTokenRequest request = new FacebookTokenRequest("someCode");
        assertThrows(RuntimeException.class, () -> target.getFacebookProfile(request));
        assertEquals(2, mockBackEnd.getRequestCount());

        RecordedRequest recordedRequest1 = mockBackEnd.takeRequest();
        assertThat(recordedRequest1.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest1.getPath()).isEqualTo("/v20.0/oauth/access_token?client_id=441766668623384&client_secret=some%20client%20secret&redirect_uri=http://localhost:4200/auth/facebook&code=someCode");

        RecordedRequest recordedRequest2 = mockBackEnd.takeRequest();
        assertThat(recordedRequest2.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest2.getPath()).isEqualTo("/me?fields=email,first_name,last_name&access_token=some%20access%20token");
    }

    @Test
    void getFacebookProfile_profile_null() throws JsonProcessingException, InterruptedException {
        FacebookAccessTokenResponse token = new FacebookAccessTokenResponse("some access token");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(token))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockBackEnd.enqueue(new MockResponse());

        FacebookTokenRequest request = new FacebookTokenRequest("someCode");
        assertThrows(AuthorizationException.class, () -> target.getFacebookProfile(request));
        assertEquals(2, mockBackEnd.getRequestCount());

        RecordedRequest recordedRequest1 = mockBackEnd.takeRequest();
        assertThat(recordedRequest1.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest1.getPath()).isEqualTo("/v20.0/oauth/access_token?client_id=441766668623384&client_secret=some%20client%20secret&redirect_uri=http://localhost:4200/auth/facebook&code=someCode");

        RecordedRequest recordedRequest2 = mockBackEnd.takeRequest();
        assertThat(recordedRequest2.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest2.getPath()).isEqualTo("/me?fields=email,first_name,last_name&access_token=some%20access%20token");
    }

    @Test
    void getFacebookProfile() throws JsonProcessingException, InterruptedException {
        FacebookAccessTokenResponse token = new FacebookAccessTokenResponse("some access token");
        FacebookProfileResponse profile = new FacebookProfileResponse(
                "nikkinicholas.romero@gmail.com",
                "Nikki Nicholas",
                "Romero");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(token))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(profile))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        FacebookTokenRequest request = new FacebookTokenRequest("someCode");
        SocialProfile expected = new SocialProfile(
                "nikkinicholas.romero@gmail.com",
                "Nikki Nicholas",
                "Romero");
        assertEquals(expected, target.getFacebookProfile(request));
        assertEquals(2, mockBackEnd.getRequestCount());

        RecordedRequest recordedRequest1 = mockBackEnd.takeRequest();
        assertThat(recordedRequest1.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest1.getPath()).isEqualTo("/v20.0/oauth/access_token?client_id=441766668623384&client_secret=some%20client%20secret&redirect_uri=http://localhost:4200/auth/facebook&code=someCode");

        RecordedRequest recordedRequest2 = mockBackEnd.takeRequest();
        assertThat(recordedRequest2.getMethod()).isEqualTo(HttpMethod.GET.toString());
        assertThat(recordedRequest2.getPath()).isEqualTo("/me?fields=email,first_name,last_name&access_token=some%20access%20token");
    }
}
