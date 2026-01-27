package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.IntegrationManagementService;
import com.jurisflow.integration.IntegrationManagementService.*;
import com.jurisflow.integration.ai.AIService;
import com.jurisflow.integration.ai.AIService.*;
import com.jurisflow.integration.calendar.CalendarService;
import com.jurisflow.integration.calendar.CalendarService.*;
import com.jurisflow.integration.notification.NotificationService;
import com.jurisflow.integration.notification.NotificationService.*;
import com.jurisflow.integration.signature.SignatureService;
import com.jurisflow.integration.signature.SignatureService.*;
import com.jurisflow.integration.tribunal.TribunalService;
import com.jurisflow.integration.tribunal.TribunalService.*;
import com.jurisflow.domain.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/integrations")
@RequiredArgsConstructor
@Tag(name = "Integrações", description = "Gerenciamento de integrações externas")
public class IntegrationController {

    private final IntegrationManagementService managementService;
    private final AIService aiService;
    private final TribunalService tribunalService;
    private final NotificationService notificationService;
    private final CalendarService calendarService;
    private final SignatureService signatureService;
    private final UsuarioService usuarioService;

    // ==================== GERENCIAMENTO ====================

    @GetMapping
    @Operation(summary = "Listar todas as integrações disponíveis")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<IntegrationDTO>>> listIntegrations() {
        return ResponseEntity.ok(ApiResponse.success(managementService.listIntegrations()));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Listar integrações agrupadas por categoria")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, List<IntegrationDTO>>>> listByCategory() {
        return ResponseEntity.ok(ApiResponse.success(managementService.listIntegrationsByCategory()));
    }

    @GetMapping("/{type}")
    @Operation(summary = "Obter configuração de uma integração")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IntegrationConfigDTO>> getIntegration(
            @PathVariable IntegrationType type) {
        return ResponseEntity.ok(ApiResponse.success(managementService.getIntegration(type)));
    }

    @PostMapping("/{type}/configure")
    @Operation(summary = "Configurar uma integração")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IntegrationConfigDTO>> configureIntegration(
            @PathVariable IntegrationType type,
            @RequestBody IntegrationConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            managementService.configureIntegration(type, request)));
    }

    @PostMapping("/{type}/toggle")
    @Operation(summary = "Habilitar/desabilitar uma integração")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IntegrationConfigDTO>> toggleIntegration(
            @PathVariable IntegrationType type,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(ApiResponse.success(
            managementService.toggleIntegration(type, enabled)));
    }

    @PostMapping("/{type}/test")
    @Operation(summary = "Testar conexão de uma integração")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TestConnectionResult>> testConnection(
            @PathVariable IntegrationType type) {
        return ResponseEntity.ok(ApiResponse.success(managementService.testConnection(type)));
    }

    @DeleteMapping("/{type}")
    @Operation(summary = "Remover configuração de uma integração")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeIntegration(
            @PathVariable IntegrationType type) {
        managementService.removeIntegration(type);
        return ResponseEntity.ok(ApiResponse.success(null, "Integração removida com sucesso"));
    }

    // ==================== INTELIGÊNCIA ARTIFICIAL ====================

    @PostMapping("/ai/summarize")
    @Operation(summary = "Resumir documento usando IA")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AIResponse>> summarizeDocument(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.summarizeDocument(escritorioId, request.get("text"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/jurisprudence")
    @Operation(summary = "Analisar jurisprudência")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AIResponse>> analyzeJurisprudence(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.analyzeJurisprudence(
            escritorioId, request.get("tema"), request.get("areaJuridica"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/generate-document")
    @Operation(summary = "Gerar documento usando IA")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AIResponse>> generateDocument(
            @RequestBody DocumentGenerationRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.generateDocument(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/analyze-contract")
    @Operation(summary = "Analisar contrato usando IA")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AIResponse>> analyzeContract(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.analyzeContract(escritorioId, request.get("text"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/predict-outcome")
    @Operation(summary = "Prever resultado de processo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AIResponse>> predictOutcome(
            @RequestBody CaseAnalysisRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.predictOutcome(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== TRIBUNAIS ====================

    @GetMapping("/tribunais")
    @Operation(summary = "Listar tribunais suportados")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TribunalInfo>>> getTribunaisSuportados() {
        return ResponseEntity.ok(ApiResponse.success(tribunalService.getTribunaisSuportados()));
    }

    @PostMapping("/tribunais/consulta")
    @Operation(summary = "Consultar processo em tribunal")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProcessoConsultaResponse>> consultarProcesso(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        ProcessoConsultaResponse response = tribunalService.consultarProcesso(
            escritorioId, request.get("numeroProcesso"), request.get("tribunal"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/tribunais/receita")
    @Operation(summary = "Consultar CPF/CNPJ na Receita Federal")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ConsultaReceitaResponse>> consultarReceita(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        ConsultaReceitaResponse response = tribunalService.consultarReceita(
            escritorioId, request.get("documento"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== NOTIFICAÇÕES ====================

    @PostMapping("/notifications/send")
    @Operation(summary = "Enviar notificação")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<NotificationResult>> sendNotification(
            @RequestBody NotificationRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        NotificationResult result = notificationService.sendNotification(escritorioId, request).join();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/notifications/prazo-reminder")
    @Operation(summary = "Enviar lembrete de prazo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<NotificationResult>> sendPrazoReminder(
            @RequestBody PrazoReminderRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        NotificationResult result = notificationService.sendPrazoReminder(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== CALENDÁRIO ====================

    @PostMapping("/calendar/audiencia")
    @Operation(summary = "Criar audiência no calendário")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CalendarSyncResult>> createAudiencia(
            @RequestBody AudienciaRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CalendarSyncResult result = calendarService.createAudiencia(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/calendar/prazo")
    @Operation(summary = "Criar prazo no calendário")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CalendarSyncResult>> createPrazo(
            @RequestBody PrazoCalendarRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CalendarSyncResult result = calendarService.createPrazo(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/calendar/reuniao")
    @Operation(summary = "Criar reunião no calendário")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CalendarSyncResult>> createReuniao(
            @RequestBody ReuniaoRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CalendarSyncResult result = calendarService.createReuniao(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/calendar/events")
    @Operation(summary = "Obter eventos do calendário")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<CalendarEvent>>> getCalendarEvents(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        List<CalendarEvent> events = calendarService.getEvents(escritorioId, start, end);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    // ==================== ASSINATURA DIGITAL ====================

    @GetMapping("/signature/providers")
    @Operation(summary = "Listar provedores de assinatura disponíveis")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<SignatureProvider>>> getSignatureProviders() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return ResponseEntity.ok(ApiResponse.success(signatureService.getAvailableProviders(escritorioId)));
    }

    @PostMapping("/signature/create-envelope")
    @Operation(summary = "Criar envelope de assinatura")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<SignatureEnvelopeResult>> createSignatureEnvelope(
            @RequestBody SignatureRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        SignatureEnvelopeResult result = signatureService.createEnvelope(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/signature/envelope/{envelopeId}/status")
    @Operation(summary = "Verificar status do envelope")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<EnvelopeStatus>> getEnvelopeStatus(
            @PathVariable String envelopeId) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        EnvelopeStatus status = signatureService.getEnvelopeStatus(escritorioId, envelopeId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/signature/validate-certificate")
    @Operation(summary = "Validar certificado digital")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CertificateValidationResult>> validateCertificate(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CertificateValidationResult result = signatureService.validateCertificate(
            escritorioId, request.get("certificateData"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

