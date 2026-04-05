package com.astraval.coreflow.employee_module.employee;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.employee_module.employee.dto.*;
import com.astraval.coreflow.employee_module.portaluser.EmployeePortalUserService;
import com.astraval.coreflow.employee_module.portaluser.dto.CreatePortalUserDto;
import com.astraval.coreflow.employee_module.portaluser.dto.PortalUserDto;
import com.astraval.coreflow.employee_module.salaryconfig.EmployeeSalaryConfigService;
import com.astraval.coreflow.employee_module.salaryconfig.dto.CreateSalaryConfigDto;
import com.astraval.coreflow.employee_module.salaryconfig.dto.SalaryConfigDto;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/modemp/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeSalaryConfigService salaryConfigService;

    @Autowired
    private EmployeePortalUserService portalUserService;

    // ── Employee CRUD ──

    @PostMapping
    public ApiResponse<Map<String, Long>> createEmployee(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateEmployeeDto dto) {
        try {
            Long id = employeeService.createEmployee(companyId, dto);
            return ApiResponseFactory.created(Map.of("employeeId", id), "Employee created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping
    public ApiResponse<List<EmployeeSummaryDto>> getEmployees(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            List<EmployeeSummaryDto> list = activeOnly
                    ? employeeService.getActiveEmployees(companyId)
                    : employeeService.getEmployees(companyId);
            return ApiResponseFactory.accepted(list, "Employees retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{employeeId}")
    public ApiResponse<EmployeeDetailDto> getEmployee(
            @PathVariable Long companyId,
            @PathVariable Long employeeId) {
        try {
            EmployeeDetailDto dto = employeeService.getEmployeeDetail(companyId, employeeId);
            return ApiResponseFactory.accepted(dto, "Employee retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{employeeId}")
    public ApiResponse<Void> updateEmployee(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeDto dto) {
        try {
            employeeService.updateEmployee(companyId, employeeId, dto);
            return ApiResponseFactory.updated(null, "Employee updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{employeeId}/deactivate")
    public ApiResponse<Void> deactivateEmployee(
            @PathVariable Long companyId,
            @PathVariable Long employeeId) {
        try {
            employeeService.deactivateEmployee(companyId, employeeId);
            return ApiResponseFactory.updated(null, "Employee deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // ── Salary Config ──

    @PostMapping("/{employeeId}/salary-config")
    public ApiResponse<Map<String, Long>> createSalaryConfig(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateSalaryConfigDto dto) {
        try {
            Long id = salaryConfigService.createSalaryConfig(companyId, employeeId, dto);
            return ApiResponseFactory.created(Map.of("configId", id), "Salary config created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{employeeId}/salary-config")
    public ApiResponse<SalaryConfigDto> getActiveSalaryConfig(
            @PathVariable Long companyId,
            @PathVariable Long employeeId) {
        try {
            SalaryConfigDto dto = salaryConfigService.getActiveConfig(employeeId);
            return ApiResponseFactory.accepted(dto, "Salary config retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{employeeId}/salary-config/history")
    public ApiResponse<List<SalaryConfigDto>> getSalaryConfigHistory(
            @PathVariable Long companyId,
            @PathVariable Long employeeId) {
        try {
            List<SalaryConfigDto> list = salaryConfigService.getConfigHistory(employeeId);
            return ApiResponseFactory.accepted(list, "Salary config history retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // ── Portal User ──

    @PostMapping("/{employeeId}/portal-user")
    public ApiResponse<Map<String, Long>> createPortalUser(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @Valid @RequestBody CreatePortalUserDto dto) {
        try {
            Long id = portalUserService.createPortalUser(companyId, employeeId, dto);
            return ApiResponseFactory.created(Map.of("portalUserId", id), "Portal user created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{employeeId}/portal-user")
    public ApiResponse<PortalUserDto> getPortalUser(
            @PathVariable Long companyId,
            @PathVariable Long employeeId) {
        try {
            PortalUserDto dto = portalUserService.getPortalUser(employeeId);
            return ApiResponseFactory.accepted(dto, "Portal user retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{employeeId}/portal-user/reset-password")
    public ApiResponse<Void> resetPortalPassword(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @RequestBody Map<String, String> body) {
        try {
            String newPassword = body.get("password");
            if (newPassword == null || newPassword.isBlank()) {
                throw new RuntimeException("Password is required");
            }
            portalUserService.resetPassword(companyId, employeeId, newPassword);
            return ApiResponseFactory.updated(null, "Password reset successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
