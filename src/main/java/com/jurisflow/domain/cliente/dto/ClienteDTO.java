package com.jurisflow.domain.cliente.dto;

import com.jurisflow.domain.cliente.entity.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para exibição de cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    
    private UUID id;
    private Cliente.TipoPessoa tipoPessoa;
    private String nome;
    private String cpfCnpj;
    private String rg;
    private LocalDate dataNascimento;
    private Cliente.EstadoCivil estadoCivil;
    private String profissao;
    private String email;
    private String telefone;
    private String celular;
    private Boolean whatsapp;
    
    // Endereço
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    
    private String fotoUrl;
    private Boolean ativo;
    private Boolean portalAtivo;
    private LocalDateTime createdAt;
}


