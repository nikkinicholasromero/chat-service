package com.chat.common.security;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcWrapper {
    public void put(String key, String val) {
        MDC.put(key, val);
    }

    public void clear() {
        MDC.clear();
    }
}
