package com.astraval.coreflow.modules.companies;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.global.repo.UserCompanyMapRepository;
import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;

@Service
public class UserCompanyService {
    
    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    public List<AdminCompaniesResponseDto> getAllCompaniesByUserId(String userId) {
        return userCompanyMapRepository.findByUserUserId(userId)
            .stream()
            .map(ucm -> companyMapper.toAdminCompanyResponseDto(ucm.getCompany()))
            .collect(Collectors.toList());
    }
}
