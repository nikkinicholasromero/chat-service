package com.chat.token;

import com.chat.token.dto.FacebookTokenRequest;
import com.chat.token.dto.GoogleTokenRequest;
import com.chat.token.dto.MicrosoftTokenRequest;
import com.chat.token.dto.TokenRequest;
import com.chat.token.dto.TokenResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/token")
public class TokenController {
    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public TokenResponse getToken(@Valid @RequestBody TokenRequest request) {
        log.info("getToken {}", request);
        return new TokenResponse(tokenService.getToken(request));
    }

    @PostMapping("/google")
    public TokenResponse getGoogleToken(@Valid @RequestBody GoogleTokenRequest request) {
        log.info("getGoogleToken {}", request);
        return new TokenResponse(tokenService.getGoogleToken(request));
    }

    @PostMapping("/facebook")
    public TokenResponse getFacebookToken(@Valid @RequestBody FacebookTokenRequest request) {
        log.info("getFacebookToken {}", request);
        return new TokenResponse(tokenService.getFacebookToken(request));
    }

    @PostMapping("/microsoft")
    public TokenResponse getMicrosoftToken(@Valid @RequestBody MicrosoftTokenRequest request) {
        log.info("getMicrosoftToken {}", request);
        return new TokenResponse(tokenService.getMicrosoftToken(request));
    }
}
