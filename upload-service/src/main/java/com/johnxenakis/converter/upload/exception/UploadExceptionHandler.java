package com.johnxenakis.converter.upload.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UploadExceptionHandler {
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<Map<String, String>> handleFileValidation(FileValidationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("file", ex.getFileName());
        errorResponse.put("error", ex.getReason());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
