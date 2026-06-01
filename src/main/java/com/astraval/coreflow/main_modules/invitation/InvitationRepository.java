package com.astraval.coreflow.main_modules.invitation;

import java.util.List;
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

    Optional<Invitation> findTopByRequestedEntityTypeAndRequestedEntityIdAndRequestTypeAndIsActiveTrueOrderByCreatedAtDesc(
            String requestedEntityType,
            Long requestedEntityId,
            String requestType);

    List<Invitation> findByToCompanyCompanyIdAndRequestTypeAndStatusAndIsActiveTrue(
            Long toCompanyId,
            String requestType,
            String status);
}
