package com.jurisflow.domain.prazo.repository;

import com.jurisflow.domain.prazo.entity.Prazo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para entidade Prazo.
 */
@Repository
public interface PrazoRepository extends JpaRepository<Prazo, UUID> {

    @Query("SELECT p FROM Prazo p WHERE p.escritorio.id = :escritorioId AND p.deletedAt IS NULL ORDER BY p.dataVencimento ASC")
    Page<Prazo> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT p FROM Prazo p WHERE p.id = :id AND p.escritorio.id = :escritorioId AND p.deletedAt IS NULL")
    Optional<Prazo> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT p FROM Prazo p WHERE p.processo.id = :processoId AND p.deletedAt IS NULL ORDER BY p.dataVencimento ASC")
    List<Prazo> findByProcessoId(@Param("processoId") UUID processoId);

    @Query("""
        SELECT p FROM Prazo p 
        WHERE p.escritorio.id = :escritorioId 
        AND p.status = 'PENDENTE' 
        AND p.dataVencimento <= :data
        AND p.deletedAt IS NULL
        ORDER BY p.dataVencimento ASC
    """)
    List<Prazo> findPrazosVencendoAte(@Param("escritorioId") UUID escritorioId, @Param("data") LocalDate data);

    @Query("""
        SELECT p FROM Prazo p 
        WHERE p.escritorio.id = :escritorioId 
        AND p.status = 'PENDENTE' 
        AND p.dataVencimento < CURRENT_DATE
        AND p.deletedAt IS NULL
    """)
    List<Prazo> findPrazosVencidos(@Param("escritorioId") UUID escritorioId);

    @Query("""
        SELECT p FROM Prazo p 
        WHERE p.status = 'PENDENTE' 
        AND p.dataVencimento = :data
        AND p.alerta7Dias = false
        AND p.deletedAt IS NULL
    """)
    List<Prazo> findPrazosParaAlerta7Dias(@Param("data") LocalDate data);

    @Query("""
        SELECT p FROM Prazo p 
        WHERE p.status = 'PENDENTE' 
        AND p.dataVencimento = :data
        AND p.alerta3Dias = false
        AND p.deletedAt IS NULL
    """)
    List<Prazo> findPrazosParaAlerta3Dias(@Param("data") LocalDate data);

    @Query("""
        SELECT p FROM Prazo p 
        WHERE p.status = 'PENDENTE' 
        AND p.dataVencimento = :data
        AND p.alerta1Dia = false
        AND p.deletedAt IS NULL
    """)
    List<Prazo> findPrazosParaAlerta1Dia(@Param("data") LocalDate data);

    @Query("SELECT COUNT(p) FROM Prazo p WHERE p.escritorio.id = :escritorioId AND p.status = 'PENDENTE' AND p.dataVencimento <= :data AND p.deletedAt IS NULL")
    long countPrazosUrgentes(@Param("escritorioId") UUID escritorioId, @Param("data") LocalDate data);
}


