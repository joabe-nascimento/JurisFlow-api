package com.jurisflow.domain.documento.repository;

import com.jurisflow.domain.documento.entity.Documento;
import com.jurisflow.domain.documento.entity.Documento.CategoriaDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

    @Query("SELECT d FROM Documento d WHERE d.escritorio.id = :escritorioId AND d.deletedAt IS NULL")
    Page<Documento> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT d FROM Documento d WHERE d.escritorio.id = :escritorioId AND d.categoria = :categoria AND d.deletedAt IS NULL")
    Page<Documento> findAllByEscritorioIdAndCategoria(
            @Param("escritorioId") UUID escritorioId,
            @Param("categoria") CategoriaDocumento categoria,
            Pageable pageable
    );

    @Query("SELECT d FROM Documento d WHERE d.processo.id = :processoId AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<Documento> findAllByProcessoId(@Param("processoId") UUID processoId);

    @Query("SELECT d FROM Documento d WHERE d.cliente.id = :clienteId AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<Documento> findAllByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT d FROM Documento d WHERE d.id = :id AND d.escritorio.id = :escritorioId AND d.deletedAt IS NULL")
    Optional<Documento> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT d FROM Documento d WHERE d.hashArquivo = :hash AND d.escritorio.id = :escritorioId AND d.deletedAt IS NULL")
    Optional<Documento> findByHashAndEscritorioId(@Param("hash") String hash, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.escritorio.id = :escritorioId AND d.deletedAt IS NULL")
    long countByEscritorioId(@Param("escritorioId") UUID escritorioId);

    @Query("SELECT SUM(d.tamanho) FROM Documento d WHERE d.escritorio.id = :escritorioId AND d.deletedAt IS NULL")
    Long sumTamanhoByEscritorioId(@Param("escritorioId") UUID escritorioId);

    @Query("SELECT d FROM Documento d WHERE d.escritorio.id = :escritorioId AND LOWER(d.nome) LIKE LOWER(CONCAT('%', :search, '%')) AND d.deletedAt IS NULL")
    Page<Documento> searchByNome(@Param("escritorioId") UUID escritorioId, @Param("search") String search, Pageable pageable);
}

