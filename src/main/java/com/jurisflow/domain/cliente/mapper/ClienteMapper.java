package com.jurisflow.domain.cliente.mapper;

import com.jurisflow.domain.cliente.dto.ClienteCreateDTO;
import com.jurisflow.domain.cliente.dto.ClienteDTO;
import com.jurisflow.domain.cliente.entity.Cliente;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Cliente e seus DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClienteMapper {

    ClienteDTO toDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "portalAtivo", constant = "false")
    Cliente toEntity(ClienteCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    void updateEntity(ClienteCreateDTO dto, @MappingTarget Cliente cliente);
}


