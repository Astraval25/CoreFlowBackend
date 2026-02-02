package com.astraval.coreflow.modules.orderdetails.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.orderdetails.OrderStatus;
import com.astraval.coreflow.modules.orderdetails.dto.OrderDetailsWithItems;
import com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto;
import com.astraval.coreflow.modules.orderdetails.service.OrderDetailsService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/companies")
public class OrderDetailsController {
  
    @Autowired
    private OrderDetailsService orderDetailsService;
    
    @GetMapping("/{companyId}/orders/{orderId}") // View Order details by Order id
    private ApiResponse<OrderDetailsWithItems> viewOrderDetailsByOrderId(@PathVariable Long companyId,
            @PathVariable Long orderId) {
        try {
            OrderDetailsWithItems orderDetailsWithItems = orderDetailsService.getOrderDetailsWithItemsByOrderId(companyId, orderId);
            return ApiResponseFactory.accepted(orderDetailsWithItems, "Order retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}") // Update Order details by Order id
    private ApiResponse<Map<String, Long>> updateOrderDetailsByOrderId(@PathVariable Long companyId,
            @PathVariable Long orderId) {
        try {
            return ApiResponseFactory.created(
                    Map.of("orderId", orderId),
                    "Order Updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @DeleteMapping("/{companyId}/orders/{orderId}") // Delete Order by Order id
    public ApiResponse<String> deleteOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.deleteOrder(companyId, orderId);
            return ApiResponseFactory.accepted("Order deleted successfully", "Order deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/deactivate") // Deactivate Order by Order id
    public ApiResponse<String> deactivateOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.deactivateOrder(companyId, orderId);
            return ApiResponseFactory.accepted("Order deactivated successfully", "Order deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/activate")
    public ApiResponse<String> activateOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.activateOrder(companyId, orderId);
            return ApiResponseFactory.accepted("Order activated successfully", "Order activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    // Update Order Status.
    @PutMapping("/{companyId}/orders/{orderId}/sales-order")
    public ApiResponse<String> convertToSalesOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatusWithOrderSnapshot(companyId, orderId, OrderStatus.getOrder());
            return ApiResponseFactory.accepted("Order confirmed successfully", "Order confirmed successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/viewed")
    public ApiResponse<String> viewedOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getOrderViewed());
            return ApiResponseFactory.accepted("Order status changed to viewed.", "Order status changed to viewed.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @GetMapping("/{companyId}/vendor/{vendorId}/unpaid-orders")
    public ApiResponse<List<UnpaidOrderDto>> getUnpaidCompanyOrdersByVendor(@PathVariable Long companyId,
            @PathVariable Long vendorId) {
        try {
            List<UnpaidOrderDto> unpaidOrders = orderDetailsService.getUnpaidOrdersByBuyerCompanyIdAndVendorId(companyId, vendorId);
            return ApiResponseFactory.accepted(unpaidOrders, "Unpaid orders retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    // Quotation Status APIs
    @PutMapping("/{companyId}/orders/{orderId}/quotation")
    public ApiResponse<String> createQuotation(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getQuotation());
            return ApiResponseFactory.accepted("Order status changed to quotation.",
                    "Order status changed to quotation.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/quotation-viewed")
    public ApiResponse<String> quotationViewed(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getQuotationViewed());
            return ApiResponseFactory.accepted("Quotation marked as viewed.", "Quotation marked as viewed.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/quotation-accepted")
    public ApiResponse<String> acceptQuotation(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getQuotationAccepted());
            return ApiResponseFactory.accepted("Quotation accepted successfully.", "Quotation accepted successfully.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/quotation-declined")
    public ApiResponse<String> declineQuotation(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getQuotationDecline());
            return ApiResponseFactory.accepted("Quotation declined.", "Quotation declined.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/invoiced")
    public ApiResponse<String> invoiceOrder(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getOrderInvoiced());
            return ApiResponseFactory.accepted("Order invoiced successfully.", "Order invoiced successfully.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/orders/{orderId}/paid")
    public ApiResponse<String> markOrderPaid(@PathVariable Long companyId, @PathVariable Long orderId) {
        try {
            orderDetailsService.updateOrderStatus(companyId, orderId, OrderStatus.getOrderPayed());
            return ApiResponseFactory.accepted("Order marked as paid.", "Order marked as paid.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
