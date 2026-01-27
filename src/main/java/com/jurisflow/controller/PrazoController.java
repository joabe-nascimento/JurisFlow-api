package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.prazo.dto.PrazoCreateDTO;
import com.jurisflow.domain.prazo.dto.PrazoCumprimentoDTO;
import com.jurisflow.domain.prazo.dto.PrazoDTO;
import com.jurisflow.domain.prazo.service.PrazoService;
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
 * Controller para gestão de prazos.
 */
@RestController
@RequestMapping("/v1/prazos")
@RequiredArgsConstructor
@Tag(name = "Prazos", description = "Gestão de prazos processuais")
public class PrazoController {

    private final PrazoService prazoService;

    @GetMapping
    @Operation(summary = "Listar prazos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<PageResponse<PrazoDTO>>> listar(
            @PageableDefault(size = 20, sort = "dataVencimento", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<PrazoDTO> response = prazoService.listar(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar prazo por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<PrazoDTO>> buscarPorId(@PathVariable UUID id) {
        PrazoDTO response = prazoService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar prazos de um processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<PrazoDTO>>> listarPorProcesso(@PathVariable UUID processoId) {
        List<PrazoDTO> response = prazoService.listarPorProcesso(processoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vencendo")
    @Operation(summary = "Listar prazos vencendo em X dias")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<PrazoDTO>>> listarVencendo(
            @RequestParam(defaultValue = "7") int dias
    ) {
        List<PrazoDTO> response = prazoService.listarPrazosVencendo(dias);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vencidos")
    @Operation(summary = "Listar prazos vencidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<PrazoDTO>>> listarVencidos() {
        List<PrazoDTO> response = prazoService.listarPrazosVencidos();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/urgentes/count")
    @Operation(summary = "Contar prazos urgentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<Long>> contarUrgentes() {
        long count = prazoService.contarPrazosUrgentes();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping
    @Operation(summary = "Criar prazo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<PrazoDTO>> criar(
            @Valid @RequestBody PrazoCreateDTO dto
    ) {
        PrazoDTO response = prazoService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Prazo criado com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar prazo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<PrazoDTO>> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PrazoCreateDTO dto
    ) {
        PrazoDTO response = prazoService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Prazo atualizado com sucesso"));
    }

    @PostMapping("/{id}/cumprir")
    @Operation(summary = "Cumprir prazo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<PrazoDTO>> cumprir(
            @PathVariable UUID id,
            @Valid @RequestBody PrazoCumprimentoDTO dto
    ) {
        PrazoDTO response = prazoService.cumprir(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Prazo cumprido com sucesso"));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar prazo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<PrazoDTO>> cancelar(
            @PathVariable UUID id,
            @RequestParam String motivo
    ) {
        PrazoDTO response = prazoService.cancelar(id, motivo);
        return ResponseEntity.ok(ApiResponse.success(response, "Prazo cancelado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir prazo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        prazoService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Prazo excluído com sucesso"));
    }
}


