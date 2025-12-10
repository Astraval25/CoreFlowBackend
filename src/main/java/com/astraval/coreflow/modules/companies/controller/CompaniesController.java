package com.astraval.coreflow.modules.companies.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;
import com.astraval.coreflow.modules.companies.service.UserCompanyService;
import com.astraval.coreflow.shared.util.ApiResponse;
import com.astraval.coreflow.shared.util.ApiResponseFactory;
import com.astraval.coreflow.shared.util.SecurityUtil;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/admin/companies")
public class CompaniesController {

    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private UserCompanyService userCompanyService;

    @GetMapping("/details")
    public ApiResponse<List<AdminCompaniesResponseDto>> getCompaniesByUserId() {
        String userId = securityUtil.getCurrentSub();
        return ApiResponseFactory.accepted(userCompanyService.getAllCompaniesByUserId(userId), null);
    }
    
}
