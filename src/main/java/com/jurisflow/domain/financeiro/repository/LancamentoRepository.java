package com.jurisflow.domain.financeiro.repository;

import com.jurisflow.domain.financeiro.entity.Lancamento;
import com.jurisflow.domain.financeiro.entity.Lancamento.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {

    @Query("SELECT l FROM Lancamento l WHERE l.escritorio.id = :escritorioId AND l.deletedAt IS NULL ORDER BY l.dataVencimento DESC")
    Page<Lancamento> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT l FROM Lancamento l WHERE l.id = :id AND l.escritorio.id = :escritorioId AND l.deletedAt IS NULL")
    Optional<Lancamento> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT l FROM Lancamento l WHERE l.escritorio.id = :escritorioId AND l.tipo = :tipo AND l.deletedAt IS NULL ORDER BY l.dataVencimento DESC")
    Page<Lancamento> findByTipo(@Param("escritorioId") UUID escritorioId, @Param("tipo") TipoLancamento tipo, Pageable pageable);

    @Query("SELECT l FROM Lancamento l WHERE l.escritorio.id = :escritorioId AND l.status = :status AND l.deletedAt IS NULL ORDER BY l.dataVencimento ASC")
    List<Lancamento> findByStatus(@Param("escritorioId") UUID escritorioId, @Param("status") StatusLancamento status);

    @Query("SELECT l FROM Lancamento l WHERE l.escritorio.id = :escritorioId " +
           "AND l.dataVencimento BETWEEN :inicio AND :fim AND l.deletedAt IS NULL ORDER BY l.dataVencimento ASC")
    List<Lancamento> findByPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT l FROM Lancamento l WHERE l.processo.id = :processoId AND l.deletedAt IS NULL ORDER BY l.dataVencimento DESC")
    List<Lancamento> findByProcessoId(@Param("processoId") UUID processoId);

    @Query("SELECT l FROM Lancamento l WHERE l.cliente.id = :clienteId AND l.deletedAt IS NULL ORDER BY l.dataVencimento DESC")
    List<Lancamento> findByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT l FROM Lancamento l WHERE l.escritorio.id = :escritorioId " +
           "AND l.status = 'PENDENTE' AND l.dataVencimento < CURRENT_DATE AND l.deletedAt IS NULL")
    List<Lancamento> findAtrasados(@Param("escritorioId") UUID escritorioId);

    // Somatórios
    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.escritorio.id = :escritorioId " +
           "AND l.tipo = 'RECEITA' AND l.status = 'PAGO' AND l.deletedAt IS NULL " +
           "AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal sumReceitasPagasPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.escritorio.id = :escritorioId " +
           "AND l.tipo = 'DESPESA' AND l.status = 'PAGO' AND l.deletedAt IS NULL " +
           "AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal sumDespesasPagasPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.escritorio.id = :escritorioId " +
           "AND l.tipo = :tipo AND l.status = 'PENDENTE' AND l.deletedAt IS NULL")
    BigDecimal sumPendentesByTipo(@Param("escritorioId") UUID escritorioId, @Param("tipo") TipoLancamento tipo);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.escritorio.id = :escritorioId " +
           "AND l.tipo = :tipo AND l.status = 'PENDENTE' AND l.dataVencimento < CURRENT_DATE AND l.deletedAt IS NULL")
    BigDecimal sumAtrasadosByTipo(@Param("escritorioId") UUID escritorioId, @Param("tipo") TipoLancamento tipo);

    @Query("SELECT COUNT(l) FROM Lancamento l WHERE l.escritorio.id = :escritorioId AND l.status = :status AND l.deletedAt IS NULL")
    long countByStatus(@Param("escritorioId") UUID escritorioId, @Param("status") StatusLancamento status);
}

