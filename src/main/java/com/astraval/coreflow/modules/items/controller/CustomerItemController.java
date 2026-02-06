package com.astraval.coreflow.modules.items.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.astraval.coreflow.modules.items.dto.CreateCustomerItemDto;
import com.astraval.coreflow.modules.items.dto.CustomerItemDetailDto;
import com.astraval.coreflow.modules.items.dto.CustomerItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateCustomerItemDto;
import com.astraval.coreflow.modules.items.service.CustomerItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CustomerItemController {

    @Autowired
    private CustomerItemService customerItemService;

    @PostMapping("/{companyId}/customers/{customerId}/items")
    public ApiResponse<Map<String, Long>> createCustomerItem(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @Valid @RequestBody CreateCustomerItemDto request) {
        try {
            Long itemCustomerPriceId = customerItemService.createCustomerItem(companyId, customerId, request);
            return ApiResponseFactory.created(
                    Map.of("itemCustomerPriceId", itemCustomerPriceId),
                    "Customer item created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers/{customerId}/items")
    public ApiResponse<List<CustomerItemSummaryDto>> getItemsByCustomer(
            @PathVariable Long companyId,
            @PathVariable Long customerId) {
        try {
            List<CustomerItemSummaryDto> items = customerItemService.getItemsByCustomer(companyId, customerId);
            return ApiResponseFactory.accepted(items, "Customer items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers/{customerId}/items/active")
    public ApiResponse<List<CustomerItemSummaryDto>> getActiveItemsByCustomer(
            @PathVariable Long companyId,
            @PathVariable Long customerId) {
        try {
            List<CustomerItemSummaryDto> items = customerItemService.getActiveItemsByCustomer(companyId, customerId);
            return ApiResponseFactory.accepted(items, "Customer active items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers/{customerId}/items/mapped")
    public ApiResponse<List<CustomerItemSummaryDto>> getMappedItemsByCustomer(
            @PathVariable Long companyId,
            @PathVariable Long customerId) {
        try {
            List<CustomerItemSummaryDto> items = customerItemService.getMappedItemsByCustomer(companyId, customerId);
            return ApiResponseFactory.accepted(items, "Customer mapped items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/customers/{customerId}/items/{itemId}")
    public ApiResponse<CustomerItemDetailDto> getCustomerItemDetail(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @PathVariable Long itemId) {
        try {
            CustomerItemDetailDto item = customerItemService.getItemDetail(companyId, customerId, itemId);
            return ApiResponseFactory.accepted(item, "Customer item retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PutMapping("/{companyId}/customers/{customerId}/items/{itemId}")
    public ApiResponse<String> updateCustomerItem(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @PathVariable Long itemId,
            @RequestBody UpdateCustomerItemDto request) {
        try {
            customerItemService.updateCustomerItem(companyId, customerId, itemId, request);
            return ApiResponseFactory.updated(null, "Customer item updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{companyId}/customers/{customerId}/items/{itemId}/deactivate")
    public ApiResponse<String> deactivateCustomerItem(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @PathVariable Long itemId) {
        try {
            customerItemService.deactivateCustomerItem(companyId, customerId, itemId);
            return ApiResponseFactory.updated(null, "Customer item deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PatchMapping("/{companyId}/customers/{customerId}/items/{itemId}/activate")
    public ApiResponse<String> activateCustomerItem(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @PathVariable Long itemId) {
        try {
            customerItemService.activateCustomerItem(companyId, customerId, itemId);
            return ApiResponseFactory.updated(null, "Customer item activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
}
