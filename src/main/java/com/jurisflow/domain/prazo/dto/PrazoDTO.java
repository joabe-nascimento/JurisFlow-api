package com.jurisflow.domain.prazo.dto;

import com.jurisflow.domain.prazo.entity.Prazo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para exibição de prazo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrazoDTO {
    
    private UUID id;
    private String titulo;
    private String descricao;
    private Prazo.TipoPrazo tipoPrazo;
    private LocalDate dataInicio;
    private LocalDate dataVencimento;
    private Integer diasPrazo;
    private Prazo.TipoContagem tipoContagem;
    private Prazo.PrazoStatus status;
    private Prazo.Prioridade prioridade;
    
    // Cumprimento
    private LocalDateTime dataCumprimento;
    private String numeroProtocolo;
    private String observacoesCumprimento;
    private String documentoUrl;
    
    // Processo resumido
    private UUID processoId;
    private String processoNumero;
    private String processoTitulo;
    
    // Responsável resumido
    private UUID responsavelId;
    private String responsavelNome;
    
    // Campos calculados
    private Long diasRestantes;
    private Boolean vencido;
    private Boolean urgente;
    
    private LocalDateTime createdAt;
}


