package com.jurisflow.domain.agenda.mapper;

import com.jurisflow.domain.agenda.dto.EventoDTO;
import com.jurisflow.domain.agenda.entity.Evento;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EventoMapper {

    public EventoDTO toDTO(Evento evento) {
        if (evento == null) return null;

        return EventoDTO.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .descricao(evento.getDescricao())
                .tipo(evento.getTipo())
                .dataInicio(evento.getDataInicio())
                .dataFim(evento.getDataFim())
                .diaInteiro(evento.getDiaInteiro())
                .local(evento.getLocal())
                .linkReuniao(evento.getLinkReuniao())
                .status(evento.getStatus())
                .cor(evento.getCor())
                .lembreteMinutos(evento.getLembreteMinutos())
                .recorrente(evento.getRecorrente())
                .tipoRecorrencia(evento.getTipoRecorrencia())
                .observacoes(evento.getObservacoes())
                .responsavelId(evento.getResponsavel() != null ? evento.getResponsavel().getId() : null)
                .responsavelNome(evento.getResponsavel() != null ? evento.getResponsavel().getNome() : null)
                .processoId(evento.getProcesso() != null ? evento.getProcesso().getId() : null)
                .processoNumero(evento.getProcesso() != null ? evento.getProcesso().getNumero() : null)
                .clienteId(evento.getCliente() != null ? evento.getCliente().getId() : null)
                .clienteNome(evento.getCliente() != null ? evento.getCliente().getNome() : null)
                .participantes(evento.getParticipantes() != null ? 
                        evento.getParticipantes().stream()
                                .map(u -> EventoDTO.ParticipanteDTO.builder()
                                        .id(u.getId())
                                        .nome(u.getNome())
                                        .email(u.getEmail())
                                        .build())
                                .collect(Collectors.toList())
                        : null)
                .createdAt(evento.getCreatedAt())
                .build();
    }
}

