package com.jurisflow.domain.prazo.mapper;

import com.jurisflow.domain.prazo.dto.PrazoCreateDTO;
import com.jurisflow.domain.prazo.dto.PrazoDTO;
import com.jurisflow.domain.prazo.entity.Prazo;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Prazo e seus DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PrazoMapper {

    @Mapping(target = "processoId", source = "processo.id")
    @Mapping(target = "processoNumero", source = "processo.numero")
    @Mapping(target = "processoTitulo", source = "processo.titulo")
    @Mapping(target = "responsavelId", source = "responsavel.id")
    @Mapping(target = "responsavelNome", source = "responsavel.nome")
    @Mapping(target = "diasRestantes", expression = "java(prazo.getDiasRestantes())")
    @Mapping(target = "vencido", expression = "java(prazo.isVencido())")
    @Mapping(target = "urgente", expression = "java(prazo.isUrgente())")
    PrazoDTO toDTO(Prazo prazo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "processo", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    @Mapping(target = "status", constant = "PENDENTE")
    @Mapping(target = "alerta7Dias", constant = "false")
    @Mapping(target = "alerta3Dias", constant = "false")
    @Mapping(target = "alerta1Dia", constant = "false")
    @Mapping(target = "alertaNoDia", constant = "false")
    Prazo toEntity(PrazoCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "processo", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    void updateEntity(PrazoCreateDTO dto, @MappingTarget Prazo prazo);
}


