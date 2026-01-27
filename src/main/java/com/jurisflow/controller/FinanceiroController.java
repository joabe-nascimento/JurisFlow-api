package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.financeiro.dto.LancamentoCreateDTO;
import com.jurisflow.domain.financeiro.dto.LancamentoDTO;
import com.jurisflow.domain.financeiro.dto.ResumoFinanceiroDTO;
import com.jurisflow.domain.financeiro.entity.Lancamento.FormaPagamento;
import com.jurisflow.domain.financeiro.entity.Lancamento.TipoLancamento;
import com.jurisflow.domain.financeiro.service.FinanceiroService;
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
 * Controller para gestão financeira.
 */
@RestController
@RequestMapping("/v1/financeiro")
@RequiredArgsConstructor
@Tag(name = "Financeiro", description = "Gestão de receitas e despesas")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    @GetMapping
    @Operation(summary = "Listar lançamentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<PageResponse<LancamentoDTO>>> listar(
            @RequestParam(required = false) TipoLancamento tipo,
            @PageableDefault(size = 20, sort = "dataVencimento", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<LancamentoDTO> response = financeiroService.listar(tipo, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lançamento por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<LancamentoDTO>> buscarPorId(@PathVariable UUID id) {
        LancamentoDTO response = financeiroService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar lançamentos por período")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<List<LancamentoDTO>>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        List<LancamentoDTO> response = financeiroService.listarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/atrasados")
    @Operation(summary = "Listar lançamentos atrasados")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<List<LancamentoDTO>>> listarAtrasados() {
        List<LancamentoDTO> response = financeiroService.listarAtrasados();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar lançamentos de um processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<List<LancamentoDTO>>> listarPorProcesso(@PathVariable UUID processoId) {
        List<LancamentoDTO> response = financeiroService.listarPorProcesso(processoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo financeiro")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<ResumoFinanceiroDTO>> getResumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        ResumoFinanceiroDTO response = financeiroService.getResumo(inicio, fim);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar lançamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<LancamentoDTO>> criar(
            @Valid @RequestBody LancamentoCreateDTO dto
    ) {
        LancamentoDTO response = financeiroService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lançamento criado com sucesso"));
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Registrar pagamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<LancamentoDTO>> registrarPagamento(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento,
            @RequestParam FormaPagamento formaPagamento
    ) {
        LancamentoDTO response = financeiroService.registrarPagamento(id, dataPagamento, formaPagamento);
        return ResponseEntity.ok(ApiResponse.success(response, "Pagamento registrado com sucesso"));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar lançamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<LancamentoDTO>> cancelar(@PathVariable UUID id) {
        LancamentoDTO response = financeiroService.cancelar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lançamento cancelado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lançamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        financeiroService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lançamento excluído com sucesso"));
    }
}

