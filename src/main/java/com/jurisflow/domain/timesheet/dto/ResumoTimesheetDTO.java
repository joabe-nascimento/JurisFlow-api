package com.jurisflow.domain.timesheet.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoTimesheetDTO {
    private Integer totalMinutos;
    private String totalHorasFormatado;
    private Integer minutosFaturaveis;
    private String horasFaturaveisFormatado;
    private BigDecimal valorTotalFaturavel;
    private long totalRegistros;
    private long registrosPendentes;
    private long registrosAprovados;
}

