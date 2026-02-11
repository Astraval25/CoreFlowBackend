package com.astraval.coreflow.modules.items.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.items.model.ItemCustomerPrice;

import java.util.List;

@Repository
public interface ItemCustomerPriceRepository extends JpaRepository<ItemCustomerPrice, Long> {

    Optional<ItemCustomerPrice> findByItemItemIdAndCustomerCustomerIdAndIsActiveTrue(
            Long itemId, Long customerId);

    Optional<ItemCustomerPrice> findByItemItemIdAndCustomerCustomerId(
            Long itemId, Long customerId);

    List<ItemCustomerPrice> findByCustomerCustomerIdAndIsActiveTrue(Long customerId);
    
    List<ItemCustomerPrice> findByCustomerCustomerId(Long customerId);

    long countByItemItemIdAndIsActiveTrue(Long itemId);
}
