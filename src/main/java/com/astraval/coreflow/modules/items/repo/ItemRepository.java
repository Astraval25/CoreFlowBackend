package com.astraval.coreflow.modules.items.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.items.dto.ItemSummaryDto;
import com.astraval.coreflow.modules.items.model.Items;

@Repository
public interface ItemRepository extends JpaRepository<Items, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.ItemSummaryDto(" +
           "i.itemId, i.itemName, i.itemDisplayName, i.itemType, i.unit, " +
           "i.salesPrice, i.preferredCustomer.customerId, i.preferredCustomer.customerName, " +
           "i.purchasePrice, i.preferredVendor.vendorId, i.preferredVendor.vendorName, " +
           "i.isActive) " +
           "FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "ORDER BY i.itemName")
    List<ItemSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.items.dto.ItemSummaryDto(" +
           "i.itemId, i.itemName, i.itemDisplayName, i.itemType, i.unit, " +
           "i.salesPrice, i.preferredCustomer.customerId, i.preferredCustomer.customerName, " +
           "i.purchasePrice, i.preferredVendor.vendorId, i.preferredVendor.vendorName, " +
           "i.isActive) " +
           "FROM Items i " +
           "WHERE i.company.companyId = :companyId " +
           "AND i.isActive = true " +
           "ORDER BY i.itemName")
    List<ItemSummaryDto> findActiveByCompanyIdSummary(@Param("companyId") Long companyId);

    Optional<Items> findByItemIdAndCompanyCompanyId(Long itemId, Long companyId);
}