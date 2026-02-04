package com.astraval.coreflow.modules.items.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.items.model.ItemCustomerPrice;

@Repository
public interface ItemCustomerPriceRepository extends JpaRepository<ItemCustomerPrice, Long> {

    Optional<ItemCustomerPrice> findByItemItemIdAndCustomerCustomerIdAndIsActiveTrue(
            Long itemId, Long customerId);

    long countByItemItemIdAndIsActiveTrue(Long itemId);
}
