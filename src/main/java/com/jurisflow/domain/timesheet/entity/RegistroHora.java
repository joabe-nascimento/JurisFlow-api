package com.jurisflow.domain.timesheet.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.processo.entity.Processo;
import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidade que representa um registro de horas trabalhadas.
 */
@Entity
@Table(name = "registros_hora", indexes = {
    @Index(name = "idx_registro_hora_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_registro_hora_usuario", columnList = "usuario_id"),
    @Index(name = "idx_registro_hora_processo", columnList = "processo_id"),
    @Index(name = "idx_registro_hora_data", columnList = "data")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroHora extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(nullable = false)
    private Integer duracao; // em minutos

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividade tipoAtividade;

    @Column(name = "faturavel")
    @Builder.Default
    private Boolean faturavel = true;

    @Column(name = "valor_hora", precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusRegistro status = StatusRegistro.PENDENTE;

    @Column(name = "aprovado_por")
    private UUID aprovadoPor;

    @Column(name = "data_aprovacao")
    private LocalDate dataAprovacao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════
    @PrePersist
    @PreUpdate
    private void calcularValorTotal() {
        if (valorHora != null && duracao != null) {
            BigDecimal horas = BigDecimal.valueOf(duracao).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.valorTotal = valorHora.multiply(horas);
        }
    }

    public String getDuracaoFormatada() {
        if (duracao == null) return "0:00";
        int horas = duracao / 60;
        int minutos = duracao % 60;
        return String.format("%d:%02d", horas, minutos);
    }

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoAtividade {
        ATENDIMENTO,
        AUDIENCIA,
        ELABORACAO_PETICAO,
        ANALISE_DOCUMENTOS,
        PESQUISA,
        REUNIAO,
        DILIGENCIA,
        DESPACHO,
        TELEFONEMA,
        EMAIL,
        OUTROS
    }

    public enum StatusRegistro {
        PENDENTE,
        APROVADO,
        REJEITADO,
        FATURADO
    }
}

