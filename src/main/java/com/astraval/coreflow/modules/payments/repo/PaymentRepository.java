package com.astraval.coreflow.modules.payments.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.payments.model.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {

}