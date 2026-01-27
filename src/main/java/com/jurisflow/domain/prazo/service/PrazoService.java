package com.jurisflow.domain.prazo.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.prazo.dto.PrazoCreateDTO;
import com.jurisflow.domain.prazo.dto.PrazoCumprimentoDTO;
import com.jurisflow.domain.prazo.dto.PrazoDTO;
import com.jurisflow.domain.prazo.entity.Prazo;
import com.jurisflow.domain.prazo.mapper.PrazoMapper;
import com.jurisflow.domain.prazo.repository.PrazoRepository;
import com.jurisflow.domain.processo.entity.Processo;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service para gestão de prazos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PrazoService {

    private final PrazoRepository prazoRepository;
    private final ProcessoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrazoMapper prazoMapper;
    private final UsuarioService usuarioService;

    /**
     * Lista prazos do escritório.
     */
    public PageResponse<PrazoDTO> listar(Pageable pageable) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Page<Prazo> page = prazoRepository.findAllByEscritorioId(escritorioId, pageable);
        Page<PrazoDTO> dtoPage = page.map(prazoMapper::toDTO);
        return PageResponse.of(dtoPage);
    }

    /**
     * Busca prazo por ID.
     */
    @Cacheable(value = "prazos", key = "#id")
    public PrazoDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Prazo prazo = prazoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prazo", "id", id));
        return prazoMapper.toDTO(prazo);
    }

    /**
     * Lista prazos de um processo.
     */
    public List<PrazoDTO> listarPorProcesso(UUID processoId) {
        List<Prazo> prazos = prazoRepository.findByProcessoId(processoId);
        return prazos.stream().map(prazoMapper::toDTO).toList();
    }

    /**
     * Lista prazos vencendo em X dias.
     */
    public List<PrazoDTO> listarPrazosVencendo(int dias) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        List<Prazo> prazos = prazoRepository.findPrazosVencendoAte(escritorioId, dataLimite);
        return prazos.stream().map(prazoMapper::toDTO).toList();
    }

    /**
     * Lista prazos vencidos.
     */
    public List<PrazoDTO> listarPrazosVencidos() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        List<Prazo> prazos = prazoRepository.findPrazosVencidos(escritorioId);
        return prazos.stream().map(prazoMapper::toDTO).toList();
    }

    /**
     * Cria um novo prazo.
     */
    @Transactional
    @CacheEvict(value = "prazos", allEntries = true)
    public PrazoDTO criar(PrazoCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        Prazo prazo = prazoMapper.toEntity(dto);
        prazo.setEscritorio(usuarioService.getUsuarioFromContext().getEscritorio());

        // Vincula processo se informado
        if (dto.getProcessoId() != null) {
            Processo processo = processoRepository.findByIdAndEscritorioId(dto.getProcessoId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", dto.getProcessoId()));
            prazo.setProcesso(processo);
        }

        // Vincula responsável se informado
        if (dto.getResponsavelId() != null) {
            Usuario responsavel = usuarioRepository.findByIdAndEscritorioId(dto.getResponsavelId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", dto.getResponsavelId()));
            prazo.setResponsavel(responsavel);
        }

        prazo = prazoRepository.save(prazo);
        log.info("Prazo {} criado - vencimento: {}", prazo.getTitulo(), prazo.getDataVencimento());

        return prazoMapper.toDTO(prazo);
    }

    /**
     * Atualiza um prazo.
     */
    @Transactional
    @CacheEvict(value = "prazos", key = "#id")
    public PrazoDTO atualizar(UUID id, PrazoCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Prazo prazo = prazoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prazo", "id", id));

        if (prazo.getStatus() == Prazo.PrazoStatus.CUMPRIDO) {
            throw new BusinessException("Não é possível editar um prazo já cumprido");
        }

        prazoMapper.updateEntity(dto, prazo);

        // Atualiza processo se alterou
        if (dto.getProcessoId() != null) {
            Processo processo = processoRepository.findByIdAndEscritorioId(dto.getProcessoId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", dto.getProcessoId()));
            prazo.setProcesso(processo);
        }

        // Atualiza responsável se alterou
        if (dto.getResponsavelId() != null) {
            Usuario responsavel = usuarioRepository.findByIdAndEscritorioId(dto.getResponsavelId(), escritorioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", dto.getResponsavelId()));
            prazo.setResponsavel(responsavel);
        }

        prazo = prazoRepository.save(prazo);
        log.info("Prazo {} atualizado", prazo.getTitulo());

        return prazoMapper.toDTO(prazo);
    }

    /**
     * Cumpre um prazo.
     */
    @Transactional
    @CacheEvict(value = "prazos", key = "#id")
    public PrazoDTO cumprir(UUID id, PrazoCumprimentoDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Prazo prazo = prazoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prazo", "id", id));

        if (prazo.getStatus() == Prazo.PrazoStatus.CUMPRIDO) {
            throw new BusinessException("Prazo já foi cumprido");
        }

        prazo.cumprir(dto.getNumeroProtocolo(), dto.getObservacoes());
        if (dto.getDocumentoUrl() != null) {
            prazo.setDocumentoUrl(dto.getDocumentoUrl());
        }

        prazo = prazoRepository.save(prazo);
        log.info("Prazo {} cumprido", prazo.getTitulo());

        return prazoMapper.toDTO(prazo);
    }

    /**
     * Cancela um prazo.
     */
    @Transactional
    @CacheEvict(value = "prazos", key = "#id")
    public PrazoDTO cancelar(UUID id, String motivo) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Prazo prazo = prazoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prazo", "id", id));

        if (prazo.getStatus() == Prazo.PrazoStatus.CUMPRIDO) {
            throw new BusinessException("Não é possível cancelar um prazo já cumprido");
        }

        prazo.setStatus(Prazo.PrazoStatus.CANCELADO);
        prazo.setObservacoes(motivo);
        prazo = prazoRepository.save(prazo);
        log.info("Prazo {} cancelado - motivo: {}", prazo.getTitulo(), motivo);

        return prazoMapper.toDTO(prazo);
    }

    /**
     * Exclui um prazo (soft delete).
     */
    @Transactional
    @CacheEvict(value = "prazos", key = "#id")
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Prazo prazo = prazoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prazo", "id", id));

        prazo.softDelete();
        prazoRepository.save(prazo);
        log.info("Prazo {} excluído", prazo.getTitulo());
    }

    /**
     * Conta prazos urgentes (vencendo em 3 dias ou menos).
     */
    public long contarPrazosUrgentes() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        LocalDate dataLimite = LocalDate.now().plusDays(3);
        return prazoRepository.countPrazosUrgentes(escritorioId, dataLimite);
    }
}


