package com.jurisflow.integration.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestração de agentes de IA especializados para o escritório jurídico.
 */
@Service
@RequiredArgsConstructor
public class AIAgentService {

    private final AIService aiService;
    private final RAGKnowledgeService ragService;
    private final PythonAIService pythonAIService;

    public List<AgentDefinition> listAgents() {
        return List.of(
                AgentDefinition.builder()
                        .id("bruna")
                        .name("Bruna")
                        .description("Assistente jurídica com RAG e LLM — chat conversacional")
                        .icon("message")
                        .capabilities(List.of("chat", "rag", "prazos", "contratos"))
                        .build(),
                AgentDefinition.builder()
                        .id("copilot")
                        .name("Copilot Jurídico")
                        .description("Assistente geral (alias da Bruna)")
                        .icon("brain")
                        .capabilities(List.of("chat", "rag", "resumo"))
                        .build(),
                AgentDefinition.builder()
                        .id("pesquisador")
                        .name("Agente Pesquisador")
                        .description("Jurisprudência, súmulas e teses jurídicas")
                        .icon("search")
                        .capabilities(List.of("jurisprudencia", "rag"))
                        .build(),
                AgentDefinition.builder()
                        .id("redator")
                        .name("Agente Redator")
                        .description("Gera petições, contratos e minutas")
                        .icon("file-text")
                        .capabilities(List.of("geracao", "contratos"))
                        .build(),
                AgentDefinition.builder()
                        .id("analista")
                        .name("Agente Analista")
                        .description("Análise de risco, contratos e previsão de resultados")
                        .icon("chart")
                        .capabilities(List.of("contratos", "predicao"))
                        .build(),
                AgentDefinition.builder()
                        .id("prazos")
                        .name("Agente de Prazos")
                        .description("Monitoramento e alertas de prazos processuais")
                        .icon("calendar")
                        .capabilities(List.of("prazos", "alertas"))
                        .build(),
                AgentDefinition.builder()
                        .id("atendimento")
                        .name("Agente de Atendimento")
                        .description("Respostas para clientes e comunicação")
                        .icon("message")
                        .capabilities(List.of("chat", "clientes"))
                        .build()
        );
    }

    public AIService.AIResponse runAgent(UUID escritorioId, String agentId, String input, boolean useRag) {
        if (pythonAIService.isAvailable()) {
            AIService.ChatRequest chatRequest = new AIService.ChatRequest();
            chatRequest.setMessage(input);
            chatRequest.setAgentId(agentId);
            chatRequest.setUseRag(useRag);
            Optional<AIService.AIResponse> llm = pythonAIService.brunaChat(escritorioId, chatRequest);
            if (llm.isPresent()) {
                return llm.get();
            }
        }

        String systemContext = buildAgentPrompt(agentId);
        String ragContext = "";

        if (useRag) {
            RAGKnowledgeService.RAGSearchResult search = ragService.search(escritorioId, input, 3);
            ragContext = ragService.buildContext(search);
        }

        String fullPrompt = systemContext +
                (ragContext.isBlank() ? "" : "\n\nCONTEXTO RAG (base de conhecimento):\n" + ragContext) +
                "\n\nSOLICITAÇÃO DO USUÁRIO:\n" + input;

        return aiService.processPrompt(escritorioId, fullPrompt);
    }

    private String buildAgentPrompt(String agentId) {
        return switch (agentId) {
            case "pesquisador" -> "Você é um agente de pesquisa jurídica. Liste teses, jurisprudência, súmulas e estratégia processual.";
            case "redator" -> "Você é um agente redator jurídico. Gere documentos formais, bem estruturados, com fundamentação legal brasileira.";
            case "analista" -> "Você é um analista jurídico. Avalie riscos, probabilidades e pontos fortes/fracos com objetividade.";
            case "prazos" -> "Você é um agente de prazos processuais. Identifique prazos aplicáveis, contagem e alertas críticos conforme CPC.";
            case "atendimento" -> "Você é um agente de atendimento ao cliente de escritório de advocacia. Responda com clareza e profissionalismo.";
            default -> "Você é o Copilot Jurídico do JurisFlow. Auxilie advogados com análises, resumos e orientações práticas.";
        };
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AgentDefinition {
        private String id;
        private String name;
        private String description;
        private String icon;
        private List<String> capabilities;
    }
}
