package com.astraval.coreflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.dto.response.AdminCompanyiesResponseDto;
import com.astraval.coreflow.mapper.CompanyMapper;
import com.astraval.coreflow.repo.UserCompanyMapRepository;

@Service
public class UserCompanyService {
    
    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    public List<AdminCompanyiesResponseDto> getAllCompaniesByUserId(String userId) {
        return userCompanyMapRepository.findByUserUserId(userId)
            .stream()
            .map(ucm -> companyMapper.toAdminCompanyResponseDto(ucm.getCompany()))
            .collect(Collectors.toList());
    }
}
