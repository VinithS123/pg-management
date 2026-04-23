package com.HostelManagement.HM.service;

import com.HostelManagement.HM.model.CustomerModel;
import com.HostelManagement.HM.repository.CustomerRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryCheckServiceTest {

    @Mock
    CustomerRepo repo;

    @InjectMocks
    RepositoryCheckService repositoryCheckService;

    @Test
    void getAllCustomerSuccess() {
        List<CustomerModel> customerModelList = new ArrayList<>();
        Page<CustomerModel> pageResult = new PageImpl<>(customerModelList);
        when(repo.findAll(any(Pageable.class))).thenReturn(pageResult);

        Iterable<CustomerModel> result = repositoryCheckService.getAllCustomer(0,5);

        assertEquals(pageResult,result);
        verify(repo,times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getAllByCustomerNameSuccess() {
        List<CustomerModel> customerModelList = new ArrayList<>();
        when(repo.findAllByName("ramesh")).thenReturn(customerModelList);

        List<CustomerModel> result = repositoryCheckService.getAllByCustomerName("ramesh");

        assertEquals(customerModelList,result);
        verify(repo,times(1)).findAllByName("ramesh");
    }

    @Test
    void getAllCustomerByPartialNameSuccess() {
        List<CustomerModel> customerModelList = new ArrayList<>();
        when(repo.findByNameHaving("ram")).thenReturn(customerModelList);

        List<CustomerModel> result = repositoryCheckService.getAllCustomerByPartialName("ram");

        assertEquals(customerModelList,result);
        verify(repo,times(1)).findByNameHaving("ram");
    }
}
