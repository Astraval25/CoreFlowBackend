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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.items.dto.CreateUpdateItemDto;
import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Create item
    @PostMapping("/{companyId}/items")
    public ApiResponse<Map<String, Long>> createItem(@PathVariable Long companyId,
            @Valid @RequestBody CreateUpdateItemDto request) {
        try {
            Long itemId = itemService.createItem(companyId, request);
            return ApiResponseFactory.created(
                    Map.of("itemId", itemId),
                    "Item created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
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
    public ApiResponse<Items> getItemById(@PathVariable Long companyId, @PathVariable Long id) {
        try {
            Items item = itemService.getItemById(companyId, id);
            return ApiResponseFactory.accepted(item, "Item retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Update
    @PutMapping("/{companyId}/items/{id}")
    public ApiResponse<Items> updateItem(@PathVariable Long companyId, @PathVariable Long id,
            @Valid @RequestBody CreateUpdateItemDto request) {
        try {
            itemService.updateItem(companyId, id, request);
            return ApiResponseFactory.updated(null, "Item updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
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
}