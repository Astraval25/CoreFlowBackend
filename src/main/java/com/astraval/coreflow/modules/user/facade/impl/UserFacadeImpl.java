package com.astraval.coreflow.modules.user.facade.impl;

import com.astraval.coreflow.modules.user.facade.UserFacade;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFacadeImpl implements UserFacade {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email);
    }
}