package com.astraval.coreflow.employee_module.employee.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEmployeeDto {
    private String employeeName;
    private String phone;
    private String email;
    private String designation;
    private LocalDate joinedDt;
}
