package com.chat;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class BaseUnitTest {
    @BeforeEach
    public void openMocks() {
        MockitoAnnotations.openMocks(this);
    }
}
