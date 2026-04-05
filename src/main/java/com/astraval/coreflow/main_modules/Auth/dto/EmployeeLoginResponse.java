package com.astraval.coreflow.main_modules.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginResponse {
    private String token;
    private String refreshToken;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long companyId;
    private String companyName;
    private String designation;
}
