package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.usuario.dto.UsuarioCreateDTO;
import com.jurisflow.domain.usuario.dto.UsuarioDTO;
import com.jurisflow.domain.usuario.dto.UsuarioUpdateDTO;
import com.jurisflow.domain.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para gestão de usuários.
 */
@RestController
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gestão de usuários do escritório")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuários")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<PageResponse<UsuarioDTO>>> listar(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UsuarioDTO> response = usuarioService.listar(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> buscarPorId(@PathVariable UUID id) {
        UsuarioDTO response = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar dados do usuário logado")
    public ResponseEntity<ApiResponse<UsuarioDTO>> me() {
        UsuarioDTO response = usuarioService.buscarPorId(
            usuarioService.getUsuarioFromContext().getId()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar usuário")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> criar(
            @Valid @RequestBody UsuarioCreateDTO dto
    ) {
        UsuarioDTO response = usuarioService.criar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Usuário criado com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioUpdateDTO dto
    ) {
        UsuarioDTO response = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuário atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        usuarioService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuário excluído com sucesso"));
    }

    @PostMapping("/{id}/alterar-senha")
    @Operation(summary = "Alterar senha do usuário")
    public ResponseEntity<ApiResponse<Void>> alterarSenha(
            @PathVariable UUID id,
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha
    ) {
        usuarioService.alterarSenha(id, senhaAtual, novaSenha);
        return ResponseEntity.ok(ApiResponse.success(null, "Senha alterada com sucesso"));
    }
}


