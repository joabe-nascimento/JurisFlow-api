package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.processo.dto.*;
import com.jurisflow.domain.processo.entity.Processo;
import com.jurisflow.domain.processo.service.ProcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gestão de processos.
 */
@RestController
@RequestMapping("/v1/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Gestão de processos jurídicos")
public class ProcessoController {

    private final ProcessoService processoService;

    @GetMapping
    @Operation(summary = "Listar processos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<PageResponse<ProcessoDTO>>> listar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) Processo.ProcessoStatus status,
            @RequestParam(required = false) Processo.AreaDireito areaDireito,
            @RequestParam(required = false) UUID advogadoId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<ProcessoDTO> response = processoService.listar(
                numero, clienteId, status, areaDireito, advogadoId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar processo por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<ProcessoDTO>> buscarPorId(@PathVariable UUID id) {
        ProcessoDTO response = processoService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Buscar processo por número CNJ")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<ProcessoDTO>> buscarPorNumero(@PathVariable String numero) {
        ProcessoDTO response = processoService.buscarPorNumero(numero);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ProcessoDTO>> criar(
            @Valid @RequestBody ProcessoCreateDTO dto
    ) {
        ProcessoDTO response = processoService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Processo criado com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ProcessoDTO>> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessoCreateDTO dto
    ) {
        ProcessoDTO response = processoService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Processo atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        processoService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Processo excluído com sucesso"));
    }

    @PostMapping("/{id}/arquivar")
    @Operation(summary = "Arquivar processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ProcessoDTO>> arquivar(@PathVariable UUID id) {
        ProcessoDTO response = processoService.arquivar(id, true);
        return ResponseEntity.ok(ApiResponse.success(response, "Processo arquivado com sucesso"));
    }

    @PostMapping("/{id}/desarquivar")
    @Operation(summary = "Desarquivar processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ProcessoDTO>> desarquivar(@PathVariable UUID id) {
        ProcessoDTO response = processoService.arquivar(id, false);
        return ResponseEntity.ok(ApiResponse.success(response, "Processo desarquivado com sucesso"));
    }

    // ═══════════════════════════════════════════════════════════════
    // ANDAMENTOS
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/{id}/andamentos")
    @Operation(summary = "Listar andamentos do processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<AndamentoDTO>>> listarAndamentos(@PathVariable UUID id) {
        List<AndamentoDTO> response = processoService.listarAndamentos(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/andamentos")
    @Operation(summary = "Adicionar andamento ao processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<AndamentoDTO>> adicionarAndamento(
            @PathVariable UUID id,
            @Valid @RequestBody AndamentoCreateDTO dto
    ) {
        AndamentoDTO response = processoService.adicionarAndamento(id, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Andamento adicionado com sucesso"));
    }
}


