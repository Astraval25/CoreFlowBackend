package com.astraval.coreflow.modules.customer;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.customer.dto.CreateUpdateCustomerDto;
import com.astraval.coreflow.modules.customer.dto.CustomerSummaryDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Create
    @PostMapping("/{companyId}/customers")
    public ApiResponse<Map<String, Long>> createCustomer(@PathVariable Long companyId,
            @Valid @RequestBody CreateUpdateCustomerDto request) {
        try {
            Long customerId = customerService.createCustomer(companyId, request);
            return ApiResponseFactory.created(
                    Map.of("customerId", customerId),
                    "Customer created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Read
    @GetMapping("/customers") // get all customers (without filter)
    public ApiResponse<List<Customers>> getAllCustomers() {
        try {
            List<Customers> customers = customerService.getAllCustomers();
            return ApiResponseFactory.accepted(customers, "customers retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers") // get all customers (by companyId)
    public ApiResponse<List<CustomerSummaryDto>> getCustomersByCompany(@PathVariable Long companyId) {
        try {
            List<CustomerSummaryDto> customers = customerService.getCustomersByCompany(companyId);
            return ApiResponseFactory.accepted(customers, "customers retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers/active") // get all customers (by companyId & is_active - true)
    public ApiResponse<List<CustomerSummaryDto>> getActiveCustomersByCompany(@PathVariable Long companyId) {
        try {
            List<CustomerSummaryDto> customers = customerService.getActiveCustomersByCompany(companyId);
            return ApiResponseFactory.accepted(customers, "customers retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers/{id}") // get customer detail (check company id and customer id both)
    public ApiResponse<Customers> getCustomerById(@PathVariable Long companyId, @PathVariable Long id) {
        try {
            Customers customer = customerService.getCustomerById(companyId, id);
            return ApiResponseFactory.accepted(customer, "customer retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Update
    @PutMapping("/{companyId}/customers/{id}")
    public ApiResponse<Customers> updateCustomer(@PathVariable Long companyId, @PathVariable Long id,
            @Valid @RequestBody CreateUpdateCustomerDto request) {
        try {
            customerService.updateCustomer(companyId, id, request);
            return ApiResponseFactory.updated(null, "Customer updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{companyId}/customers/{id}/deactivate")
    public ApiResponse<String> deactivateCustomer(@PathVariable Long id) {
        try {
            customerService.deactivateCustomer(id);
            return ApiResponseFactory.updated(null, "Customer deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PatchMapping("/{companyId}/customers/{id}/activate")
    public ApiResponse<String> activateCustomer(@PathVariable Long id) {
        try {
            customerService.activateCustomer(id);
            return ApiResponseFactory.updated(null, "Customer activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Delete
    @DeleteMapping("/{companyId}/customers/{id}")
    public ApiResponse<String> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ApiResponseFactory.deleted("Customer deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
}
