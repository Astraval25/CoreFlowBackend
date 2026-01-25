package com.astraval.coreflow.modules.orderitemdetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderItemDetailsRepository extends JpaRepository<OrderItemDetails, Long>{
  
    List<OrderItemDetails> findByOrderId(Long orderId);
    
    List<OrderItemDetails> findByOrderIdAndIsActive(Long orderId, Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OrderItemDetails o WHERE o.orderId = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
