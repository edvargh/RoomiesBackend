package com.roomies.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.MailSendException;


import java.util.*;

/**
 * Global error handler for every controller in the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String ERROR_KEY = "error";

  /**
   * Handles IllegalArgumentException
   * @param ex the exception thrown
   * @return a ResponseEntity with a bad request status and error message
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("IllegalArgumentException : {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Map.of(ERROR_KEY, ex.getMessage()));
  }

  /**
   * Handles MethodArgumentNotValidException
   * @param ex the exception thrown when validation fails
   * @return a ResponseEntity with a bad request status and validation error details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

    log.warn("Validation failed : {}", fieldErrors);
    return ResponseEntity.badRequest()
        .body(Map.of(
            ERROR_KEY, "Validation failed",
            "details", fieldErrors
        ));
  }

  /**
   * Handles ConstraintViolationException
   * @param ex the exception thrown when a constraint is violated
   * @return a ResponseEntity with a bad request status and error message
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
    log.warn("ConstraintViolation : {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Map.of(ERROR_KEY, "Validation failed"));
  }

  /**
   * Handles AuthenticationException
   * @param ex the exception thrown during authentication
   * @return a ResponseEntity with an unauthorized status and error message
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex) {
    log.warn("Authentication failure : {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of(ERROR_KEY, ex.getMessage()));
  }

  /**
   * Handles MailSendException thrown when an e-mail cannot be delivered.
   */
  @ExceptionHandler(MailSendException.class)
  public ResponseEntity<Map<String, String>> handleMailSend(MailSendException ex) {
    log.error("MailSendException : {}", ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            ERROR_KEY,
            "Unable to send e-mail at the moment. Please try again later."
        ));
  }

  /**
   * Handles AccessDeniedException
   * @param ex the exception thrown when access is denied
   * @return a ResponseEntity with a forbidden status and error message
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied : {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of(ERROR_KEY, "You do not have permission to access this resource."));
  }

  /**
   * Handles all other exceptions
   * @param ex the exception thrown
   * @return a ResponseEntity with an internal server error status and generic error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(ERROR_KEY, "Unexpected error occurred. Please try again later."));
  }
}
