package com.jurisflow.domain.usuario.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.usuario.dto.UsuarioCreateDTO;
import com.jurisflow.domain.usuario.dto.UsuarioDTO;
import com.jurisflow.domain.usuario.dto.UsuarioUpdateDTO;
import com.jurisflow.domain.usuario.entity.Usuario;
import com.jurisflow.domain.usuario.mapper.UsuarioMapper;
import com.jurisflow.domain.usuario.repository.UsuarioRepository;
import com.jurisflow.exception.BusinessException;
import com.jurisflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para gestão de usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos os usuários do escritório.
     */
    public PageResponse<UsuarioDTO> listar(Pageable pageable) {
        UUID escritorioId = getEscritorioIdFromContext();
        Page<Usuario> page = usuarioRepository.findAll(pageable);
        Page<UsuarioDTO> dtoPage = page.map(usuarioMapper::toDTO);
        return PageResponse.of(dtoPage);
    }

    /**
     * Busca usuário por ID.
     */
    public UsuarioDTO buscarPorId(UUID id) {
        UUID escritorioId = getEscritorioIdFromContext();
        Usuario usuario = usuarioRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Cria um novo usuário no escritório.
     */
    @Transactional
    public UsuarioDTO criar(UsuarioCreateDTO dto) {
        // Verifica se email já existe
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado no sistema");
        }

        Usuario usuarioLogado = getUsuarioFromContext();
        Escritorio escritorio = usuarioLogado.getEscritorio();

        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setEscritorio(escritorio);
        
        if (dto.getRole() == null) {
            usuario.setRole(Usuario.Role.ADVOGADO);
        }

        usuario = usuarioRepository.save(usuario);
        log.info("Usuário {} criado no escritório {}", usuario.getEmail(), escritorio.getId());

        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Atualiza um usuário.
     */
    @Transactional
    public UsuarioDTO atualizar(UUID id, UsuarioUpdateDTO dto) {
        UUID escritorioId = getEscritorioIdFromContext();
        Usuario usuario = usuarioRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        // Verifica se novo email já existe (se foi alterado)
        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new BusinessException("Email já cadastrado no sistema");
            }
        }

        usuarioMapper.updateEntity(dto, usuario);
        usuario = usuarioRepository.save(usuario);
        log.info("Usuário {} atualizado", usuario.getEmail());

        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Exclui um usuário (soft delete).
     */
    @Transactional
    public void excluir(UUID id) {
        UUID escritorioId = getEscritorioIdFromContext();
        Usuario usuario = usuarioRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        Usuario usuarioLogado = getUsuarioFromContext();
        if (usuario.getId().equals(usuarioLogado.getId())) {
            throw new BusinessException("Não é possível excluir o próprio usuário");
        }

        usuario.softDelete();
        usuarioRepository.save(usuario);
        log.info("Usuário {} excluído", usuario.getEmail());
    }

    /**
     * Altera a senha do usuário.
     */
    @Transactional
    public void alterarSenha(UUID id, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        log.info("Senha do usuário {} alterada", usuario.getEmail());
    }

    /**
     * Obtém o usuário logado do contexto de segurança.
     */
    public Usuario getUsuarioFromContext() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /**
     * Obtém o ID do usuário logado.
     */
    public UUID getUsuarioIdFromContext() {
        return getUsuarioFromContext().getId();
    }

    /**
     * Obtém o ID do escritório do usuário logado.
     */
    public UUID getEscritorioIdFromContext() {
        return getUsuarioFromContext().getEscritorio().getId();
    }

    /**
     * Obtém o escritório do usuário logado.
     */
    public Escritorio getEscritorioFromContext() {
        return getUsuarioFromContext().getEscritorio();
    }
}


