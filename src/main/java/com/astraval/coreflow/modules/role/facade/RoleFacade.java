package com.astraval.coreflow.modules.role.facade;

import com.astraval.coreflow.modules.role.Role;

public interface RoleFacade {
    Role getRoleByCode(String roleCode);
}