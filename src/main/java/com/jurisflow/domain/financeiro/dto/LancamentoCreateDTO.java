package com.jurisflow.domain.financeiro.dto;

import com.jurisflow.domain.financeiro.entity.Lancamento.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoCreateDTO {

    @NotNull(message = "Tipo é obrigatório")
    private TipoLancamento tipo;

    @NotNull(message = "Categoria é obrigatória")
    private CategoriaFinanceira categoria;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    @NotNull(message = "Data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    private FormaPagamento formaPagamento;

    private String numeroDocumento;

    private Integer numeroParcelas; // Para criar parcelamento

    private Boolean recorrente;

    private String observacoes;

    private UUID processoId;

    private UUID clienteId;
}

