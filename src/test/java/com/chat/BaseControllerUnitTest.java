package com.chat;

import com.chat.common.config.AppConfig;
import com.chat.user.repository.UserProfileRepository;
import com.chat.common.security.MdcWrapper;
import com.chat.common.encryption.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = {Application.class, AppConfig.class})
public abstract class BaseControllerUnitTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserProfileRepository userProfileRepository;

    @MockBean
    private MdcWrapper mdcWrapper;

    @Autowired
    protected ObjectMapper objectMapper;
}
