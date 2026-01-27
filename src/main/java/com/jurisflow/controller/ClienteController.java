package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.cliente.dto.ClienteCreateDTO;
import com.jurisflow.domain.cliente.dto.ClienteDTO;
import com.jurisflow.domain.cliente.service.ClienteService;
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

import java.util.UUID;

/**
 * Controller para gestão de clientes.
 */
@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestão de clientes do escritório")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<PageResponse<ClienteDTO>>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpfCnpj,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<ClienteDTO> response = clienteService.listar(nome, cpfCnpj, ativo, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<ClienteDTO>> buscarPorId(@PathVariable UUID id) {
        ClienteDTO response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<ClienteDTO>> criar(
            @Valid @RequestBody ClienteCreateDTO dto
    ) {
        ClienteDTO response = clienteService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente criado com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<ClienteDTO>> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteCreateDTO dto
    ) {
        ClienteDTO response = clienteService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Cliente atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        clienteService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cliente excluído com sucesso"));
    }

    @PostMapping("/{id}/portal/ativar")
    @Operation(summary = "Ativar portal do cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ClienteDTO>> ativarPortal(@PathVariable UUID id) {
        ClienteDTO response = clienteService.togglePortal(id, true);
        return ResponseEntity.ok(ApiResponse.success(response, "Portal do cliente ativado"));
    }

    @PostMapping("/{id}/portal/desativar")
    @Operation(summary = "Desativar portal do cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<ClienteDTO>> desativarPortal(@PathVariable UUID id) {
        ClienteDTO response = clienteService.togglePortal(id, false);
        return ResponseEntity.ok(ApiResponse.success(response, "Portal do cliente desativado"));
    }
}


