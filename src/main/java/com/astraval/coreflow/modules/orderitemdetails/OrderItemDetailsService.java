package com.astraval.coreflow.modules.orderitemdetails;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderItemDetailsService {
  
    @Autowired
    private OrderItemDetailsRepository orderItemDetailsRepository;
    
    
    @Transactional
    public OrderItemDetails createOrderItem(OrderItemDetails orderItemDetails) {
        OrderItemDetails savedItem = orderItemDetailsRepository.save(orderItemDetails);
                
        return savedItem;
    }
    
    
}
