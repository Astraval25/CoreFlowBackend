package com.astraval.coreflow.modules.orderdetails.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.orderdetails.dto.PurchaseOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.service.PurchaseOrderDetailsService;

@RestController
@RequestMapping("/api/companies")
public class PurchaseOrderDetailsController {

  @Autowired
  PurchaseOrderDetailsService purchaseOrderDetailsService;

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
}
