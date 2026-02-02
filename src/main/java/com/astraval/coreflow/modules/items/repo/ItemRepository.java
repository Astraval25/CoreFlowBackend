package com.astraval.coreflow.modules.items.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.items.dto.GetOrderItemsDto;
import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.PurchasableItemDto;
import com.astraval.coreflow.modules.items.dto.SellableItemDto;
import com.astraval.coreflow.modules.items.model.Items;

@Repository
public interface ItemRepository extends JpaRepository<Items, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.ItemSummaryDto(" +
           "i.itemId, i.itemName, i.itemType, i.unit, " +
           "i.salesPrice, pc.customerId, pc.customerName, " +
           "i.purchasePrice, pv.vendorId, pv.vendorName, " +
           "i.isActive) " +
           "FROM Items i " +
           "LEFT JOIN i.preferredCustomer pc " +
           "LEFT JOIN i.preferredVendor pv " +
           "WHERE i.company.companyId = :companyId " +
           "ORDER BY i.itemName")
    List<ItemSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.ItemSummaryDto(" +
           "i.itemId, i.itemName, i.itemType, i.unit, " +
           "i.salesPrice, pc.customerId, pc.customerName, " +
           "i.purchasePrice, pv.vendorId, pv.vendorName, " +
           "i.isActive) " +
           "FROM Items i " +
           "LEFT JOIN i.preferredCustomer pc " +
           "LEFT JOIN i.preferredVendor pv " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "ORDER BY i.itemName")
    List<ItemSummaryDto> findActiveByCompanyIdSummary(@Param("companyId") Long companyId);

    Optional<Items> findByItemIdAndCompanyCompanyId(Long itemId, Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.SellableItemDto(" +
           "i.itemId, i.itemName, i.salesDescription, i.salesPrice, " +
           "pc.customerName, i.taxRate, i.hsnCode) " +
           "FROM Items i " +
           "LEFT JOIN i.preferredCustomer pc " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "AND i.salesPrice IS NOT NULL " +
           "ORDER BY i.itemName")
    List<SellableItemDto> findSellableItemsByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.PurchasableItemDto(" +
           "i.itemId, i.itemName, i.purchaseDescription, i.purchasePrice, " +
           "pv.vendorName, i.taxRate, i.hsnCode) " +
           "FROM Items i " +
           "LEFT JOIN i.preferredVendor pv " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "AND i.purchasePrice IS NOT NULL " +
           "ORDER BY i.itemName")
    List<PurchasableItemDto> findPurchasableItemsByCompanyId(@Param("companyId") Long companyId);
    
    // Case 1: Linked vendor - get items from vendor's company for specific customer
    @Query("SELECT i FROM Items i " +
           "JOIN Vendors v ON v.vendorId = :vendorId " +
           "JOIN Customers c ON c.customerCompany.companyId = :companyId " +
           "WHERE i.company.companyId = v.vendorCompany.companyId " +
           "AND i.preferredCustomer.customerId = c.customerId " +
           "AND i.isActive = true " +
           "ORDER BY i.itemName")
    List<Items> findItemsForLinkedVendor(@Param("companyId") Long companyId, @Param("vendorId") Long vendorId);
    
    // Case 2: Unlinked vendor - get items from company with preferred vendor or no preference
    @Query("SELECT i FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "AND (i.preferredVendor.vendorId = :vendorId OR i.preferredVendor IS NULL) " +
           "AND i.isActive = true " +
           "ORDER BY i.itemName")
    List<Items> findItemsForUnlinkedVendor(@Param("companyId") Long companyId, @Param("vendorId") Long vendorId);
}