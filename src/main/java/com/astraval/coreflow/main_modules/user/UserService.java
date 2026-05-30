package com.astraval.coreflow.main_modules.user;

import java.util.Optional;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.main_modules.user.dto.UpdateUserProfileDto;
import com.astraval.coreflow.main_modules.user.dto.UserProfileDto;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final SecurityUtil securityUtil;
  
  public UserService(UserRepository userRepository, SecurityUtil securityUtil) {
    this.userRepository = userRepository;
    this.securityUtil = securityUtil;
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserByEmail(String email) {
    return userRepository.findActiveUserByEmail(email);
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserByContactNo(String contactNo) {
    return userRepository.findActiveUserByContactNo(contactNo);
  }
  
  @Transactional
  public User saveUser(User user) {
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public UserProfileDto getCurrentUserProfile() {
    User user = getCurrentActiveUser();
    return toUserProfileDto(user);
  }

  @Transactional
  public UserProfileDto updateCurrentUserProfile(UpdateUserProfileDto request) {
    User user = getCurrentActiveUser();

    String requestedUserName = trimToNull(request.getUserName());
    if (requestedUserName == null) {
      throw new RuntimeException("Username is required");
    }

    Optional<User> byUserName = userRepository.findByUserNameAndIsActiveTrue(requestedUserName);
    if (byUserName.isPresent() && !byUserName.get().getUserId().equals(user.getUserId())) {
      throw new RuntimeException("Username already exists");
    }

    String requestedEmail = normalizeEmail(request.getEmail());
    if (requestedEmail != null) {
      Optional<User> byEmail = userRepository.findActiveUserByEmail(requestedEmail);
      if (byEmail.isPresent() && !byEmail.get().getUserId().equals(user.getUserId())) {
        throw new RuntimeException("Email already exists");
      }
    }

    user.setUserName(requestedUserName);
    user.setFirstName(trimToNull(request.getFirstName()));
    user.setLastName(trimToNull(request.getLastName()));
    user.setEmail(requestedEmail);
    User savedUser = userRepository.save(user);

    return toUserProfileDto(savedUser);
  }

  private User getCurrentActiveUser() {
    Long userId = parseCurrentUserId();
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    if (user.getIsActive() == null || !user.getIsActive()) {
      throw new RuntimeException("User is not active");
    }
    return user;
  }

  private Long parseCurrentUserId() {
    try {
      String currentSub = securityUtil.getCurrentSub();
      return Long.parseLong(currentSub);
    } catch (Exception ex) {
      throw new RuntimeException("Invalid authenticated user context");
    }
  }

  private UserProfileDto toUserProfileDto(User user) {
    return new UserProfileDto(
            user.getUserId(),
            user.getUserName(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getContactNo());
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String normalizeEmail(String value) {
    String trimmed = trimToNull(value);
    return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
  }

}
