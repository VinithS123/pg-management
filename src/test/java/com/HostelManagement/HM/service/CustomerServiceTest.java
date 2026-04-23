package com.HostelManagement.HM.service;

import com.HostelManagement.HM.dto.CustomerDto;
import com.HostelManagement.HM.dto.MessageDto;
import com.HostelManagement.HM.enums.FeeStatus;
import com.HostelManagement.HM.exception.CustomerNotFoundException;
import com.HostelManagement.HM.mapper.CustomerMapper;
import com.HostelManagement.HM.model.CustomerModel;
import com.HostelManagement.HM.repository.CustomerRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    CustomerRepo customerRepo;

    @Mock
    TwilioService twilioService;

    @Mock
    CustomerMapper customerMapper;

    @InjectMocks
    CustomerService customerService;

    @Test
    void getCustomerByIdSuccess() {

        CustomerDto inputDto = CustomerDto.builder().build();
        CustomerModel customerModel = CustomerModel.builder().build();
        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.of(customerModel));
        when(customerMapper.toCustomerDto(customerModel)).thenReturn(inputDto);

        CustomerDto result = customerService.getCustomerById(10,100L);
        assertEquals(inputDto,result);

        verify(customerRepo).findByIdAndUserId(10,100L) ;
        verify(customerMapper).toCustomerDto(customerModel);

    }

    @Test
    void getCustomerByIdFailure(){
        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.empty());
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,()->{
            customerService.getCustomerById(10,100L);
        });
        assertEquals("Customer Not Found",exception.getMessage());
        verify(customerRepo).findByIdAndUserId(10,100L);
        verifyNoInteractions(customerMapper);
    }


    @Test
    void addCustomerSuccess() {

        CustomerDto inputDto = CustomerDto.builder().build();
        CustomerModel customerModel = CustomerModel.builder().build();
        CustomerModel editedModel = CustomerModel.builder().build();
        CustomerDto expectedDto = CustomerDto.builder().build();

        when(customerMapper.toCustomerDto(customerModel)).thenReturn(expectedDto);
        when(customerMapper.toCustomerModel(inputDto)).thenReturn(editedModel);

        when(customerRepo.save(editedModel)).thenReturn(customerModel);
        CustomerDto result  = customerService.addCustomer(inputDto);

        assertEquals(expectedDto,result);
        assertEquals(LocalDate.now(),editedModel.getJoinDate());

        verify(customerRepo).save(editedModel);
        verify(customerMapper).toCustomerDto(customerModel);
        verify(customerMapper).toCustomerModel(inputDto);

    }

    @Test
    void getAllCustomerSuccess_ASC() {

        List<CustomerModel> customerModelList = new ArrayList<>();
        List<CustomerDto> customerDtoList = new ArrayList<>();
        when(customerRepo.findByUserId(anyLong(),any(Pageable.class))).thenReturn(customerModelList);
        when(customerMapper.toCustomerDtoList(customerModelList)).thenReturn(customerDtoList);

        List<CustomerDto> result = customerService.getAllCustomer(100L,1,3,"ASC","id");

        assertEquals(customerDtoList,result);
        verify(customerRepo).findByUserId(anyLong(),any(Pageable.class));
        verify(customerMapper).toCustomerDtoList(customerModelList);
    }
    @Test
    void getAllCustomerSuccess_DESC() {

        List<CustomerModel> customerModelList = new ArrayList<>();
        List<CustomerDto> customerDtoList = new ArrayList<>();
        when(customerRepo.findByUserId(anyLong(),any(Pageable.class))).thenReturn(customerModelList);
        when(customerMapper.toCustomerDtoList(customerModelList)).thenReturn(customerDtoList);

        List<CustomerDto> result = customerService.getAllCustomer(100L,1,3,"DESC","id");

        assertEquals(customerDtoList,result);
        verify(customerRepo).findByUserId(anyLong(),any(Pageable.class));
        verify(customerMapper).toCustomerDtoList(customerModelList);
    }
    @Test
    void editCustomerSuccess() {

        CustomerDto inputDto = CustomerDto.builder().build();
        CustomerModel foundModel = CustomerModel.builder().build();
        CustomerModel savedModel = CustomerModel.builder().build();
        CustomerDto savedDto = CustomerDto.builder().id(10).userId(100L).build();

        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.of(foundModel));
        when(customerMapper.toCustomerDto(savedModel)).thenReturn(savedDto);
        when(customerRepo.save(foundModel)).thenReturn(savedModel);

        CustomerDto result = customerService.editCustomer(inputDto,10,100L);
        assertEquals(savedDto,result);
        assertEquals(10,inputDto.getId());
        assertEquals(100L,inputDto.getUserId());

        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerMapper).updateToCustomerModel(inputDto,foundModel);
        verify(customerRepo).save(foundModel);
        verify(customerMapper).toCustomerDto(savedModel);
    }

    @Test
    void editCustomerFailure() {
    CustomerDto inputDto = CustomerDto.builder().build();
    when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.empty());

    CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,()->{
        customerService.editCustomer(inputDto,10,100L);});

    assertEquals("Customer Not Found",exception.getMessage());
    verify(customerRepo).findByIdAndUserId(10,100L);
    verifyNoInteractions(customerMapper);
    }

    @Test
    void sendAlertBeforeSuccess() {

        CustomerModel matchingCustomer = CustomerModel.builder()
                .name("Ramesh")
                .feeStatus(FeeStatus.PENDING)
                .joinDate(LocalDate.now().plusDays(2).minusMonths(2)).build();

        List<CustomerModel> foundModelList = List.of(matchingCustomer);

        CustomerDto matchingCustomerDto = CustomerDto.builder().build();

        List<CustomerDto> expectedDtoList = List.of(matchingCustomerDto);
        when(customerRepo.findByUserId(100L)).thenReturn(foundModelList);
        when(customerMapper.toCustomerDto(matchingCustomer)).thenReturn(matchingCustomerDto);


        List<CustomerDto> result = customerService.sendAlertBefore(100L,2);

        assertEquals(expectedDtoList,result);
        verify(twilioService).sendCustomizeMessage(any(CustomerDto.class),anyString());
        verify(customerMapper).toCustomerDto(matchingCustomer);
        verify(customerRepo).findByUserId(100L);
    }

    @Test
    void deleteCustomerSuccess() {
        CustomerModel customerModel = CustomerModel.builder().build();
        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.of(customerModel));

        customerService.deleteCustomer(100L,10);

        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerRepo).delete(customerModel);
    }

    @Test
    void deleteCustomerFailure() {
        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                customerService.deleteCustomer(100L,10));

        assertEquals("Customer Not Found",exception.getMessage());
        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerRepo, never()).delete(any(CustomerModel.class));
    }

    @Test
    void messageCustomerSuccess() {
        MessageDto messageDto = new MessageDto();
        messageDto.setMessage("Pay hostel rent");
        CustomerModel customerModel = CustomerModel.builder().build();
        CustomerDto customerDto = CustomerDto.builder().build();

        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.of(customerModel));
        when(customerMapper.toCustomerDto(customerModel)).thenReturn(customerDto);

        MessageDto result = customerService.messageCustomer(10,100L,messageDto);

        assertEquals("Successful",result.getStatus());
        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerMapper).toCustomerDto(customerModel);
        verify(twilioService).sendCustomizeMessage(customerDto,"Pay hostel rent");
    }

    @Test
    void patchCustomerSuccess() {
        CustomerDto patchDto = CustomerDto.builder().name("updated").build();
        CustomerModel foundModel = CustomerModel.builder().build();
        CustomerModel savedModel = CustomerModel.builder().build();
        CustomerDto expectedDto = CustomerDto.builder().name("updated").build();

        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.of(foundModel));
        when(customerRepo.save(foundModel)).thenReturn(savedModel);
        when(customerMapper.toCustomerDto(savedModel)).thenReturn(expectedDto);

        CustomerDto result = customerService.patchCustomer(10,100L,patchDto);

        assertEquals(expectedDto,result);
        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerMapper).patchToCustomerModel(patchDto,foundModel);
        verify(customerRepo).save(foundModel);
        verify(customerMapper).toCustomerDto(savedModel);
    }

    @Test
    void searchKeywordSuccess() {
        List<CustomerModel> customerModelList = new ArrayList<>();
        List<CustomerDto> customerDtoList = new ArrayList<>();

        when(customerRepo.findProductByKeyword(anyString(),anyLong(),any(Pageable.class))).thenReturn(customerModelList);
        when(customerMapper.toCustomerDtoList(customerModelList)).thenReturn(customerDtoList);

        List<CustomerDto> result = customerService.searchKeyword("abc","ASC","id",0,5,100L);

        assertEquals(customerDtoList,result);
        verify(customerRepo).findProductByKeyword(anyString(),anyLong(),any(Pageable.class));
        verify(customerMapper).toCustomerDtoList(customerModelList);
    }

    @Test
    void sendAlertSuccess() {
        CustomerDto pendingCustomer = CustomerDto.builder()
                .name("Ramesh")
                .feeStatus(FeeStatus.PENDING)
                .joinDate(LocalDate.now().plusDays(2))
                .build();
        CustomerDto paidCustomer = CustomerDto.builder()
                .name("Suresh")
                .feeStatus(FeeStatus.PAID)
                .joinDate(LocalDate.now().plusDays(2))
                .build();
        List<CustomerDto> inputList = List.of(pendingCustomer, paidCustomer);

        List<CustomerDto> result = customerService.sendAlert(inputList);

        assertEquals(1,result.size());
        assertEquals(pendingCustomer,result.get(0));
        verify(twilioService,times(1)).sendCustomizeMessage(any(CustomerDto.class),anyString());
    }

    @Test
    void messageCustomerFailure() {
        MessageDto messageDto = new MessageDto();
        messageDto.setMessage("Pay hostel rent");
        CustomerModel customerModel = CustomerModel.builder().build();
        CustomerDto customerDto = CustomerDto.builder().build();

        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.of(customerModel));
        when(customerMapper.toCustomerDto(customerModel)).thenReturn(customerDto);
        doThrow(new RuntimeException("Twilio failed")).when(twilioService)
                .sendCustomizeMessage(customerDto,"Pay hostel rent");

        MessageDto result = customerService.messageCustomer(10,100L,messageDto);

        assertEquals("Unsuccessful",result.getStatus());
        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerMapper).toCustomerDto(customerModel);
        verify(twilioService).sendCustomizeMessage(customerDto,"Pay hostel rent");
    }

    @Test
    void messageCustomerCustomerNotFound() {
        MessageDto messageDto = new MessageDto();
        messageDto.setMessage("Pay hostel rent");
        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                customerService.messageCustomer(10,100L,messageDto));

        assertEquals("Customer Not Found",exception.getMessage());
        verify(customerRepo).findByIdAndUserId(10,100L);
        verifyNoInteractions(customerMapper,twilioService);
    }

    @Test
    void patchCustomerFailure() {
        CustomerDto patchDto = CustomerDto.builder().build();
        when(customerRepo.findByIdAndUserId(10,100L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                customerService.patchCustomer(10,100L,patchDto));

        assertEquals("Customer Not Found",exception.getMessage());
        verify(customerRepo).findByIdAndUserId(10,100L);
        verify(customerMapper,never()).patchToCustomerModel(any(CustomerDto.class),any(CustomerModel.class));
    }

    @Test
    void updatePendingSuccess() {
        CustomerModel pendingCustomer = CustomerModel.builder()
                .joinDate(LocalDate.now().minusMonths(1))
                .feeStatus(FeeStatus.PAID)
                .build();
        CustomerModel nonPendingCustomer = CustomerModel.builder()
                .joinDate(LocalDate.now())
                .feeStatus(FeeStatus.PAID)
                .build();

        CustomerDto pendingCustomerDto = CustomerDto.builder()
                .feeStatus(FeeStatus.PENDING)
                .build();

        when(customerRepo.findByUserId(100L)).thenReturn(List.of(pendingCustomer,nonPendingCustomer));
        when(customerMapper.toCustomerDto(pendingCustomer)).thenReturn(pendingCustomerDto);

        List<CustomerDto> result = customerService.updatePending(100L);

        assertEquals(1,result.size());
        assertEquals(pendingCustomerDto,result.get(0));
        assertEquals(FeeStatus.PENDING,pendingCustomer.getFeeStatus());
        verify(customerRepo).findByUserId(100L);
        verify(customerRepo).save(pendingCustomer);
        verify(customerMapper).toCustomerDto(pendingCustomer);
        verify(customerRepo,never()).save(nonPendingCustomer);
    }


}
