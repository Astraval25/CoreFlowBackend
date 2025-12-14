package com.astraval.coreflow.modules.companies.facade;

import java.util.List;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;

public interface CompanyFacade {
    Companies createCompany(String companyName, String industry, String createdBy);
    List<AdminCompaniesResponseDto> getAllCompaniesByUserId(Integer userId);
}