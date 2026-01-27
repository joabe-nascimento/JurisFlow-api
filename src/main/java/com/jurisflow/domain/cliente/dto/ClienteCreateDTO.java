package com.jurisflow.domain.cliente.dto;

import com.jurisflow.domain.cliente.entity.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para criação de cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteCreateDTO {
    
    private Cliente.TipoPessoa tipoPessoa;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;
    
    private String cpfCnpj;
    private String rg;
    private String orgaoEmissor;
    private LocalDate dataNascimento;
    private Cliente.EstadoCivil estadoCivil;
    private String profissao;
    private String nacionalidade;
    
    @Email(message = "Email inválido")
    private String email;
    private String emailSecundario;
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
    
    private String comoConheceu;
    private String indicadoPor;
    private String observacoes;
}


