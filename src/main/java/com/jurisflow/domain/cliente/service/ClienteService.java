package com.jurisflow.domain.cliente.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.cliente.dto.ClienteCreateDTO;
import com.jurisflow.domain.cliente.dto.ClienteDTO;
import com.jurisflow.domain.cliente.entity.Cliente;
import com.jurisflow.domain.cliente.mapper.ClienteMapper;
import com.jurisflow.domain.cliente.repository.ClienteRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import com.jurisflow.exception.BusinessException;
import com.jurisflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para gestão de clientes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final UsuarioService usuarioService;

    /**
     * Lista todos os clientes do escritório com filtros.
     */
    public PageResponse<ClienteDTO> listar(String nome, String cpfCnpj, Boolean ativo, Pageable pageable) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        String nomePattern = null;
        if (nome != null && !nome.isBlank()) {
            nomePattern = "%" + nome.toLowerCase() + "%";
        }
        if (cpfCnpj != null && cpfCnpj.isBlank()) {
            cpfCnpj = null;
        }

        Page<Cliente> page = clienteRepository.findByFiltro(escritorioId, nomePattern, cpfCnpj, ativo, pageable);
        Page<ClienteDTO> dtoPage = page.map(clienteMapper::toDTO);
        return PageResponse.of(dtoPage);
    }

    /**
     * Busca cliente por ID.
     */
    @Cacheable(value = "clientes", key = "#id")
    public ClienteDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Cliente cliente = clienteRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return clienteMapper.toDTO(cliente);
    }

    /**
     * Cria um novo cliente.
     */
    @Transactional
    @CacheEvict(value = "clientes", allEntries = true)
    public ClienteDTO criar(ClienteCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        // Verifica duplicidade de CPF/CNPJ
        if (dto.getCpfCnpj() != null && !dto.getCpfCnpj().isBlank()) {
            if (clienteRepository.existsByCpfCnpjAndEscritorioId(dto.getCpfCnpj(), escritorioId)) {
                throw new BusinessException("CPF/CNPJ já cadastrado");
            }
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setEscritorio(usuarioService.getUsuarioFromContext().getEscritorio());

        cliente = clienteRepository.save(cliente);
        log.info("Cliente {} criado", cliente.getNome());

        return clienteMapper.toDTO(cliente);
    }

    /**
     * Atualiza um cliente.
     */
    @Transactional
    @CacheEvict(value = "clientes", key = "#id")
    public ClienteDTO atualizar(UUID id, ClienteCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Cliente cliente = clienteRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        // Verifica duplicidade de CPF/CNPJ (se alterou)
        if (dto.getCpfCnpj() != null && !dto.getCpfCnpj().equals(cliente.getCpfCnpj())) {
            if (clienteRepository.existsByCpfCnpjAndEscritorioId(dto.getCpfCnpj(), escritorioId)) {
                throw new BusinessException("CPF/CNPJ já cadastrado");
            }
        }

        clienteMapper.updateEntity(dto, cliente);
        cliente = clienteRepository.save(cliente);
        log.info("Cliente {} atualizado", cliente.getNome());

        return clienteMapper.toDTO(cliente);
    }

    /**
     * Exclui um cliente (soft delete).
     */
    @Transactional
    @CacheEvict(value = "clientes", key = "#id")
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Cliente cliente = clienteRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        cliente.softDelete();
        clienteRepository.save(cliente);
        log.info("Cliente {} excluído", cliente.getNome());
    }

    /**
     * Ativa/desativa o portal do cliente.
     */
    @Transactional
    @CacheEvict(value = "clientes", key = "#id")
    public ClienteDTO togglePortal(UUID id, boolean ativar) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Cliente cliente = clienteRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        cliente.setPortalAtivo(ativar);
        cliente = clienteRepository.save(cliente);
        log.info("Portal do cliente {} {}", cliente.getNome(), ativar ? "ativado" : "desativado");

        return clienteMapper.toDTO(cliente);
    }
}


