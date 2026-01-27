package com.jurisflow.domain.processo.dto;

import com.jurisflow.domain.processo.entity.Andamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para criação de andamento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AndamentoCreateDTO {
    
    @NotNull(message = "Data da movimentação é obrigatória")
    private LocalDateTime dataMovimentacao;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    private Andamento.TipoAndamento tipo;
    private Andamento.FonteAndamento fonte;
    private String codigoMovimentacao;
    private String documentoUrl;
    private String observacoes;
    private Boolean importante;
}


