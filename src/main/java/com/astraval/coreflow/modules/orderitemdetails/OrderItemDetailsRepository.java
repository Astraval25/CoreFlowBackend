package com.astraval.coreflow.modules.orderitemdetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemDetailsRepository extends JpaRepository<OrderItemDetails, Long>{
  
    List<OrderItemDetails> findByOrderId(Long orderId);
    
    List<OrderItemDetails> findByOrderIdAndIsActive(Long orderId, Boolean isActive);
}
