package com.jurisflow.config;

import com.jurisflow.domain.usuario.entity.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Configuração para auditoria JPA.
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof Usuario usuario) {
                return Optional.of(usuario.getId());
            }
            
            return Optional.empty();
        };
    }
}


