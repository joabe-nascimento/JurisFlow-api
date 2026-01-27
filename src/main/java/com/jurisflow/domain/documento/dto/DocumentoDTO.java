package com.jurisflow.domain.documento.dto;

import com.jurisflow.domain.documento.entity.Documento.CategoriaDocumento;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {
    private UUID id;
    private String nome;
    private String nomeOriginal;
    private String descricao;
    private String tipo;
    private Long tamanho;
    private String url;
    private CategoriaDocumento categoria;
    private Integer versao;
    private Boolean confidencial;
    
    // Relacionamentos simplificados
    private UUID processoId;
    private String processoNumero;
    private UUID clienteId;
    private String clienteNome;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

