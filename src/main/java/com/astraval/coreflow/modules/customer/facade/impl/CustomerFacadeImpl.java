package com.astraval.coreflow.modules.customer.facade.impl;

import com.astraval.coreflow.modules.customer.facade.CustomerFacade;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerFacadeImpl implements CustomerFacade {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customers getCustomerById(Integer customerId) {
        return customerRepository.findById(Long.valueOf(customerId)).orElse(null);
    }

    @Override
    public List<Customers> getCustomersByCompanyId(Integer companyId) {
        return customerRepository.findAll();
    }
}