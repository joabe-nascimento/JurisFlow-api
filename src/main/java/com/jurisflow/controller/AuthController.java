package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.domain.usuario.dto.LoginRequestDTO;
import com.jurisflow.domain.usuario.dto.LoginResponseDTO;
import com.jurisflow.domain.usuario.dto.UsuarioCreateDTO;
import com.jurisflow.domain.usuario.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticação.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna tokens JWT")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login realizado com sucesso"));
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo escritório", description = "Cria um novo escritório e usuário administrador")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> registrar(
            @Valid @RequestBody UsuarioCreateDTO usuario,
            @RequestParam String nomeEscritorio
    ) {
        LoginResponseDTO response = authService.registrar(usuario, nomeEscritorio);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Escritório registrado com sucesso"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualizar token", description = "Gera novo access token usando refresh token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        LoginResponseDTO response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}


