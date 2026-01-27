package com.jurisflow.domain.agenda.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.cliente.entity.Cliente;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.processo.entity.Processo;
import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa um evento na agenda.
 */
@Entity
@Table(name = "eventos", indexes = {
    @Index(name = "idx_evento_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_evento_data_inicio", columnList = "data_inicio"),
    @Index(name = "idx_evento_responsavel", columnList = "responsavel_id"),
    @Index(name = "idx_evento_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoEvento tipo = TipoEvento.REUNIAO;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "dia_inteiro")
    @Builder.Default
    private Boolean diaInteiro = false;

    @Column(length = 300)
    private String local;

    @Column(name = "link_reuniao", length = 500)
    private String linkReuniao; // Teams, Zoom, Meet

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusEvento status = StatusEvento.AGENDADO;

    @Column(name = "cor", length = 7)
    @Builder.Default
    private String cor = "#3b82f6"; // Cor hex para exibição no calendário

    @Column(name = "lembrete_minutos")
    @Builder.Default
    private Integer lembreteMinutos = 30;

    @Column(name = "lembrete_enviado")
    @Builder.Default
    private Boolean lembreteEnviado = false;

    @Column(name = "recorrente")
    @Builder.Default
    private Boolean recorrente = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recorrencia")
    private TipoRecorrencia tipoRecorrencia;

    @Column(name = "recorrencia_fim")
    private LocalDateTime recorrenciaFim;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToMany
    @JoinTable(
            name = "evento_participantes",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    @Builder.Default
    private List<Usuario> participantes = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════
    public void addParticipante(Usuario usuario) {
        participantes.add(usuario);
    }

    public void removeParticipante(Usuario usuario) {
        participantes.remove(usuario);
    }

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoEvento {
        AUDIENCIA,
        REUNIAO,
        PRAZO,
        PERICIA,
        DEPOIMENTO,
        JURI,
        CONCILIACAO,
        MEDIACAO,
        DILIGENCIA,
        LEMBRETE,
        OUTROS
    }

    public enum StatusEvento {
        AGENDADO,
        CONFIRMADO,
        EM_ANDAMENTO,
        CONCLUIDO,
        CANCELADO,
        ADIADO
    }

    public enum TipoRecorrencia {
        DIARIO,
        SEMANAL,
        QUINZENAL,
        MENSAL,
        ANUAL
    }
}

