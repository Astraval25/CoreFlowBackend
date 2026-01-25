package com.astraval.coreflow.modules.orderdetails.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.orderdetails.dto.CreateSalesOrder;
import com.astraval.coreflow.modules.orderdetails.dto.SalesOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.dto.UpdateSalesOrder;
import com.astraval.coreflow.modules.orderdetails.service.SalesOrderDetailsService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/companies")
public class SalesOrderDetailsController {
  
    @Autowired
    private SalesOrderDetailsService salesOrderDetailsService;
    
    
    @PostMapping("/{companyId}/sales/orders") // Create New Order
    public ApiResponse<Map<String, Long>> createOrder(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateSalesOrder createOrder) {
        try {
            Long orderId = salesOrderDetailsService.createSalesOrder(companyId, createOrder);
            return ApiResponseFactory.created(
                    Map.of("orderId", orderId),
                    "Order created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @GetMapping("/{companyId}/sales/orders") // List All Order by Company Id
    private ApiResponse<List<SalesOrderSummaryDto>> getOrderSummaryByCompanyId (@PathVariable Long companyId, SalesOrderSummaryDto orderSummaryDto){
        try {
            List<SalesOrderSummaryDto> result = salesOrderDetailsService.getOrderSummaryByCompanyId(companyId);
            return ApiResponseFactory.accepted(result, "Order retrieved successfully");
        } catch (Exception e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/sales/orders/{orderId}") // Update Order details by Order id
    public ApiResponse<Map<String, Long>> updateOrderDetailsByOrderId(@PathVariable Long companyId,
            @PathVariable Long orderId, @Valid @RequestBody UpdateSalesOrder updateOrder) {
        try {
            salesOrderDetailsService.updateSalesOrder(companyId, orderId, updateOrder);
            return ApiResponseFactory.accepted(
                    Map.of("orderId", orderId),
                    "Order updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

}
