package com.astraval.coreflow.employee_module.employee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.astraval.coreflow.employee_module.enums.SalaryType;
import com.astraval.coreflow.employee_module.salaryconfig.dto.SalaryConfigDto;

@Data
public class EmployeeDetailDto {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String phone;
    private String email;
    private String designation;
    private LocalDate joinedDt;
    private Boolean isActive;
    private SalaryType currentSalaryType;
    private BigDecimal currentMonthlyAmount;
    private List<SalaryConfigDto> salaryConfigHistory;
    private Long createdBy;
    private LocalDateTime createdDt;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDt;
}
