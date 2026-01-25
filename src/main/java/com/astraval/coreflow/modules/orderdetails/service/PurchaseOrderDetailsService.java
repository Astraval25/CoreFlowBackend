package com.astraval.coreflow.modules.orderdetails.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.orderdetails.dto.PurchaseOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.repo.PurchaseOrderDetailsRepository;

@Service
public class PurchaseOrderDetailsService {
  @Autowired
  private PurchaseOrderDetailsRepository purchaseOrderDetailsRepository;

  public List<PurchaseOrderSummaryDto> getOrderSummaryByCompanyId(Long companyId) {
    return purchaseOrderDetailsRepository.findPurchaseOrdersByCompanyId(companyId);
  }

}
