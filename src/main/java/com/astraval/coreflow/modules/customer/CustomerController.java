package com.astraval.coreflow.modules.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import java.util.List;

import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.dto.UpdateCustomerRequest;
import com.astraval.coreflow.modules.customer.facade.CustomerFacade;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerController {

  @Autowired
  private CustomerFacade customerFacade;

  @PostMapping
  public ApiResponse<CustomerProjection> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
    try {
      CustomerProjection customer = customerFacade.createCustomer(request);
      return ApiResponseFactory.accepted(customer, "Customer created successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @GetMapping
  public ApiResponse<List<CustomerProjection>> getAllCustomers() {
    try {
      List<CustomerProjection> customers = customerFacade.getAllCustomers();
      return ApiResponseFactory.accepted(customers, "Customers retrieved successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @PutMapping("/{customerId}")
  public ApiResponse<CustomerProjection> updateCustomer(@PathVariable Long customerId, @Valid @RequestBody UpdateCustomerRequest request) {
    try {
      CustomerProjection customer = customerFacade.updateCustomer(customerId, request);
      return ApiResponseFactory.accepted(customer, "Customer updated successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @DeleteMapping("/{customerId}")
  public ApiResponse<Void> deactivateCustomer(@PathVariable Long customerId) {
    try {
      customerFacade.deactivateCustomer(customerId);
      return ApiResponseFactory.accepted(null, "Customer deactivated successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
}
