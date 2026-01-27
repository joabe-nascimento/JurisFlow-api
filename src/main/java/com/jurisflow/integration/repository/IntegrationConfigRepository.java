package com.jurisflow.integration.repository;

import com.jurisflow.integration.IntegrationConfig;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, UUID> {
    
    List<IntegrationConfig> findByEscritorioId(UUID escritorioId);
    
    List<IntegrationConfig> findByEscritorioIdAndEnabled(UUID escritorioId, boolean enabled);
    
    Optional<IntegrationConfig> findByEscritorioIdAndType(UUID escritorioId, IntegrationType type);
    
    boolean existsByEscritorioIdAndType(UUID escritorioId, IntegrationType type);
}

