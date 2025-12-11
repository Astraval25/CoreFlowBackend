package com.astraval.coreflow.modules.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerController {

  @Autowired
  private CustomerService customerService;

  @PostMapping
  public ApiResponse<CustomerProjection> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
    try {
      CustomerProjection customer = customerService.createCustomer(request);
      return ApiResponseFactory.accepted(customer, "Customer created successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
}
