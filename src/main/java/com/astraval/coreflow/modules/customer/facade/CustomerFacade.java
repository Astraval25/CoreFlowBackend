package com.astraval.coreflow.modules.customer.facade;

import java.util.List;

import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.dto.UpdateCustomerRequest;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

public interface CustomerFacade {
    CustomerProjection createCustomer(CreateCustomerRequest request);
    List<CustomerProjection> getAllCustomers();
    CustomerProjection updateCustomer(Long customerId, UpdateCustomerRequest request);
    void deactivateCustomer(Long customerId);
}