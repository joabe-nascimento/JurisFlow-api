package com.jurisflow.domain.escritorio.repository;

import com.jurisflow.domain.escritorio.entity.Escritorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para entidade Escritorio.
 */
@Repository
public interface EscritorioRepository extends JpaRepository<Escritorio, UUID> {

    Optional<Escritorio> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);
}


