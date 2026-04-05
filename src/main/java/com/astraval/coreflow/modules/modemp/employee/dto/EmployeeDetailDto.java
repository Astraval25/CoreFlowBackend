package com.astraval.coreflow.modules.modemp.employee.dto;

import com.astraval.coreflow.modules.modemp.enums.SalaryType;
import com.astraval.coreflow.modules.modemp.salaryconfig.dto.SalaryConfigDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
