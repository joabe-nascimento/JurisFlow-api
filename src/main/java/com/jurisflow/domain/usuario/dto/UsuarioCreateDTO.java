package com.jurisflow.domain.usuario.dto;

import com.jurisflow.domain.usuario.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para criação de usuário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;
    
    private String cpf;
    private String telefone;
    private String celular;
    private String numeroOab;
    private String seccionalOab;
    private String especialidade;
    private BigDecimal valorHora;
    private Usuario.Role role;
}


