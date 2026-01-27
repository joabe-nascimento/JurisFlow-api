package com.jurisflow.domain.usuario.mapper;

import com.jurisflow.domain.usuario.dto.UsuarioCreateDTO;
import com.jurisflow.domain.usuario.dto.UsuarioDTO;
import com.jurisflow.domain.usuario.dto.UsuarioUpdateDTO;
import com.jurisflow.domain.usuario.entity.Usuario;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Usuario e seus DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsuarioMapper {

    UsuarioDTO toDTO(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "emailVerificado", constant = "false")
    @Mapping(target = "twoFactorEnabled", constant = "false")
    @Mapping(target = "ativo", constant = "true")
    Usuario toEntity(UsuarioCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escritorio", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "emailVerificado", ignore = true)
    @Mapping(target = "twoFactorEnabled", ignore = true)
    @Mapping(target = "twoFactorSecret", ignore = true)
    void updateEntity(UsuarioUpdateDTO dto, @MappingTarget Usuario usuario);
}


