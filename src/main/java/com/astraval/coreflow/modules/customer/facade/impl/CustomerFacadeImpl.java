package com.astraval.coreflow.modules.customer.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.astraval.coreflow.modules.customer.CustomerService;
import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.facade.CustomerFacade;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

@Service
public class CustomerFacadeImpl implements CustomerFacade {

    @Autowired
    private CustomerService customerService;

    @Override
    public CustomerProjection createCustomer(CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @Override
    public List<CustomerProjection> getAllCustomers() {
        return customerService.getAllCustomers();
    }
}