package com.chat.common.security;

import com.chat.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SecurityContextHelperTest extends BaseUnitTest {
    @InjectMocks
    private SecurityContextHelper target;

    @Mock
    private SecurityContext securityContext;

    private UserPrincipal expected;

    @BeforeEach
    public void setup() {
        expected = new UserPrincipal("nikki@gmail.com", "some token");
        Authentication authentication = new UsernamePasswordAuthenticationToken(expected, List.of());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void principal() {
        UserPrincipal actual = target.principal();
        assertEquals(actual, expected);
    }
}
