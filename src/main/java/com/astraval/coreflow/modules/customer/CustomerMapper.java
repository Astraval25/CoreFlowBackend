package com.astraval.coreflow.modules.customer;

import org.springframework.stereotype.Component;
import com.astraval.coreflow.modules.customer.dto.CustomerDetailDto;

@Component
public class CustomerMapper {

  public CustomerDetailDto toDetailDto(Customers customer) {
    return new CustomerDetailDto(
      customer.getCustomerId(),
      customer.getCustomerName(),
      customer.getDisplayName(),
      customer.getEmail(),
      customer.getPhone(),
      customer.getLang(),
      customer.getPan(),
      customer.getGst(),
      customer.getAdvanceAmount(),
      customer.getIsActive(),
      customer.getCreatedDt()
    );
  }
}
