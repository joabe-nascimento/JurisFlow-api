package com.jurisflow.domain.agenda.dto;

import com.jurisflow.domain.agenda.entity.Evento.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {
    private UUID id;
    private String titulo;
    private String descricao;
    private TipoEvento tipo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Boolean diaInteiro;
    private String local;
    private String linkReuniao;
    private StatusEvento status;
    private String cor;
    private Integer lembreteMinutos;
    private Boolean recorrente;
    private TipoRecorrencia tipoRecorrencia;
    private String observacoes;
    
    // Relacionamentos simplificados
    private UUID responsavelId;
    private String responsavelNome;
    private UUID processoId;
    private String processoNumero;
    private UUID clienteId;
    private String clienteNome;
    private List<ParticipanteDTO> participantes;
    
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipanteDTO {
        private UUID id;
        private String nome;
        private String email;
    }
}

