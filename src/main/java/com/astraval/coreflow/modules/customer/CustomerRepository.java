package com.astraval.coreflow.modules.customer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {
    List<Customers> findByCompanyCompanyIdAndIsActiveTrue(Integer companyId);
}
