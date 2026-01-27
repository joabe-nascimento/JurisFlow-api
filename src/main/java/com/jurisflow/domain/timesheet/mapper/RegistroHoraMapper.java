package com.jurisflow.domain.timesheet.mapper;

import com.jurisflow.domain.timesheet.dto.RegistroHoraDTO;
import com.jurisflow.domain.timesheet.entity.RegistroHora;
import org.springframework.stereotype.Component;

@Component
public class RegistroHoraMapper {

    public RegistroHoraDTO toDTO(RegistroHora registro) {
        if (registro == null) return null;

        return RegistroHoraDTO.builder()
                .id(registro.getId())
                .data(registro.getData())
                .horaInicio(registro.getHoraInicio())
                .horaFim(registro.getHoraFim())
                .duracao(registro.getDuracao())
                .duracaoFormatada(registro.getDuracaoFormatada())
                .descricao(registro.getDescricao())
                .tipoAtividade(registro.getTipoAtividade())
                .faturavel(registro.getFaturavel())
                .valorHora(registro.getValorHora())
                .valorTotal(registro.getValorTotal())
                .status(registro.getStatus())
                .observacoes(registro.getObservacoes())
                .usuarioId(registro.getUsuario().getId())
                .usuarioNome(registro.getUsuario().getNome())
                .processoId(registro.getProcesso() != null ? registro.getProcesso().getId() : null)
                .processoNumero(registro.getProcesso() != null ? registro.getProcesso().getNumero() : null)
                .clienteNome(registro.getProcesso() != null && registro.getProcesso().getCliente() != null 
                        ? registro.getProcesso().getCliente().getNome() : null)
                .createdAt(registro.getCreatedAt())
                .build();
    }
}

