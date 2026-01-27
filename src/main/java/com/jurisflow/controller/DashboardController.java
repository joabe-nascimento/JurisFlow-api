package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.domain.cliente.repository.ClienteRepository;
import com.jurisflow.domain.prazo.service.PrazoService;
import com.jurisflow.domain.processo.repository.ProcessoRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller para dashboard com estatísticas.
 */
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Estatísticas e KPIs do escritório")
public class DashboardController {

    private final UsuarioService usuarioService;
    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final PrazoService prazoService;

    @GetMapping("/stats")
    @Operation(summary = "Obter estatísticas do dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        Map<String, Object> stats = new HashMap<>();
        
        // Contadores
        stats.put("processosAtivos", processoRepository.countAtivosbyEscritorioId(escritorioId));
        stats.put("clientesAtivos", clienteRepository.countAtivosbyEscritorioId(escritorioId));
        stats.put("prazosUrgentes", prazoService.contarPrazosUrgentes());
        
        // Prazos vencendo essa semana
        stats.put("prazosVencendoSemana", prazoService.listarPrazosVencendo(7).size());
        
        // Prazos vencidos
        stats.put("prazosVencidos", prazoService.listarPrazosVencidos().size());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}


