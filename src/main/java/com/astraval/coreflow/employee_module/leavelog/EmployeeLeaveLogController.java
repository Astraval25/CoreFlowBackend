package com.astraval.coreflow.employee_module.leavelog;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.employee_module.leavelog.dto.*;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/modemp/leave-logs")
public class EmployeeLeaveLogController {

    @Autowired
    private EmployeeLeaveLogService leaveLogService;

    @PostMapping
    public ApiResponse<Map<String, Long>> createLeaveLog(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateLeaveLogDto dto) {
        try {
            Long id = leaveLogService.createLeaveLog(companyId, dto);
            return ApiResponseFactory.created(Map.of("leaveId", id), "Leave log created successfully");
        } catch (DataIntegrityViolationException e) {
            return ApiResponseFactory.error(
                    "Leave record already exists for this employee on the selected date",
                    409);
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping
    public ApiResponse<List<LeaveLogDto>> getLeaveLogs(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<LeaveLogDto> list = leaveLogService.getLeaveLogsByCompany(companyId, from, to);
            return ApiResponseFactory.accepted(list, "Leave logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<LeaveLogDto>> getLeaveLogsByEmployee(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<LeaveLogDto> list = leaveLogService.getLeaveLogsByEmployee(employeeId, from, to);
            return ApiResponseFactory.accepted(list, "Leave logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/pending")
    public ApiResponse<List<LeaveLogDto>> getPendingLeaveLogs(
            @PathVariable Long companyId) {
        try {
            List<LeaveLogDto> list = leaveLogService.getPendingLeaveLogs(companyId);
            return ApiResponseFactory.accepted(list, "Pending leave logs retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{leaveId}/review")
    public ApiResponse<Void> reviewLeaveLog(
            @PathVariable Long companyId,
            @PathVariable Long leaveId,
            @Valid @RequestBody ReviewLeaveLogDto dto) {
        try {
            leaveLogService.reviewLeaveLog(companyId, leaveId, dto);
            return ApiResponseFactory.updated(null, "Leave log reviewed successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PreAuthorize("hasRole('EMP')")
    @PutMapping("/employee")
    public ApiResponse<Void> updateMyLeaveLog(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateLeaveLogDto dto) {
        try {
            Long currentEmployeeId = extractCurrentEmployeeId();
            if (!currentEmployeeId.equals(dto.getEmployeeId())) {
                return ApiResponseFactory.error("Access denied: you can only update your own leave logs", 403);
            }

            leaveLogService.updateLeaveLog(companyId, dto);
            return ApiResponseFactory.updated(null, "Leave log updated successfully");
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
