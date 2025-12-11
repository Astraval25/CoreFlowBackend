package com.astraval.coreflow.modules.companies;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import com.astraval.coreflow.global.util.SecurityUtil;
import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;

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
        String userIdStr = securityUtil.getCurrentSub();
        Integer userId = Integer.valueOf(userIdStr);
        return ApiResponseFactory.accepted(userCompanyService.getAllCompaniesByUserId(userId), null);
    }
    
}
