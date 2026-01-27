package com.jurisflow.domain.processo.mapper;

import com.jurisflow.domain.processo.dto.AndamentoCreateDTO;
import com.jurisflow.domain.processo.dto.AndamentoDTO;
import com.jurisflow.domain.processo.entity.Andamento;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Andamento e seus DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AndamentoMapper {

    @Mapping(target = "processoId", source = "processo.id")
    AndamentoDTO toDTO(Andamento andamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processo", ignore = true)
    @Mapping(target = "lido", constant = "false")
    Andamento toEntity(AndamentoCreateDTO dto);
}


