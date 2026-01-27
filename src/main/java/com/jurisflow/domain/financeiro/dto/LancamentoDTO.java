package com.jurisflow.domain.financeiro.dto;

import com.jurisflow.domain.financeiro.entity.Lancamento.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoDTO {
    private UUID id;
    private TipoLancamento tipo;
    private CategoriaFinanceira categoria;
    private String descricao;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private StatusLancamento status;
    private FormaPagamento formaPagamento;
    private String numeroDocumento;
    private Integer numeroParcela;
    private Integer totalParcelas;
    private Boolean recorrente;
    private String observacoes;
    private String comprovanteUrl;
    
    // Relacionamentos
    private UUID processoId;
    private String processoNumero;
    private UUID clienteId;
    private String clienteNome;
    
    private LocalDateTime createdAt;
}

