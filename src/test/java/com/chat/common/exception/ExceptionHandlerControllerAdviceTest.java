package com.chat.common.exception;

import com.chat.BaseUnitTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExceptionHandlerControllerAdviceTest extends BaseUnitTest {
    @InjectMocks
    private ExceptionHandlerControllerAdvice target;

    @Mock
    private MessageSource messageSource;

    @Test
    void handleException_constraintViolationException() {
        ConstraintViolation<?> violation1 = Mockito.mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = Mockito.mock(ConstraintViolation.class);

        when(violation1.getMessageTemplate()).thenReturn("error1");
        when(violation2.getMessageTemplate()).thenReturn("error2");
        ConstraintViolationException exception = Mockito.mock(ConstraintViolationException.class);
        when(exception.getConstraintViolations()).thenReturn(Set.of(violation1, violation2));

        when(messageSource.getMessage(any(), any(), any())).thenReturn("message1", "message2");

        ProblemDetail actual = target.handleException(exception);
        ProblemDetail expected = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "message1. message2");
        expected.setProperty("error_codes", List.of("error1", "error2"));
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void handleException_methodArgumentNotValidException() {
        MethodParameter methodParameter = Mockito.mock(MethodParameter.class);

        FieldError fieldError1 = Mockito.mock(FieldError.class);
        FieldError fieldError2 = Mockito.mock(FieldError.class);

        when(fieldError1.getDefaultMessage()).thenReturn("error1");
        when(fieldError2.getDefaultMessage()).thenReturn("error2");

        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        when(messageSource.getMessage(any(), any(), any())).thenReturn("message1", "message2");

        ProblemDetail actual = target.handleException(exception);
        ProblemDetail expected = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "message1. message2");
        expected.setProperty("error_codes", List.of("error1", "error2"));
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void handleException_failedPreconditionException() {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");

        ProblemDetail actual = target.handleException(new FailedPreconditionException("error"));
        ProblemDetail expected = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "message");
        expected.setProperty("error_codes", List.of("error"));
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void handleException_authorizationException() {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");

        ProblemDetail actual = target.handleException(new AuthorizationException("error"));
        ProblemDetail expected = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "message");
        expected.setProperty("error_codes", List.of("error"));
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}
