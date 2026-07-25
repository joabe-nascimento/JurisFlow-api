package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.IntegrationManagementService;
import com.jurisflow.integration.IntegrationManagementService.*;
import com.jurisflow.integration.ai.AIService;
import com.jurisflow.integration.ai.AIService.*;
import com.jurisflow.integration.ai.AIAgentService;
import com.jurisflow.integration.ai.AIAgentService.AgentDefinition;
import com.jurisflow.integration.ai.AIPipelineService;
import com.jurisflow.integration.ai.AIPipelineService.*;
import com.jurisflow.integration.ai.AIPromptTemplateService;
import com.jurisflow.integration.ai.AIPromptTemplateService.PromptTemplate;
import com.jurisflow.integration.ai.AIAnalyticsService;
import com.jurisflow.integration.ai.AIAnalyticsService.AIMetricsDTO;
import com.jurisflow.integration.ai.PythonAIService;
import com.jurisflow.integration.ai.RAGKnowledgeService;
import com.jurisflow.integration.ai.RAGKnowledgeService.*;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/integrations")
@RequiredArgsConstructor
@Tag(name = "Integrações", description = "Gerenciamento de integrações externas")
public class IntegrationController {

    private final IntegrationManagementService managementService;
    private final AIService aiService;
    private final AIAgentService agentService;
    private final RAGKnowledgeService ragService;
    private final AIPipelineService pipelineService;
    private final AIPromptTemplateService promptTemplateService;
    private final AIAnalyticsService analyticsService;
    private final PythonAIService pythonAIService;
    private final TribunalService tribunalService;
    private final NotificationService notificationService;
    private final CalendarService calendarService;
    private final SignatureService signatureService;
    private final UsuarioService usuarioService;

    // ==================== GERENCIAMENTO ====================

    @GetMapping
    @Operation(summary = "Listar todas as integrações disponíveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<IntegrationDTO>>> listIntegrations() {
        return ResponseEntity.ok(ApiResponse.success(managementService.listIntegrations()));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Listar integrações agrupadas por categoria")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> summarizeDocument(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.summarizeDocument(escritorioId, request.get("text"));
        recordAi(escritorioId, "summarize", response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/jurisprudence")
    @Operation(summary = "Analisar jurisprudência")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> analyzeJurisprudence(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.analyzeJurisprudence(
            escritorioId, request.get("tema"), request.get("areaJuridica"));
        recordAi(escritorioId, "jurisprudence", response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/generate-document")
    @Operation(summary = "Gerar documento usando IA")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> generateDocument(
            @RequestBody DocumentGenerationRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.generateDocument(escritorioId, request);
        recordAi(escritorioId, "generate-document", response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/analyze-contract")
    @Operation(summary = "Analisar contrato usando IA")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> analyzeContract(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.analyzeContract(escritorioId, request.get("text"));
        recordAi(escritorioId, "analyze-contract", response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/predict-outcome")
    @Operation(summary = "Prever resultado de processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> predictOutcome(
            @RequestBody CaseAnalysisRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        AIResponse response = aiService.predictOutcome(escritorioId, request);
        recordAi(escritorioId, "predict-outcome", response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/chat")
    @Operation(summary = "Chat com Bruna (assistente jurídica — RAG + LLM)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> chat(
            @RequestBody ChatRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        // Python + OpenRouter (gratuito) — sempre preferir LLM real via Bruna
        if (pythonAIService.isAvailable()) {
            Optional<AIResponse> bruna = pythonAIService.brunaChat(escritorioId, request);
            if (bruna.isPresent()) {
                recordAi(escritorioId, "bruna-chat", bruna.get());
                return ResponseEntity.ok(ApiResponse.success(bruna.get()));
            }
        }

        String agentId = request.getAgentId();
        String ragContext = "";
        if (request.isUseRag()) {
            RAGSearchResult search = ragService.search(escritorioId, request.getMessage(), 4);
            ragContext = ragService.buildContext(search);
        }
        AIResponse response = aiService.chat(escritorioId, request, ragContext);
        recordAi(escritorioId, "chat", response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ai/agents")
    @Operation(summary = "Listar agentes de IA disponíveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<AgentDefinition>>> listAgents() {
        return ResponseEntity.ok(ApiResponse.success(agentService.listAgents()));
    }

    @PostMapping("/ai/agents/{agentId}/run")
    @Operation(summary = "Executar agente de IA especializado")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> runAgent(
            @PathVariable String agentId,
            @RequestBody Map<String, Object> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        String input = (String) request.get("input");
        boolean useRag = request.get("useRag") != null && Boolean.TRUE.equals(request.get("useRag"));
        AIResponse response = agentService.runAgent(escritorioId, agentId, input, useRag);
        recordAi(escritorioId, "agent:" + agentId, response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ai/rag/documents")
    @Operation(summary = "Listar documentos da base RAG")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<KnowledgeDocument>>> listRagDocuments() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return ResponseEntity.ok(ApiResponse.success(ragService.listDocuments(escritorioId)));
    }

    @PostMapping("/ai/rag/documents")
    @Operation(summary = "Adicionar documento à base RAG")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<KnowledgeDocument>> addRagDocument(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        KnowledgeDocument doc = ragService.addDocument(
            escritorioId,
            request.get("title"),
            request.get("content"),
            request.get("category"),
            request.get("source")
        );
        return ResponseEntity.ok(ApiResponse.success(doc, "Documento indexado com sucesso"));
    }

    @PostMapping("/ai/rag/search")
    @Operation(summary = "Buscar na base RAG")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<RAGSearchResult>> searchRag(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        int limit = request.containsKey("limit") ? Integer.parseInt(request.get("limit")) : 5;
        RAGSearchResult result = ragService.search(escritorioId, request.get("query"), limit);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/ai/rag/documents/{documentId}")
    @Operation(summary = "Remover documento da base RAG")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeRagDocument(@PathVariable String documentId) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        ragService.removeDocument(escritorioId, documentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Documento removido"));
    }

    @GetMapping("/ai/stack")
    @Operation(summary = "Status do stack cognitivo (Java + Python)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiStack() {
        Map<String, Object> stack = new LinkedHashMap<>();
        stack.put("layers", List.of(
            Map.of("id", "frontend", "name", "Next.js", "role", "UI / Copilot"),
            Map.of("id", "api", "name", "Spring Boot", "role", "API / Orquestração"),
            Map.of("id", "python", "name", "FastAPI", "role", "RAG / Pipelines")
        ));
        stack.put("pythonEnabled", pythonAIService.isAvailable());
        stack.put("pythonUrl", pythonAIService.isAvailable() ? "connected" : "offline");

        pythonAIService.getStackStatus().ifPresent(py -> {
            stack.put("pythonService", py.getService());
            stack.put("retrieval", py.getRetrieval());
            stack.put("pythonDocuments", py.getTotal_documents());
            stack.put("pythonChunks", py.getTotal_chunks());
            stack.put("capabilities", py.getCapabilities());
        });

        if (!pythonAIService.isAvailable()) {
            stack.put("retrieval", "lexical-java");
            stack.put("fallback", "Motor Java em memória (demo)");
        }

        return ResponseEntity.ok(ApiResponse.success(stack));
    }

    @GetMapping("/ai/metrics")
    @Operation(summary = "Métricas de uso de IA do escritório")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIMetricsDTO>> getAiMetrics() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        int ragCount = ragService.listDocuments(escritorioId).size();
        int agentCount = agentService.listAgents().size();
        return ResponseEntity.ok(ApiResponse.success(
            analyticsService.getMetrics(escritorioId, ragCount, agentCount)));
    }

    @GetMapping("/ai/prompt-templates")
    @Operation(summary = "Listar templates de prompts jurídicos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<PromptTemplate>>> listPromptTemplates() {
        return ResponseEntity.ok(ApiResponse.success(promptTemplateService.listTemplates()));
    }

    @PostMapping("/ai/prompt-templates/{templateId}/run")
    @Operation(summary = "Executar template de prompt com variáveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<AIResponse>> runPromptTemplate(
            @PathVariable String templateId,
            @RequestBody Map<String, String> variables) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        String filled = promptTemplateService.fillTemplate(templateId, variables);
        if (filled == null) {
            return ResponseEntity.ok(ApiResponse.error("Template não encontrado"));
        }
        PromptTemplate template = promptTemplateService.getTemplate(templateId);
        boolean useRag = true;
        String ragContext = "";
        if (useRag) {
            RAGSearchResult search = ragService.search(escritorioId, filled, 3);
            ragContext = ragService.buildContext(search);
        }
        String prompt = filled + (ragContext.isBlank() ? "" : "\n\nContexto RAG:\n" + ragContext);
        AIResponse response = aiService.processPrompt(escritorioId, prompt);
        recordAi(escritorioId, "template:" + templateId, response);
        if (response.getMetadata() == null) {
            response.setMetadata(Map.of("templateId", templateId, "agentId", template.getAgentId()));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ai/pipelines")
    @Operation(summary = "Listar pipelines cognitivos disponíveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<PipelineDefinition>>> listPipelines() {
        return ResponseEntity.ok(ApiResponse.success(pipelineService.listPipelines()));
    }

    @PostMapping("/ai/pipelines/{pipelineId}/run")
    @Operation(summary = "Executar pipeline cognitivo multi-etapa")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<PipelineRunResult>> runPipeline(
            @PathVariable String pipelineId,
            @RequestBody Map<String, Object> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        String input = (String) request.get("input");
        boolean useRag = request.get("useRag") == null || Boolean.TRUE.equals(request.get("useRag"));
        PipelineRunResult result = pipelineService.runPipeline(escritorioId, pipelineId, input, useRag);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private void recordAi(UUID escritorioId, String operation, AIResponse response) {
        String provider = "demo";
        if (response.getMetadata() != null && response.getMetadata().get("provider") != null) {
            provider = String.valueOf(response.getMetadata().get("provider"));
        } else if (response.getMetadata() != null && response.getMetadata().get("mode") != null) {
            provider = String.valueOf(response.getMetadata().get("mode"));
        }
        analyticsService.record(escritorioId, operation, response.isSuccess(), provider);
    }

    // ==================== TRIBUNAIS ====================

    @GetMapping("/tribunais")
    @Operation(summary = "Listar tribunais suportados")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<TribunalInfo>>> getTribunaisSuportados() {
        return ResponseEntity.ok(ApiResponse.success(tribunalService.getTribunaisSuportados()));
    }

    @PostMapping("/tribunais/consulta")
    @Operation(summary = "Consultar processo em tribunal")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<ProcessoConsultaResponse>> consultarProcesso(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        ProcessoConsultaResponse response = tribunalService.consultarProcesso(
            escritorioId, request.get("numeroProcesso"), request.get("tribunal"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/tribunais/receita")
    @Operation(summary = "Consultar CPF/CNPJ na Receita Federal")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<NotificationResult>> sendNotification(
            @RequestBody NotificationRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        NotificationResult result = notificationService.sendNotification(escritorioId, request).join();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/notifications/prazo-reminder")
    @Operation(summary = "Enviar lembrete de prazo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<NotificationResult>> sendPrazoReminder(
            @RequestBody PrazoReminderRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        NotificationResult result = notificationService.sendPrazoReminder(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== CALENDÁRIO ====================

    @PostMapping("/calendar/audiencia")
    @Operation(summary = "Criar audiência no calendário")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<CalendarSyncResult>> createAudiencia(
            @RequestBody AudienciaRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CalendarSyncResult result = calendarService.createAudiencia(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/calendar/prazo")
    @Operation(summary = "Criar prazo no calendário")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<CalendarSyncResult>> createPrazo(
            @RequestBody PrazoCalendarRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CalendarSyncResult result = calendarService.createPrazo(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/calendar/reuniao")
    @Operation(summary = "Criar reunião no calendário")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<CalendarSyncResult>> createReuniao(
            @RequestBody ReuniaoRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CalendarSyncResult result = calendarService.createReuniao(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/calendar/events")
    @Operation(summary = "Obter eventos do calendário")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<List<SignatureProvider>>> getSignatureProviders() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return ResponseEntity.ok(ApiResponse.success(signatureService.getAvailableProviders(escritorioId)));
    }

    @PostMapping("/signature/create-envelope")
    @Operation(summary = "Criar envelope de assinatura")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<SignatureEnvelopeResult>> createSignatureEnvelope(
            @RequestBody SignatureRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        SignatureEnvelopeResult result = signatureService.createEnvelope(escritorioId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/signature/envelope/{envelopeId}/status")
    @Operation(summary = "Verificar status do envelope")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<EnvelopeStatus>> getEnvelopeStatus(
            @PathVariable String envelopeId) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        EnvelopeStatus status = signatureService.getEnvelopeStatus(escritorioId, envelopeId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/signature/validate-certificate")
    @Operation(summary = "Validar certificado digital")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA', 'FINANCEIRO', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<CertificateValidationResult>> validateCertificate(
            @RequestBody Map<String, String> request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        CertificateValidationResult result = signatureService.validateCertificate(
            escritorioId, request.get("certificateData"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

