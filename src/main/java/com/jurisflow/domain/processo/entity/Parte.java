package com.jurisflow.domain.processo.entity;

import com.jurisflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidade que representa uma parte (autor, réu, etc.) de um processo.
 */
@Entity
@Table(name = "partes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parte extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "cpf_cnpj", length = 18)
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_parte", nullable = false)
    private TipoParte tipoParte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Polo polo = Polo.ATIVO;

    @Column(length = 200)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 300)
    private String endereco;

    // ═══════════════════════════════════════════════════════════════
    // ADVOGADO DA PARTE (se for parte contrária)
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "advogado_nome", length = 200)
    private String advogadoNome;

    @Column(name = "advogado_oab", length = 20)
    private String advogadoOab;

    @Column(name = "advogado_email", length = 200)
    private String advogadoEmail;

    @Column(name = "advogado_telefone", length = 20)
    private String advogadoTelefone;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoParte {
        AUTOR,
        REU,
        TERCEIRO_INTERESSADO,
        ASSISTENTE,
        LITISCONSORTE,
        TESTEMUNHA,
        PERITO,
        OUTROS
    }

    public enum Polo {
        ATIVO,
        PASSIVO,
        NEUTRO
    }
}


