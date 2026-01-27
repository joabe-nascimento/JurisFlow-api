package com.jurisflow.domain.timesheet.repository;

import com.jurisflow.domain.timesheet.entity.RegistroHora;
import com.jurisflow.domain.timesheet.entity.RegistroHora.StatusRegistro;
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

@Repository
public interface RegistroHoraRepository extends JpaRepository<RegistroHora, UUID> {

    @Query("SELECT r FROM RegistroHora r WHERE r.escritorio.id = :escritorioId AND r.deletedAt IS NULL ORDER BY r.data DESC, r.horaInicio DESC")
    Page<RegistroHora> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT r FROM RegistroHora r WHERE r.id = :id AND r.escritorio.id = :escritorioId AND r.deletedAt IS NULL")
    Optional<RegistroHora> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT r FROM RegistroHora r WHERE r.usuario.id = :usuarioId AND r.deletedAt IS NULL ORDER BY r.data DESC")
    Page<RegistroHora> findByUsuarioId(@Param("usuarioId") UUID usuarioId, Pageable pageable);

    @Query("SELECT r FROM RegistroHora r WHERE r.escritorio.id = :escritorioId " +
           "AND r.data BETWEEN :inicio AND :fim AND r.deletedAt IS NULL ORDER BY r.data DESC")
    List<RegistroHora> findByPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT r FROM RegistroHora r WHERE r.usuario.id = :usuarioId " +
           "AND r.data BETWEEN :inicio AND :fim AND r.deletedAt IS NULL ORDER BY r.data DESC")
    List<RegistroHora> findByUsuarioIdAndPeriodo(
            @Param("usuarioId") UUID usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT r FROM RegistroHora r WHERE r.processo.id = :processoId AND r.deletedAt IS NULL ORDER BY r.data DESC")
    List<RegistroHora> findByProcessoId(@Param("processoId") UUID processoId);

    @Query("SELECT r FROM RegistroHora r WHERE r.escritorio.id = :escritorioId AND r.status = :status AND r.deletedAt IS NULL ORDER BY r.data DESC")
    List<RegistroHora> findByStatus(@Param("escritorioId") UUID escritorioId, @Param("status") StatusRegistro status);

    // Somatórios
    @Query("SELECT COALESCE(SUM(r.duracao), 0) FROM RegistroHora r WHERE r.escritorio.id = :escritorioId " +
           "AND r.data BETWEEN :inicio AND :fim AND r.deletedAt IS NULL")
    Integer sumDuracaoByPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT COALESCE(SUM(r.duracao), 0) FROM RegistroHora r WHERE r.usuario.id = :usuarioId " +
           "AND r.data BETWEEN :inicio AND :fim AND r.deletedAt IS NULL")
    Integer sumDuracaoByUsuarioAndPeriodo(
            @Param("usuarioId") UUID usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT COALESCE(SUM(r.duracao), 0) FROM RegistroHora r WHERE r.escritorio.id = :escritorioId " +
           "AND r.faturavel = true AND r.data BETWEEN :inicio AND :fim AND r.deletedAt IS NULL")
    Integer sumDuracaoFaturavelByPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT COUNT(r) FROM RegistroHora r WHERE r.escritorio.id = :escritorioId AND r.status = :status AND r.deletedAt IS NULL")
    long countByStatus(@Param("escritorioId") UUID escritorioId, @Param("status") StatusRegistro status);
}

