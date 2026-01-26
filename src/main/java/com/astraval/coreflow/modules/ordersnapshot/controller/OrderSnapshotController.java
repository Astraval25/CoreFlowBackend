package com.astraval.coreflow.modules.ordersnapshot.controller;

// import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.service.OrderSnapshotService;

// import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/companies")
public class OrderSnapshotController {

    @Autowired
    private OrderSnapshotService orderSnapshotService;

    @GetMapping("/{companyId}/orders/snapshot/{orderId}") // View Order Snapshot by Order id
    private ApiResponse<OrderSnapshot> viewOrderSnapshotByOrderId(@PathVariable Long companyId,
            @PathVariable Long orderId) {
        try {
            OrderSnapshot orderSnapshot = orderSnapshotService.getOrderSnapshotByOrderId(companyId, orderId);
            return ApiResponseFactory.accepted(orderSnapshot, "Order retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
    
    @GetMapping("/{companyId}/orders/snapshot") // Get Order Snapshot by order reference and status
    public ApiResponse<OrderSnapshot> getSnapshotByOrderReferenceAndStatus(
            @PathVariable Long companyId,
            @RequestParam Long orderReference,
            @RequestParam String status) {
        try {
            OrderSnapshot orderSnapshot = orderSnapshotService.getSnapshotByOrderReferenceAndStatus(companyId, orderReference, status);
            return ApiResponseFactory.accepted(orderSnapshot, "Order snapshot retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // @PutMapping("/{companyId}/orders/snapshot/{orderId}") // Update Order Snapshot by Order id
    // private ApiResponse<Map<String, Long>> updateOrderSnapshotByOrderId(@PathVariable Long companyId,
    //         @PathVariable Long orderId) {
    //     try {
    //         return ApiResponseFactory.created(
    //                 Map.of("orderId", orderId),
    //                 "Order Updated successfully");
    //     } catch (RuntimeException e) {
    //         return ApiResponseFactory.error(e.getMessage(), 406);
    //     }
    // }

    // @DeleteMapping("/{companyId}/orders/snapshot/{orderId}") // Delete Order by Order id
    // public ApiResponse<String> deleteOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
    //     try {
    //         orderSnapshotService.deleteOrder(companyId, orderId);
    //         return ApiResponseFactory.accepted("Order deleted successfully", "Order deleted successfully");
    //     } catch (RuntimeException e) {
    //         return ApiResponseFactory.error(e.getMessage(), 406);
    //     }
    // }

    // @PutMapping("/{companyId}/orders/snapshot/{orderId}/deactivate") // Deactivate Order by Order id
    // public ApiResponse<String> deactivateOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
    //     try {
    //         orderSnapshotService.deactivateOrder(companyId, orderId);
    //         return ApiResponseFactory.accepted("Order deactivated successfully", "Order deactivated successfully");
    //     } catch (RuntimeException e) {
    //         return ApiResponseFactory.error(e.getMessage(), 406);
    //     }
    // }

    // @PutMapping("/{companyId}/orders/snapshot/{orderId}/activate")
    // public ApiResponse<String> activateOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
    //     try {
    //         orderSnapshotService.activateOrder(companyId, orderId);
    //         return ApiResponseFactory.accepted("Order activated successfully", "Order activated successfully");
    //     } catch (RuntimeException e) {
    //         return ApiResponseFactory.error(e.getMessage(), 406);
    //     }
    // }
}
