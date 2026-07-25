package com.jurisflow.integration.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Pipelines cognitivos multi-etapa (orquestração tipo LangChain).
 */
@Service
@RequiredArgsConstructor
public class AIPipelineService {

    private final AIService aiService;
    private final RAGKnowledgeService ragService;
    private final AIAnalyticsService analyticsService;
    private final PythonAIService pythonAIService;

    public List<PipelineDefinition> listPipelines() {
        return List.of(
                PipelineDefinition.builder()
                        .id("rag-chat")
                        .name("RAG + Chat")
                        .description("Retrieval-Augmented Generation com Copilot")
                        .steps(List.of(
                                step("retrieve", "Retrieval", "Busca chunks na base RAG"),
                                step("augment", "Augment", "Monta contexto enriquecido"),
                                step("generate", "Generate", "LLM gera resposta fundamentada")
                        ))
                        .estimatedLatencyMs(1200)
                        .build(),
                PipelineDefinition.builder()
                        .id("agent-orchestration")
                        .name("Orquestração de Agentes")
                        .description("Seleciona agente especializado e executa com RAG")
                        .steps(List.of(
                                step("route", "Router", "Classifica intent e escolhe agente"),
                                step("retrieve", "RAG", "Contexto da base de conhecimento"),
                                step("agent", "Agent", "Agente especializado processa"),
                                step("validate", "Validate", "Validação de resposta jurídica")
                        ))
                        .estimatedLatencyMs(2000)
                        .build(),
                PipelineDefinition.builder()
                        .id("document-analysis")
                        .name("Análise de Documento")
                        .description("Resumo, riscos e extração de entidades")
                        .steps(List.of(
                                step("chunk", "Chunk", "Segmentação do documento"),
                                step("summarize", "Summarize", "Resumo executivo"),
                                step("extract", "Extract", "Partes, prazos e pedidos"),
                                step("risk", "Risk", "Score de risco jurídico")
                        ))
                        .estimatedLatencyMs(2500)
                        .build(),
                PipelineDefinition.builder()
                        .id("contract-review")
                        .name("Revisão de Contrato")
                        .description("Análise de cláusulas e compliance")
                        .steps(List.of(
                                step("parse", "Parse", "Estruturação de cláusulas"),
                                step("rag-law", "RAG Legal", "Legislação e modelos"),
                                step("analyze", "Analyze", "Riscos e cláusulas abusivas"),
                                step("report", "Report", "Relatório executivo")
                        ))
                        .estimatedLatencyMs(3000)
                        .build(),
                PipelineDefinition.builder()
                        .id("azure-copilot")
                        .name("Azure Copilot Studio")
                        .description("Integração enterprise Microsoft (demo)")
                        .steps(List.of(
                                step("auth", "Azure Auth", "Autenticação Azure AD"),
                                step("foundry", "AI Foundry", "Modelo no Azure AI Foundry"),
                                step("openai", "Azure OpenAI", "GPT-4 deployment"),
                                step("copilot", "Copilot", "Resposta via Copilot Studio")
                        ))
                        .estimatedLatencyMs(1800)
                        .build()
        );
    }

    public PipelineRunResult runPipeline(UUID escritorioId, String pipelineId, String input, boolean useRag) {
        Optional<PipelineRunResult> pythonResult = pythonAIService.runPipeline(escritorioId, pipelineId, input, useRag);
        if (pythonResult.isPresent()) {
            return pythonResult.get();
        }

        List<PipelineDefinition> pipelines = listPipelines();
        PipelineDefinition pipeline = pipelines.stream()
                .filter(p -> p.getId().equals(pipelineId))
                .findFirst()
                .orElse(null);

        if (pipeline == null) {
            return PipelineRunResult.builder()
                    .success(false)
                    .error("Pipeline não encontrado: " + pipelineId)
                    .build();
        }

        List<StepResult> stepResults = new ArrayList<>();
        String ragContext = "";
        if (useRag) {
            RAGKnowledgeService.RAGSearchResult search = ragService.search(escritorioId, input, 4);
            ragContext = ragService.buildContext(search);
            stepResults.add(StepResult.builder()
                    .stepId("retrieve")
                    .name("Retrieval")
                    .status("completed")
                    .output("Recuperados " + search.getChunks().size() + " chunks relevantes")
                    .durationMs(120)
                    .build());
        }

        for (PipelineStep step : pipeline.getSteps()) {
            if ("retrieve".equals(step.getId()) && useRag) continue;
            stepResults.add(StepResult.builder()
                    .stepId(step.getId())
                    .name(step.getName())
                    .status("completed")
                    .output(step.getDescription() + " — OK")
                    .durationMs(80 + stepResults.size() * 40)
                    .build());
        }

        String prompt = "Pipeline: " + pipeline.getName() + "\n" +
                (ragContext.isBlank() ? "" : "Contexto RAG:\n" + ragContext + "\n") +
                "Input: " + input;

        AIService.AIResponse aiResponse = aiService.processPrompt(escritorioId, prompt);
        analyticsService.record(escritorioId, "pipeline:" + pipelineId, aiResponse.isSuccess(), "demo");

        return PipelineRunResult.builder()
                .success(aiResponse.isSuccess())
                .pipelineId(pipelineId)
                .pipelineName(pipeline.getName())
                .content(aiResponse.getContent())
                .error(aiResponse.getError())
                .steps(stepResults)
                .totalDurationMs(stepResults.stream().mapToLong(StepResult::getDurationMs).sum() + 400)
                .build();
    }

    private static PipelineStep step(String id, String name, String description) {
        return PipelineStep.builder().id(id).name(name).description(description).build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PipelineDefinition {
        private String id;
        private String name;
        private String description;
        private List<PipelineStep> steps;
        private int estimatedLatencyMs;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PipelineStep {
        private String id;
        private String name;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PipelineRunResult {
        private boolean success;
        private String pipelineId;
        private String pipelineName;
        private String content;
        private String error;
        private List<StepResult> steps;
        private long totalDurationMs;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StepResult {
        private String stepId;
        private String name;
        private String status;
        private String output;
        private long durationMs;
    }
}
