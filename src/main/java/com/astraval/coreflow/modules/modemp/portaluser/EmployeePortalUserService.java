package com.astraval.coreflow.modules.modemp.portaluser;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.modemp.employee.Employee;
import com.astraval.coreflow.modules.modemp.employee.EmployeeRepository;
import com.astraval.coreflow.modules.modemp.portaluser.dto.CreatePortalUserDto;
import com.astraval.coreflow.modules.modemp.portaluser.dto.PortalUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeePortalUserService {

    @Autowired
    private EmployeePortalUserRepository portalUserRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Long createPortalUser(Long companyId, Long employeeId, CreatePortalUserDto dto) {
        Employee employee = employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        if (portalUserRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' is already taken");
        }

        if (portalUserRepository.findByEmployeeEmployeeId(employeeId).isPresent()) {
            throw new RuntimeException("Portal user already exists for this employee");
        }

        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        EmployeePortalUser portalUser = new EmployeePortalUser();
        portalUser.setEmployee(employee);
        portalUser.setCompany(company);
        portalUser.setUsername(dto.getUsername());
        portalUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        portalUser.setIsActive(true);

        EmployeePortalUser saved = portalUserRepository.save(portalUser);
        return saved.getPortalUserId();
    }

    public PortalUserDto getPortalUser(Long employeeId) {
        return portalUserRepository.findByEmployeeEmployeeId(employeeId)
                .map(pu -> new PortalUserDto(
                        pu.getPortalUserId(),
                        pu.getEmployee().getEmployeeId(),
                        pu.getUsername(),
                        pu.getIsActive(),
                        pu.getLastLoginDt()))
                .orElse(null);
    }

    @Transactional
    public void resetPassword(Long companyId, Long employeeId, String newPassword) {
        employeeRepository.findByEmployeeIdAndCompanyCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        EmployeePortalUser portalUser = portalUserRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Portal user not found for employee ID: " + employeeId));

        portalUser.setPassword(passwordEncoder.encode(newPassword));
        portalUserRepository.save(portalUser);
    }
}
