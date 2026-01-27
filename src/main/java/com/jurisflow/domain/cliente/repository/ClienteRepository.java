package com.jurisflow.domain.cliente.repository;

import com.jurisflow.domain.cliente.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para entidade Cliente.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    @Query("SELECT c FROM Cliente c WHERE c.escritorio.id = :escritorioId AND c.deletedAt IS NULL")
    Page<Cliente> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.id = :id AND c.escritorio.id = :escritorioId AND c.deletedAt IS NULL")
    Optional<Cliente> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT c FROM Cliente c WHERE c.cpfCnpj = :cpfCnpj AND c.escritorio.id = :escritorioId AND c.deletedAt IS NULL")
    Optional<Cliente> findByCpfCnpjAndEscritorioId(@Param("cpfCnpj") String cpfCnpj, @Param("escritorioId") UUID escritorioId);

    boolean existsByCpfCnpjAndEscritorioId(String cpfCnpj, UUID escritorioId);

    @Query("""
        SELECT c FROM Cliente c 
        WHERE c.escritorio.id = :escritorioId 
        AND c.deletedAt IS NULL
        AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        AND (:cpfCnpj IS NULL OR c.cpfCnpj = :cpfCnpj)
        AND (:ativo IS NULL OR c.ativo = :ativo)
    """)
    Page<Cliente> findByFiltro(
        @Param("escritorioId") UUID escritorioId,
        @Param("nome") String nome,
        @Param("cpfCnpj") String cpfCnpj,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.escritorio.id = :escritorioId AND c.ativo = true AND c.deletedAt IS NULL")
    long countAtivosbyEscritorioId(@Param("escritorioId") UUID escritorioId);
}


