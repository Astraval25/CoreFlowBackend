package com.astraval.coreflow.modules.user.facade;

import com.astraval.coreflow.modules.user.User;

public interface UserFacade {
    User getUserById(Integer userId);
    User getUserByEmail(String email);
}