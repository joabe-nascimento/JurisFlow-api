package com.jurisflow.domain.usuario.repository;

import com.jurisflow.domain.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para entidade Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.escritorio.id = :escritorioId AND u.deletedAt IS NULL")
    List<Usuario> findAllByEscritorioId(@Param("escritorioId") UUID escritorioId);

    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.escritorio.id = :escritorioId AND u.deletedAt IS NULL")
    Optional<Usuario> findByIdAndEscritorioId(@Param("id") UUID id, @Param("escritorioId") UUID escritorioId);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.escritorio.id = :escritorioId AND u.ativo = true AND u.deletedAt IS NULL")
    long countActiveByEscritorioId(@Param("escritorioId") UUID escritorioId);
}


