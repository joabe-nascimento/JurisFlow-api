package com.jurisflow.domain.documento.dto;

import com.jurisflow.domain.documento.entity.Documento.CategoriaDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoUploadDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    private String descricao;
    
    @NotNull(message = "Categoria é obrigatória")
    private CategoriaDocumento categoria;
    
    private UUID processoId;
    
    private UUID clienteId;
    
    private Boolean confidencial;
}

