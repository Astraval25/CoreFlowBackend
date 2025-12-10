package com.astraval.coreflow.global.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.global.model.Companies;

@Repository
public interface CompaniesRepository extends JpaRepository<Companies, String> {
}