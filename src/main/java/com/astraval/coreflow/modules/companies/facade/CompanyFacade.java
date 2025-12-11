package com.astraval.coreflow.modules.companies.facade;

import com.astraval.coreflow.modules.companies.Companies;

public interface CompanyFacade {
    Companies createCompany(String companyName, String industry, String createdBy);
}