package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.timesheet.dto.RegistroHoraCreateDTO;
import com.jurisflow.domain.timesheet.dto.RegistroHoraDTO;
import com.jurisflow.domain.timesheet.dto.ResumoTimesheetDTO;
import com.jurisflow.domain.timesheet.service.TimesheetService;
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
 * Controller para gestão de timesheet.
 */
@RestController
@RequestMapping("/v1/timesheet")
@RequiredArgsConstructor
@Tag(name = "Timesheet", description = "Controle de horas trabalhadas")
public class TimesheetController {

    private final TimesheetService timesheetService;

    @GetMapping
    @Operation(summary = "Listar registros de hora")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<PageResponse<RegistroHoraDTO>>> listar(
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<RegistroHoraDTO> response = timesheetService.listar(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar registro por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<RegistroHoraDTO>> buscarPorId(@PathVariable UUID id) {
        RegistroHoraDTO response = timesheetService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar registros por período")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<List<RegistroHoraDTO>>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        List<RegistroHoraDTO> response = timesheetService.listarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/meus")
    @Operation(summary = "Listar meus registros")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<RegistroHoraDTO>>> listarMeusRegistros(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        List<RegistroHoraDTO> response = timesheetService.listarMeusRegistros(inicio, fim);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar registros de um processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<List<RegistroHoraDTO>>> listarPorProcesso(@PathVariable UUID processoId) {
        List<RegistroHoraDTO> response = timesheetService.listarPorProcesso(processoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Listar registros pendentes de aprovação")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<List<RegistroHoraDTO>>> listarPendentes() {
        List<RegistroHoraDTO> response = timesheetService.listarPendentes();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo do timesheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ResumoTimesheetDTO>> getResumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        ResumoTimesheetDTO response = timesheetService.getResumo(inicio, fim);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar registro de hora")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<RegistroHoraDTO>> criar(
            @Valid @RequestBody RegistroHoraCreateDTO dto
    ) {
        RegistroHoraDTO response = timesheetService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registro criado com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar registro de hora")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<RegistroHoraDTO>> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody RegistroHoraCreateDTO dto
    ) {
        RegistroHoraDTO response = timesheetService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Registro atualizado com sucesso"));
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar registro de hora")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<RegistroHoraDTO>> aprovar(@PathVariable UUID id) {
        RegistroHoraDTO response = timesheetService.aprovar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Registro aprovado com sucesso"));
    }

    @PostMapping("/{id}/rejeitar")
    @Operation(summary = "Rejeitar registro de hora")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<RegistroHoraDTO>> rejeitar(
            @PathVariable UUID id,
            @RequestParam String motivo
    ) {
        RegistroHoraDTO response = timesheetService.rejeitar(id, motivo);
        return ResponseEntity.ok(ApiResponse.success(response, "Registro rejeitado"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir registro de hora")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        timesheetService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Registro excluído com sucesso"));
    }
}

