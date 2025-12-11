package com.astraval.coreflow.modules.role.facade.impl;

import com.astraval.coreflow.modules.role.facade.RoleFacade;
import com.astraval.coreflow.modules.role.Role;
import com.astraval.coreflow.modules.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleFacadeImpl implements RoleFacade {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role getRoleByCode(String roleCode) {
        return roleRepository.findByRoleCodeAndIsActiveTrue(roleCode);
    }
}