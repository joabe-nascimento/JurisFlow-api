package com.jurisflow.domain.agenda.repository;

import com.jurisflow.domain.agenda.entity.Evento;
import com.jurisflow.domain.agenda.entity.Evento.TipoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {

    @Query("SELECT e FROM Evento e WHERE e.escritorio.id = :escritorioId AND e.deletedAt IS NULL ORDER BY e.dataInicio ASC")
    Page<Evento> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.id = :id AND e.escritorio.id = :escritorioId AND e.deletedAt IS NULL")
    Optional<Evento> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT e FROM Evento e WHERE e.escritorio.id = :escritorioId " +
           "AND e.dataInicio >= :inicio AND e.dataInicio <= :fim AND e.deletedAt IS NULL " +
           "ORDER BY e.dataInicio ASC")
    List<Evento> findByEscritorioIdAndPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("SELECT e FROM Evento e WHERE e.responsavel.id = :usuarioId " +
           "AND e.dataInicio >= :inicio AND e.dataInicio <= :fim AND e.deletedAt IS NULL " +
           "ORDER BY e.dataInicio ASC")
    List<Evento> findByResponsavelIdAndPeriodo(
            @Param("usuarioId") UUID usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("SELECT e FROM Evento e WHERE e.processo.id = :processoId AND e.deletedAt IS NULL ORDER BY e.dataInicio ASC")
    List<Evento> findByProcessoId(@Param("processoId") UUID processoId);

    @Query("SELECT e FROM Evento e WHERE e.cliente.id = :clienteId AND e.deletedAt IS NULL ORDER BY e.dataInicio ASC")
    List<Evento> findByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT e FROM Evento e WHERE e.escritorio.id = :escritorioId AND e.tipo = :tipo " +
           "AND e.dataInicio >= :inicio AND e.deletedAt IS NULL ORDER BY e.dataInicio ASC")
    List<Evento> findByTipoAndPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("tipo") TipoEvento tipo,
            @Param("inicio") LocalDateTime inicio
    );

    @Query("SELECT e FROM Evento e WHERE e.escritorio.id = :escritorioId " +
           "AND e.lembreteEnviado = false AND e.dataInicio <= :limite AND e.deletedAt IS NULL")
    List<Evento> findEventosParaLembrete(
            @Param("escritorioId") UUID escritorioId,
            @Param("limite") LocalDateTime limite
    );

    @Query("SELECT e FROM Evento e WHERE e.escritorio.id = :escritorioId " +
           "AND e.dataInicio >= CURRENT_DATE AND e.deletedAt IS NULL " +
           "ORDER BY e.dataInicio ASC")
    List<Evento> findProximosEventos(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.escritorio.id = :escritorioId " +
           "AND e.dataInicio >= :inicio AND e.dataInicio <= :fim AND e.deletedAt IS NULL")
    long countByPeriodo(
            @Param("escritorioId") UUID escritorioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}

