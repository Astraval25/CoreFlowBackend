package com.astraval.coreflow.modules.user;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

  private final UserRepository userRepository;
  
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserByEmail(String email) {
    return userRepository.findActiveUserByEmail(email);
  }
  
  @Transactional
  public User saveUser(User user) {
    return userRepository.save(user);
  }

}
