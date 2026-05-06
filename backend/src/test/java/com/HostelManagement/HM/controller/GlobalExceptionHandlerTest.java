package com.HostelManagement.HM.controller;

import com.HostelManagement.HM.dto.CustomerExceptionResponse;
import com.HostelManagement.HM.exception.CustomerNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleCustomerNotFoundException() {

        CustomerNotFoundException exception = new CustomerNotFoundException("Customer Not Found");
        when(request.getRequestURI()).thenReturn("/customers");
        ResponseEntity<CustomerExceptionResponse> response = handler.handleCustomerNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        CustomerExceptionResponse exceptionResponse = response.getBody();

        assertNotNull(exceptionResponse);
        assertNotNull(exceptionResponse.getTimeStamp());
        assertEquals("Customer Not Found", exceptionResponse.getMessage());
        assertEquals("Customer Not Found", exceptionResponse.getDetails());
        assertEquals("/customers", exceptionResponse.getPath());

    }


    @Test
    void handleException() {

        when(request.getRequestURI()).thenReturn("/customers");

        Exception exception = new Exception("Internal Error Occurred");

        ResponseEntity<CustomerExceptionResponse> response = handler.handleException(exception,request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,response.getStatusCode());

        CustomerExceptionResponse exceptionResponse = response.getBody();

        assertNotNull(exceptionResponse);
        assertNotNull(exceptionResponse.getTimeStamp());
        assertEquals("Internal Error Occurred",exceptionResponse.getMessage());
        assertEquals("Internal Error Occurred",exceptionResponse.getDetails());
        assertEquals("/customers",exceptionResponse.getPath());
    }

    @Test
    void handleHttpMessageNotReadableException() {
        when(request.getRequestURI()).thenReturn("/customers");
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid Json");

        ResponseEntity<CustomerExceptionResponse> response =
                handler.handleHttpMessageNotReadableException(exception,request);

        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());

        CustomerExceptionResponse exceptionResponse = response.getBody();
        assertNotNull(exceptionResponse);
        assertNotNull(exceptionResponse.getTimeStamp());
        assertEquals("Invalid Json",exceptionResponse.getMessage());
        assertEquals("Bad request",exceptionResponse.getDetails());
        assertEquals("/customers",exceptionResponse.getPath());
    }


}

