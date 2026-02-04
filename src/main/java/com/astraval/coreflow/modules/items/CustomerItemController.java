package com.astraval.coreflow.modules.items;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.items.dto.CustomerItemDetailDto;
import com.astraval.coreflow.modules.items.dto.CustomerItemSummaryDto;

@RestController
@RequestMapping("/api/companies")
public class CustomerItemController {

    @Autowired
    private CustomerItemService customerItemService;

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
}
