package com.astraval.coreflow.employee_module.salaryconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.employee_module.employee.Employee;
import com.astraval.coreflow.employee_module.employee.EmployeeRepository;
import com.astraval.coreflow.employee_module.enums.SalaryType;
import com.astraval.coreflow.employee_module.salaryconfig.dto.CreateSalaryConfigDto;
import com.astraval.coreflow.employee_module.salaryconfig.dto.SalaryConfigDto;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeSalaryConfigService {

    @Autowired
    private EmployeeSalaryConfigRepository salaryConfigRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public Long createSalaryConfig(Long companyId, Long employeeId, CreateSalaryConfigDto dto) {
        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        // Close the current active config
        Optional<EmployeeSalaryConfig> activeConfig =
                salaryConfigRepository.findByEmployeeEmployeeIdAndEffectiveToIsNull(employeeId);
        activeConfig.ifPresent(config -> {
            config.setEffectiveTo(dto.getEffectiveFrom().minusDays(1));
            salaryConfigRepository.save(config);
        });

        // Validate salary type
        if (dto.getSalaryType() == SalaryType.MONTHLY && dto.getMonthlyAmount() == null) {
            throw new RuntimeException("Monthly amount is required for MONTHLY salary type");
        }

        // Create new config
        EmployeeSalaryConfig newConfig = new EmployeeSalaryConfig();
        newConfig.setEmployee(employee);
        newConfig.setSalaryType(dto.getSalaryType());
        newConfig.setMonthlyAmount(dto.getSalaryType() == SalaryType.MONTHLY ? dto.getMonthlyAmount() : null);
        newConfig.setEffectiveFrom(dto.getEffectiveFrom());

        EmployeeSalaryConfig saved = salaryConfigRepository.save(newConfig);
        return saved.getConfigId();
    }

    public SalaryConfigDto getActiveConfig(Long employeeId) {
        return salaryConfigRepository.findByEmployeeEmployeeIdAndEffectiveToIsNull(employeeId)
                .map(c -> new SalaryConfigDto(c.getConfigId(), c.getSalaryType(),
                        c.getMonthlyAmount(), c.getEffectiveFrom(), c.getEffectiveTo()))
                .orElse(null);
    }

    public List<SalaryConfigDto> getConfigHistory(Long employeeId) {
        return salaryConfigRepository.findByEmployeeEmployeeIdOrderByEffectiveFromDesc(employeeId)
                .stream()
                .map(c -> new SalaryConfigDto(c.getConfigId(), c.getSalaryType(),
                        c.getMonthlyAmount(), c.getEffectiveFrom(), c.getEffectiveTo()))
                .toList();
    }
}
