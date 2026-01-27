package com.jurisflow.domain.usuario.dto;

import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para atualização de usuário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {
    
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;
    
    @Email(message = "Email inválido")
    private String email;
    
    private String cpf;
    private String telefone;
    private String celular;
    private String fotoUrl;
    private String numeroOab;
    private String seccionalOab;
    private String especialidade;
    private BigDecimal valorHora;
    private Usuario.Role role;
    private Boolean ativo;
}


