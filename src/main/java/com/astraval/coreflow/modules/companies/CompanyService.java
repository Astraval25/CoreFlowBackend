package com.astraval.coreflow.modules.companies;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.modules.companies.dto.CompanySummaryDto;
import com.astraval.coreflow.modules.companies.dto.CreateUpdateCompanyDto;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMapRepository;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtil securityUtil;

    public List<Companies> getAllCompanies() {
        return companyRepository.findAll();
    }

    public List<CompanySummaryDto> getCompaniesByUserId() {
        String currentUserId = securityUtil.getCurrentSub();
        Long userId = Long.parseLong(currentUserId);
        return companyRepository.findCompaniesByUserId(userId);
    }

    public List<CompanySummaryDto> getActiveCompaniesByUserId() {
        String currentUserId = securityUtil.getCurrentSub();
        Long userId = Long.parseLong(currentUserId);
        return companyRepository.findActiveCompaniesByUserId(userId);
    }

    @Transactional
    public Long createCompany(CreateUpdateCompanyDto request) {
        String currentUserId = securityUtil.getCurrentSub();
        Long userId = Long.parseLong(currentUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Companies company = new Companies();
        company.setCompanyName(request.getCompanyName());
        company.setIndustry(request.getIndustry());
        company.setPan(request.getPan());
        company.setGstNo(request.getGstNo());
        company.setHsnCode(request.getHsnCode());
        company.setShortName(request.getShortName());

        Companies savedCompany = companyRepository.save(company);

        // Map company with user
        UserCompanyMap userCompanyMap = new UserCompanyMap();
        userCompanyMap.setUser(user);
        userCompanyMap.setCompany(savedCompany);
        userCompanyMapRepository.save(userCompanyMap);

        return savedCompany.getCompanyId();
    }

    @Transactional
    public void updateCompany(Long companyId, CreateUpdateCompanyDto request) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        company.setCompanyName(request.getCompanyName());
        company.setIndustry(request.getIndustry());
        company.setPan(request.getPan());
        company.setGstNo(request.getGstNo());
        company.setHsnCode(request.getHsnCode());
        company.setShortName(request.getShortName());

        companyRepository.save(company);
    }

    @Transactional
    public void deactivateCompany(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        company.setIsActive(false);
        companyRepository.save(company);
    }

    @Transactional
    public void activateCompany(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        company.setIsActive(true);
        companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new RuntimeException("Company not found with ID: " + companyId);
        }
        companyRepository.deleteById(companyId);
    }
}