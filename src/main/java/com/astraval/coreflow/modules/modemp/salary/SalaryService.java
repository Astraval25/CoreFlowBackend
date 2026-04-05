package com.astraval.coreflow.modules.modemp.salary;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.modemp.employee.Employee;
import com.astraval.coreflow.modules.modemp.employee.EmployeeRepository;
import com.astraval.coreflow.modules.modemp.enums.*;
import com.astraval.coreflow.modules.modemp.leavelog.EmployeeLeaveLog;
import com.astraval.coreflow.modules.modemp.leavelog.EmployeeLeaveLogRepository;
import com.astraval.coreflow.modules.modemp.salaryconfig.EmployeeSalaryConfig;
import com.astraval.coreflow.modules.modemp.salaryconfig.EmployeeSalaryConfigRepository;
import com.astraval.coreflow.modules.modemp.salary.dto.*;
import com.astraval.coreflow.modules.modemp.worklog.EmployeeWorkLog;
import com.astraval.coreflow.modules.modemp.worklog.EmployeeWorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    @Autowired
    private EmployeeSalaryPeriodRepository salaryPeriodRepository;

    @Autowired
    private EmployeeSalaryLineRepository salaryLineRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmployeeSalaryConfigRepository salaryConfigRepository;

    @Autowired
    private EmployeeWorkLogRepository workLogRepository;

    @Autowired
    private EmployeeLeaveLogRepository leaveLogRepository;

    @Transactional
    public List<Long> calculateSalary(Long companyId, CalculateSalaryRequestDto request) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        String period = request.getPeriod();
        YearMonth ym = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyyMM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Employee> employees;
        if (request.getEmployeeId() != null) {
            Employee emp = employeeRepository.findByEmployeeIdAndCompanyCompanyId(request.getEmployeeId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmployeeId()));
            employees = List.of(emp);
        } else {
            employees = employeeRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByEmployeeName(companyId);
        }

        List<Long> createdIds = new ArrayList<>();

        for (Employee employee : employees) {
            EmployeeSalaryConfig activeConfig =
                    salaryConfigRepository.findByEmployeeEmployeeIdAndEffectiveToIsNull(employee.getEmployeeId())
                            .orElse(null);

            if (activeConfig == null) continue;

            // Delete existing DRAFT period if recalculating
            salaryPeriodRepository.findByEmployeeEmployeeIdAndPeriod(employee.getEmployeeId(), period)
                    .ifPresent(existing -> {
                        if (existing.getStatus() != SalaryPeriodStatus.DRAFT) {
                            throw new RuntimeException("Salary for employee " + employee.getEmployeeName()
                                    + " period " + period + " is already " + existing.getStatus());
                        }
                        salaryLineRepository.deleteBySalaryPeriodSalaryPeriodId(existing.getSalaryPeriodId());
                        salaryPeriodRepository.delete(existing);
                    });

            EmployeeSalaryPeriod salaryPeriod;
            if (activeConfig.getSalaryType() == SalaryType.MONTHLY) {
                salaryPeriod = calculateMonthlySalary(employee, activeConfig, companyId,
                        period, request.getWorkingDaysInMonth(), startDate, endDate);
            } else {
                salaryPeriod = calculateWorkBasedSalary(employee, companyId,
                        period, startDate, endDate);
            }

            if (salaryPeriod != null) {
                createdIds.add(salaryPeriod.getSalaryPeriodId());
            }
        }

        return createdIds;
    }

    private EmployeeSalaryPeriod calculateMonthlySalary(Employee employee, EmployeeSalaryConfig config,
                                                         Long companyId, String period,
                                                         Integer workingDays, LocalDate startDate, LocalDate endDate) {
        Companies company = employee.getCompany();

        // Get approved leaves for the period
        List<EmployeeLeaveLog> leaves = leaveLogRepository.findApprovedLeavesByEmployeeAndPeriod(
                employee.getEmployeeId(), startDate, endDate);

        BigDecimal totalLeaveDays = BigDecimal.ZERO;
        BigDecimal lopDays = BigDecimal.ZERO;

        for (EmployeeLeaveLog leave : leaves) {
            BigDecimal dayValue = leave.getLeaveType() == LeaveType.HALF_DAY
                    ? new BigDecimal("0.5") : BigDecimal.ONE;

            totalLeaveDays = totalLeaveDays.add(dayValue);

            if (leave.getLeaveCategory() == LeaveCategory.UNPAID
                    || leave.getLeaveCategory() == LeaveCategory.LOP) {
                lopDays = lopDays.add(dayValue);
            }
        }

        BigDecimal workingDaysBd = new BigDecimal(workingDays);
        BigDecimal daysPresent = workingDaysBd.subtract(totalLeaveDays);
        BigDecimal monthlyAmount = config.getMonthlyAmount();
        BigDecimal perDay = monthlyAmount.divide(workingDaysBd, 2, RoundingMode.HALF_UP);
        BigDecimal lopDeduction = perDay.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = monthlyAmount.subtract(lopDeduction);

        // Create salary period
        EmployeeSalaryPeriod sp = new EmployeeSalaryPeriod();
        sp.setEmployee(employee);
        sp.setCompany(company);
        sp.setPeriod(period);
        sp.setSalaryType(SalaryType.MONTHLY);
        sp.setWorkingDaysInMonth(workingDays);
        sp.setDaysPresent(daysPresent);
        sp.setDaysAbsent(totalLeaveDays);
        sp.setLopDays(lopDays);
        sp.setGrossAmount(monthlyAmount);
        sp.setLopDeduction(lopDeduction);
        sp.setOtherDeductions(BigDecimal.ZERO);
        sp.setNetAmount(netAmount);
        sp.setStatus(SalaryPeriodStatus.DRAFT);
        sp.setComputedDt(LocalDateTime.now());

        EmployeeSalaryPeriod saved = salaryPeriodRepository.save(sp);

        // Create salary lines
        EmployeeSalaryLine fixedLine = new EmployeeSalaryLine();
        fixedLine.setSalaryPeriod(saved);
        fixedLine.setLineType(SalaryLineType.FIXED);
        fixedLine.setDescription("Monthly salary");
        fixedLine.setAmount(monthlyAmount);
        salaryLineRepository.save(fixedLine);

        if (lopDeduction.compareTo(BigDecimal.ZERO) > 0) {
            EmployeeSalaryLine deductionLine = new EmployeeSalaryLine();
            deductionLine.setSalaryPeriod(saved);
            deductionLine.setLineType(SalaryLineType.DEDUCTION);
            deductionLine.setDescription("LOP (" + lopDays + " days)");
            deductionLine.setAmount(lopDeduction.negate());
            salaryLineRepository.save(deductionLine);
        }

        return saved;
    }

    private EmployeeSalaryPeriod calculateWorkBasedSalary(Employee employee, Long companyId,
                                                           String period, LocalDate startDate, LocalDate endDate) {
        Companies company = employee.getCompany();

        List<EmployeeWorkLog> approvedLogs = workLogRepository.findApprovedLogsByEmployeeAndPeriod(
                employee.getEmployeeId(), startDate, endDate);

        if (approvedLogs.isEmpty()) return null;

        // Group by work definition
        Map<Long, List<EmployeeWorkLog>> grouped = approvedLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getWorkDefinition().getWorkDefId()));

        BigDecimal grossAmount = approvedLogs.stream()
                .map(EmployeeWorkLog::getAmountEarned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        EmployeeSalaryPeriod sp = new EmployeeSalaryPeriod();
        sp.setEmployee(employee);
        sp.setCompany(company);
        sp.setPeriod(period);
        sp.setSalaryType(SalaryType.WORK_BASED);
        sp.setGrossAmount(grossAmount);
        sp.setLopDeduction(BigDecimal.ZERO);
        sp.setOtherDeductions(BigDecimal.ZERO);
        sp.setNetAmount(grossAmount);
        sp.setStatus(SalaryPeriodStatus.DRAFT);
        sp.setComputedDt(LocalDateTime.now());

        EmployeeSalaryPeriod saved = salaryPeriodRepository.save(sp);

        // Create salary lines per work type
        for (Map.Entry<Long, List<EmployeeWorkLog>> entry : grouped.entrySet()) {
            List<EmployeeWorkLog> logs = entry.getValue();
            EmployeeWorkLog firstLog = logs.getFirst();

            BigDecimal totalQty = logs.stream()
                    .map(EmployeeWorkLog::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalAmount = logs.stream()
                    .map(EmployeeWorkLog::getAmountEarned)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            EmployeeSalaryLine line = new EmployeeSalaryLine();
            line.setSalaryPeriod(saved);
            line.setWorkDefinition(firstLog.getWorkDefinition());
            line.setLineType(SalaryLineType.WORK_EARNING);
            line.setDescription(firstLog.getWorkDefinition().getWorkName());
            line.setTotalQty(totalQty);
            line.setUnit(firstLog.getUnit());
            line.setRateUsed(firstLog.getRateSnapshot());
            line.setAmount(totalAmount);
            salaryLineRepository.save(line);
        }

        return saved;
    }

    public List<SalaryPeriodSummaryDto> getSalaryPeriods(Long companyId, String period) {
        return salaryPeriodRepository.findByCompanyCompanyIdAndPeriodOrderByEmployeeEmployeeName(companyId, period)
                .stream().map(sp -> new SalaryPeriodSummaryDto(
                        sp.getSalaryPeriodId(),
                        sp.getEmployee().getEmployeeId(),
                        sp.getEmployee().getEmployeeName(),
                        sp.getEmployee().getEmployeeCode(),
                        sp.getPeriod(),
                        sp.getSalaryType(),
                        sp.getGrossAmount(),
                        sp.getNetAmount(),
                        sp.getStatus()
                )).toList();
    }

    public SalaryPeriodDetailDto getSalaryPeriodDetail(Long companyId, Long salaryPeriodId) {
        EmployeeSalaryPeriod sp = salaryPeriodRepository.findBySalaryPeriodIdAndCompanyCompanyId(salaryPeriodId, companyId)
                .orElseThrow(() -> new RuntimeException("Salary period not found with ID: " + salaryPeriodId));

        SalaryPeriodDetailDto dto = new SalaryPeriodDetailDto();
        dto.setSalaryPeriodId(sp.getSalaryPeriodId());
        dto.setEmployeeId(sp.getEmployee().getEmployeeId());
        dto.setEmployeeName(sp.getEmployee().getEmployeeName());
        dto.setEmployeeCode(sp.getEmployee().getEmployeeCode());
        dto.setPeriod(sp.getPeriod());
        dto.setSalaryType(sp.getSalaryType());
        dto.setWorkingDaysInMonth(sp.getWorkingDaysInMonth());
        dto.setDaysPresent(sp.getDaysPresent());
        dto.setDaysAbsent(sp.getDaysAbsent());
        dto.setLopDays(sp.getLopDays());
        dto.setGrossAmount(sp.getGrossAmount());
        dto.setLopDeduction(sp.getLopDeduction());
        dto.setOtherDeductions(sp.getOtherDeductions());
        dto.setNetAmount(sp.getNetAmount());
        dto.setStatus(sp.getStatus());
        dto.setApprovedBy(sp.getApprovedBy());
        dto.setApprovedDt(sp.getApprovedDt());
        dto.setPaidDt(sp.getPaidDt());
        dto.setPaymentRef(sp.getPaymentRef());
        dto.setComputedDt(sp.getComputedDt());

        List<EmployeeSalaryLine> lines = salaryLineRepository.findBySalaryPeriodSalaryPeriodId(salaryPeriodId);
        dto.setLines(lines.stream().map(line -> new SalaryLineDto(
                line.getLineId(),
                line.getLineType(),
                line.getDescription(),
                line.getTotalQty(),
                line.getUnit(),
                line.getRateUsed(),
                line.getAmount(),
                line.getWorkDefinition() != null ? line.getWorkDefinition().getWorkDefId() : null,
                line.getWorkDefinition() != null ? line.getWorkDefinition().getWorkName() : null
        )).toList());

        return dto;
    }

    @Transactional
    public void approveSalaryPeriod(Long companyId, Long salaryPeriodId) {
        EmployeeSalaryPeriod sp = salaryPeriodRepository.findBySalaryPeriodIdAndCompanyCompanyId(salaryPeriodId, companyId)
                .orElseThrow(() -> new RuntimeException("Salary period not found with ID: " + salaryPeriodId));

        if (sp.getStatus() != SalaryPeriodStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT salary periods can be approved");
        }

        sp.setStatus(SalaryPeriodStatus.APPROVED);
        sp.setApprovedBy(getCurrentUserId());
        sp.setApprovedDt(LocalDateTime.now());
        salaryPeriodRepository.save(sp);
    }

    @Transactional
    public void markSalaryPaid(Long companyId, Long salaryPeriodId, MarkPaidDto dto) {
        EmployeeSalaryPeriod sp = salaryPeriodRepository.findBySalaryPeriodIdAndCompanyCompanyId(salaryPeriodId, companyId)
                .orElseThrow(() -> new RuntimeException("Salary period not found with ID: " + salaryPeriodId));

        if (sp.getStatus() != SalaryPeriodStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED salary periods can be marked as paid");
        }

        sp.setStatus(SalaryPeriodStatus.PAID);
        sp.setPaidDt(LocalDateTime.now());
        sp.setPaymentRef(dto != null ? dto.getPaymentRef() : null);
        salaryPeriodRepository.save(sp);
    }

    private Long getCurrentUserId() {
        try {
            return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return 0L;
        }
    }
}
