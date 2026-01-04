package com.astraval.coreflow.modules.orderitemdetails;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;

@RestController
@RequestMapping("/api/companies")
public class OrderItemDetailsController {
  
    @Autowired
    private OrderItemDetailsService orderItemDetailsService;
    
    @PatchMapping("/{companyId}/orders/{orderId}/items/{orderItemId}/quantity")
    public ApiResponse<String> updateOrderItemQuantity(
            @PathVariable Long companyId,
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @RequestBody Map<String, Integer> quantityUpdate) {
        try {
            orderItemDetailsService.updateOrderItemQuantity(orderItemId, quantityUpdate.get("quantity"));
            return ApiResponseFactory.updated(null, "Order item quantity updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @PatchMapping("/{companyId}/orders/{orderId}/items/{orderItemId}/price")
    public ApiResponse<String> updateOrderItemPrice(
            @PathVariable Long companyId,
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @RequestBody Map<String, Double> priceUpdate) {
        try {
            orderItemDetailsService.updateOrderItemPrice(orderItemId, priceUpdate.get("price"));
            return ApiResponseFactory.updated(null, "Order item price updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
