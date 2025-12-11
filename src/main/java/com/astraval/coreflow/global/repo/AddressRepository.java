package com.astraval.coreflow.global.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.global.model.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}
