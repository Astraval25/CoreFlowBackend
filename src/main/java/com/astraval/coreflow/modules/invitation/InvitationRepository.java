package com.astraval.coreflow.modules.invitation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationCode(String invitationCode);
    boolean existsByInvitationCode(String invitationCode);

    Optional<Invitation> findTopByFromCompanyCompanyIdAndRequestedEntityTypeAndRequestedEntityIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
            Long fromCompanyId,
            String requestedEntityType,
            Long requestedEntityId,
            String status);
}
