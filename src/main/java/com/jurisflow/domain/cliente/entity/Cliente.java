package com.jurisflow.domain.cliente.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade que representa um cliente do escritório.
 */
@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_cliente_cpf_cnpj", columnList = "cpf_cnpj"),
    @Index(name = "idx_cliente_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_cliente_nome", columnList = "nome")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    @Builder.Default
    private TipoPessoa tipoPessoa = TipoPessoa.FISICA;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "cpf_cnpj", length = 18)
    private String cpfCnpj;

    @Column(length = 20)
    private String rg;

    @Column(name = "orgao_emissor", length = 20)
    private String orgaoEmissor;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil")
    private EstadoCivil estadoCivil;

    @Column(length = 100)
    private String profissao;

    @Column(length = 100)
    private String nacionalidade;

    // ═══════════════════════════════════════════════════════════════
    // CONTATO
    // ═══════════════════════════════════════════════════════════════
    @Column(length = 200)
    private String email;

    @Column(name = "email_secundario", length = 200)
    private String emailSecundario;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String celular;

    @Column(name = "whatsapp")
    @Builder.Default
    private Boolean whatsapp = false;

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
    // INFORMAÇÕES ADICIONAIS
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "como_conheceu", length = 100)
    private String comoConheceu;

    @Column(name = "indicado_por", length = 200)
    private String indicadoPor;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    // ═══════════════════════════════════════════════════════════════
    // PORTAL DO CLIENTE
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "portal_ativo")
    @Builder.Default
    private Boolean portalAtivo = false;

    @Column(name = "portal_senha")
    private String portalSenha;

    @Column(name = "portal_ultimo_acesso")
    private java.time.LocalDateTime portalUltimoAcesso;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoPessoa {
        FISICA,
        JURIDICA
    }

    public enum EstadoCivil {
        SOLTEIRO,
        CASADO,
        DIVORCIADO,
        VIUVO,
        SEPARADO,
        UNIAO_ESTAVEL
    }
}


