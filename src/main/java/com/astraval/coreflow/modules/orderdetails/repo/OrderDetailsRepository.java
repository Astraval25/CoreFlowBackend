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
            LEFT JOIN FETCH o.customers c
            LEFT JOIN FETCH c.company sc
            LEFT JOIN FETCH o.vendors v
            LEFT JOIN FETCH v.company bc
            WHERE o.orderId = :orderId
            AND (sc.companyId = :companyId OR bc.companyId = :companyId)
            """)
    Optional<OrderDetails> findOrderForCompany(@Param("orderId") Long orderId, @Param("companyId") Long companyId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderDetails o SET o.orderStatus = :status WHERE o.orderId = :orderId AND o.customers.company.companyId = :companyId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("companyId") Long companyId,
            @Param("status") String status);

    @Query("SELECT new com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto(" +
           "o.orderId, o.orderNumber, o.orderDate, o.orderStatus, " +
           "COALESCE(sc.companyName, ''), v.displayName, " +
           "COALESCE(sc.companyId, 0L), o.hasBill, o.orderAmount, o.totalAmount, o.paidAmount, o.isActive) " +
           "FROM OrderDetails o " +
           "LEFT JOIN o.customers c " +
           "LEFT JOIN c.company sc " +
           "LEFT JOIN o.vendors v " +
           "LEFT JOIN v.company bc " +
           "WHERE bc.companyId = :buyerCompanyId " +
           "AND v.vendorId = :vendorId " +
           "AND o.orderStatus = :orderStatus")
    List<UnpaidOrderDto> findUnpaidOrdersByBuyerCompanyIdAndVendorId(
            @Param("buyerCompanyId") Long buyerCompanyId,
            @Param("vendorId") Long vendorId,
            @Param("orderStatus") String orderStatus);

    @Query("SELECT new com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto(" +
           "o.orderId, o.orderNumber, o.orderDate, o.orderStatus, " +
           "COALESCE(bc.companyName, ''), COALESCE(c.displayName, ''), " +
           "COALESCE(sc.companyId, 0L), o.hasBill, o.orderAmount, o.totalAmount, o.paidAmount, o.isActive) " +
           "FROM OrderDetails o " +
           "LEFT JOIN o.customers c " +
           "LEFT JOIN c.company sc " +
           "LEFT JOIN o.vendors v " +
           "LEFT JOIN v.company bc " +
           "WHERE sc.companyId = :sellerCompanyId " +
           "AND c.customerId = :customerId " +
           "AND o.orderStatus = :orderStatus")
    List<UnpaidOrderDto> findUnpaidOrdersBySellerCompanyIdAndCustomerId(
            @Param("sellerCompanyId") Long sellerCompanyId,
            @Param("customerId") Long customerId,
            @Param("orderStatus") String orderStatus);
}
