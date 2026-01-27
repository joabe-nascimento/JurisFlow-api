package com.jurisflow.domain.timesheet.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.processo.repository.ProcessoRepository;
import com.jurisflow.domain.timesheet.dto.RegistroHoraCreateDTO;
import com.jurisflow.domain.timesheet.dto.RegistroHoraDTO;
import com.jurisflow.domain.timesheet.dto.ResumoTimesheetDTO;
import com.jurisflow.domain.timesheet.entity.RegistroHora;
import com.jurisflow.domain.timesheet.entity.RegistroHora.StatusRegistro;
import com.jurisflow.domain.timesheet.mapper.RegistroHoraMapper;
import com.jurisflow.domain.timesheet.repository.RegistroHoraRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import com.jurisflow.exception.BusinessException;
import com.jurisflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimesheetService {

    private final RegistroHoraRepository registroHoraRepository;
    private final ProcessoRepository processoRepository;
    private final RegistroHoraMapper registroHoraMapper;
    private final UsuarioService usuarioService;

    /**
     * Lista registros de hora paginados
     */
    @Transactional(readOnly = true)
    public PageResponse<RegistroHoraDTO> listar(Pageable pageable) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Page<RegistroHora> page = registroHoraRepository.findAllByEscritorioId(escritorioId, pageable);

        List<RegistroHoraDTO> content = page.getContent().stream()
                .map(registroHoraMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<RegistroHoraDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Busca registro por ID
     */
    @Transactional(readOnly = true)
    public RegistroHoraDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        RegistroHora registro = registroHoraRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de hora", id));
        return registroHoraMapper.toDTO(registro);
    }

    /**
     * Lista registros por período
     */
    @Transactional(readOnly = true)
    public List<RegistroHoraDTO> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return registroHoraRepository.findByPeriodo(escritorioId, inicio, fim).stream()
                .map(registroHoraMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista meus registros
     */
    @Transactional(readOnly = true)
    public List<RegistroHoraDTO> listarMeusRegistros(LocalDate inicio, LocalDate fim) {
        UUID usuarioId = usuarioService.getUsuarioIdFromContext();
        return registroHoraRepository.findByUsuarioIdAndPeriodo(usuarioId, inicio, fim).stream()
                .map(registroHoraMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista registros por processo
     */
    @Transactional(readOnly = true)
    public List<RegistroHoraDTO> listarPorProcesso(UUID processoId) {
        return registroHoraRepository.findByProcessoId(processoId).stream()
                .map(registroHoraMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista registros pendentes de aprovação
     */
    @Transactional(readOnly = true)
    public List<RegistroHoraDTO> listarPendentes() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return registroHoraRepository.findByStatus(escritorioId, StatusRegistro.PENDENTE).stream()
                .map(registroHoraMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Criar registro de hora
     */
    public RegistroHoraDTO criar(RegistroHoraCreateDTO dto) {
        RegistroHora registro = RegistroHora.builder()
                .data(dto.getData())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .duracao(dto.getDuracao())
                .descricao(dto.getDescricao())
                .tipoAtividade(dto.getTipoAtividade())
                .faturavel(dto.getFaturavel() != null ? dto.getFaturavel() : true)
                .valorHora(dto.getValorHora())
                .observacoes(dto.getObservacoes())
                .usuario(usuarioService.getUsuarioFromContext())
                .escritorio(usuarioService.getEscritorioFromContext())
                .build();

        // Vincular processo
        if (dto.getProcessoId() != null) {
            registro.setProcesso(processoRepository.findById(dto.getProcessoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Processo", dto.getProcessoId())));
        }

        registro = registroHoraRepository.save(registro);
        log.info("Registro de hora {} criado", registro.getId());

        return registroHoraMapper.toDTO(registro);
    }

    /**
     * Atualizar registro de hora
     */
    public RegistroHoraDTO atualizar(UUID id, RegistroHoraCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        RegistroHora registro = registroHoraRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de hora", id));

        if (registro.getStatus() == StatusRegistro.APROVADO || registro.getStatus() == StatusRegistro.FATURADO) {
            throw new BusinessException("Não é possível alterar um registro aprovado ou faturado");
        }

        registro.setData(dto.getData());
        registro.setHoraInicio(dto.getHoraInicio());
        registro.setHoraFim(dto.getHoraFim());
        registro.setDuracao(dto.getDuracao());
        registro.setDescricao(dto.getDescricao());
        registro.setTipoAtividade(dto.getTipoAtividade());
        registro.setFaturavel(dto.getFaturavel());
        registro.setValorHora(dto.getValorHora());
        registro.setObservacoes(dto.getObservacoes());

        registro = registroHoraRepository.save(registro);
        log.info("Registro de hora {} atualizado", id);

        return registroHoraMapper.toDTO(registro);
    }

    /**
     * Aprovar registro de hora
     */
    public RegistroHoraDTO aprovar(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        UUID aprovadorId = usuarioService.getUsuarioIdFromContext();

        RegistroHora registro = registroHoraRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de hora", id));

        registro.setStatus(StatusRegistro.APROVADO);
        registro.setAprovadoPor(aprovadorId);
        registro.setDataAprovacao(LocalDate.now());

        registro = registroHoraRepository.save(registro);
        log.info("Registro de hora {} aprovado", id);

        return registroHoraMapper.toDTO(registro);
    }

    /**
     * Rejeitar registro de hora
     */
    public RegistroHoraDTO rejeitar(UUID id, String motivo) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        RegistroHora registro = registroHoraRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de hora", id));

        registro.setStatus(StatusRegistro.REJEITADO);
        registro.setObservacoes((registro.getObservacoes() != null ? registro.getObservacoes() + "\n" : "") + "Rejeitado: " + motivo);

        registro = registroHoraRepository.save(registro);
        log.info("Registro de hora {} rejeitado", id);

        return registroHoraMapper.toDTO(registro);
    }

    /**
     * Excluir registro de hora
     */
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        RegistroHora registro = registroHoraRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de hora", id));

        if (registro.getStatus() == StatusRegistro.FATURADO) {
            throw new BusinessException("Não é possível excluir um registro já faturado");
        }

        registro.softDelete();
        registroHoraRepository.save(registro);
        log.info("Registro de hora {} excluído", id);
    }

    /**
     * Resumo do timesheet
     */
    @Transactional(readOnly = true)
    public ResumoTimesheetDTO getResumo(LocalDate inicio, LocalDate fim) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        Integer totalMinutos = registroHoraRepository.sumDuracaoByPeriodo(escritorioId, inicio, fim);
        Integer minutosFaturaveis = registroHoraRepository.sumDuracaoFaturavelByPeriodo(escritorioId, inicio, fim);
        long totalRegistros = registroHoraRepository.count();
        long registrosPendentes = registroHoraRepository.countByStatus(escritorioId, StatusRegistro.PENDENTE);
        long registrosAprovados = registroHoraRepository.countByStatus(escritorioId, StatusRegistro.APROVADO);

        return ResumoTimesheetDTO.builder()
                .totalMinutos(totalMinutos)
                .totalHorasFormatado(formatarHoras(totalMinutos))
                .minutosFaturaveis(minutosFaturaveis)
                .horasFaturaveisFormatado(formatarHoras(minutosFaturaveis))
                .totalRegistros(totalRegistros)
                .registrosPendentes(registrosPendentes)
                .registrosAprovados(registrosAprovados)
                .build();
    }

    private String formatarHoras(Integer minutos) {
        if (minutos == null || minutos == 0) return "0:00";
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }
}

