package com.astraval.coreflow.modules.companies;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.companies.dto.CompanySummaryDto;
import com.astraval.coreflow.modules.companies.dto.CreateUpdateCompanyDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    // Get all companies
    @GetMapping
    public ApiResponse<List<Companies>> getAllCompanies() {
        try {
            List<Companies> companies = companyService.getAllCompanies();
            return ApiResponseFactory.accepted(companies, "Companies retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Get companies by user id (from JWT)
    @GetMapping("/my-companies")
    public ApiResponse<List<CompanySummaryDto>> getCompaniesByUserId() {
        try {
            List<CompanySummaryDto> companies = companyService.getCompaniesByUserId();
            return ApiResponseFactory.accepted(companies, "User companies retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Get active companies by user id
    @GetMapping("/my-companies/active")
    public ApiResponse<List<CompanySummaryDto>> getActiveCompaniesByUserId() {
        try {
            List<CompanySummaryDto> companies = companyService.getActiveCompaniesByUserId();
            return ApiResponseFactory.accepted(companies, "Active user companies retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Create new company
    @PostMapping
    public ApiResponse<Map<String, Long>> createCompany(@Valid @RequestBody CreateUpdateCompanyDto request) {
        try {
            Long companyId = companyService.createCompany(request);
            return ApiResponseFactory.created(
                    Map.of("companyId", companyId),
                    "Company created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Update company
    @PutMapping("/{companyId}")
    public ApiResponse<Void> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateUpdateCompanyDto request) {
        try {
            companyService.updateCompany(companyId, request);
            return ApiResponseFactory.updated(null, "Company updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Deactivate company
    @PatchMapping("/{companyId}/deactivate")
    public ApiResponse<String> deactivateCompany(@PathVariable Long companyId) {
        try {
            companyService.deactivateCompany(companyId);
            return ApiResponseFactory.updated(null, "Company deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Activate company
    @PatchMapping("/{companyId}/activate")
    public ApiResponse<String> activateCompany(@PathVariable Long companyId) {
        try {
            companyService.activateCompany(companyId);
            return ApiResponseFactory.updated(null, "Company activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Delete company
    @DeleteMapping("/{companyId}")
    public ApiResponse<String> deleteCompany(@PathVariable Long companyId) {
        try {
            companyService.deleteCompany(companyId);
            return ApiResponseFactory.deleted("Company deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
}
