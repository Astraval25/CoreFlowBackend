package com.astraval.coreflow.modules.items;

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
import com.astraval.coreflow.modules.items.dto.CreateVendorItemDto;
import com.astraval.coreflow.modules.items.dto.VendorItemDetailDto;
import com.astraval.coreflow.modules.items.dto.VendorItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateVendorItemDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class VendorItemController {

    @Autowired
    private VendorItemService vendorItemService;

    @PostMapping("/{companyId}/vendors/{vendorId}/items")
    public ApiResponse<Map<String, Long>> createVendorItem(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @Valid @RequestBody CreateVendorItemDto request) {
        try {
            Long itemVendorPriceId = vendorItemService.createVendorItem(companyId, vendorId, request);
            return ApiResponseFactory.created(
                    Map.of("itemVendorPriceId", itemVendorPriceId),
                    "Vendor item created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

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

    @GetMapping("/{companyId}/vendors/{vendorId}/items/mapped")
    public ApiResponse<List<VendorItemSummaryDto>> getMappedItemsByVendor(
            @PathVariable Long companyId,
            @PathVariable Long vendorId) {
        try {
            List<VendorItemSummaryDto> items = vendorItemService.getMappedItemsByVendor(companyId, vendorId);
            return ApiResponseFactory.accepted(items, "Vendor mapped items retrieved successfully");
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

    @PutMapping("/{companyId}/vendors/{vendorId}/items/{itemId}")
    public ApiResponse<String> updateVendorItem(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @PathVariable Long itemId,
            @RequestBody UpdateVendorItemDto request) {
        try {
            vendorItemService.updateVendorItem(companyId, vendorId, itemId, request);
            return ApiResponseFactory.updated(null, "Vendor item updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{companyId}/vendors/{vendorId}/items/{itemId}/deactivate")
    public ApiResponse<String> deactivateVendorItem(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @PathVariable Long itemId) {
        try {
            vendorItemService.deactivateVendorItem(companyId, vendorId, itemId);
            return ApiResponseFactory.updated(null, "Vendor item deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PatchMapping("/{companyId}/vendors/{vendorId}/items/{itemId}/activate")
    public ApiResponse<String> activateVendorItem(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @PathVariable Long itemId) {
        try {
            vendorItemService.activateVendorItem(companyId, vendorId, itemId);
            return ApiResponseFactory.updated(null, "Vendor item activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
}
