package com.astraval.coreflow.modules.employees.dto;

import java.time.LocalDate;

//import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.dto.CreateUpdateAddressDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeCreateDto {

    @NotBlank(message = "Employee First name is required")
    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format")
    private String personalemail;

    private String officialEmail;

    private String phoneNumber;

    private String jobTitle;

    private LocalDate hireDate;

    private LocalDate terminationDate;

    private LocalDate dateOfBirth;

    private CreateUpdateAddressDto address;

}
