package com.astraval.coreflow.employee_module.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.astraval.coreflow.employee_module.enums.SalaryType;

@Data
public class CreateEmployeeDto {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    private String phone;
    private String email;
    private String designation;
    private LocalDate joinedDt;

    @NotNull(message = "Salary type is required")
    private SalaryType salaryType;

    private BigDecimal monthlyAmount;
}
