package com.astraval.coreflow.modules.user;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

  // Constructor Injection
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public User findUserByEmail(String email) {

    return userRepository.findByEmailAndIsActiveTrue(email)
        .orElseThrow(() -> new RuntimeException("User not found: " + email));
  }
  
  @Transactional(readOnly = true)
  public Optional<User> runSelect(String email) {
    // List<AuthRow> rows = userRepository.findAuthDataByEmail(email);
    return  userRepository.findActiveUserByEmail(email);
  
  }

}
