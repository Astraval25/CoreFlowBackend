package com.astraval.coreflow.modules.items;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.items.dto.VendorItemDetailDto;
import com.astraval.coreflow.modules.items.dto.VendorItemSummaryDto;

@RestController
@RequestMapping("/api/companies")
public class VendorItemController {

    @Autowired
    private VendorItemService vendorItemService;

    @GetMapping("/{companyId}/vendors/{vendorId}/items")
    public ApiResponse<List<VendorItemSummaryDto>> getItemsByVendor(
            @PathVariable Long companyId,
            @PathVariable Long vendorId) {
        try {
            List<VendorItemSummaryDto> items = vendorItemService.getItemsByVendor(companyId, vendorId);
            return ApiResponseFactory.accepted(items, "Vendor items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/vendors/{vendorId}/items/active")
    public ApiResponse<List<VendorItemSummaryDto>> getActiveItemsByVendor(
            @PathVariable Long companyId,
            @PathVariable Long vendorId) {
        try {
            List<VendorItemSummaryDto> items = vendorItemService.getActiveItemsByVendor(companyId, vendorId);
            return ApiResponseFactory.accepted(items, "Vendor active items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/vendors/{vendorId}/items/{itemId}")
    public ApiResponse<VendorItemDetailDto> getVendorItemDetail(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @PathVariable Long itemId) {
        try {
            VendorItemDetailDto item = vendorItemService.getItemDetail(companyId, vendorId, itemId);
            return ApiResponseFactory.accepted(item, "Vendor item retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
}
