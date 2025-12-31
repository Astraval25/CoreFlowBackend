package com.astraval.coreflow.modules.orderitemdetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemDetailsRepository extends JpaRepository<OrderItemDetails, Long>{
  
}
