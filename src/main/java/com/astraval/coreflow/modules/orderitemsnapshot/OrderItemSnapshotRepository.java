package com.astraval.coreflow.modules.orderitemsnapshot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderItemSnapshotRepository extends JpaRepository<OrderItemSnapshot, Long>{
  
    List<OrderItemSnapshot> findByOrderId(Long orderId);
    
    List<OrderItemSnapshot> findByOrderIdAndIsActive(Long orderId, Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OrderItemSnapshot o WHERE o.orderId = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
