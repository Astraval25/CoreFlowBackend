package com.astraval.coreflow.modules.customer.facade;

import com.astraval.coreflow.modules.customer.Customers;
import java.util.List;

public interface CustomerFacade {
    Customers getCustomerById(Integer customerId);
    List<Customers> getCustomersByCompanyId(Integer companyId);
}