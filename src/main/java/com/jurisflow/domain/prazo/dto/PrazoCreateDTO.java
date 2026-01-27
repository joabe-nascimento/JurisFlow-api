package com.jurisflow.domain.prazo.dto;

import com.jurisflow.domain.prazo.entity.Prazo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para criação de prazo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrazoCreateDTO {
    
    @NotBlank(message = "Título é obrigatório")
    private String titulo;
    
    private String descricao;
    
    @NotNull(message = "Tipo de prazo é obrigatório")
    private Prazo.TipoPrazo tipoPrazo;
    
    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;
    
    @NotNull(message = "Data de vencimento é obrigatória")
    private LocalDate dataVencimento;
    
    private Integer diasPrazo;
    private Prazo.TipoContagem tipoContagem;
    private Prazo.Prioridade prioridade;
    private String observacoes;
    
    private UUID processoId;
    private UUID responsavelId;
}


