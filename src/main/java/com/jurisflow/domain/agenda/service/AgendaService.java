package com.jurisflow.domain.agenda.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.agenda.dto.EventoCreateDTO;
import com.jurisflow.domain.agenda.dto.EventoDTO;
import com.jurisflow.domain.agenda.entity.Evento;
import com.jurisflow.domain.agenda.entity.Evento.StatusEvento;
import com.jurisflow.domain.agenda.mapper.EventoMapper;
import com.jurisflow.domain.agenda.repository.EventoRepository;
import com.jurisflow.domain.cliente.repository.ClienteRepository;
import com.jurisflow.domain.processo.repository.ProcessoRepository;
import com.jurisflow.domain.usuario.repository.UsuarioRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import com.jurisflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AgendaService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final EventoMapper eventoMapper;
    private final UsuarioService usuarioService;

    /**
     * Lista eventos paginados
     */
    @Transactional(readOnly = true)
    public PageResponse<EventoDTO> listar(Pageable pageable) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Page<Evento> page = eventoRepository.findAllByEscritorioId(escritorioId, pageable);

        List<EventoDTO> content = page.getContent().stream()
                .map(eventoMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<EventoDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Busca evento por ID
     */
    @Transactional(readOnly = true)
    public EventoDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Evento evento = eventoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
        return eventoMapper.toDTO(evento);
    }

    /**
     * Lista eventos por período
     */
    @Transactional(readOnly = true)
    public List<EventoDTO> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        LocalDateTime dataInicio = inicio.atStartOfDay();
        LocalDateTime dataFim = fim.atTime(LocalTime.MAX);

        return eventoRepository.findByEscritorioIdAndPeriodo(escritorioId, dataInicio, dataFim).stream()
                .map(eventoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista eventos do dia
     */
    @Transactional(readOnly = true)
    public List<EventoDTO> listarEventosHoje() {
        return listarPorPeriodo(LocalDate.now(), LocalDate.now());
    }

    /**
     * Lista eventos da semana
     */
    @Transactional(readOnly = true)
    public List<EventoDTO> listarEventosSemana() {
        LocalDate hoje = LocalDate.now();
        LocalDate fimSemana = hoje.plusDays(7);
        return listarPorPeriodo(hoje, fimSemana);
    }

    /**
     * Lista próximos eventos
     */
    @Transactional(readOnly = true)
    public List<EventoDTO> listarProximos(int limite) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return eventoRepository.findProximosEventos(escritorioId, PageRequest.of(0, limite)).stream()
                .map(eventoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista eventos por processo
     */
    @Transactional(readOnly = true)
    public List<EventoDTO> listarPorProcesso(UUID processoId) {
        return eventoRepository.findByProcessoId(processoId).stream()
                .map(eventoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Criar evento
     */
    public EventoDTO criar(EventoCreateDTO dto) {
        Evento evento = Evento.builder()
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .tipo(dto.getTipo())
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .diaInteiro(dto.getDiaInteiro() != null ? dto.getDiaInteiro() : false)
                .local(dto.getLocal())
                .linkReuniao(dto.getLinkReuniao())
                .cor(dto.getCor() != null ? dto.getCor() : "#3b82f6")
                .lembreteMinutos(dto.getLembreteMinutos() != null ? dto.getLembreteMinutos() : 30)
                .recorrente(dto.getRecorrente() != null ? dto.getRecorrente() : false)
                .tipoRecorrencia(dto.getTipoRecorrencia())
                .recorrenciaFim(dto.getRecorrenciaFim())
                .observacoes(dto.getObservacoes())
                .escritorio(usuarioService.getEscritorioFromContext())
                .build();

        // Vincular responsável
        if (dto.getResponsavelId() != null) {
            evento.setResponsavel(usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getResponsavelId())));
        }

        // Vincular processo
        if (dto.getProcessoId() != null) {
            evento.setProcesso(processoRepository.findById(dto.getProcessoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Processo", dto.getProcessoId())));
        }

        // Vincular cliente
        if (dto.getClienteId() != null) {
            evento.setCliente(clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.getClienteId())));
        }

        // Adicionar participantes
        if (dto.getParticipantesIds() != null && !dto.getParticipantesIds().isEmpty()) {
            final Evento eventoFinal = evento;
            dto.getParticipantesIds().forEach(id -> {
                usuarioRepository.findById(id).ifPresent(eventoFinal::addParticipante);
            });
        }

        Evento eventoSalvo = eventoRepository.save(evento);
        log.info("Evento {} criado", eventoSalvo.getId());

        return eventoMapper.toDTO(eventoSalvo);
    }

    /**
     * Atualizar evento
     */
    public EventoDTO atualizar(UUID id, EventoCreateDTO dto) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Evento evento = eventoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));

        evento.setTitulo(dto.getTitulo());
        evento.setDescricao(dto.getDescricao());
        evento.setTipo(dto.getTipo());
        evento.setDataInicio(dto.getDataInicio());
        evento.setDataFim(dto.getDataFim());
        evento.setDiaInteiro(dto.getDiaInteiro());
        evento.setLocal(dto.getLocal());
        evento.setLinkReuniao(dto.getLinkReuniao());
        evento.setCor(dto.getCor());
        evento.setLembreteMinutos(dto.getLembreteMinutos());
        evento.setRecorrente(dto.getRecorrente());
        evento.setTipoRecorrencia(dto.getTipoRecorrencia());
        evento.setObservacoes(dto.getObservacoes());

        evento = eventoRepository.save(evento);
        log.info("Evento {} atualizado", id);

        return eventoMapper.toDTO(evento);
    }

    /**
     * Atualizar status do evento
     */
    public EventoDTO atualizarStatus(UUID id, StatusEvento status) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Evento evento = eventoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));

        evento.setStatus(status);
        evento = eventoRepository.save(evento);
        log.info("Status do evento {} atualizado para {}", id, status);

        return eventoMapper.toDTO(evento);
    }

    /**
     * Excluir evento
     */
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Evento evento = eventoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));

        evento.softDelete();
        eventoRepository.save(evento);
        log.info("Evento {} excluído", id);
    }
}

