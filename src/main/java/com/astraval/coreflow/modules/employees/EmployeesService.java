package com.astraval.coreflow.modules.employees;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.employees.dto.EmployeeCreateDto;
import com.astraval.coreflow.modules.employees.mapper.EmployeeMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeesService {

    private final EmployeesRepository employeesRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeesService(EmployeesRepository employeesRepository, CompanyRepository companyRepository,EmployeeMapper employeeMapper) {
        this.employeesRepository = employeesRepository;
        this.companyRepository = companyRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public void createEmployee(Long companyId, EmployeeCreateDto employeeDto) {
        // 1. Database Lookup (Service responsibility)
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        // 2. Map and Link (Mapper responsibility)
        Employees employee = employeeMapper.toEntity(employeeDto, company);

        // 3. Save
        employeesRepository.save(employee);
    }
}
