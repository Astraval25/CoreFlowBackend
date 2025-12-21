package com.astraval.coreflow.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

  @Bean
  public AuditorAware<Long> auditorProvider() {
    return () -> {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()) && auth.getPrincipal() instanceof String) {
        String userId = (String) auth.getPrincipal();
        return Optional.of(Long.parseLong(userId));
      }
      return Optional.of(0L); // Default for anonymous users
    };
  }

}
