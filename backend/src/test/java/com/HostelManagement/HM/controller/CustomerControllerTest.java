package com.HostelManagement.HM.controller;

import com.HostelManagement.HM.config.SecurityUtils;
import com.HostelManagement.HM.dto.CustomerDto;
import com.HostelManagement.HM.dto.MessageDto;
import com.HostelManagement.HM.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//    Failure cases handled by Controller Advice

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

    @Mock
    SecurityUtils securityUtils;

    @Mock
    CustomerService customerService;

    @InjectMocks
    CustomerController customerController;

    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        customerDto = CustomerDto.builder().build();
    }


    @Test
    void addCustomerSuccess() {

        when(securityUtils.getUserId()).thenReturn(100L);
        CustomerDto savedDto = CustomerDto.builder()
                .userId(100L)
                .build();
        when(customerService.addCustomer(any(CustomerDto.class))).thenReturn(savedDto);
        ResponseEntity<CustomerDto> response = customerController.addCustomer(customerDto);
        // assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedDto, response.getBody());
        // verify interactions
        verify(securityUtils, times(1)).getUserId();
        verify(customerService).addCustomer(any(CustomerDto.class));

    }

    @Test
    void editCustomerSuccess() {
        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.editCustomer(any(CustomerDto.class), anyInt(), anyLong())).thenReturn(customerDto);
        ResponseEntity<CustomerDto> response = customerController.editCustomer(customerDto, 100);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDto, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).editCustomer(eq(customerDto), anyInt(), anyLong());
    }


    @Test
    void getCustomerByIdSuccess() {

        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.getCustomerById(anyInt(), anyLong())).thenReturn(customerDto);

        ResponseEntity<CustomerDto> response = customerController.getCustomerById(100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDto, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).getCustomerById(100, 100L);
    }


    @Test
    void getAllCustomerSuccess() {

        List<CustomerDto> customerDtoList = new ArrayList<>();

        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.getAllCustomer(anyLong(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(customerDtoList);

        ResponseEntity<List<CustomerDto>> response = customerController.getAllCustomer(0, 10, "id", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDtoList, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).getAllCustomer(anyLong(), anyInt(), anyInt(), anyString(), anyString());

    }

    @Test
    void sendAlertBeforeSuccess() {

        List<CustomerDto> customerDtoList = new ArrayList<>();

        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.sendAlertBefore(anyLong(), anyInt())).thenReturn(customerDtoList);

        ResponseEntity<List<CustomerDto>> response = customerController.sendAlertBefore(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDtoList, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).sendAlertBefore(anyLong(), anyInt());
    }

    @Test
    void deleteCustomerSuccess() {

        when(securityUtils.getUserId()).thenReturn(100L);
        ResponseEntity<Void> response = customerController.deleteCustomer(10);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).deleteCustomer(100L, 10);
    }


    @Test
    void messageCustomerSuccess() {
        MessageDto messageDto = new MessageDto();
        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.messageCustomer(anyInt(), anyLong(), any(MessageDto.class))).thenReturn(messageDto);

        ResponseEntity<MessageDto> response = customerController.messageCustomer(10, messageDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(messageDto, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).messageCustomer(anyInt(), anyLong(), any(MessageDto.class));
    }

    @Test
    void patchCustomerSuccess() {
        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.patchCustomer(anyInt(), anyLong(), any(CustomerDto.class))).thenReturn(customerDto);

        ResponseEntity<CustomerDto> response = customerController.patchCustomer(10, customerDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDto, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).patchCustomer(anyInt(), anyLong(), any(CustomerDto.class));
    }

    @Test
    void searchCustomerSuccess(){
        List<CustomerDto> customerDtoList = new ArrayList<>();

        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.searchKeyword(anyString(),anyString(),anyString(),anyInt(),anyInt(),anyLong())).thenReturn(customerDtoList);

        ResponseEntity<List<CustomerDto>> response = customerController.searchCustomer("Hello","ASC","id",0,5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDtoList, response.getBody());

        verify(securityUtils,times(1)).getUserId();
        verify(customerService,times(1)).searchKeyword(anyString(),anyString(),anyString(),anyInt(),anyInt(),anyLong());

    }

    @Test
    void makeAlertSuccess(){
        List<CustomerDto> customerDtoList = new ArrayList<>();
        when(customerService.sendAlert(anyList())).thenReturn(customerDtoList);

        ResponseEntity<List<CustomerDto>> response = customerController.makeAlert(customerDtoList);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(customerDtoList,response.getBody());
        verify(customerService, times(1)).sendAlert(customerDtoList);
    }

    @Test
    void updatePendingSuccess() {
        List<CustomerDto> customerDtoList = new ArrayList<>();

        when(securityUtils.getUserId()).thenReturn(100L);
        when(customerService.updatePending(anyLong())).thenReturn(customerDtoList);

        ResponseEntity<List<CustomerDto>> response = customerController.updatePending();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDtoList, response.getBody());

        verify(securityUtils, times(1)).getUserId();
        verify(customerService, times(1)).updatePending(100L);

    }
}








