package com.astraval.coreflow.main_modules.devicetoken;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserUserIdAndIsActiveTrue(Long userId);

    List<DeviceToken> findByUserUserIdInAndIsActiveTrue(List<Long> userIds);

    void deleteByToken(String token);
}
