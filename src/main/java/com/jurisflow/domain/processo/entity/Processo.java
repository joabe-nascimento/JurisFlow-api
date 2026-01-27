package com.jurisflow.domain.processo.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.cliente.entity.Cliente;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa um processo jurídico.
 */
@Entity
@Table(name = "processos", indexes = {
    @Index(name = "idx_processo_numero", columnList = "numero"),
    @Index(name = "idx_processo_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_processo_cliente", columnList = "cliente_id"),
    @Index(name = "idx_processo_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Processo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 25)
    private String numero;

    @Column(name = "numero_antigo", length = 25)
    private String numeroAntigo;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acao", nullable = false)
    private TipoAcao tipoAcao;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_direito", nullable = false)
    private AreaDireito areaDireito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProcessoStatus status = ProcessoStatus.EM_ANDAMENTO;

    @Column(length = 100)
    private String tribunal;

    @Column(length = 100)
    private String vara;

    @Column(length = 100)
    private String comarca;

    @Column(length = 2)
    private String uf;

    @Column(name = "valor_causa", precision = 15, scale = 2)
    private BigDecimal valorCausa;

    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "data_distribuicao")
    private LocalDate dataDistribuicao;

    @Column(name = "data_encerramento")
    private LocalDate dataEncerramento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Prioridade prioridade = Prioridade.MEDIA;

    @Column(name = "segredo_justica")
    @Builder.Default
    private Boolean segredoJustica = false;

    @Column(name = "pasta_fisica", length = 50)
    private String pastaFisica;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_responsavel_id")
    private Usuario advogadoResponsavel;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Parte> partes = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataMovimentacao DESC")
    @Builder.Default
    private List<Andamento> andamentos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "processo_tags", joinColumns = @JoinColumn(name = "processo_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════
    public void addParte(Parte parte) {
        partes.add(parte);
        parte.setProcesso(this);
    }

    public void removeParte(Parte parte) {
        partes.remove(parte);
        parte.setProcesso(null);
    }

    public void addAndamento(Andamento andamento) {
        andamentos.add(andamento);
        andamento.setProcesso(this);
    }

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoAcao {
        CONHECIMENTO,
        EXECUCAO,
        CAUTELAR,
        RECURSO,
        INCIDENTE,
        OUTROS
    }

    public enum AreaDireito {
        TRABALHISTA,
        CIVEL,
        FAMILIA,
        CRIMINAL,
        TRIBUTARIO,
        EMPRESARIAL,
        CONSUMIDOR,
        PREVIDENCIARIO,
        ADMINISTRATIVO,
        AMBIENTAL,
        IMOBILIARIO,
        OUTROS
    }

    public enum ProcessoStatus {
        EM_ANDAMENTO,
        SUSPENSO,
        ARQUIVADO,
        GANHO,
        PERDIDO,
        ACORDO,
        BAIXADO
    }

    public enum Prioridade {
        BAIXA,
        MEDIA,
        ALTA,
        URGENTE
    }
}


