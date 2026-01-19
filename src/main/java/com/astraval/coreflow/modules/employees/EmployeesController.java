package com.astraval.coreflow.modules.employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class EmployeesController {

    private final EmployeesService employeesService;

    @Autowired
    public EmployeesController(EmployeesService employeesService) {
        this.employeesService = employeesService;
    }

    @PostMapping("/{companyId}/employees/create")
    public String createEmployee(@PathVariable Long companyId, @RequestBody Employees employee) {
        
        employeesService.createEmployee(companyId, employee);
        return "Data saved successfully";
    }
}