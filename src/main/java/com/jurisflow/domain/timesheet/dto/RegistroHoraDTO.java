package com.jurisflow.domain.timesheet.dto;

import com.jurisflow.domain.timesheet.entity.RegistroHora.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHoraDTO {
    private UUID id;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Integer duracao;
    private String duracaoFormatada;
    private String descricao;
    private TipoAtividade tipoAtividade;
    private Boolean faturavel;
    private BigDecimal valorHora;
    private BigDecimal valorTotal;
    private StatusRegistro status;
    private String observacoes;
    
    // Relacionamentos
    private UUID usuarioId;
    private String usuarioNome;
    private UUID processoId;
    private String processoNumero;
    private String clienteNome;
    
    private LocalDateTime createdAt;
}

