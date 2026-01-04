package com.astraval.coreflow.modules.orderdetails;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.orderdetails.dto.CreateOrder;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class OrderDetailsController {
  
    @Autowired
    private OrderDetailsService orderDetailsService;
    
    
    @PostMapping("/{companyId}/orders")
    public ApiResponse<Map<String, Long>> createOrder(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateOrder createOrder) {
        try {
            Long orderId = orderDetailsService.createOrder(companyId, createOrder);
            return ApiResponseFactory.created(
                    Map.of("orderId", orderId),
                    "Order created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    

}
