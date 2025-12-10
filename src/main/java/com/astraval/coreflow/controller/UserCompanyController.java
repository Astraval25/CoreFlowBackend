package com.astraval.coreflow.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.dto.response.AdminCompaniesResponseDto;
import com.astraval.coreflow.dto.response.ApiResponse;
import com.astraval.coreflow.service.UserCompanyService;
import com.astraval.coreflow.util.ApiResponseFactory;
import com.astraval.coreflow.util.SecurityUtil;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/admin/companies")
public class UserCompanyController {

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
