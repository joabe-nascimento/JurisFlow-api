package com.jurisflow.domain.usuario.dto;

import com.jurisflow.domain.usuario.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para exibição de usuário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    
    private UUID id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String celular;
    private String fotoUrl;
    private String numeroOab;
    private String seccionalOab;
    private String especialidade;
    private Usuario.Role role;
    private Boolean ativo;
    private Boolean emailVerificado;
    private Boolean twoFactorEnabled;
    private LocalDateTime ultimoLogin;
    private LocalDateTime createdAt;
}


