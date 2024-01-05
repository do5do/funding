package com.zerobase.funding.api.exception;

import static com.zerobase.funding.api.exception.ErrorCode.*;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        return toResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        assert fieldError != null;
        String message = fieldError.getField() + " " + fieldError.getDefaultMessage();
        return toResponse(INVALID_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintValidException(ConstraintViolationException e) {
        return toResponse(INVALID_REQUEST, e.getMessage().substring(19));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException is occurred. ", e);
        return toResponse(RESOURCE_NOT_FOUND, RESOURCE_NOT_FOUND.getMessage());
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleException(Exception e) {
//        log.error("Exception is occurred. ", e);
//        return toResponse(INTERNAL_ERROR, e.getMessage());
//    }

    private static ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode, message));
    }
}
