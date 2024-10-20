package com.chat.token;

import com.chat.common.encryption.JwtService;
import com.chat.token.dto.SocialProfile;
import com.chat.token.social.FacebookService;
import com.chat.token.dto.FacebookTokenRequest;
import com.chat.token.social.GoogleService;
import com.chat.token.dto.GoogleTokenRequest;
import com.chat.token.social.MicrosoftService;
import com.chat.token.dto.MicrosoftTokenRequest;
import com.chat.token.dto.TokenRequest;
import com.chat.user.UserProfileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenService {
    private final UserProfileService userProfileService;
    private final JwtService jwtService;
    private final GoogleService googleService;
    private final FacebookService facebookService;
    private final MicrosoftService microsoftService;

    public TokenService(
            UserProfileService userProfileService,
            JwtService jwtService,
            GoogleService googleService,
            FacebookService facebookService,
            MicrosoftService microsoftService) {
        this.userProfileService = userProfileService;
        this.jwtService = jwtService;
        this.googleService = googleService;
        this.facebookService = facebookService;
        this.microsoftService = microsoftService;
    }

    public String getToken(TokenRequest request) {
        userProfileService.validateCredentials(request.email(), request.password());

        return getToken(request.email());
    }

    public String getGoogleToken(GoogleTokenRequest request) {
        return getSocialToken(googleService.getGoogleProfile(request));
    }

    public String getFacebookToken(FacebookTokenRequest request) {
        return getSocialToken(facebookService.getFacebookProfile(request));
    }

    public String getMicrosoftToken(MicrosoftTokenRequest request) {
        return getSocialToken(microsoftService.getMicrosoftProfile(request));
    }

    private String getSocialToken(SocialProfile socialProfile) {
        userProfileService.registerSocialUser(socialProfile);

        return getToken(socialProfile.email());
    }

    private String getToken(String email) {
        return jwtService.createJWT(
                cleanEmail(email),
                UUID.randomUUID().toString());
    }

    private String cleanEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase();
    }
}
