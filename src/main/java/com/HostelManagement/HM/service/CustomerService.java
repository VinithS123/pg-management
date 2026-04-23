package com.HostelManagement.HM.service;

import com.HostelManagement.HM.enums.FeeStatus;
import com.HostelManagement.HM.mapper.CustomerMapper;
import com.HostelManagement.HM.dto.CustomerDto;
import com.HostelManagement.HM.exception.CustomerNotFoundException;
import com.HostelManagement.HM.model.CustomerModel;
import com.HostelManagement.HM.dto.MessageDto;
import com.HostelManagement.HM.repository.CustomerRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@Getter
@RequiredArgsConstructor
public class CustomerService {


    //Should add admission proof message method
    private final CustomerRepo customerRepo;

    private final TwilioService twilioService;

    private final CustomerMapper customerMapper;


    public CustomerDto getCustomerById(int id, long userId){

        CustomerModel customerModel = customerRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()->new CustomerNotFoundException("Customer Not Found"));
        return customerMapper.toCustomerDto(customerModel);

    }

    public CustomerDto addCustomer(CustomerDto customerDto) {

            CustomerModel customerModel = customerMapper.toCustomerModel(customerDto);
            customerModel.setJoinDate(LocalDate.now());
            CustomerModel savedCustomer = customerRepo.save(customerModel);
            return customerMapper.toCustomerDto(savedCustomer);

    }


    public List<CustomerDto> getAllCustomer(long userId, int page, int size, String sortAs, String sortBy) {

        Sort sort = Sort.by(sortBy).ascending();

        if(sortAs.equalsIgnoreCase("DESC")){
            sort =Sort.by(sortBy).descending();
        }
        return customerMapper.toCustomerDtoList(customerRepo.findByUserId(userId, PageRequest.of(page,size,sort)));

    }

    public CustomerDto editCustomer(CustomerDto editedCustomerDto,int id, long userId) {

        CustomerModel customerModel = customerRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()->new CustomerNotFoundException("Customer Not Found"));
        editedCustomerDto.setId(id);
        editedCustomerDto.setUserId(userId);
        customerMapper.updateToCustomerModel(editedCustomerDto, customerModel);
        CustomerModel savedCustomerModel = customerRepo.save(customerModel);

        return customerMapper.toCustomerDto(savedCustomerModel);

    }

    //Send reminder before n days
    public List<CustomerDto> sendAlertBefore(long userId, int numberOfDays) {

        List<CustomerModel> customerModelList = customerRepo.findByUserId(userId);

        return customerModelList.stream()
                .filter(customer -> isEligibleForAlert(numberOfDays,customer))
                .map(customer->{
                    String simpleMessage = "Hello %s ! Time to pay your Hostel Rent, %d days remaining";
                    String body = String.format(simpleMessage, customer.getName(), numberOfDays);
                    CustomerDto filteredCustomerDto = customerMapper.toCustomerDto(customer);
                    twilioService.sendCustomizeMessage(filteredCustomerDto, body);
                    return filteredCustomerDto;})
                .toList();
    }

    public void deleteCustomer(long userId, int id) {
        CustomerModel customer = customerRepo.findByIdAndUserId(id,userId).orElseThrow(
                ()->new CustomerNotFoundException("Customer Not Found"));
        customerRepo.delete(customer);
    }

    public MessageDto messageCustomer(int id,long userId,MessageDto messageDto) {

        CustomerDto customer = getCustomerById(id,userId);
        try {
            twilioService.sendCustomizeMessage(customer, messageDto.message);
            messageDto.setStatus("Successful");
        }catch (Exception e) {
            messageDto.setStatus("Unsuccessful");
        }
        return messageDto;
    }

    public CustomerDto patchCustomer(int id, long userId, CustomerDto customerDto){

        CustomerModel customer = customerRepo.findByIdAndUserId(id,userId).orElseThrow(
                ()->new CustomerNotFoundException("Customer Not Found"));

        customerMapper.patchToCustomerModel(customerDto,customer);
        return customerMapper.toCustomerDto(customerRepo.save(customer));
    }

    public List<CustomerDto> searchKeyword(String keyword,
                                           String sortAs,
                                           String sortBy,
                                           int page,
                                           int size,
                                           long userId) {

        Sort sort = null;
        if(sortAs.equalsIgnoreCase("ASC")){
            sort=Sort.by(sortBy).ascending();
        }else{
            sort=Sort.by(sortBy).descending();
        }
        List<CustomerModel> customerModelList =
                customerRepo.findProductByKeyword(keyword,userId,PageRequest.of(page,size,sort));
        return customerMapper.toCustomerDtoList(customerModelList);
    }

    public List<CustomerDto> sendAlert(List<CustomerDto> customerDtoList) {

        return customerDtoList.stream()
                .filter(customerDto -> customerDto.getFeeStatus().equals(FeeStatus.PENDING))
                .map(customerDto -> {
                    String simpleMessage = "Hello %s ! Time to pay your Hostel Rent, %d days remaining";
                    int numberOfDays = customerDto.getJoinDate().getDayOfMonth() - LocalDate.now().getDayOfMonth();
                    String body = String.format(simpleMessage, customerDto.getName(), numberOfDays);
                    twilioService.sendCustomizeMessage(customerDto, body);
                    return customerDto;})
                .toList();
    }

    private boolean isEligibleForAlert(int numberOfDays, CustomerModel customer) {
        return (customer.getFeeStatus().equals(FeeStatus.PENDING))
                && ((customer.getJoinDate().getDayOfMonth()) == (LocalDate.now().plusDays(numberOfDays).getDayOfMonth()))
                && (!Objects.equals(customer.getJoinDate().plusDays(numberOfDays), LocalDate.now().plusDays(numberOfDays)));
    }


    public List<CustomerDto> updatePending(long userId) {
        List<CustomerModel> customerModels = customerRepo.findByUserId(userId);
        return customerModels.stream()
                .filter(this::isPending)
                .map(customer->{
                    customer.setFeeStatus(FeeStatus.PENDING);
                    customerRepo.save(customer);
                    return customerMapper.toCustomerDto(customer);})
                .toList();
    }

    private boolean isPending(CustomerModel customer){
        return customer.getJoinDate().getDayOfMonth() == LocalDate.now().getDayOfMonth() && ! customer.getJoinDate().equals(LocalDate.now());
    }
}
