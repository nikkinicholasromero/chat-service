package com.chat.common.mail;

public enum MailTemplate {
    REGISTRATION_CONFIRMATION("registration_confirmation", "Chat - Registration Confirmation"),
    PASSWORD_RESET("password_reset", "Chat - Password Reset");

    private final String template;
    private final String subject;

    MailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }

    public String template() {
        return template;
    }

    public String subject() {
        return subject;
    }
}
