package com.astraval.coreflow.employee_module.leavelog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.employee.EmployeeRepository;
import com.astraval.coreflow.employee_module.enums.LeaveStatus;
import com.astraval.coreflow.employee_module.leavelog.dto.*;
import com.astraval.coreflow.employee_module.salary.EmployeeSalaryPeriodRepository;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeLeaveLogService {

    @Autowired
    private EmployeeLeaveLogRepository leaveLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmployeeSalaryPeriodRepository salaryPeriodRepository;

    @Transactional
    public Long createLeaveLog(Long companyId, CreateLeaveLogDto dto) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(dto.getEmployeeId(), companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

        if (leaveLogRepository.existsByEmployeeEmployeeIdAndCompanyCompanyIdAndLeaveDate(
                employee.getEmployeeId(), companyId, dto.getLeaveDate())) {
            throw new RuntimeException("Leave record already exists for employee "
                    + employee.getEmployeeName() + " on " + dto.getLeaveDate());
        }

        if (salaryPeriodRepository.existsSalaryPeriodForEmployeeOnDate(companyId, employee.getEmployeeId(),
                dto.getLeaveDate())) {
            throw new RuntimeException("Cannot create leave log for " + dto.getLeaveDate()
                    + " because salary is already calculated for this date. "
                    + "Please use the admin salary adjustment API with reason.");
        }

        EmployeeLeaveLog log = new EmployeeLeaveLog();
        log.setEmployee(employee);
        log.setCompany(company);
        log.setLeaveDate(dto.getLeaveDate());
        log.setLeaveType(dto.getLeaveType());
        log.setLeaveCategory(dto.getLeaveCategory());
        log.setReason(dto.getReason());
        log.setStatus(LeaveStatus.PENDING);

        EmployeeLeaveLog saved = leaveLogRepository.save(log);
        return saved.getLeaveId();
    }

    public List<LeaveLogDto> getLeaveLogsByCompany(Long companyId, LocalDate from, LocalDate to) {
        return leaveLogRepository.findByCompanyCompanyIdAndLeaveDateBetweenOrderByLeaveDateDesc(companyId, from, to)
                .stream().map(this::toDto).toList();
    }

    public List<LeaveLogDto> getLeaveLogsByEmployee(Long employeeId, LocalDate from, LocalDate to) {
        return leaveLogRepository.findByEmployeeEmployeeIdAndLeaveDateBetweenOrderByLeaveDateDesc(employeeId, from, to)
                .stream().map(this::toDto).toList();
    }

    public List<LeaveLogDto> getPendingLeaveLogs(Long companyId) {
        return leaveLogRepository.findByCompanyCompanyIdAndStatusOrderByLeaveDateDesc(companyId, LeaveStatus.PENDING)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void reviewLeaveLog(Long companyId, Long leaveId, ReviewLeaveLogDto dto) {
        EmployeeLeaveLog log = leaveLogRepository.findByLeaveIdAndCompanyCompanyId(leaveId, companyId)
                .orElseThrow(() -> new RuntimeException("Leave log not found with ID: " + leaveId));

        if (log.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave log has already been reviewed");
        }

        if (dto.getStatus() == LeaveStatus.PENDING) {
            throw new RuntimeException("Status must be APPROVED or REJECTED");
        }

        log.setStatus(dto.getStatus());
        log.setApprovedBy(getCurrentUserId());
        log.setApprovedDt(LocalDateTime.now());

        leaveLogRepository.save(log);
    }

    @Transactional
    public void updateLeaveLog(Long companyId, CreateLeaveLogDto dto) {
        employeeRepository.findByEmployeeIdAndCompanyCompanyId(dto.getEmployeeId(), companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

        EmployeeLeaveLog log = leaveLogRepository
                .findByEmployeeEmployeeIdAndCompanyCompanyIdAndLeaveDate(dto.getEmployeeId(), companyId, dto.getLeaveDate())
                .orElseThrow(() -> new RuntimeException("Leave log not found for the given employee and leave date"));

        if (log.getStatus() == LeaveStatus.APPROVED) {
            throw new RuntimeException("Cannot update an APPROVED leave log");
        }

        log.setLeaveType(dto.getLeaveType());
        log.setLeaveCategory(dto.getLeaveCategory());
        log.setReason(dto.getReason());
        log.setStatus(LeaveStatus.PENDING);
        log.setApprovedBy(null);
        log.setApprovedDt(null);

        leaveLogRepository.save(log);
    }

    private LeaveLogDto toDto(EmployeeLeaveLog log) {
        LeaveLogDto dto = new LeaveLogDto();
        dto.setLeaveId(log.getLeaveId());
        dto.setEmployeeId(log.getEmployee().getEmployeeId());
        dto.setEmployeeName(log.getEmployee().getEmployeeName());
        dto.setLeaveDate(log.getLeaveDate());
        dto.setLeaveType(log.getLeaveType());
        dto.setLeaveCategory(log.getLeaveCategory());
        dto.setReason(log.getReason());
        dto.setStatus(log.getStatus());
        dto.setApprovedBy(log.getApprovedBy());
        dto.setApprovedDt(log.getApprovedDt());
        return dto;
    }

    private Long getCurrentUserId() {
        try {
            return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return 0L;
        }
    }
}
