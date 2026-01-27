package com.jurisflow.domain.prazo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cumprimento de prazo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrazoCumprimentoDTO {
    
    private String numeroProtocolo;
    private String observacoes;
    private String documentoUrl;
}


