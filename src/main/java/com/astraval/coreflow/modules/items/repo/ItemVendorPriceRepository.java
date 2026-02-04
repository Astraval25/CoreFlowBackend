package com.astraval.coreflow.modules.items.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.items.model.ItemVendorPrice;

import java.util.List;

@Repository
public interface ItemVendorPriceRepository extends JpaRepository<ItemVendorPrice, Long> {

    Optional<ItemVendorPrice> findByItemItemIdAndVendorVendorIdAndIsActiveTrue(
            Long itemId, Long vendorId);

    Optional<ItemVendorPrice> findByItemItemIdAndVendorVendorId(
                    Long itemId, Long vendorId);

    List<ItemVendorPrice> findByVendorVendorIdAndIsActiveTrue(Long vendorId);

    long countByItemItemIdAndIsActiveTrue(Long itemId);
}
