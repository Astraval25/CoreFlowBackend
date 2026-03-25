package com.astraval.coreflow.modules.config;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigDefinitionRepository extends JpaRepository<ConfigDefinition, Long> {

    Optional<ConfigDefinition> findByConfigKey(String configKey);

    List<ConfigDefinition> findByCategory(String category);
}
