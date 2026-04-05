package com.astraval.coreflow.modules.modemp.salary;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.modemp.salary.dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/modemp/salary")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @PostMapping("/calculate")
    public ApiResponse<Map<String, List<Long>>> calculateSalary(
            @PathVariable Long companyId,
            @Valid @RequestBody CalculateSalaryRequestDto dto) {
        try {
            List<Long> ids = salaryService.calculateSalary(companyId, dto);
            return ApiResponseFactory.created(Map.of("salaryPeriodIds", ids),
                    "Salary calculated for " + ids.size() + " employee(s)");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/periods")
    public ApiResponse<List<SalaryPeriodSummaryDto>> getSalaryPeriods(
            @PathVariable Long companyId,
            @RequestParam String period) {
        try {
            List<SalaryPeriodSummaryDto> list = salaryService.getSalaryPeriods(companyId, period);
            return ApiResponseFactory.accepted(list, "Salary periods retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/periods/{salaryPeriodId}")
    public ApiResponse<SalaryPeriodDetailDto> getSalaryPeriodDetail(
            @PathVariable Long companyId,
            @PathVariable Long salaryPeriodId) {
        try {
            SalaryPeriodDetailDto dto = salaryService.getSalaryPeriodDetail(companyId, salaryPeriodId);
            return ApiResponseFactory.accepted(dto, "Salary period detail retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/periods/{salaryPeriodId}/approve")
    public ApiResponse<Void> approveSalaryPeriod(
            @PathVariable Long companyId,
            @PathVariable Long salaryPeriodId) {
        try {
            salaryService.approveSalaryPeriod(companyId, salaryPeriodId);
            return ApiResponseFactory.updated(null, "Salary period approved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/periods/{salaryPeriodId}/mark-paid")
    public ApiResponse<Void> markSalaryPaid(
            @PathVariable Long companyId,
            @PathVariable Long salaryPeriodId,
            @RequestBody(required = false) MarkPaidDto dto) {
        try {
            salaryService.markSalaryPaid(companyId, salaryPeriodId, dto);
            return ApiResponseFactory.updated(null, "Salary period marked as paid");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
