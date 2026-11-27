
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

    /**
     * Handles duplicate email registration attempts.
     *
     * @param exception the duplicate email exception
     * @param request the current HTTP request
     * @return a conflict response
     */
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

    /**
     * Handles requests for missing notes.
     *
     * @param exception the note not found exception
     * @param request the current HTTP request
     * @return a not found response
     */
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

    /**
     * Handles invalid authentication credentials.
     *
     * @param exception the invalid credentials exception
     * @param request the current HTTP request
     * @return an unauthorized response
     */
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

    /**
     * Handles bean validation failures on request bodies.
     *
     * @param exception the validation exception
     * @param request the current HTTP request
     * @return a bad request response with field errors
     */
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

    /**
     * Handles duplicate label creation attempts.
     *
     * @param exception the duplicate label exception
     * @param request the current HTTP request
     * @return a conflict response
     */
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

    /**
     * Handles requests for missing labels.
     *
     * @param exception the label not found exception
     * @param request the current HTTP request
     * @return a not found response
     */
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

    /**
     * Handles requests for missing reminders.
     *
     * @param exception the reminder not found exception
     * @param request the current HTTP request
     * @return a not found response
     */
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

    /**
     * Handles duplicate reminder creation attempts.
     *
     * @param exception the duplicate reminder exception
     * @param request the current HTTP request
     * @return a conflict response
     */
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

    /**
     * Handles login attempts from unverified accounts.
     *
     * @param exception the email not verified exception
     * @param request the current HTTP request
     * @return a forbidden response
     */
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

    /**
     * Handles invalid verification or reset tokens.
     *
     * @param exception the invalid token exception
     * @param request the current HTTP request
     * @return a bad request response
     */
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

    /**
     * Handles requests for missing attachments or missing stored files.
     *
     * @param exception the attachment not found exception
     * @param request the current HTTP request
     * @return a not found response
     */
    @ExceptionHandler(AttachmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleAttachmentNotFound(
            AttachmentNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    /**
     * Handles invalid file type, size, name and storage errors.
     *
     * @param exception the invalid attachment exception
     * @param request the current HTTP request
     * @return a bad request response
     */
    @ExceptionHandler(InvalidAttachmentException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidAttachment(
            InvalidAttachmentException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null
        );
    }

    /**
     * Handles all unexpected application exceptions.
     *
     * @param exception the unexpected exception
     * @param request the current HTTP request
     * @return an internal server error response
     */
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

    /**
     * Builds the standard API error response body.
     *
     * @param status the HTTP status
     * @param message the error message
     * @param request the current HTTP request
     * @param validationErrors optional field validation errors
     * @return the configured error response
     */
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
