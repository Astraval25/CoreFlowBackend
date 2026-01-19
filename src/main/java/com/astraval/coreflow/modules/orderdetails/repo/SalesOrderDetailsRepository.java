package com.astraval.coreflow.modules.orderdetails.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.dto.SalesOrderSummaryDto;

@Repository
public interface SalesOrderDetailsRepository extends JpaRepository<OrderDetails, Long>{
  
    @Query("SELECT new com.astraval.coreflow.modules.orderdetails.dto.SalesOrderSummaryDto(" +
            "o.orderId, " +
            "o.orderNumber," +
            "o.orderDate, " +
            "o.sellerCompany.companyName, " +
            "o.customers.displayName, " +
            "o.orderAmount, " +
            "o.paidAmount, " +
            "o.orderStatus) " +
            "FROM OrderDetails o " +
            "WHERE o.sellerCompany.companyId = :companyId OR o.buyerCompany.companyId = :companyId " +
            "ORDER BY o.orderDate DESC")
    List<SalesOrderSummaryDto> findOrdersByCompanyId(@Param("companyId") Long companyId);
        
    @Query(value = "SELECT generate_order_number(?1)", nativeQuery = true)
    String generateOrderNumber(@Param("companyId") Long companyId);
    
    Optional<OrderDetails> findByOrderIdAndSellerCompany_CompanyId(Long orderId, Long companyId);
}
