package com.chat.common.security;

import com.chat.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.MDC;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class MdcWrapperTest extends BaseUnitTest {
    @InjectMocks
    private MdcWrapper target;

    private String key;

    private String value;

    @BeforeEach
    public void setup() {
        Random random = new Random();
        key = String.valueOf(random.nextLong());
        value = String.valueOf(random.nextLong());

        MDC.clear();
    }

    @Test
    void putAndClear() {
        target.put(key, value);
        assertThat(MDC.get(key)).isEqualTo(value);

        target.clear();
        assertThat(MDC.get(key)).isNull();
    }
}
