package com.jurisflow.domain.financeiro.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.cliente.entity.Cliente;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.processo.entity.Processo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade que representa um lançamento financeiro.
 */
@Entity
@Table(name = "lancamentos", indexes = {
    @Index(name = "idx_lancamento_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_lancamento_tipo", columnList = "tipo"),
    @Index(name = "idx_lancamento_data_vencimento", columnList = "data_vencimento"),
    @Index(name = "idx_lancamento_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lancamento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaFinanceira categoria;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusLancamento status = StatusLancamento.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "recorrente")
    @Builder.Default
    private Boolean recorrente = false;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "comprovante_url", length = 500)
    private String comprovanteUrl;

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
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum TipoLancamento {
        RECEITA,
        DESPESA
    }

    public enum CategoriaFinanceira {
        // Receitas
        HONORARIOS,
        HONORARIOS_SUCUMBENCIA,
        HONORARIOS_EXITO,
        CUSTAS_REEMBOLSADAS,
        CONSULTORIA,
        
        // Despesas
        CUSTAS_PROCESSUAIS,
        DESPESAS_CARTORIO,
        DILIGENCIAS,
        HONORARIOS_PERITOS,
        SALARIOS,
        ALUGUEL,
        SERVICOS,
        MATERIAIS,
        IMPOSTOS,
        OUTROS
    }

    public enum StatusLancamento {
        PENDENTE,
        PAGO,
        ATRASADO,
        CANCELADO,
        PARCIAL
    }

    public enum FormaPagamento {
        DINHEIRO,
        PIX,
        TED,
        DOC,
        BOLETO,
        CARTAO_CREDITO,
        CARTAO_DEBITO,
        CHEQUE,
        DEPOSITO
    }
}

