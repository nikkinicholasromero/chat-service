package com.chat.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@ResponseBody
@ControllerAdvice
public class ExceptionHandlerControllerAdvice {
    private static final String ERROR_CODES = "error_codes";
    private final MessageSource messageSource;

    public ExceptionHandlerControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleException(ConstraintViolationException e) {
        List<String> errorCodes = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessageTemplate)
                .sorted()
                .toList();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errorCodes.stream()
                        .map(this::getLocalizedMessage)
                        .collect(Collectors.joining(". ")));
        problemDetail.setProperty(ERROR_CODES, errorCodes);
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleException(MethodArgumentNotValidException e) {
        List<String> errorCodes = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errorCodes.stream()
                        .map(this::getLocalizedMessage)
                        .collect(Collectors.joining(". ")));
        problemDetail.setProperty(ERROR_CODES, errorCodes);
        return problemDetail;
    }

    @ExceptionHandler(FailedPreconditionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleException(FailedPreconditionException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                getLocalizedMessage(e.getMessage()));
        problemDetail.setProperty(ERROR_CODES, List.of(e.getMessage()));
        return problemDetail;
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleException(AuthorizationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                getLocalizedMessage(e.getMessage()));
        problemDetail.setProperty(ERROR_CODES, List.of(e.getMessage()));
        return problemDetail;
    }

    private String getLocalizedMessage(String code, Object... messageParams) {
        return messageSource.getMessage(code, messageParams, Locale.getDefault());
    }
}
