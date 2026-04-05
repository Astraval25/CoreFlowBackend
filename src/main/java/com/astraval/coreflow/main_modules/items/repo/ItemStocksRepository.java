package com.astraval.coreflow.main_modules.items.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.main_modules.items.model.ItemStocks;

@Repository
public interface ItemStocksRepository extends JpaRepository<ItemStocks, Long> {
}