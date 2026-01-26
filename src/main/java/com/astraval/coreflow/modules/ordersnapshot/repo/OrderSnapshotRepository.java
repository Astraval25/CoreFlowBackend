package com.astraval.coreflow.modules.ordersnapshot.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;

@Repository
public interface OrderSnapshotRepository extends JpaRepository<OrderSnapshot, Long> {

     @Query(value = "SELECT generate_order_number(?1)", nativeQuery = true)
     String generateOrderNumber(@Param("companyId") Long companyId);

     @Query("""
               SELECT o FROM OrderSnapshot o
               WHERE o.orderId = :orderId
               AND (
                    o.sellerCompany.companyId = :companyId
                    OR o.buyerCompany.companyId = :companyId
               )
               """)
     Optional<OrderSnapshot> findOrderForCompany(@Param("orderId") Long orderId, @Param("companyId") Long companyId);

     @Modifying
     @Transactional
     @Query("UPDATE OrderSnapshot o SET o.orderStatus = :status WHERE o.orderId = :orderId AND o.sellerCompany.companyId = :companyId")
     void updateOrderStatus(@Param("orderId") Long orderId, @Param("companyId") Long companyId,
               @Param("status") String status);
}
