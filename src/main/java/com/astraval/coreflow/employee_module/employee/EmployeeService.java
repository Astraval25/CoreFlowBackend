package com.astraval.coreflow.employee_module.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.employee_module.employee.dto.*;
import com.astraval.coreflow.employee_module.enums.LeaveStatus;
import com.astraval.coreflow.employee_module.enums.SalaryType;
import com.astraval.coreflow.employee_module.enums.WorkLogStatus;
import com.astraval.coreflow.employee_module.leavelog.EmployeeLeaveLogRepository;
import com.astraval.coreflow.employee_module.salaryconfig.EmployeeSalaryConfig;
import com.astraval.coreflow.employee_module.salaryconfig.EmployeeSalaryConfigRepository;
import com.astraval.coreflow.employee_module.salaryconfig.dto.SalaryConfigDto;
import com.astraval.coreflow.employee_module.worklog.EmployeeWorkLogRepository;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.notification.NotificationService;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmployeeSalaryConfigRepository salaryConfigRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmployeeWorkLogRepository employeeWorkLogRepository;

    @Autowired
    private EmployeeLeaveLogRepository employeeLeaveLogRepository;

    @Transactional
    public Long createEmployee(Long companyId, CreateEmployeeDto dto) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        if (employeeRepository.existsByCompanyCompanyIdAndEmployeeCode(companyId, dto.getEmployeeCode())) {
            throw new RuntimeException("Employee code '" + dto.getEmployeeCode() + "' already exists in this company");
        }

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setEmployeeName(dto.getEmployeeName());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setDesignation(dto.getDesignation());
        employee.setJoinedDt(dto.getJoinedDt());
        employee.setIsActive(true);

        Employee saved = employeeRepository.save(employee);

        // Create initial salary config
        if (dto.getSalaryType() != null) {
            if (dto.getSalaryType() == SalaryType.MONTHLY && dto.getMonthlyAmount() == null) {
                throw new RuntimeException("Monthly amount is required for MONTHLY salary type");
            }

            EmployeeSalaryConfig config = new EmployeeSalaryConfig();
            config.setEmployee(saved);
            config.setSalaryType(dto.getSalaryType());
            config.setMonthlyAmount(dto.getSalaryType() == SalaryType.MONTHLY ? dto.getMonthlyAmount() : null);
            config.setEffectiveFrom(dto.getJoinedDt() != null ? dto.getJoinedDt() : LocalDate.now());
            salaryConfigRepository.save(config);
        }

        return saved.getEmployeeId();
    }

    public List<EmployeeSummaryDto> getEmployees(Long companyId) {
        List<Employee> employees = employeeRepository.findByCompanyCompanyIdOrderByEmployeeName(companyId);
        List<EmployeeSummaryDto> summaries = applyUnreadCounts(
                employees.stream().map(this::toSummaryDto).toList(),
                companyId);
        return applyPendingCounts(summaries, companyId);
    }

    public List<EmployeeSummaryDto> getActiveEmployees(Long companyId) {
        List<Employee> employees = employeeRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByEmployeeName(companyId);
        List<EmployeeSummaryDto> summaries = applyUnreadCounts(
                employees.stream().map(this::toSummaryDto).toList(),
                companyId);
        return applyPendingCounts(summaries, companyId);
    }

    public EmployeeDetailDto getEmployeeDetail(Long companyId, Long employeeId) {
        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        EmployeeDetailDto dto = new EmployeeDetailDto();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setEmployeeName(employee.getEmployeeName());
        dto.setPhone(employee.getPhone());
        dto.setEmail(employee.getEmail());
        dto.setDesignation(employee.getDesignation());
        dto.setJoinedDt(employee.getJoinedDt());
        dto.setIsActive(employee.getIsActive());
        dto.setCreatedBy(employee.getCreatedBy());
        dto.setCreatedDt(employee.getCreatedDt());
        dto.setLastModifiedBy(employee.getLastModifiedBy());
        dto.setLastModifiedDt(employee.getLastModifiedDt());

        // Active salary config
        Optional<EmployeeSalaryConfig> activeConfig =
                salaryConfigRepository.findByEmployeeEmployeeIdAndEffectiveToIsNull(employeeId);
        activeConfig.ifPresent(config -> {
            dto.setCurrentSalaryType(config.getSalaryType());
            dto.setCurrentMonthlyAmount(config.getMonthlyAmount());
        });

        // Salary config history
        List<EmployeeSalaryConfig> configs =
                salaryConfigRepository.findByEmployeeEmployeeIdOrderByEffectiveFromDesc(employeeId);
        dto.setSalaryConfigHistory(configs.stream().map(c -> new SalaryConfigDto(
                c.getConfigId(), c.getSalaryType(), c.getMonthlyAmount(),
                c.getEffectiveFrom(), c.getEffectiveTo()
        )).toList());

        return dto;
    }

    @Transactional
    public void updateEmployee(Long companyId, Long employeeId, UpdateEmployeeDto dto) {
        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        if (dto.getEmployeeName() != null) employee.setEmployeeName(dto.getEmployeeName());
        if (dto.getPhone() != null) employee.setPhone(dto.getPhone());
        if (dto.getEmail() != null) employee.setEmail(dto.getEmail());
        if (dto.getDesignation() != null) employee.setDesignation(dto.getDesignation());
        if (dto.getJoinedDt() != null) employee.setJoinedDt(dto.getJoinedDt());

        employeeRepository.save(employee);
    }

    @Transactional
    public void deactivateEmployee(Long companyId, Long employeeId) {
        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    @Transactional
    public void activateEmployee(Long companyId, Long employeeId) {
        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        employee.setIsActive(true);
        employeeRepository.save(employee);
    }

    private EmployeeSummaryDto toSummaryDto(Employee emp) {
        EmployeeSummaryDto dto = new EmployeeSummaryDto();
        dto.setEmployeeId(emp.getEmployeeId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setEmployeeName(emp.getEmployeeName());
        dto.setPhone(emp.getPhone());
        dto.setDesignation(emp.getDesignation());
        dto.setIsActive(emp.getIsActive());

        salaryConfigRepository.findByEmployeeEmployeeIdAndEffectiveToIsNull(emp.getEmployeeId())
                .ifPresent(config -> {
                    dto.setCurrentSalaryType(config.getSalaryType());
                    dto.setCurrentMonthlyAmount(config.getMonthlyAmount());
                });
        dto.setUnreadCount(0L);
        dto.setPendingWorkLogCount(0L);
        dto.setPendingLeaveLogCount(0L);
        dto.setPendingTotalCount(0L);

        return dto;
    }

    private List<EmployeeSummaryDto> applyUnreadCounts(List<EmployeeSummaryDto> employees, Long companyId) {
        var unreadCountByEmployee = notificationService.getCompanyUnreadCountBySubjectType(companyId, "EMPLOYEE");
        employees.forEach(employee -> employee.setUnreadCount(
                unreadCountByEmployee.getOrDefault(employee.getEmployeeId(), 0L)));
        return employees;
    }

    private List<EmployeeSummaryDto> applyPendingCounts(List<EmployeeSummaryDto> employees, Long companyId) {
        Map<Long, Long> pendingWorkCountByEmployee = employeeWorkLogRepository
                .countByCompanyAndStatusGroupedByEmployee(companyId, WorkLogStatus.PENDING)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()));

        Map<Long, Long> pendingLeaveCountByEmployee = employeeLeaveLogRepository
                .countByCompanyAndStatusGroupedByEmployee(companyId, LeaveStatus.PENDING)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()));

        employees.forEach(employee -> {
            Long pendingWork = pendingWorkCountByEmployee.getOrDefault(employee.getEmployeeId(), 0L);
            Long pendingLeave = pendingLeaveCountByEmployee.getOrDefault(employee.getEmployeeId(), 0L);
            employee.setPendingWorkLogCount(pendingWork);
            employee.setPendingLeaveLogCount(pendingLeave);
            employee.setPendingTotalCount(pendingWork + pendingLeave);
        });

        return employees;
    }
}
