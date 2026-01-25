package com.astraval.coreflow.modules.orderdetails.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.dto.PurchaseOrderSummaryDto;

@Repository
public interface PurchaseOrderDetailsRepository extends JpaRepository<OrderDetails, Long> {

  @Query("""
      SELECT new com.astraval.coreflow.modules.orderdetails.dto.PurchaseOrderSummaryDto(
          o.orderId,
          o.orderNumber,
          o.orderDate,
          sc.companyName,
          c.displayName,
          o.orderAmount,
          o.paidAmount,
          o.orderStatus,
          o.isActive
      )
      FROM OrderDetails o
          LEFT JOIN o.sellerCompany sc
          LEFT JOIN o.customers c
          WHERE o.buyerCompany.companyId = :companyId
          ORDER BY o.orderDate DESC
      """)
  List<PurchaseOrderSummaryDto> findPurchaseOrdersByCompanyId(@Param("companyId") Long companyId);

}
