package com.jurisflow.domain.processo.entity;

import com.jurisflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um andamento/movimentação de um processo.
 */
@Entity
@Table(name = "andamentos", indexes = {
    @Index(name = "idx_andamento_processo", columnList = "processo_id"),
    @Index(name = "idx_andamento_data", columnList = "data_movimentacao")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Andamento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoAndamento tipo = TipoAndamento.OUTROS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FonteAndamento fonte = FonteAndamento.MANUAL;

    @Column(name = "codigo_movimentacao", length = 50)
    private String codigoMovimentacao;

    @Column(name = "documento_url", length = 500)
    private String documentoUrl;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "lido")
    @Builder.Default
    private Boolean lido = false;

    @Column(name = "importante")
    @Builder.Default
    private Boolean importante = false;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoAndamento {
        DISTRIBUICAO,
        DESPACHO,
        DECISAO,
        SENTENCA,
        ACORDAO,
        INTIMACAO,
        CITACAO,
        AUDIENCIA,
        PERICIA,
        PETICAO,
        JUNTADA,
        CERTIDAO,
        BAIXA,
        RECURSO,
        OUTROS
    }

    public enum FonteAndamento {
        MANUAL,
        TRIBUNAL,
        PJE,
        ESAJ,
        IMPORTACAO
    }
}


