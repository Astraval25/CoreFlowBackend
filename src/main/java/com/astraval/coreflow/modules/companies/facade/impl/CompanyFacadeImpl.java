package com.astraval.coreflow.modules.companies.facade.impl;

import java.util.List;

import com.astraval.coreflow.modules.companies.facade.CompanyFacade;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompaniesRepository;
import com.astraval.coreflow.modules.companies.UserCompanyService;
import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CompanyFacadeImpl implements CompanyFacade {

    @Autowired
    private CompaniesRepository companiesRepository;
    
    @Autowired
    private UserCompanyService userCompanyService;

    @Override
    public Companies createCompany(String companyName, String industry, String createdBy) {
        Companies company = new Companies();
        company.setCompanyName(companyName);
        company.setIndustry(industry);
        company.setPan("");
        company.setGstNo("");
        company.setHsnCode("");
        company.setShortName("");
        company.setCreatedBy(createdBy);
        company.setCreatedDt(LocalDateTime.now());
        return companiesRepository.save(company);
    }
    
    @Override
    public List<AdminCompaniesResponseDto> getAllCompaniesByUserId(Integer userId) {
        return userCompanyService.getAllCompaniesByUserId(userId);
    }
}