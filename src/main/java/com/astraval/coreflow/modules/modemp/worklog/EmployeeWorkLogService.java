package com.astraval.coreflow.modules.modemp.worklog;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.modemp.employee.Employee;
import com.astraval.coreflow.modules.modemp.employee.EmployeeRepository;
import com.astraval.coreflow.modules.modemp.enums.WorkLogStatus;
import com.astraval.coreflow.modules.modemp.salary.EmployeeSalaryPeriodRepository;
import com.astraval.coreflow.modules.modemp.workdef.WorkDefinition;
import com.astraval.coreflow.modules.modemp.workdef.WorkDefinitionRepository;
import com.astraval.coreflow.modules.modemp.worklog.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeWorkLogService {

    @Autowired
    private EmployeeWorkLogRepository workLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkDefinitionRepository workDefinitionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmployeeSalaryPeriodRepository salaryPeriodRepository;

    @Transactional
    public Long createWorkLog(Long companyId, CreateWorkLogDto dto) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(dto.getEmployeeId(), companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

        if (salaryPeriodRepository.existsSalaryPeriodForEmployeeOnDate(companyId, employee.getEmployeeId(),
                dto.getLogDate())) {
            throw new RuntimeException("Cannot create work log for " + dto.getLogDate()
                    + " because salary is already calculated for this date. "
                    + "Please Contact the admin to adjustment  salary .");
        }

        WorkDefinition workDef = workDefinitionRepository.findByWorkDefIdAndCompanyCompanyId(dto.getWorkDefId(), companyId)
                .orElseThrow(() -> new RuntimeException("Work definition not found with ID: " + dto.getWorkDefId()));

        if (!workDef.getIsActive()) {
            throw new RuntimeException("Work definition '" + workDef.getWorkName() + "' is inactive");
        }

        EmployeeWorkLog log = new EmployeeWorkLog();
        log.setEmployee(employee);
        log.setCompany(company);
        log.setWorkDefinition(workDef);
        log.setLogDate(dto.getLogDate());
        log.setQuantity(dto.getQuantity());
        log.setUnit(workDef.getUnit());
        log.setRateSnapshot(workDef.getRatePerUnit());
        log.setAmountEarned(dto.getQuantity().multiply(workDef.getRatePerUnit()));
        log.setEmployeeRemarks(dto.getEmployeeRemarks());
        log.setStatus(WorkLogStatus.PENDING);

        EmployeeWorkLog saved = workLogRepository.save(log);
        return saved.getLogId();
    }

    public List<WorkLogDto> getWorkLogsByCompany(Long companyId, LocalDate from, LocalDate to) {
        return workLogRepository.findByCompanyCompanyIdAndLogDateBetweenOrderByLogDateDesc(companyId, from, to)
                .stream().map(this::toDto).toList();
    }

    public List<WorkLogDto> getWorkLogsByEmployee(Long employeeId, LocalDate from, LocalDate to) {
        return workLogRepository.findByEmployeeEmployeeIdAndLogDateBetweenOrderByLogDateDesc(employeeId, from, to)
                .stream().map(this::toDto).toList();
    }

    public List<WorkLogDto> getPendingWorkLogs(Long companyId) {
        return workLogRepository.findByCompanyCompanyIdAndStatusOrderByLogDateDesc(companyId, WorkLogStatus.PENDING)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void reviewWorkLog(Long companyId, Long logId, ReviewWorkLogDto dto) {
        EmployeeWorkLog log = workLogRepository.findByLogIdAndCompanyCompanyId(logId, companyId)
                .orElseThrow(() -> new RuntimeException("Work log not found with ID: " + logId));

        if (log.getStatus() != WorkLogStatus.PENDING) {
            throw new RuntimeException("Work log has already been reviewed");
        }

        if (dto.getStatus() == WorkLogStatus.PENDING) {
            throw new RuntimeException("Status must be APPROVED or REJECTED");
        }

        log.setStatus(dto.getStatus());
        log.setAdminRemarks(dto.getAdminRemarks());
        log.setReviewedBy(getCurrentUserId());
        log.setReviewedDt(LocalDateTime.now());

        workLogRepository.save(log);
    }

    private WorkLogDto toDto(EmployeeWorkLog log) {
        WorkLogDto dto = new WorkLogDto();
        dto.setLogId(log.getLogId());
        dto.setEmployeeId(log.getEmployee().getEmployeeId());
        dto.setEmployeeName(log.getEmployee().getEmployeeName());
        dto.setWorkDefId(log.getWorkDefinition().getWorkDefId());
        dto.setWorkName(log.getWorkDefinition().getWorkName());
        dto.setLogDate(log.getLogDate());
        dto.setQuantity(log.getQuantity());
        dto.setUnit(log.getUnit());
        dto.setRateSnapshot(log.getRateSnapshot());
        dto.setAmountEarned(log.getAmountEarned());
        dto.setEmployeeRemarks(log.getEmployeeRemarks());
        dto.setStatus(log.getStatus());
        dto.setReviewedBy(log.getReviewedBy());
        dto.setReviewedDt(log.getReviewedDt());
        dto.setAdminRemarks(log.getAdminRemarks());
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
