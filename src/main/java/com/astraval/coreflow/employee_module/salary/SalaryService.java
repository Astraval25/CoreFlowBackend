package com.astraval.coreflow.employee_module.salary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.employee.EmployeeRepository;
import com.astraval.coreflow.employee_module.enums.*;
import com.astraval.coreflow.employee_module.leavelog.EmployeeLeaveLog;
import com.astraval.coreflow.employee_module.leavelog.EmployeeLeaveLogRepository;
import com.astraval.coreflow.employee_module.salary.dto.*;
import com.astraval.coreflow.employee_module.salaryconfig.EmployeeSalaryConfig;
import com.astraval.coreflow.employee_module.salaryconfig.EmployeeSalaryConfigRepository;
import com.astraval.coreflow.employee_module.worklog.EmployeeWorkLog;
import com.astraval.coreflow.employee_module.worklog.EmployeeWorkLogRepository;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();

        // Validation: fromDate must be <= toDate
        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date must be before or equal to to date");
        }

        // Validation: future dates not allowed
        if (fromDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("Cannot calculate salary for future dates");
        }

        String period = fromDate.format(DateTimeFormatter.ofPattern("yyyyMM"));

        List<Employee> employees;
        if (request.getEmployeeId() != null) {
            Employee emp = employeeRepository.findByEmployeeIdAndCompanyCompanyId(request.getEmployeeId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmployeeId()));
            if (!emp.getIsActive()) {
                throw new RuntimeException("Cannot calculate salary for deactivated employee: " + emp.getEmployeeName());
            }
            employees = List.of(emp);
        } else {
            employees = employeeRepository.findByCompanyCompanyIdAndIsActiveTrueOrderByEmployeeName(companyId);
        }

        if (employees.isEmpty()) {
            throw new RuntimeException("No active employees found for this company");
        }

        List<Long> createdIds = new ArrayList<>();

        for (Employee employee : employees) {
            EmployeeSalaryConfig activeConfig =
                    salaryConfigRepository.findByEmployeeEmployeeIdAndEffectiveToIsNull(employee.getEmployeeId())
                            .orElse(null);

            if (activeConfig == null) continue;

            // Handle exact same range first (recalculate only if existing is DRAFT)
            salaryPeriodRepository.findByEmployeeEmployeeIdAndFromDateAndToDate(
                    employee.getEmployeeId(), fromDate, toDate)
                    .ifPresent(existing -> {
                        if (existing.getStatus() != SalaryPeriodStatus.DRAFT) {
                            throw new RuntimeException("Salary for employee " + employee.getEmployeeName()
                                    + " period " + fromDate + " to " + toDate + " is already " + existing.getStatus());
                        }
                        salaryLineRepository.deleteBySalaryPeriodSalaryPeriodId(existing.getSalaryPeriodId());
                        salaryPeriodRepository.delete(existing);
                        // Ensure delete is executed before insert, avoiding unique-key violation on same key.
                        salaryPeriodRepository.flush();
                    });

            // Check for overlapping salary periods
            List<EmployeeSalaryPeriod> overlapping = salaryPeriodRepository.findOverlappingPeriods(
                    employee.getEmployeeId(), fromDate, toDate);

            if (!overlapping.isEmpty()) {
                EmployeeSalaryPeriod conflict = overlapping.getFirst();
                throw new RuntimeException("Salary for employee " + employee.getEmployeeName()
                        + " overlaps with existing period " + conflict.getFromDate()
                        + " to " + conflict.getToDate() + " (status: " + conflict.getStatus() + ")");
            }

            EmployeeSalaryPeriod salaryPeriod;
            if (activeConfig.getSalaryType() == SalaryType.MONTHLY) {
                salaryPeriod = calculateMonthlySalary(employee, activeConfig, period, fromDate, toDate);
            } else {
                salaryPeriod = calculateWorkBasedSalary(employee, period, fromDate, toDate);
            }

            if (salaryPeriod != null) {
                createdIds.add(salaryPeriod.getSalaryPeriodId());
            }
        }

        return createdIds;
    }

    private EmployeeSalaryPeriod calculateMonthlySalary(Employee employee, EmployeeSalaryConfig config,
                                                         String period, LocalDate fromDate, LocalDate toDate) {
        Companies company = employee.getCompany();

        // Total calendar days in the full month
        YearMonth ym = YearMonth.from(fromDate);
        int totalDaysInMonth = ym.lengthOfMonth();

        // Days in the requested range (inclusive)
        long daysInRange = ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        // Get approved leaves within the date range
        List<EmployeeLeaveLog> leaves = leaveLogRepository.findApprovedLeavesByEmployeeAndPeriod(
                employee.getEmployeeId(), fromDate, toDate);

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

        BigDecimal totalDaysInMonthBd = new BigDecimal(totalDaysInMonth);
        BigDecimal daysInRangeBd = new BigDecimal(daysInRange);
        BigDecimal daysPresent = daysInRangeBd.subtract(totalLeaveDays);
        BigDecimal monthlyAmount = config.getMonthlyAmount();

        // Per-day rate based on full month
        BigDecimal perDay = monthlyAmount.divide(totalDaysInMonthBd, 4, RoundingMode.HALF_UP);

        // Gross = pro-rated amount for the date range
        BigDecimal grossAmount = perDay.multiply(daysInRangeBd).setScale(2, RoundingMode.HALF_UP);

        // LOP deduction
        BigDecimal lopDeduction = perDay.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);

        // Net = gross - LOP deduction
        BigDecimal netAmount = grossAmount.subtract(lopDeduction);

        // Create salary period
        EmployeeSalaryPeriod sp = new EmployeeSalaryPeriod();
        sp.setEmployee(employee);
        sp.setCompany(company);
        sp.setPeriod(period);
        sp.setFromDate(fromDate);
        sp.setToDate(toDate);
        sp.setSalaryType(SalaryType.MONTHLY);
        sp.setWorkingDaysInMonth((int) daysInRange);
        sp.setDaysPresent(daysPresent);
        sp.setDaysAbsent(totalLeaveDays);
        sp.setLopDays(lopDays);
        sp.setGrossAmount(grossAmount);
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
        fixedLine.setDescription("Monthly salary (" + daysInRange + "/" + totalDaysInMonth + " days)");
        fixedLine.setTotalQty(daysInRangeBd);
        fixedLine.setRateUsed(perDay);
        fixedLine.setAmount(grossAmount);
        salaryLineRepository.save(fixedLine);

        if (lopDeduction.compareTo(BigDecimal.ZERO) > 0) {
            EmployeeSalaryLine deductionLine = new EmployeeSalaryLine();
            deductionLine.setSalaryPeriod(saved);
            deductionLine.setLineType(SalaryLineType.DEDUCTION);
            deductionLine.setDescription("LOP deduction (" + lopDays + " days)");
            deductionLine.setTotalQty(lopDays);
            deductionLine.setRateUsed(perDay);
            deductionLine.setAmount(lopDeduction.negate());
            salaryLineRepository.save(deductionLine);
        }

        return saved;
    }

    private EmployeeSalaryPeriod calculateWorkBasedSalary(Employee employee,
                                                           String period, LocalDate fromDate, LocalDate toDate) {
        Companies company = employee.getCompany();

        List<EmployeeWorkLog> approvedLogs = workLogRepository.findApprovedLogsByEmployeeAndPeriod(
                employee.getEmployeeId(), fromDate, toDate);

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
        sp.setFromDate(fromDate);
        sp.setToDate(toDate);
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
                .stream().map(this::toSummaryDto).toList();
    }

    public List<SalaryPeriodSummaryDto> getSalaryPeriods(Long companyId, Long employeeId, String period) {
        return salaryPeriodRepository
                .findByCompanyCompanyIdAndEmployeeEmployeeIdAndPeriodOrderByFromDateDesc(companyId, employeeId, period)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public List<SalaryPeriodSummaryDto> getSalaryPeriods(Long companyId, Long employeeId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date must be before or equal to to date");
        }

        return salaryPeriodRepository
                .findByCompanyCompanyIdAndEmployeeEmployeeIdAndFromDateGreaterThanEqualAndToDateLessThanEqualOrderByFromDateDesc(
                        companyId, employeeId, fromDate, toDate)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public SalaryPeriodDetailDto getSalaryPeriodDetail(Long companyId, Long salaryPeriodId) {
        EmployeeSalaryPeriod sp = salaryPeriodRepository.findBySalaryPeriodIdAndCompanyCompanyId(salaryPeriodId, companyId)
                .orElseThrow(() -> new RuntimeException("Salary period not found with ID: " + salaryPeriodId));

        return toDetailDto(sp);
    }

    private SalaryPeriodDetailDto toDetailDto(EmployeeSalaryPeriod sp) {
        SalaryPeriodDetailDto dto = new SalaryPeriodDetailDto();
        dto.setSalaryPeriodId(sp.getSalaryPeriodId());
        dto.setEmployeeId(sp.getEmployee().getEmployeeId());
        dto.setEmployeeName(sp.getEmployee().getEmployeeName());
        dto.setEmployeeCode(sp.getEmployee().getEmployeeCode());
        dto.setPeriod(sp.getPeriod());
        dto.setFromDate(sp.getFromDate());
        dto.setToDate(sp.getToDate());
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

        List<EmployeeSalaryLine> lines = salaryLineRepository.findBySalaryPeriodSalaryPeriodId(sp.getSalaryPeriodId());
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

    private SalaryPeriodSummaryDto toSummaryDto(EmployeeSalaryPeriod sp) {
        SalaryPeriodSummaryDto dto = new SalaryPeriodSummaryDto();
        dto.setSalaryPeriodId(sp.getSalaryPeriodId());
        dto.setEmployeeId(sp.getEmployee().getEmployeeId());
        dto.setEmployeeName(sp.getEmployee().getEmployeeName());
        dto.setEmployeeCode(sp.getEmployee().getEmployeeCode());
        dto.setPeriod(sp.getPeriod());
        dto.setFromDate(sp.getFromDate());
        dto.setToDate(sp.getToDate());
        dto.setSalaryType(sp.getSalaryType());
        dto.setGrossAmount(sp.getGrossAmount());
        dto.setNetAmount(sp.getNetAmount());
        dto.setStatus(sp.getStatus());
        return dto;
    }

    public SalaryReportDto getSalaryReport(Long companyId, LocalDate fromDate, LocalDate toDate) {
        List<EmployeeSalaryPeriod> periods = salaryPeriodRepository
                .findByCompanyCompanyIdAndFromDateGreaterThanEqualAndToDateLessThanEqualOrderByEmployeeEmployeeName(
                        companyId, fromDate, toDate);

        List<SalaryPeriodDetailDto> details = periods.stream()
                .map(this::toDetailDto)
                .toList();

        BigDecimal totalGross = periods.stream()
                .map(EmployeeSalaryPeriod::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductions = periods.stream()
                .map(sp -> sp.getLopDeduction().add(sp.getOtherDeductions()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = periods.stream()
                .map(EmployeeSalaryPeriod::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SalaryReportDto report = new SalaryReportDto();
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setTotalEmployees(details.size());
        report.setTotalGrossAmount(totalGross);
        report.setTotalDeductions(totalDeductions);
        report.setTotalNetAmount(totalNet);
        report.setSalaryDetails(details);

        return report;
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
