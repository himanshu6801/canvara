package com.canvara.app.exception;

import com.canvara.app.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(UnauthorizedAccessException ex) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFile(InvalidFileException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid File", ex.getMessage(), null);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorage(FileStorageException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Storage Error", ex.getMessage(), null);
    }

    // Bean validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                (a, b) -> a   // keep first message if duplicate fields
            ));
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields are invalid", fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", null);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, String error, String message, Map<String, String> fieldErrors) {

        ApiErrorResponse body = ApiErrorResponse.builder()
            .status(status.value())
            .error(error)
            .message(message)
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(status).body(body);
    }
}
