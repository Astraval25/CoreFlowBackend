package com.astraval.coreflow.shared.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.shared.model.Companies;

@Repository
public interface CompaniesRepository extends JpaRepository<Companies, String> {
}