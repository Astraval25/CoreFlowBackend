package com.astraval.coreflow.employee_module.portal;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.employee_module.employee.EmployeeService;
import com.astraval.coreflow.employee_module.employee.dto.EmployeeDetailDto;
import com.astraval.coreflow.employee_module.leavelog.EmployeeLeaveLogService;
import com.astraval.coreflow.employee_module.leavelog.dto.LeaveLogDto;
import com.astraval.coreflow.employee_module.salary.SalaryService;
import com.astraval.coreflow.employee_module.salary.SalarySlipPdfService;
import com.astraval.coreflow.employee_module.salary.dto.SalaryPeriodDetailDto;
import com.astraval.coreflow.employee_module.salary.dto.SalaryPeriodSummaryDto;
import com.astraval.coreflow.employee_module.worklog.EmployeeWorkLogService;
import com.astraval.coreflow.employee_module.worklog.dto.WorkLogDto;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Employee self-service portal APIs.
 * Only accessible with ROLE_EMP tokens.
 * Employees can only view their own data — employeeId is extracted from the JWT.
 */
@RestController
@RequestMapping("/api/emp")
public class EmployeePortalController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private SalarySlipPdfService salarySlipPdfService;

    @Autowired
    private EmployeeWorkLogService workLogService;

    @Autowired
    private EmployeeLeaveLogService leaveLogService;

    // ── My Profile ──

    @GetMapping("/me")
    public ApiResponse<EmployeeDetailDto> getMyProfile() {
        try {
            Long[] ids = extractEmployeeAndCompanyId();
            EmployeeDetailDto dto = employeeService.getEmployeeDetail(ids[1], ids[0]);
            return ApiResponseFactory.accepted(dto, "Profile retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // ── My Salary ──

    @GetMapping("/salary/periods")
    public ApiResponse<List<SalaryPeriodSummaryDto>> getMySalaryPeriods(
            @RequestParam String period) {
        try {
            Long[] ids = extractEmployeeAndCompanyId();
            List<SalaryPeriodSummaryDto> all = salaryService.getSalaryPeriods(ids[1], period);
            List<SalaryPeriodSummaryDto> mine = all.stream()
                    .filter(sp -> sp.getEmployeeId().equals(ids[0]))
                    .toList();
            return ApiResponseFactory.accepted(mine, "Salary periods retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/salary/periods/{salaryPeriodId}")
    public ApiResponse<SalaryPeriodDetailDto> getMySalaryDetail(
            @PathVariable Long salaryPeriodId) {
        try {
            Long[] ids = extractEmployeeAndCompanyId();
            SalaryPeriodDetailDto dto = salaryService.getSalaryPeriodDetail(ids[1], salaryPeriodId);
            if (!dto.getEmployeeId().equals(ids[0])) {
                return ApiResponseFactory.error("Access denied: this salary record does not belong to you", 403);
            }
            return ApiResponseFactory.accepted(dto, "Salary detail retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/salary/periods/{salaryPeriodId}/slip")
    public ResponseEntity<byte[]> downloadMySalarySlip(
            @PathVariable Long salaryPeriodId) {
        try {
            Long[] ids = extractEmployeeAndCompanyId();
            SalaryPeriodDetailDto dto = salaryService.getSalaryPeriodDetail(ids[1], salaryPeriodId);
            if (!dto.getEmployeeId().equals(ids[0])) {
                return ResponseEntity.status(403).build();
            }
            byte[] pdf = salarySlipPdfService.generateSalarySlip(ids[1], salaryPeriodId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=salary-slip-" + salaryPeriodId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── My Work Logs ──

    @GetMapping("/work-logs")
    public ApiResponse<List<WorkLogDto>> getMyWorkLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            Long[] ids = extractEmployeeAndCompanyId();
            List<WorkLogDto> logs = workLogService.getWorkLogsByEmployee(ids[0], from, to);
            return ApiResponseFactory.accepted(logs, "Work logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // ── My Leave Logs ──

    @GetMapping("/leave-logs")
    public ApiResponse<List<LeaveLogDto>> getMyLeaveLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            Long[] ids = extractEmployeeAndCompanyId();
            List<LeaveLogDto> logs = leaveLogService.getLeaveLogsByEmployee(ids[0], from, to);
            return ApiResponseFactory.accepted(logs, "Leave logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // ── Extract employee context from JWT ──

    private Long[] extractEmployeeAndCompanyId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            throw new RuntimeException("Authentication required");
        }
        Claims claims = (Claims) auth.getDetails();
        Long employeeId = claims.get("employeeId", Long.class);
        Long companyId = claims.get("companyId", Long.class);
        if (employeeId == null || companyId == null) {
            throw new RuntimeException("Invalid employee token");
        }
        return new Long[]{employeeId, companyId};
    }
}
