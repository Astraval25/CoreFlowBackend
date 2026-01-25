package com.astraval.coreflow.modules.orderdetails.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.orderdetails.dto.CreatePurchaseOrder;
import com.astraval.coreflow.modules.orderdetails.dto.PurchaseOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.service.PurchaseOrderDetailsService;

import com.astraval.coreflow.modules.orderdetails.dto.UpdatePurchaseOrder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class PurchaseOrderDetailsController {

  @Autowired
  PurchaseOrderDetailsService purchaseOrderDetailsService;

  @PostMapping("/{companyId}/purchase/orders") // Create New Purchase Order
  public ApiResponse<Map<String, Long>> createOrder(
          @PathVariable Long companyId,
          @Valid @RequestBody CreatePurchaseOrder createOrder) {
      try {
          Long orderId = purchaseOrderDetailsService.createPurchaseOrder(companyId, createOrder);
          return ApiResponseFactory.created(
                  Map.of("orderId", orderId),
                  "Purchase order created successfully");
      } catch (RuntimeException e) {
          return ApiResponseFactory.error(e.getMessage(), 406);
      }
  }

  @GetMapping("/{companyId}/purchase/orders") // List All Purchase Order by Company Id
  private ApiResponse<List<PurchaseOrderSummaryDto>> getOrderSummaryByCompanyId(@PathVariable Long companyId,
      PurchaseOrderSummaryDto orderSummaryDto) {
    try {
      List<PurchaseOrderSummaryDto> result = purchaseOrderDetailsService.getOrderSummaryByCompanyId(companyId);
      return ApiResponseFactory.accepted(result, "Order retrieved successfully");
    } catch (Exception e) {
      return ApiResponseFactory.error(e.getMessage(), 406);
    }
  }

  @PutMapping("/{companyId}/purchase/orders/{orderId}") // Update Purchase Order details by Order id
  public ApiResponse<Map<String, Long>> updateOrderDetailsByOrderId(@PathVariable Long companyId,
          @PathVariable Long orderId, @Valid @RequestBody UpdatePurchaseOrder updateOrder) {
      try {
          purchaseOrderDetailsService.updatePurchaseOrder(companyId, orderId, updateOrder);
          return ApiResponseFactory.accepted(
                  Map.of("orderId", orderId),
                  "Purchase order updated successfully");
      } catch (RuntimeException e) {
          return ApiResponseFactory.error(e.getMessage(), 406);
      }
  }
}
