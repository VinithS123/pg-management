package com.HostelManagement.HM.service;

import com.HostelManagement.HM.model.CustomerModel;
import com.HostelManagement.HM.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryCheckService {

    @Autowired
    CustomerRepo repo;

    public Iterable<CustomerModel> getAllCustomer(int page, int size) {
        return repo.findAll(PageRequest.of(page,size));
    }


    public List<CustomerModel> getAllByCustomerName(String name) {
        return repo.findAllByName(name);
    }

    public List<CustomerModel> getAllCustomerByPartialName(String name) {
        return repo.findByNameHaving(name);
    }
}
