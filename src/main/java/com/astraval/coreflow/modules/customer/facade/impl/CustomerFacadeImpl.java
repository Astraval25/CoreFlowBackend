package com.astraval.coreflow.modules.customer.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.astraval.coreflow.modules.customer.CustomerService;
import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.dto.UpdateCustomerRequest;
import com.astraval.coreflow.modules.customer.facade.CustomerFacade;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

@Service
public class CustomerFacadeImpl implements CustomerFacade {

    @Autowired
    private CustomerService customerService;

    @Override
    public CustomerProjection createCustomer(Integer companyId, CreateCustomerRequest request) {
        return customerService.createCustomer(companyId, request);
    }

    @Override
    public List<CustomerProjection> getAllCustomers(Integer companyId) {
        return customerService.getAllCustomers(companyId);
    }

    @Override
    public CustomerProjection getCustomerById(Integer companyId, Long customerId) {
        return customerService.getCustomerById(companyId, customerId);
    }

    @Override
    public CustomerProjection updateCustomer(Integer companyId, Long customerId, UpdateCustomerRequest request) {
        return customerService.updateCustomer(companyId, customerId, request);
    }

    @Override
    public void deactivateCustomer(Integer companyId, Long customerId) {
        customerService.deactivateCustomer(companyId, customerId);
    }
}