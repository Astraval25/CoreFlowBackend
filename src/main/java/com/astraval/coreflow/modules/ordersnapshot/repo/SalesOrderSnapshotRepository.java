package com.astraval.coreflow.modules.ordersnapshot.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.dto.SalesOrderSummaryDto;

@Repository
public interface SalesOrderSnapshotRepository extends JpaRepository<OrderSnapshot, Long> {

    @Query("""
            SELECT new com.astraval.coreflow.modules.ordersnapshot.dto.SalesOrderSummaryDto(
                o.orderId,
                o.orderNumber,
                o.orderDate,
                bc.companyName,
                c.displayName,
                o.totalAmount,
                o.paidAmount,
                o.orderStatus,
                o.isActive
            )
            FROM OrderSnapshot o
                LEFT JOIN o.buyerCompany bc
                LEFT JOIN o.customers c
                WHERE o.sellerCompany.companyId = :companyId
                ORDER BY o.orderDate DESC
            """)
    List<SalesOrderSummaryDto> findOrdersByCompanyId(@Param("companyId") Long companyId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderSnapshot o SET o.orderStatus = :status WHERE o.orderId = :orderId AND o.sellerCompany.companyId = :companyId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("companyId") Long companyId,
            @Param("status") String status);
}
