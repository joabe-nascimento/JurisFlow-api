package com.jurisflow.domain.financeiro.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoFinanceiroDTO {
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldo;
    private BigDecimal receitasPendentes;
    private BigDecimal despesasPendentes;
    private BigDecimal receitasAtrasadas;
    private BigDecimal despesasAtrasadas;
    private long totalLancamentos;
    private long lancamentosPendentes;
    private long lancamentosAtrasados;
}

