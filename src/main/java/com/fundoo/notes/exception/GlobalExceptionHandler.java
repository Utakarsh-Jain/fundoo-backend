
package com.fundoo.notes.exception;

import com.fundoo.notes.dto.response.ApiErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Class Name  : GlobalExceptionHandler
 * Description : Centrally handles application exceptions and
 *               returns standardized HTTP error responses.
 * Author      : Utakarsh Jain
 * Date        : 26/11/2026
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNoteNotFound(
            NoteNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            validationErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                validationErrors
        );
    }

    @ExceptionHandler(DuplicateLabelException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateLabel(
            DuplicateLabelException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(LabelNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleLabelNotFound(
            LabelNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleReminderNotFound(
            ReminderNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(DuplicateReminderException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateReminder(
            DuplicateReminderException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleEmailNotVerified(
            EmailNotVerifiedException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidToken(
            InvalidTokenException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGenericException(
            Exception exception,
            HttpServletRequest request) {

        logger.error(
                "Unexpected exception at {}: ",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponseDto> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {

        ApiErrorResponseDto response = new ApiErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
