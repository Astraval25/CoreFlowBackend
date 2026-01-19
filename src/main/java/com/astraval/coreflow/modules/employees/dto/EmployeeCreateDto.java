package com.astraval.coreflow.modules.employees.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeCreateDto {

    @NotBlank(message = "Employee First name is required")
    private String firstName;

    @NotBlank(message = "Employee Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String personalemail;

    private String phoneNumber;
}
