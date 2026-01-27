package com.jurisflow.domain.escritorio.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa um escritório de advocacia.
 * Base do sistema multi-tenant.
 */
@Entity
@Table(name = "escritorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escritorio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    @Column(unique = true, length = 18)
    private String cnpj;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String celular;

    @Column(length = 500)
    private String website;

    // ═══════════════════════════════════════════════════════════════
    // ENDEREÇO
    // ═══════════════════════════════════════════════════════════════
    @Column(length = 10)
    private String cep;

    @Column(length = 200)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    // ═══════════════════════════════════════════════════════════════
    // OAB / DADOS JURÍDICOS
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "numero_oab", length = 20)
    private String numeroOab;

    @Column(name = "seccional_oab", length = 2)
    private String seccionalOab;

    // ═══════════════════════════════════════════════════════════════
    // CONFIGURAÇÕES
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cor_primaria", length = 7)
    @Builder.Default
    private String corPrimaria = "#1a56db";

    @Column(name = "cor_secundaria", length = 7)
    @Builder.Default
    private String corSecundaria = "#7c3aed";

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "America/Sao_Paulo";

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    // ═══════════════════════════════════════════════════════════════
    // PLANO / ASSINATURA
    // ═══════════════════════════════════════════════════════════════
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlanoEscritorio plano = PlanoEscritorio.TRIAL;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @OneToMany(mappedBy = "escritorio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Usuario> usuarios = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum PlanoEscritorio {
        TRIAL,
        SOLO,
        PROFESSIONAL,
        BUSINESS,
        ENTERPRISE
    }
}


