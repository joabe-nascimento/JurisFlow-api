package com.jurisflow.domain.agenda.dto;

import com.jurisflow.domain.agenda.entity.Evento.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoCreateDTO {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    private String descricao;

    @NotNull(message = "Tipo é obrigatório")
    private TipoEvento tipo;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private Boolean diaInteiro;

    private String local;

    private String linkReuniao;

    private String cor;

    private Integer lembreteMinutos;

    private Boolean recorrente;

    private TipoRecorrencia tipoRecorrencia;

    private LocalDateTime recorrenciaFim;

    private String observacoes;

    private UUID responsavelId;

    private UUID processoId;

    private UUID clienteId;

    private List<UUID> participantesIds;
}

