package com.astraval.coreflow.modules.orderdetails.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {

    
    @Query(value = "SELECT generate_order_number(?1)", nativeQuery = true)
    String generateOrderNumber(@Param("companyId") Long companyId);

    @Query("""
            SELECT o FROM OrderDetails o
            WHERE o.orderId = :orderId
            AND (
                 o.sellerCompany.companyId = :companyId
                 OR o.buyerCompany.companyId = :companyId
            )
            """)
    Optional<OrderDetails> findOrderForCompany(@Param("orderId") Long orderId, @Param("companyId") Long companyId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderDetails o SET o.orderStatus = :status WHERE o.orderId = :orderId AND o.sellerCompany.companyId = :companyId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("companyId") Long companyId,
            @Param("status") String status);

    @Query("SELECT new com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto(" +
           "o.orderId, o.orderNumber, o.orderDate, o.orderStatus, " +
           "COALESCE(o.sellerCompany.companyName, ''), o.vendors.displayName, " +
           "COALESCE(o.sellerCompany.companyId, 0L), o.hasBill, o.orderAmount, o.totalAmount, o.paidAmount, o.isActive) " +
           "FROM OrderDetails o " +
           "LEFT JOIN o.sellerCompany " +
           "LEFT JOIN o.vendors " +
           "WHERE o.buyerCompany.companyId = :buyerCompanyId " +
           "AND o.vendors.vendorId = :vendorId " +
           "AND o.orderStatus = :orderStatus")
    List<UnpaidOrderDto> findUnpaidOrdersByBuyerCompanyIdAndVendorId(
            @Param("buyerCompanyId") Long buyerCompanyId,
            @Param("vendorId") Long vendorId,
            @Param("orderStatus") String orderStatus);

    @Query("SELECT new com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto(" +
           "o.orderId, o.orderNumber, o.orderDate, o.orderStatus, " +
           "COALESCE(o.buyerCompany.companyName, ''), COALESCE(o.customers.displayName, ''), " +
           "COALESCE(o.sellerCompany.companyId, 0L), o.hasBill, o.orderAmount, o.totalAmount, o.paidAmount, o.isActive) " +
           "FROM OrderDetails o " +
           "LEFT JOIN o.buyerCompany " +
           "LEFT JOIN o.customers " +
           "WHERE o.sellerCompany.companyId = :sellerCompanyId " +
           "AND o.customers.customerId = :customerId " +
           "AND o.orderStatus = :orderStatus")
    List<UnpaidOrderDto> findUnpaidOrdersBySellerCompanyIdAndCustomerId(
            @Param("sellerCompanyId") Long sellerCompanyId,
            @Param("customerId") Long customerId,
            @Param("orderStatus") String orderStatus);
}
