package com.astraval.coreflow.modules.items;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.items.dto.CreateItemDto;
import com.astraval.coreflow.modules.items.dto.GetOrderItemsDto;
import com.astraval.coreflow.modules.items.dto.ItemDetailDto;
import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.UpdateItemDto;
import com.astraval.coreflow.modules.items.dto.PurchasableItemDto;
import com.astraval.coreflow.modules.items.dto.SellableItemDto;
import com.astraval.coreflow.modules.items.model.Items;


@RestController
@RequestMapping("/api/companies")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Create item
    @PostMapping(value = "/{companyId}/items", consumes = {"multipart/form-data"})
    public ApiResponse<Map<String, Long>> createItem(
            @PathVariable Long companyId,
            @RequestParam("item") String itemJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            CreateItemDto request = mapper.readValue(itemJson, CreateItemDto.class);
            
            Long itemId = itemService.createItem(companyId, request, file);
            return ApiResponseFactory.created(
                    Map.of("itemId", itemId),
                    "Item created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        } catch (Exception e) {
            return ApiResponseFactory.error("Failed to process request: " + e.getMessage(), 400);
        }
    }

    // Read
    @GetMapping("/items") // get all items (without filter)
    public ApiResponse<List<Items>> getAllItems() {
        try {
            List<Items> items = itemService.getAllItems();
            return ApiResponseFactory.accepted(items, "Items retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/items") // get all items (by companyId)
    public ApiResponse<List<ItemSummaryDto>> getItemsByCompany(@PathVariable Long companyId) {
        try {
            List<ItemSummaryDto> items = itemService.getItemsByCompany(companyId);
            return ApiResponseFactory.accepted(items, "Items retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/items/active") // get all items (by companyId & is_active - true)
    public ApiResponse<List<ItemSummaryDto>> getActiveItemsByCompany(@PathVariable Long companyId) {
        try {
            List<ItemSummaryDto> items = itemService.getActiveItemsByCompany(companyId);
            return ApiResponseFactory.accepted(items, "Items retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/items/{id}") // get item detail (check company id and item id both)
    public ApiResponse<ItemDetailDto> getItemById(@PathVariable Long companyId, @PathVariable Long id) {
        try {
            ItemDetailDto item = itemService.getItemDetail(companyId, id);
            return ApiResponseFactory.accepted(item, "Item retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Update
    @PutMapping(value = "/{companyId}/items/{id}", consumes = {"multipart/form-data"})
    public ApiResponse<Items> updateItem(
            @PathVariable Long companyId, 
            @PathVariable Long id,
            @RequestParam("item") String itemJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            UpdateItemDto request = mapper.readValue(itemJson, UpdateItemDto.class);
            
            itemService.updateItem(companyId, id, request, file);
            return ApiResponseFactory.updated(null, "Item updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        } catch (Exception e) {
            return ApiResponseFactory.error("Failed to process request: " + e.getMessage(), 400);
        }
    }

    @PatchMapping("/{companyId}/items/{id}/deactivate")
    public ApiResponse<String> deactivateItem(@PathVariable Long id) {
        try {
            itemService.deactivateItem(id);
            return ApiResponseFactory.updated(null, "Item deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PatchMapping("/{companyId}/items/{id}/activate")
    public ApiResponse<String> activateItem(@PathVariable Long id) {
        try {
            itemService.activateItem(id);
            return ApiResponseFactory.updated(null, "Item activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Delete
    @DeleteMapping("/{companyId}/items/{id}")
    public ApiResponse<String> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ApiResponseFactory.deleted("Item deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
    
    @GetMapping("/{companyId}/customers/{customerId}/items/sellable")
    public ApiResponse<List<SellableItemDto>> getSellableItemsByCompanyAndCustomer(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @RequestParam("customerCompanyId") Long customerCompanyId) {
        try {
            List<SellableItemDto> items = itemService.getSellableItemsByCompanyAndCustomer(
                    companyId, customerId, customerCompanyId);
            return ApiResponseFactory.accepted(items, "Sellable items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @GetMapping("/{companyId}/vendors/{vendorId}/items/purchasable")
    public ApiResponse<List<PurchasableItemDto>> getPurchasableItemsByCompanyAndVendor(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @RequestParam("vendorCompanyId") Long vendorCompanyId) {
        try {
            List<PurchasableItemDto> items = itemService.getPurchasableItemsByCompanyAndVendor(
                    companyId, vendorId, vendorCompanyId);
            return ApiResponseFactory.accepted(items, "Purchasable items retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
