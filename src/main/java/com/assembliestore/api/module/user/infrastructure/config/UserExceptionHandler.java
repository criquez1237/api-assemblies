package com.assembliestore.api.module.user.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.assembliestore.api.common.error.CredentialInvalidExeption;
import com.assembliestore.api.common.error.EmailAlreadyExistsException;
import com.assembliestore.api.common.error.InfrastructureException;
import com.assembliestore.api.common.error.UserNotFoundException;

//"com.assembliestore.api.module.user.application.controllers"

@RestControllerAdvice(basePackages = "com.assembliestore.api.module.user.infrastructure.adapter.in.api.controllers")
public class UserExceptionHandler {

    @ExceptionHandler({ InfrastructureException.class })
    public ResponseEntity<Object> handleInfrastructureException(InfrastructureException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());

    }

    @ExceptionHandler({ UserNotFoundException.class })
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler({ CredentialInvalidExeption.class })
    public ResponseEntity<Object> handleCredentialInvalidFoundException(CredentialInvalidExeption exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler({ EmailAlreadyExistsException.class })
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    /*
     * @ExceptionHandler(UserNotFoundException.class)
     * public ResponseEntity<ErrorResponse>
     * handleUserNotFoundException(UserNotFoundException ex) {
     * // Traduce la excepción de dominio a un código de estado HTTP 404
     * ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
     * ex.getMessage());
     * return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
     * }
     */
}
