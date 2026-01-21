package com.astraval.coreflow.modules.employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.astraval.coreflow.modules.employees.dto.EmployeeCreateDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class EmployeesController {

    private final EmployeesService employeesService;

    @Autowired
    public EmployeesController(EmployeesService employeesService) {
        this.employeesService = employeesService;
    }

    @PostMapping("/{companyId}/employees/create")
    public String createEmployee(@PathVariable Long companyId, @Valid @RequestBody EmployeeCreateDto employeeDto) {
        
        employeesService.createEmployee(companyId, employeeDto);
        return "Data saved successfully";
    }
}