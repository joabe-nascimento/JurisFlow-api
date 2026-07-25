package com.jurisflow.integration.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Cliente HTTP para o motor de IA em Python (FastAPI).
 * RAG com TF-IDF, pipelines e status do stack cognitivo.
 */
@Service
@Slf4j
public class PythonAIService {

    @Value("${jurisflow.ai.python.url:http://localhost:8090}")
    private String baseUrl;

    @Value("${jurisflow.ai.python.enabled:true}")
    private boolean enabled;

    private final RestTemplate restTemplate = new RestTemplate();
    private volatile boolean available = false;
    private volatile long lastHealthCheck = 0;
    private static final long HEALTH_TTL_MS = 15000;

    public boolean isAvailable() {
        if (!enabled) return false;
        long now = System.currentTimeMillis();
        if (now - lastHealthCheck > HEALTH_TTL_MS) {
            checkHealth();
            lastHealthCheck = now;
        }
        return available;
    }

    public void checkHealth() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = restTemplate.getForObject(baseUrl + "/health", Map.class);
            available = res != null && "ok".equals(String.valueOf(res.get("status")));
        } catch (Exception e) {
            available = false;
            log.debug("Python AI service unavailable: {}", e.getMessage());
        }
    }

    public Optional<AIStackStatus> getStackStatus() {
        if (!isAvailable()) return Optional.empty();
        try {
            AIStackStatus status = restTemplate.getForObject(baseUrl + "/v1/status", AIStackStatus.class);
            return Optional.ofNullable(status);
        } catch (Exception e) {
            log.warn("Failed to fetch Python stack status: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void seedEscritorio(UUID escritorioId) {
        if (!isAvailable()) return;
        try {
            restTemplate.postForObject(
                baseUrl + "/v1/rag/" + escritorioId + "/seed",
                null,
                Map.class
            );
        } catch (Exception e) {
            log.warn("Python RAG seed failed: {}", e.getMessage());
        }
    }

    public Optional<RAGKnowledgeService.RAGSearchResult> search(UUID escritorioId, String query, int limit) {
        if (!isAvailable()) return Optional.empty();
        try {
            Map<String, Object> body = Map.of("query", query, "limit", limit);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
            ResponseEntity<PythonSearchResponse> response = restTemplate.exchange(
                baseUrl + "/v1/rag/" + escritorioId + "/search",
                HttpMethod.POST,
                request,
                PythonSearchResponse.class
            );
            PythonSearchResponse data = response.getBody();
            if (data == null) return Optional.empty();
            return Optional.of(mapSearchResult(data));
        } catch (Exception e) {
            log.warn("Python RAG search failed, fallback local: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RAGKnowledgeService.KnowledgeDocument> addDocument(
            UUID escritorioId, String title, String content, String category, String source) {
        if (!isAvailable()) return Optional.empty();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("title", title);
            body.put("content", content);
            body.put("category", category != null ? category : "Geral");
            body.put("source", source != null ? source : "Manual");
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body);
            ResponseEntity<PythonDocument> response = restTemplate.exchange(
                baseUrl + "/v1/rag/" + escritorioId + "/documents",
                HttpMethod.POST,
                request,
                PythonDocument.class
            );
            PythonDocument doc = response.getBody();
            if (doc == null) return Optional.empty();
            return Optional.of(mapDocument(doc));
        } catch (Exception e) {
            log.warn("Python RAG index failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Chat com Bruna (assistente jurídica — RAG + LLM via LangChain).
     */
    public Optional<AIService.AIResponse> brunaChat(
            UUID escritorioId,
            AIService.ChatRequest request) {
        if (!isAvailable()) return Optional.empty();
        try {
            String message = enrichMessageWithAgent(request.getMessage(), request.getAgentId());
            Map<String, Object> body = new HashMap<>();
            body.put("message", message);
            body.put("escritorio_id", escritorioId.toString());
            body.put("use_rag", request.isUseRag());
            if (request.getHistory() != null) {
                List<Map<String, String>> history = new ArrayList<>();
                for (AIService.ChatMessage msg : request.getHistory()) {
                    history.add(Map.of(
                        "role", msg.getRole() != null ? msg.getRole() : "user",
                        "content", msg.getContent() != null ? msg.getContent() : ""
                    ));
                }
                body.put("history", history);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body);
            ResponseEntity<PythonBrunaResponse> response = restTemplate.exchange(
                baseUrl + "/v1/assistant/bruna/chat",
                HttpMethod.POST,
                entity,
                PythonBrunaResponse.class
            );
            PythonBrunaResponse data = response.getBody();
            if (data == null || data.answer == null) return Optional.empty();
            Map<String, Object> meta = new HashMap<>();
            meta.put("mode", "bruna");
            meta.put("assistant", "bruna");
            meta.put("engine", "python-langchain");
            meta.put("llm", "openrouter-free");
            getStackStatus().ifPresent(status -> {
                if (status.getLlm_provider() != null) meta.put("llm_provider", status.getLlm_provider());
                if (status.getLlm_model() != null) meta.put("llm_model", status.getLlm_model());
                if (status.getRetrieval() != null) meta.put("retrieval", status.getRetrieval());
            });
            return Optional.of(AIService.AIResponse.success(data.answer, meta));
        } catch (Exception e) {
            log.warn("Bruna chat via Python failed: {}", e.getMessage());
            String detail = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
            if (detail.contains("429") || detail.toLowerCase(Locale.ROOT).contains("rate limit")) {
                return Optional.of(AIService.AIResponse.error(
                    "Limite diário do LLM atingido (OpenRouter free: 50 requisições/dia). "
                    + "Configure GROQ_API_KEY no JurisFlow-ai-service/.env como fallback grátis "
                    + "ou aguarde o reset do provedor."
                ));
            }
            return Optional.of(AIService.AIResponse.error(
                "Motor de IA indisponível: " + sanitizePythonError(detail)
            ));
        }
    }

    private String sanitizePythonError(String detail) {
        if (detail.length() > 280) {
            return detail.substring(0, 280) + "...";
        }
        return detail;
    }

    public Optional<AIPipelineService.PipelineRunResult> runPipeline(
            UUID escritorioId, String pipelineId, String input, boolean useRag) {
        if (!isAvailable()) return Optional.empty();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("pipeline_id", pipelineId);
            body.put("input", input);
            body.put("use_rag", useRag);
            body.put("escritorio_id", escritorioId.toString());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
            ResponseEntity<PythonPipelineResult> response = restTemplate.exchange(
                baseUrl + "/v1/pipelines/run",
                HttpMethod.POST,
                request,
                PythonPipelineResult.class
            );
            PythonPipelineResult data = response.getBody();
            if (data == null) return Optional.empty();
            return Optional.of(mapPipelineResult(data));
        } catch (Exception e) {
            log.warn("Python pipeline failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private RAGKnowledgeService.RAGSearchResult mapSearchResult(PythonSearchResponse data) {
        List<RAGKnowledgeService.ScoredChunk> chunks = new ArrayList<>();
        if (data.chunks != null) {
            for (PythonChunk c : data.chunks) {
                chunks.add(RAGKnowledgeService.ScoredChunk.builder()
                    .documentId(c.document_id)
                    .documentTitle(c.document_title)
                    .category(c.category)
                    .content(c.content)
                    .score(c.score)
                    .build());
            }
        }
        return RAGKnowledgeService.RAGSearchResult.builder()
            .query(data.query)
            .totalMatches(data.total_matches)
            .chunks(chunks)
            .build();
    }

    private RAGKnowledgeService.KnowledgeDocument mapDocument(PythonDocument doc) {
        return RAGKnowledgeService.KnowledgeDocument.builder()
            .id(doc.id)
            .title(doc.title)
            .content(doc.content)
            .category(doc.category)
            .source(doc.source)
            .chunkCount(doc.chunk_count)
            .build();
    }

    private AIPipelineService.PipelineRunResult mapPipelineResult(PythonPipelineResult data) {
        List<AIPipelineService.StepResult> steps = new ArrayList<>();
        if (data.steps != null) {
            for (PythonStep s : data.steps) {
                steps.add(AIPipelineService.StepResult.builder()
                    .stepId(s.step_id)
                    .name(s.name)
                    .status(s.status)
                    .output(s.output)
                    .durationMs(s.duration_ms)
                    .build());
            }
        }
        return AIPipelineService.PipelineRunResult.builder()
            .success(data.success)
            .pipelineId(data.pipeline_id)
            .pipelineName(data.pipeline_name)
            .content(data.content)
            .error(data.success ? null : data.content)
            .steps(steps)
            .totalDurationMs(data.total_duration_ms)
            .build();
    }

  @lombok.Data
    public static class AIStackStatus {
        private String service;
        private String version;
        private String status;
        private String retrieval;
        private List<String> capabilities;
        private int escritorios_indexed;
        private int total_documents;
        private int total_chunks;
        private String llm_provider;
        private String llm_model;
        private String llm_cost;
    }

    private String enrichMessageWithAgent(String message, String agentId) {
        if (agentId == null || agentId.isBlank()
                || "bruna".equals(agentId) || "copilot".equals(agentId)) {
            return message;
        }
        String persona = switch (agentId) {
            case "pesquisador" -> "Atue como agente de pesquisa jurídica. Liste teses, jurisprudência, súmulas e estratégia processual.";
            case "redator" -> "Atue como agente redator jurídico. Gere documentos formais com fundamentação legal brasileira.";
            case "analista" -> "Atue como analista jurídico. Avalie riscos, probabilidades e pontos fortes/fracos.";
            case "prazos" -> "Atue como agente de prazos processuais. Identifique prazos, contagem e alertas conforme CPC.";
            case "atendimento" -> "Atue como agente de atendimento ao cliente de escritório de advocacia.";
            default -> "Atue como assistente jurídico do JurisFlow.";
        };
        return persona + "\n\nSOLICITAÇÃO DO USUÁRIO:\n" + message;
    }

    @lombok.Data
    private static class PythonSearchResponse {
        private String query;
        private int total_matches;
        private List<PythonChunk> chunks;
        private String retrieval;
    }

    @lombok.Data
    private static class PythonChunk {
        private String document_id;
        private String document_title;
        private String category;
        private String content;
        private double score;
    }

    @lombok.Data
    private static class PythonDocument {
        private String id;
        private String title;
        private String content;
        private String category;
        private String source;
        private int chunk_count;
    }

    @lombok.Data
    private static class PythonBrunaResponse {
        private String answer;
        private String assistant;
        private String escritorio_id;
        private boolean used_rag;
    }

    @lombok.Data
    private static class PythonPipelineResult {
        private boolean success;
        private String pipeline_id;
        private String pipeline_name;
        private String content;
        private List<PythonStep> steps;
        private long total_duration_ms;
        private String engine;
    }

    @lombok.Data
    private static class PythonStep {
        private String step_id;
        private String name;
        private String status;
        private String output;
        private long duration_ms;
    }
}
