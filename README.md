FULL FACADE EXAMPLE (step-by-step)
🔷 MODULE 1: USER MODULE
1. Create User DTO

modules/user/dto/UserDto.java

package com.app.modules.user.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
}

2. Create the Facade Interface

modules/user/facade/UserFacade.java

package com.app.modules.user.facade;

import com.app.modules.user.dto.UserDto;

public interface UserFacade {
    UserDto getUserById(Long id);
}

3. Implement the Facade

modules/user/facade/impl/UserFacadeImpl.java

package com.app.modules.user.facade.impl;

import com.app.modules.user.dto.UserDto;
import com.app.modules.user.entity.User;
import com.app.modules.user.facade.UserFacade;
import com.app.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFacadeImpl implements UserFacade {

    @Autowired
    private UserService userService;

    @Override
    public UserDto getUserById(Long id) {
        User user = userService.findById(id);

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return dto;
    }
}

4. Expose the Facade as a Bean (Optional but clean)

modules/user/UserModuleConfig.java

package com.app.modules.user;

import com.app.modules.user.facade.UserFacade;
import com.app.modules.user.facade.impl.UserFacadeImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModuleConfig {

    @Bean
    public UserFacade userFacade(UserFacadeImpl impl) {
        return impl;
    }
}


Now UserFacade is available everywhere.

🔷 MODULE 2: COMPANY MODULE
1. Inject the Facade (NOT the UserService)

modules/company/service/CompanyService.java

package com.app.modules.company.service;

import com.app.modules.company.dto.CompanyDto;
import com.app.modules.company.entity.Company;
import com.app.modules.company.facade.CompanyMapper;
import com.app.modules.user.facade.UserFacade;
import com.app.modules.user.dto.UserDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private UserFacade userFacade;

    public CompanyDto updateCompany(Long companyId) {

        // Example: Get company entity however you normally fetch it
        Company company = findCompany(companyId);

        // Get user details via facade
        UserDto user = userFacade.getUserById(company.getUserId());

        // Use user details in company update logic
        company.setContactName(user.getName());
        company.setContactEmail(user.getEmail());

        // Save company etc.
        saveCompany(company);

        return CompanyMapper.toDto(company);
    }

    private Company findCompany(Long id) {
        // dummy placeholder
        return new Company();
    }

    private void saveCompany(Company company) {
        // save logic
    }
}


💡 Notice:
You did NOT autowire UserService
You used the facade → perfect separation

🔷 What Happens When You Convert to Microservices?

When you extract User Module → user-service,
just change the facade implementation:

UserFacadeImpl.java (new microservice version)

@Service
public class UserFacadeImpl implements UserFacade {

    private final WebClient webClient = WebClient.create("http://user-service");

    @Override
    public UserDto getUserById(Long id) {
        return webClient.get()
                .uri("/users/" + id)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }
}


⚡ NO change in CompanyService required!
company-service still calls:

userFacade.getUserById()


This is why Facade Pattern is the best for modular → microservice evolution.

🎯 SUMMARY
Layer	What it does
UserFacade	Interface → contract
UserFacadeImpl	Actual implementation inside user module
UserModuleConfig	Exports facade as bean
CompanyService	Uses facade (perfectly clean boundary)

Advantages:

No module coupling

Easy to extract as microservice later

Monolith code stays clean

No circular dependencies