package com.chat.common.exception;

public class FailedPreconditionException extends RuntimeException {
    public FailedPreconditionException(String message) {
        super(message);
    }
}
