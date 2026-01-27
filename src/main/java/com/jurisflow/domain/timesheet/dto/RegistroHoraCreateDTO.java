package com.jurisflow.domain.timesheet.dto;

import com.jurisflow.domain.timesheet.entity.RegistroHora.TipoAtividade;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHoraCreateDTO {

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    @NotNull(message = "Duração é obrigatória")
    @Min(value = 1, message = "Duração mínima é 1 minuto")
    private Integer duracao; // em minutos

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Tipo de atividade é obrigatório")
    private TipoAtividade tipoAtividade;

    private Boolean faturavel;

    private BigDecimal valorHora;

    private String observacoes;

    private UUID processoId;
}

