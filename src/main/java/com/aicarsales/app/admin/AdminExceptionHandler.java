
package com.aicarsales.app.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.aicarsales.app.admin")
public class AdminExceptionHandler {

    @ExceptionHandler(CsvValidationException.class)
    public ResponseEntity<String> handleCsvValidation(CsvValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
