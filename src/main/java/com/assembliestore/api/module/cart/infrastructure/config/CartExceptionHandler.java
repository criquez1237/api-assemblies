package com.assembliestore.api.module.cart.infrastructure.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.assembliestore.api.common.response.ApiErrorResponse;
import com.assembliestore.api.common.response.ErrorDetail;
import com.assembliestore.api.common.response.ResponseUtil;
import com.assembliestore.api.common.response.TechnicalDetails;
import com.assembliestore.api.config.AppEnvConfig;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

@RestControllerAdvice(basePackages = "com.assembliestore.api.module.cart.infrastructure.adapter.in.api.controller")
public class CartExceptionHandler {

    @Autowired
    private AppEnvConfig appEnvConfig;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorDetail> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> {
            details.add(new ErrorDetail(fe.getField(), fe.getDefaultMessage()));
        });

        TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, 0, appEnvConfig);
        ApiErrorResponse err = new ApiErrorResponse("Validation error", "VALIDATION_ERROR", details, tech);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest request) {
        List<ErrorDetail> details = List.of(new ErrorDetail("argument", ex.getMessage()));
        TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, 0, appEnvConfig);
        ApiErrorResponse err = new ApiErrorResponse("Invalid argument", "INVALID_ARGUMENT", details, tech);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}
