package com.jurisflow.domain.processo.dto;

import com.jurisflow.domain.processo.entity.Processo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para exibição de processo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoDTO {
    
    private UUID id;
    private String numero;
    private String numeroAntigo;
    private String titulo;
    private String descricao;
    private Processo.TipoAcao tipoAcao;
    private Processo.AreaDireito areaDireito;
    private Processo.ProcessoStatus status;
    private String tribunal;
    private String vara;
    private String comarca;
    private String uf;
    private BigDecimal valorCausa;
    private BigDecimal valorEstimado;
    private LocalDate dataDistribuicao;
    private LocalDate dataEncerramento;
    private Processo.Prioridade prioridade;
    private Boolean segredoJustica;
    private String pastaFisica;
    private List<String> tags;
    
    // Cliente resumido
    private UUID clienteId;
    private String clienteNome;
    
    // Advogado responsável resumido
    private UUID advogadoResponsavelId;
    private String advogadoResponsavelNome;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


