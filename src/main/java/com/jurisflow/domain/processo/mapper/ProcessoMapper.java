package com.jurisflow.domain.processo.mapper;

import com.jurisflow.domain.processo.dto.ProcessoCreateDTO;
import com.jurisflow.domain.processo.dto.ProcessoDTO;
import com.jurisflow.domain.processo.entity.Processo;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Processo e seus DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessoMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNome", source = "cliente.nome")
    @Mapping(target = "advogadoResponsavelId", source = "advogadoResponsavel.id")
    @Mapping(target = "advogadoResponsavelNome", source = "advogadoResponsavel.nome")
    ProcessoDTO toDTO(Processo processo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "advogadoResponsavel", ignore = true)
    @Mapping(target = "partes", ignore = true)
    @Mapping(target = "andamentos", ignore = true)
    @Mapping(target = "status", constant = "EM_ANDAMENTO")
    Processo toEntity(ProcessoCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "advogadoResponsavel", ignore = true)
    @Mapping(target = "partes", ignore = true)
    @Mapping(target = "andamentos", ignore = true)
    void updateEntity(ProcessoCreateDTO dto, @MappingTarget Processo processo);
}


