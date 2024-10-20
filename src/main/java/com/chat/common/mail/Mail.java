package com.chat.common.mail;

import java.util.List;
import java.util.Map;

public record Mail(
        MailTemplate template,
        List<String> recipients,
        Map<String, Object> variables
) {
}
