package com.HostelManagement.HM.controller;

import com.HostelManagement.HM.config.SecurityUtils;
import com.HostelManagement.HM.dto.CustomerDto;
import com.HostelManagement.HM.dto.MessageDto;
import com.HostelManagement.HM.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService  customerService;

    private final SecurityUtils securityUtils;

    @PostMapping("/customers")
    public ResponseEntity<CustomerDto> addCustomer(@RequestBody CustomerDto customerDto){
        long userId = securityUtils.getUserId();
        customerDto.setUserId(userId);
        return ResponseEntity.ok(customerService.addCustomer(customerDto));
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerDto> editCustomer(
            @RequestBody CustomerDto customerDto,
            @PathVariable int id){

        long userId = securityUtils.getUserId();
        return ResponseEntity.ok(customerService.editCustomer(customerDto, id, userId));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @PathVariable int id){

        long userId = securityUtils.getUserId();
        CustomerDto customer = customerService.getCustomerById(id,userId);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDto>> getAllCustomer(
            //index - of start page
            @RequestParam(defaultValue = "0") int page,
            //Records per page 5
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortAs){

        long userId = securityUtils.getUserId();
        List<CustomerDto> customerDtoList = customerService.getAllCustomer(userId,page,size,sortAs,sortBy);
        return ResponseEntity.ok(customerDtoList);
    }

    @GetMapping("/alert/{days}")
    public ResponseEntity<List<CustomerDto>> sendAlertBefore(
            @PathVariable int days){

        long userId = securityUtils.getUserId();
        List<CustomerDto>  filteredResults = customerService.sendAlertBefore(userId,days);
        return ResponseEntity.ok(filteredResults);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable int id){

        long userId = securityUtils.getUserId();
        customerService.deleteCustomer(userId, id);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/customers/{id}/sms")
    public ResponseEntity<MessageDto> messageCustomer(
            @PathVariable int id ,
            @RequestBody MessageDto messageDto){

        long userId = securityUtils.getUserId();
        return ResponseEntity.ok(customerService.messageCustomer(id,userId, messageDto));

    }


    @PatchMapping("/customers/{id}")
    public ResponseEntity<CustomerDto> patchCustomer(
            @PathVariable int id,
            @RequestBody CustomerDto customerDto){

            long userId = securityUtils.getUserId();
            return ResponseEntity.ok(customerService.patchCustomer(id, userId, customerDto));
    }

    @GetMapping("/customers/search")
    public ResponseEntity<List<CustomerDto>> searchCustomer(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "ASC") String sortAs,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        long userId = securityUtils.getUserId();
        return ResponseEntity.ok(customerService.searchKeyword(keyword,sortAs,sortBy,page,size,userId));
    }

    @PostMapping("/customers/alert")
    public ResponseEntity<List<CustomerDto>> makeAlert(@RequestBody List<CustomerDto> customerDtoList){
        return ResponseEntity.ok(customerService.sendAlert(customerDtoList));
    }

    @GetMapping("/customers/rent-pending")
    public ResponseEntity<List<CustomerDto>> updatePending(){
        long userId = securityUtils.getUserId();
        return ResponseEntity.ok(customerService.updatePending(userId));
    }
}
