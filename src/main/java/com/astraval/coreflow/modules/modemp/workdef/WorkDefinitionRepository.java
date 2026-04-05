package com.astraval.coreflow.modules.modemp.workdef;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkDefinitionRepository extends JpaRepository<WorkDefinition, Long> {

    List<WorkDefinition> findByCompanyCompanyIdAndIsActiveTrueOrderByWorkName(Long companyId);

    List<WorkDefinition> findByCompanyCompanyIdOrderByWorkName(Long companyId);

    Optional<WorkDefinition> findByWorkDefIdAndCompanyCompanyId(Long workDefId, Long companyId);

    boolean existsByCompanyCompanyIdAndWorkCode(Long companyId, String workCode);
}
