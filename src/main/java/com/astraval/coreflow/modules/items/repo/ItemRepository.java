package com.astraval.coreflow.modules.items.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.dto.PurchasableItemDto;
import com.astraval.coreflow.modules.items.dto.SellableItemDto;
import com.astraval.coreflow.modules.items.model.Items;

@Repository
public interface ItemRepository extends JpaRepository<Items, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.ItemSummaryDto(" +
           "i.itemId, i.itemName, i.itemType, i.unit, " +
           "i.salesDescription, i.purchaseDescription, " +
           "i.baseSalesPrice, i.basePurchasePrice, " +
           "i.isActive, i.isSellable, i.isPurchasable, i.fsId) " +
           "FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "ORDER BY i.itemName")
    List<ItemSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.ItemSummaryDto(" +
           "i.itemId, i.itemName, i.itemType, i.unit, " +
           "i.salesDescription, i.purchaseDescription, " +
           "i.baseSalesPrice, i.basePurchasePrice, " +
           "i.isActive, i.isSellable, i.isPurchasable, i.fsId) " +
           "FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "ORDER BY i.itemName")
    List<ItemSummaryDto> findActiveByCompanyIdSummary(@Param("companyId") Long companyId);

    List<Items> findByCompanyCompanyIdOrderByItemName(Long companyId);

    List<Items> findByCompanyCompanyIdAndIsActiveTrueOrderByItemName(Long companyId);

    Optional<Items> findByItemIdAndCompanyCompanyId(Long itemId, Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.SellableItemDto(" +
           "i.itemId, i.itemName, i.salesDescription, i.baseSalesPrice, " +
           "i.taxRate, i.hsnCode, i.fsId) " +
           "FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "AND i.baseSalesPrice IS NOT NULL " +
           "ORDER BY i.itemName")
    List<SellableItemDto> findSellableItemsByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.PurchasableItemDto(" +
           "i.itemId, i.itemName, i.purchaseDescription, i.basePurchasePrice, " +
           "i.taxRate, i.hsnCode, 'ITEM_BASE', i.fsId) " +
           "FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "AND i.basePurchasePrice IS NOT NULL " +
           "ORDER BY i.itemName")
    List<PurchasableItemDto> findPurchasableItemsByCompanyId(@Param("companyId") Long companyId);
    
}
