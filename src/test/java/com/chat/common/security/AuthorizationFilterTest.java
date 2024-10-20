package com.chat.common.security;

import com.chat.BaseUnitTest;
import com.chat.common.encryption.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthorizationFilterTest extends BaseUnitTest {
    @InjectMocks
    private AuthorizationFilter target;

    @Mock
    private JwtService jwtService;

    @Mock
    private MdcWrapper mdc;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private Claims claims;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenBlankHeader() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(StringUtils.EMPTY);

        target.doFilterInternal(request, response, chain);

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoInteractions(jwtService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(response);
        verify(chain).doFilter(request, response);
        verify(mdc).clear();
        verifyNoMoreInteractions(mdc);
    }

    @Test
    void doFilterInternal_whenAnonymous() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("NotBearer");

        target.doFilterInternal(request, response, chain);

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoInteractions(jwtService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(response);
        verify(chain).doFilter(request, response);
        verify(mdc).clear();
        verifyNoMoreInteractions(mdc);
    }

    @Test
    void doFilterInternal_whenAuthorized() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer abc");

        when(jwtService.decodeJWT(anyString())).thenReturn(claims);
        when(claims.get(Claims.SUBJECT)).thenReturn("nikki@gmail.com");
        when(claims.get(JwtService.CLAIMS_SESSION_ID, String.class)).thenReturn("someSessionId");
        when(jwtService.createJWT(any(), any())).thenReturn("someToken");

        target.doFilterInternal(request, response, chain);

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verify(jwtService).decodeJWT("abc");
        verify(jwtService).createJWT("nikki@gmail.com", "someSessionId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        assertThat(userPrincipal.email()).isEqualTo("nikki@gmail.com");
        assertThat(userPrincipal.token()).isEqualTo("someToken");
        verify(response).addHeader(HttpHeaders.AUTHORIZATION, "Bearer someToken");
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(mdc, times(2)).put(keyCaptor.capture(), valueCaptor.capture());
        List<String> actualKeys = keyCaptor.getAllValues();
        assertThat(actualKeys).isNotNull()
                .hasSize(2)
                .contains("user")
                .contains("session");
        List<String> actualValues = valueCaptor.getAllValues();
        assertThat(actualValues).isNotNull()
                .hasSize(2)
                .contains("nikki@gmail.com")
                .contains("someSessionId");
        verify(chain).doFilter(request, response);
        verify(mdc).clear();
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        verify(response, never()).getWriter();
    }
}
