package com.astraval.coreflow.modules.modemp.leavelog;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.modemp.employee.Employee;
import com.astraval.coreflow.modules.modemp.employee.EmployeeRepository;
import com.astraval.coreflow.modules.modemp.enums.LeaveStatus;
import com.astraval.coreflow.modules.modemp.leavelog.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Long createLeaveLog(Long companyId, CreateLeaveLogDto dto) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(dto.getEmployeeId(), companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

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
