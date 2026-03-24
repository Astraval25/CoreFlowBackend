package com.astraval.coreflow.modules.ordersnapshot.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.dto.PurchaseOrderSummaryDto;

@Repository
public interface PurchaseOrderSnapshotRepository extends JpaRepository<OrderSnapshot, Long> {

    @Query("""
            SELECT new com.astraval.coreflow.modules.ordersnapshot.dto.PurchaseOrderSummaryDto(
                o.orderId,
                o.orderNumber,
                o.orderDate,
                sc.companyName,
                v.displayName,
                o.totalAmount,
                o.paidAmount,
                o.orderStatus,
                o.isActive,
                o.platformRef,
                cor.localOrderNumber
            )
            FROM OrderSnapshot o
                LEFT JOIN o.customers c
                LEFT JOIN c.company sc
                LEFT JOIN o.vendors v
                LEFT JOIN v.company bc
                LEFT JOIN com.astraval.coreflow.modules.companyref.CompanyOrderRef cor
                    ON cor.orderDetails.orderId = o.orderReference AND cor.company.companyId = :companyId
                WHERE bc.companyId = :companyId
                ORDER BY o.orderDate DESC
            """)
    List<PurchaseOrderSummaryDto> findPurchaseOrdersByCompanyId(@Param("companyId") Long companyId);

}
