package com.jurisflow.domain.usuario.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa um usuário do sistema.
 * Implementa UserDetails para integração com Spring Security.
 */
@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email"),
    @Index(name = "idx_usuario_escritorio", columnList = "escritorio_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String celular;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    // ═══════════════════════════════════════════════════════════════
    // DADOS PROFISSIONAIS
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "numero_oab", length = 20)
    private String numeroOab;

    @Column(name = "seccional_oab", length = 2)
    private String seccionalOab;

    @Column(length = 100)
    private String especialidade;

    @Column(name = "valor_hora", precision = 10)
    private java.math.BigDecimal valorHora;

    // ═══════════════════════════════════════════════════════════════
    // SEGURANÇA
    // ═══════════════════════════════════════════════════════════════
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ADVOGADO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "email_verificado")
    @Builder.Default
    private Boolean emailVerificado = false;

    @Column(name = "two_factor_enabled")
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 100)
    private String twoFactorSecret;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "tentativas_login")
    @Builder.Default
    private Integer tentativasLogin = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    // ═══════════════════════════════════════════════════════════════
    // SPRING SECURITY - UserDetails
    // ═══════════════════════════════════════════════════════════════
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (bloqueadoAte == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(bloqueadoAte);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo && !isDeleted();
    }

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum Role {
        ADMIN,
        SOCIO,
        ADVOGADO,
        ESTAGIARIO,
        SECRETARIA,
        FINANCEIRO,
        VISUALIZADOR
    }
}


