package com.iodigital.assignment.tedtalks.web.exeptionhandler;

import com.iodigital.assignment.tedtalks.importcsv.exception.TedTalkImportException;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle validation errors from @Valid annotations
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorDTO apiError = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                ex.getMessage(),
                errors
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // Handle CSV import exceptions
    @ExceptionHandler(TedTalkImportException.class)
    public ResponseEntity<ApiErrorDTO> handleCsvImportException(TedTalkImportException ex) {
        log.error("CSV import error: {}", ex.getMessage(), ex);

        ApiErrorDTO apiError = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST,
                "CSV Import Error",
                ex.getMessage(),
                null
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // Handle entity not found exceptions
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        ApiErrorDTO apiError = new ApiErrorDTO(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                null
        );

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    // Handle data integrity violations (e.g., duplicate entries)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        ApiErrorDTO apiError = new ApiErrorDTO(
                HttpStatus.CONFLICT,
                "Data Integrity Violation",
                "Operation could not be performed due to data constraint",
                null
        );

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    // Handle constraint violations from validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorDTO apiError = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                ex.getMessage(),
                errors
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleAllUncaughtException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiErrorDTO apiError = new ApiErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please contact support.",
                null
        );

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}