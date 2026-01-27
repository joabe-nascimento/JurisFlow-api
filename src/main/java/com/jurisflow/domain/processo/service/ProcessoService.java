package com.jurisflow.domain.processo.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.cliente.entity.Cliente;
import com.jurisflow.domain.cliente.repository.ClienteRepository;
import com.jurisflow.domain.processo.dto.*;
import com.jurisflow.domain.processo.entity.Andamento;
import com.jurisflow.domain.processo.entity.Parte;
import com.jurisflow.domain.processo.entity.Processo;
import com.jurisflow.domain.processo.mapper.AndamentoMapper;
import com.jurisflow.domain.processo.mapper.ProcessoMapper;
import com.jurisflow.domain.processo.repository.ProcessoRepository;
import com.jurisflow.domain.usuario.entity.Usuario;
import com.jurisflow.domain.usuario.repository.UsuarioRepository;
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

import java.util.List;
import java.util.UUID;

/**
 * Service para gestão de processos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcessoMapper processoMapper;
    private final AndamentoMapper andamentoMapper;
    private final UsuarioService usuarioService;

    /**
     * Lista processos com filtros.
     */
    public PageResponse<ProcessoDTO> listar(
            String numero,
            UUID clienteId,
            Processo.ProcessoStatus status,
            Processo.AreaDireito areaDireito,
            UUID advogadoId,
            Pageable pageable
    ) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Page<Processo> page = processoRepository.findByFiltro(
                escritorioId, numero, clienteId, status, areaDireito, advogadoId, pageable
        );
        Page<ProcessoDTO> dtoPage = page.map(processoMapper::toDTO);
        return PageResponse.of(dtoPage);
    }

    /**
     * Busca processo por ID.
     */
    @Cacheable(value = "processos", key = "#id")
    public ProcessoDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", id));
        return processoMapper.toDTO(processo);
    }

    /**
     * Busca processo pelo número CNJ.
     */
    public ProcessoDTO buscarPorNumero(String numero) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByNumeroAndEscritorioId(numero, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "numero", numero));
        return processoMapper.toDTO(processo);
    }

    /**
     * Cria um novo processo.
     */
    @Transactional
    @CacheEvict(value = "processos", allEntries = true)
    public ProcessoDTO criar(ProcessoCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        // Verifica duplicidade de número
        if (processoRepository.existsByNumeroAndEscritorioId(dto.getNumero(), escritorioId)) {
            throw new BusinessException("Já existe um processo com este número");
        }

        // Busca cliente
        Cliente cliente = clienteRepository.findByIdAndEscritorioId(dto.getClienteId(), escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", dto.getClienteId()));

        Processo processo = processoMapper.toEntity(dto);
        processo.setEscritorio(usuarioService.getUsuarioFromContext().getEscritorio());
        processo.setCliente(cliente);

        // Vincula advogado responsável se informado
        if (dto.getAdvogadoResponsavelId() != null) {
            Usuario advogado = usuarioRepository.findByIdAndEscritorioId(dto.getAdvogadoResponsavelId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Advogado", "id", dto.getAdvogadoResponsavelId()));
            processo.setAdvogadoResponsavel(advogado);
        }

        // Adiciona partes se informadas
        if (dto.getPartes() != null && !dto.getPartes().isEmpty()) {
            for (ParteCreateDTO parteDTO : dto.getPartes()) {
                Parte parte = Parte.builder()
                        .nome(parteDTO.getNome())
                        .cpfCnpj(parteDTO.getCpfCnpj())
                        .tipoParte(parteDTO.getTipoParte())
                        .polo(parteDTO.getPolo() != null ? parteDTO.getPolo() : Parte.Polo.ATIVO)
                        .email(parteDTO.getEmail())
                        .telefone(parteDTO.getTelefone())
                        .endereco(parteDTO.getEndereco())
                        .advogadoNome(parteDTO.getAdvogadoNome())
                        .advogadoOab(parteDTO.getAdvogadoOab())
                        .advogadoEmail(parteDTO.getAdvogadoEmail())
                        .advogadoTelefone(parteDTO.getAdvogadoTelefone())
                        .observacoes(parteDTO.getObservacoes())
                        .build();
                processo.addParte(parte);
            }
        }

        processo = processoRepository.save(processo);
        log.info("Processo {} criado", processo.getNumero());

        return processoMapper.toDTO(processo);
    }

    /**
     * Atualiza um processo.
     */
    @Transactional
    @CacheEvict(value = "processos", key = "#id")
    public ProcessoDTO atualizar(UUID id, ProcessoCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", id));

        // Verifica duplicidade de número (se alterou)
        if (!dto.getNumero().equals(processo.getNumero())) {
            if (processoRepository.existsByNumeroAndEscritorioId(dto.getNumero(), escritorioId)) {
                throw new BusinessException("Já existe um processo com este número");
            }
        }

        processoMapper.updateEntity(dto, processo);

        // Atualiza cliente se alterou
        if (!dto.getClienteId().equals(processo.getCliente().getId())) {
            Cliente cliente = clienteRepository.findByIdAndEscritorioId(dto.getClienteId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", dto.getClienteId()));
            processo.setCliente(cliente);
        }

        // Atualiza advogado responsável
        if (dto.getAdvogadoResponsavelId() != null) {
            Usuario advogado = usuarioRepository.findByIdAndEscritorioId(dto.getAdvogadoResponsavelId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Advogado", "id", dto.getAdvogadoResponsavelId()));
            processo.setAdvogadoResponsavel(advogado);
        } else {
            processo.setAdvogadoResponsavel(null);
        }

        processo = processoRepository.save(processo);
        log.info("Processo {} atualizado", processo.getNumero());

        return processoMapper.toDTO(processo);
    }

    /**
     * Exclui um processo (soft delete).
     */
    @Transactional
    @CacheEvict(value = "processos", key = "#id")
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", id));

        processo.softDelete();
        processoRepository.save(processo);
        log.info("Processo {} excluído", processo.getNumero());
    }

    /**
     * Arquiva/desarquiva um processo.
     */
    @Transactional
    @CacheEvict(value = "processos", key = "#id")
    public ProcessoDTO arquivar(UUID id, boolean arquivar) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", id));

        processo.setStatus(arquivar ? Processo.ProcessoStatus.ARQUIVADO : Processo.ProcessoStatus.EM_ANDAMENTO);
        processo = processoRepository.save(processo);
        log.info("Processo {} {}", processo.getNumero(), arquivar ? "arquivado" : "desarquivado");

        return processoMapper.toDTO(processo);
    }

    /**
     * Adiciona um andamento ao processo.
     */
    @Transactional
    @CacheEvict(value = "processos", key = "#processoId")
    public AndamentoDTO adicionarAndamento(UUID processoId, AndamentoCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByIdAndEscritorioId(processoId, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", processoId));

        Andamento andamento = andamentoMapper.toEntity(dto);
        processo.addAndamento(andamento);

        processoRepository.save(processo);
        log.info("Andamento adicionado ao processo {}", processo.getNumero());

        return andamentoMapper.toDTO(andamento);
    }

    /**
     * Lista andamentos de um processo.
     */
    public List<AndamentoDTO> listarAndamentos(UUID processoId) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Processo processo = processoRepository.findByIdAndEscritorioId(processoId, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", processoId));

        return processo.getAndamentos().stream()
                .map(andamentoMapper::toDTO)
                .toList();
    }
}


