package com.astraval.coreflow.modules.employees;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeesService {

    private final EmployeesRepository employeesRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public EmployeesService(EmployeesRepository employeesRepository, CompanyRepository companyRepository) {
        this.employeesRepository = employeesRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public String createEmployee(Long companyId, Employees employee) {

        Companies company = companyRepository.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company not found"));
        employee.setCompany(company);

        if (employee.getAddressId() != null) {
            Address addr = employee.getAddressId();

            String fullName = employee.getFirstName() + " " + employee.getLastName();
            addr.setAttentionName(fullName.trim());
            addr.setEmail(employee.getPersonalEmail());
            addr.setPhone(employee.getPhoneNumber());
        }

        employeesRepository.save(employee);

        return "Data saved successfully";
    }
}