package com.jurisflow.domain.processo.dto;

import com.jurisflow.domain.processo.entity.Andamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para exibição de andamento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AndamentoDTO {
    
    private UUID id;
    private UUID processoId;
    private LocalDateTime dataMovimentacao;
    private String descricao;
    private Andamento.TipoAndamento tipo;
    private Andamento.FonteAndamento fonte;
    private String codigoMovimentacao;
    private String documentoUrl;
    private String observacoes;
    private Boolean lido;
    private Boolean importante;
    private LocalDateTime createdAt;
}


