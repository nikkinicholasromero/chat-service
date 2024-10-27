package com.chat.common.security;

import com.chat.common.encryption.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {
    private static final String BEARER = "Bearer ";
    private static final String USER = "user";
    private static final String SESSION = "session";

    private final JwtService jwtService;
    private final MdcWrapper mdc;

    public AuthorizationFilter(
            JwtService jwtService,
            MdcWrapper mdc) {
        this.jwtService = jwtService;
        this.mdc = mdc;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!request.getRequestURI().startsWith("/token") && StringUtils.isNotBlank(header) && header.startsWith(BEARER)) {
            Claims claims = jwtService.decodeJWT(header.replace(BEARER, StringUtils.EMPTY));
            String email = String.valueOf(claims.get(Claims.SUBJECT));
            String sessionId = claims.get(JwtService.CLAIMS_SESSION_ID, String.class);
            String token = jwtService.createJWT(email, sessionId);
            UserPrincipal userPrincipal = new UserPrincipal(email, token);

            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userPrincipal, null));

            response.addHeader(HttpHeaders.AUTHORIZATION, BEARER + token);

            mdc.put(USER, email);
            mdc.put(SESSION, sessionId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            mdc.clear();
        }
    }
}
