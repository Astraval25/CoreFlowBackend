package com.astraval.coreflow.modules.companies;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMapRepository;

@Service
public class UserCompanyService {
    
    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    public List<AdminCompaniesResponseDto> getAllCompaniesByUserId(Integer userId) {
        return userCompanyMapRepository.findByUserUserId(userId)
            .stream()
            .map(ucm -> companyMapper.toAdminCompanyResponseDto(ucm.getCompany()))
            .collect(Collectors.toList());
    }
}
