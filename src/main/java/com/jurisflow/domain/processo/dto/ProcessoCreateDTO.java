package com.jurisflow.domain.processo.dto;

import com.jurisflow.domain.processo.entity.Processo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO para criação de processo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoCreateDTO {
    
    @NotBlank(message = "Número do processo é obrigatório")
    @Size(max = 25, message = "Número deve ter no máximo 25 caracteres")
    private String numero;
    
    private String numeroAntigo;
    
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 300, message = "Título deve ter no máximo 300 caracteres")
    private String titulo;
    
    private String descricao;
    
    @NotNull(message = "Tipo de ação é obrigatório")
    private Processo.TipoAcao tipoAcao;
    
    @NotNull(message = "Área do direito é obrigatória")
    private Processo.AreaDireito areaDireito;
    
    private String tribunal;
    private String vara;
    private String comarca;
    private String uf;
    private BigDecimal valorCausa;
    private BigDecimal valorEstimado;
    private LocalDate dataDistribuicao;
    private Processo.Prioridade prioridade;
    private Boolean segredoJustica;
    private String pastaFisica;
    private String observacoes;
    private List<String> tags;
    
    @NotNull(message = "Cliente é obrigatório")
    private UUID clienteId;
    
    private UUID advogadoResponsavelId;
    
    // Partes iniciais (opcional)
    private List<ParteCreateDTO> partes;
}


