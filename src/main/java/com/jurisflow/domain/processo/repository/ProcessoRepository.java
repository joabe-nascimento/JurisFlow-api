package com.jurisflow.domain.processo.repository;

import com.jurisflow.domain.processo.entity.Processo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para entidade Processo.
 */
@Repository
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {

    @Query("SELECT p FROM Processo p WHERE p.escritorio.id = :escritorioId AND p.deletedAt IS NULL")
    Page<Processo> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT p FROM Processo p WHERE p.id = :id AND p.escritorio.id = :escritorioId AND p.deletedAt IS NULL")
    Optional<Processo> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT p FROM Processo p WHERE p.numero = :numero AND p.escritorio.id = :escritorioId AND p.deletedAt IS NULL")
    Optional<Processo> findByNumeroAndEscritorioId(@Param("numero") String numero, @Param("escritorioId") UUID escritorioId);

    boolean existsByNumeroAndEscritorioId(String numero, UUID escritorioId);

    @Query("SELECT COUNT(p) FROM Processo p WHERE p.escritorio.id = :escritorioId AND p.status = 'EM_ANDAMENTO' AND p.deletedAt IS NULL")
    long countAtivosbyEscritorioId(@Param("escritorioId") UUID escritorioId);

    @Query("""
        SELECT p FROM Processo p 
        WHERE p.escritorio.id = :escritorioId 
        AND p.deletedAt IS NULL
        AND (:numero IS NULL OR p.numero LIKE %:numero%)
        AND (:clienteId IS NULL OR p.cliente.id = :clienteId)
        AND (:status IS NULL OR p.status = :status)
        AND (:areaDireito IS NULL OR p.areaDireito = :areaDireito)
        AND (:advogadoId IS NULL OR p.advogadoResponsavel.id = :advogadoId)
    """)
    Page<Processo> findByFiltro(
        @Param("escritorioId") UUID escritorioId,
        @Param("numero") String numero,
        @Param("clienteId") UUID clienteId,
        @Param("status") Processo.ProcessoStatus status,
        @Param("areaDireito") Processo.AreaDireito areaDireito,
        @Param("advogadoId") UUID advogadoId,
        Pageable pageable
    );
}


