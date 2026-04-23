package com.HostelManagement.HM.controller;
import com.HostelManagement.HM.exception.CustomerNotFoundException;
import com.HostelManagement.HM.dto.CustomerExceptionResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<CustomerExceptionResponse> handleCustomerNotFoundException(CustomerNotFoundException exception, HttpServletRequest request){
        CustomerExceptionResponse errorResponse = new CustomerExceptionResponse(
                LocalDateTime.now(),
                exception.getMessage(),
                "Customer Not Found",
                request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomerExceptionResponse> handleException(Exception exception,HttpServletRequest request){
        CustomerExceptionResponse errorResponse = new CustomerExceptionResponse(
                LocalDateTime.now(),
                exception.getMessage(),
                "Internal Error Occurred",
                request.getRequestURI());
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomerExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception, HttpServletRequest request){
        String message = "Invalid Json";
        if(exception.getCause() instanceof InvalidFormatException formatException){
            String fieldName = formatException.getPath().getFirst().getFieldName();
            Object invalidValue = formatException.getValue();
            message = String.format("Invalid value '%s provided for field '%s'",invalidValue,fieldName);
        }
        CustomerExceptionResponse response = new CustomerExceptionResponse(
                LocalDateTime.now(),
                message,
                "Bad request",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
