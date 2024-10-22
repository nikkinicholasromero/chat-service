package com.chat.token;

import com.chat.BaseUnitTest;
import com.chat.common.encryption.JwtService;
import com.chat.token.dto.SocialProfile;
import com.chat.token.social.FacebookService;
import com.chat.token.dto.FacebookTokenRequest;
import com.chat.token.social.GoogleService;
import com.chat.token.dto.GoogleTokenRequest;
import com.chat.token.social.MicrosoftService;
import com.chat.token.dto.MicrosoftTokenRequest;
import com.chat.user.UserProfileService;
import com.chat.token.dto.TokenRequest;
import com.chat.common.repository.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenServiceTest extends BaseUnitTest {
    @InjectMocks
    private TokenService target;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleService googleService;

    @Mock
    private FacebookService facebookService;

    @Mock
    private MicrosoftService microsoftService;

    @Mock
    private UserProfile userProfile;

    private String email;

    private SocialProfile expectedSocialProfile;

    @BeforeEach
    public void setup() {
        email = " NIKKI@gmail.com ";
        when(userProfile.email()).thenReturn(email);
        when(userProfile.salt()).thenReturn("some salt");
        when(userProfile.hash()).thenReturn("some hash");
        when(userProfile.confirmed()).thenReturn(true);
        expectedSocialProfile = new SocialProfile(
                "nikkinicholas.romero@gmail.com",
                "Nikki Nicholas",
                "Romero");
        when(googleService.getGoogleProfile(any())).thenReturn(expectedSocialProfile);
        when(facebookService.getFacebookProfile(any())).thenReturn(expectedSocialProfile);
        when(microsoftService.getMicrosoftProfile(any())).thenReturn(expectedSocialProfile);
        when(jwtService.createJWT(any(), any())).thenReturn("some jwt");
    }

    @Test
    void getToken() {
        TokenRequest request = new TokenRequest(
                email,
                "some password");

        String actual = target.getToken(request);
        assertThat(actual).isEqualTo("some jwt");

        verify(userProfileService).validateCredentials(request.email(), request.password());
        verify(jwtService).createJWT(eq("nikki@gmail.com"), anyString());
    }

    @Test
    void getGoogleToken() {
        GoogleTokenRequest request = new GoogleTokenRequest(
                "someState",
                "someCode",
                "someScope");

        String actual = target.getGoogleToken(request);
        assertThat(actual).isEqualTo("some jwt");

        verify(googleService).getGoogleProfile(request);
        verify(userProfileService).registerSocialUser(expectedSocialProfile);
        verify(jwtService).createJWT(eq("nikkinicholas.romero@gmail.com"), anyString());
    }

    @Test
    void getFacebookToken() {
        FacebookTokenRequest request = new FacebookTokenRequest("someCode");

        String actual = target.getFacebookToken(request);
        assertThat(actual).isEqualTo("some jwt");

        verify(facebookService).getFacebookProfile(request);
        verify(userProfileService).registerSocialUser(expectedSocialProfile);
        verify(jwtService).createJWT(eq("nikkinicholas.romero@gmail.com"), anyString());
    }

    @Test
    void getMicrosoftToken() {
        MicrosoftTokenRequest request = new MicrosoftTokenRequest("someCode");

        String actual = target.getMicrosoftToken(request);
        assertThat(actual).isEqualTo("some jwt");

        verify(microsoftService).getMicrosoftProfile(request);
        verify(userProfileService).registerSocialUser(expectedSocialProfile);
        verify(jwtService).createJWT(eq("nikkinicholas.romero@gmail.com"), anyString());
    }
}
