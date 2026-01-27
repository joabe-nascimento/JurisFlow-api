package com.jurisflow.domain.prazo.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.processo.entity.Processo;
import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um prazo processual.
 */
@Entity
@Table(name = "prazos", indexes = {
    @Index(name = "idx_prazo_processo", columnList = "processo_id"),
    @Index(name = "idx_prazo_vencimento", columnList = "data_vencimento"),
    @Index(name = "idx_prazo_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_prazo_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prazo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_prazo", nullable = false)
    private TipoPrazo tipoPrazo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "dias_prazo")
    private Integer diasPrazo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contagem", nullable = false)
    @Builder.Default
    private TipoContagem tipoContagem = TipoContagem.DIAS_UTEIS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PrazoStatus status = PrazoStatus.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Prioridade prioridade = Prioridade.MEDIA;

    // ═══════════════════════════════════════════════════════════════
    // CUMPRIMENTO
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "data_cumprimento")
    private LocalDateTime dataCumprimento;

    @Column(name = "numero_protocolo", length = 50)
    private String numeroProtocolo;

    @Column(name = "observacoes_cumprimento", columnDefinition = "TEXT")
    private String observacoesCumprimento;

    @Column(name = "documento_url", length = 500)
    private String documentoUrl;

    // ═══════════════════════════════════════════════════════════════
    // ALERTAS
    // ═══════════════════════════════════════════════════════════════
    @Column(name = "alerta_7_dias")
    @Builder.Default
    private Boolean alerta7Dias = false;

    @Column(name = "alerta_3_dias")
    @Builder.Default
    private Boolean alerta3Dias = false;

    @Column(name = "alerta_1_dia")
    @Builder.Default
    private Boolean alerta1Dia = false;

    @Column(name = "alerta_no_dia")
    @Builder.Default
    private Boolean alertaNoDia = false;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════
    public boolean isVencido() {
        return status == PrazoStatus.PENDENTE && 
               LocalDate.now().isAfter(dataVencimento);
    }

    public boolean isUrgente() {
        if (status != PrazoStatus.PENDENTE) return false;
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), dataVencimento
        );
        return diasRestantes <= 3;
    }

    public long getDiasRestantes() {
        return java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), dataVencimento
        );
    }

    public void cumprir(String protocolo, String observacoes) {
        this.status = PrazoStatus.CUMPRIDO;
        this.dataCumprimento = LocalDateTime.now();
        this.numeroProtocolo = protocolo;
        this.observacoesCumprimento = observacoes;
    }

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoPrazo {
        CONTESTACAO,
        RECURSO,
        MANIFESTACAO,
        AUDIENCIA,
        CUMPRIMENTO_SENTENCA,
        DILIGENCIA,
        PAGAMENTO,
        INTERNO,
        CUSTOMIZADO
    }

    public enum TipoContagem {
        DIAS_UTEIS,
        DIAS_CORRIDOS
    }

    public enum PrazoStatus {
        PENDENTE,
        CUMPRIDO,
        CANCELADO,
        ADIADO
    }

    public enum Prioridade {
        BAIXA,
        MEDIA,
        ALTA,
        URGENTE
    }
}


