package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.agenda.dto.EventoCreateDTO;
import com.jurisflow.domain.agenda.dto.EventoDTO;
import com.jurisflow.domain.agenda.entity.Evento.StatusEvento;
import com.jurisflow.domain.agenda.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controller para gestão da agenda.
 */
@RestController
@RequestMapping("/v1/agenda")
@RequiredArgsConstructor
@Tag(name = "Agenda", description = "Gestão de eventos e compromissos")
public class AgendaController {

    private final AgendaService agendaService;

    @GetMapping
    @Operation(summary = "Listar eventos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<PageResponse<EventoDTO>>> listar(
            @PageableDefault(size = 20, sort = "dataInicio", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<EventoDTO> response = agendaService.listar(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<EventoDTO>> buscarPorId(@PathVariable UUID id) {
        EventoDTO response = agendaService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar eventos por período")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        List<EventoDTO> response = agendaService.listarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/hoje")
    @Operation(summary = "Listar eventos de hoje")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listarHoje() {
        List<EventoDTO> response = agendaService.listarEventosHoje();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/semana")
    @Operation(summary = "Listar eventos da semana")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listarSemana() {
        List<EventoDTO> response = agendaService.listarEventosSemana();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/proximos")
    @Operation(summary = "Listar próximos eventos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listarProximos(
            @RequestParam(defaultValue = "5") int limite
    ) {
        List<EventoDTO> response = agendaService.listarProximos(limite);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar eventos de um processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listarPorProcesso(@PathVariable UUID processoId) {
        List<EventoDTO> response = agendaService.listarPorProcesso(processoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar evento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<EventoDTO>> criar(
            @Valid @RequestBody EventoCreateDTO dto
    ) {
        EventoDTO response = agendaService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evento criado com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<EventoDTO>> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EventoCreateDTO dto
    ) {
        EventoDTO response = agendaService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Evento atualizado com sucesso"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do evento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<EventoDTO>> atualizarStatus(
            @PathVariable UUID id,
            @RequestParam StatusEvento status
    ) {
        EventoDTO response = agendaService.atualizarStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Status atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        agendaService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Evento excluído com sucesso"));
    }
}

