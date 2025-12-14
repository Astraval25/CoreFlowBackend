package com.astraval.coreflow.modules.customer.facade;

import java.util.List;

import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.dto.UpdateCustomerRequest;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

public interface CustomerFacade {
    CustomerProjection createCustomer(Integer companyId, CreateCustomerRequest request);
    List<CustomerProjection> getAllCustomers(Integer companyId);
    CustomerProjection getCustomerById(Integer companyId, Long customerId);
    CustomerProjection updateCustomer(Integer companyId, Long customerId, UpdateCustomerRequest request);
    void deactivateCustomer(Integer companyId, Long customerId);
}