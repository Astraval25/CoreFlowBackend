package com.astraval.coreflow.employee_module.worklog;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.employee_module.worklog.dto.*;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/modemp/work-logs")
public class EmployeeWorkLogController {

    @Autowired
    private EmployeeWorkLogService workLogService;

    @PostMapping
    public ApiResponse<Map<String, Long>> createWorkLog(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateWorkLogDto dto) {
        try {
            Long id = workLogService.createWorkLog(companyId, dto);
            return ApiResponseFactory.created(Map.of("logId", id), "Work log created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping
    public ApiResponse<List<WorkLogDto>> getWorkLogs(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<WorkLogDto> list = workLogService.getWorkLogsByCompany(companyId, from, to);
            return ApiResponseFactory.accepted(list, "Work logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<WorkLogDto>> getWorkLogsByEmployee(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<WorkLogDto> list = workLogService.getWorkLogsByEmployee(employeeId, from, to);
            return ApiResponseFactory.accepted(list, "Work logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/pending")
    public ApiResponse<List<WorkLogDto>> getPendingWorkLogs(
            @PathVariable Long companyId) {
        try {
            List<WorkLogDto> list = workLogService.getPendingWorkLogs(companyId);
            return ApiResponseFactory.accepted(list, "Pending work logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{logId}/review")
    public ApiResponse<Void> reviewWorkLog(
            @PathVariable Long companyId,
            @PathVariable Long logId,
            @Valid @RequestBody ReviewWorkLogDto dto) {
        try {
            workLogService.reviewWorkLog(companyId, logId, dto);
            return ApiResponseFactory.updated(null, "Work log reviewed successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PreAuthorize("hasRole('EMP')")
    @PutMapping("/employee")
    public ApiResponse<Void> updateMyWorkLog(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateWorkLogDto dto) {
        try {
            Long currentEmployeeId = extractCurrentEmployeeId();
            if (!currentEmployeeId.equals(dto.getEmployeeId())) {
                return ApiResponseFactory.error("Access denied: you can only update your own work logs", 403);
            }

            workLogService.updateWorkLog(companyId, dto);
            return ApiResponseFactory.updated(null, "Work log updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    private Long extractCurrentEmployeeId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof Claims claims)) {
            throw new RuntimeException("Authentication required");
        }

        Long employeeId = claims.get("employeeId", Long.class);
        if (employeeId == null) {
            throw new RuntimeException("Invalid employee token");
        }

        return employeeId;
    }
}
