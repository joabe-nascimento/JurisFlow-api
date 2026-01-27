package com.jurisflow.domain.processo.dto;

import com.jurisflow.domain.processo.entity.Parte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de parte.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParteCreateDTO {
    
    @NotBlank(message = "Nome da parte é obrigatório")
    private String nome;
    
    private String cpfCnpj;
    
    @NotNull(message = "Tipo da parte é obrigatório")
    private Parte.TipoParte tipoParte;
    
    private Parte.Polo polo;
    private String email;
    private String telefone;
    private String endereco;
    
    // Advogado da parte contrária
    private String advogadoNome;
    private String advogadoOab;
    private String advogadoEmail;
    private String advogadoTelefone;
    
    private String observacoes;
}


