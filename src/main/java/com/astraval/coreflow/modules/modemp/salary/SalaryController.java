package com.astraval.coreflow.modules.modemp.salary;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.modemp.salary.dto.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/companies/{companyId}/modemp/salary")
public class SalaryController {
    private static final Pattern DUPLICATE_SALARY_PERIOD_PATTERN =
            Pattern.compile("Key \\(employee_id, from_date, to_date\\)=\\((\\d+),\\s*([0-9\\-]+),\\s*([0-9\\-]+)\\)");

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
        } catch (DataIntegrityViolationException e) {
            return ApiResponseFactory.error(getSalaryConflictMessage(e), 409);
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    private String getSalaryConflictMessage(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (raw == null) {
            return "Salary already exists for the selected employee and date range";
        }

        Matcher matcher = DUPLICATE_SALARY_PERIOD_PATTERN.matcher(raw);
        if (matcher.find()) {
            return "Salary already calculated for employee ID " + matcher.group(1)
                    + " from " + matcher.group(2)
                    + " to " + matcher.group(3);
        }

        if (raw.contains("employee_salary_periods") || raw.contains("ukpxhk0n931cwu5pqb77sfuhg4d")) {
            return "Salary already exists for the selected employee and date range";
        }

        return "Salary calculation failed due to a data conflict";
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

    @GetMapping("/report")
    public ApiResponse<SalaryReportDto> getSalaryReport(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            SalaryReportDto report = salaryService.getSalaryReport(companyId, from, to);
            return ApiResponseFactory.accepted(report, "Salary report retrieved successfully");
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
