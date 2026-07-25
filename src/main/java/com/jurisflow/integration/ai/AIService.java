package com.jurisflow.integration.ai;

import com.jurisflow.integration.IntegrationConfig;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.repository.IntegrationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Serviço de Inteligência Artificial para análise de documentos jurídicos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final IntegrationConfigRepository configRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api-key:}")
    private String defaultOpenAiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String AZURE_OPENAI_URL_PATTERN = "https://%s.openai.azure.com/openai/deployments/%s/chat/completions?api-version=2024-02-15-preview";
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    /**
     * Chat conversacional com histórico e RAG opcional.
     */
    public AIResponse chat(UUID escritorioId, ChatRequest request, String ragContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é o assistente jurídico inteligente do JurisFlow.\n");
        if (request.getAgentId() != null) {
            prompt.append("Modo agente: ").append(request.getAgentId()).append("\n");
        }
        if (ragContext != null && !ragContext.isBlank()) {
            prompt.append("\nContexto recuperado (RAG):\n").append(ragContext).append("\n");
        }
        if (request.getHistory() != null) {
            for (ChatMessage msg : request.getHistory()) {
                prompt.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
        }
        prompt.append("\nUsuário: ").append(request.getMessage());
        return processPrompt(escritorioId, prompt.toString());
    }

    /**
     * Processa prompt genérico (usado por agentes e ferramentas).
     */
    public AIResponse processPrompt(UUID escritorioId, String prompt) {
        IntegrationConfig config = getActiveAIConfig(escritorioId);
        if (config == null) {
            return demoResponse(prompt);
        }
        return callAI(config, prompt);
    }

    /**
     * Resumir peça processual usando IA
     */
    public AIResponse summarizeDocument(UUID escritorioId, String documentText) {
        String prompt = """
            Você é um assistente jurídico especializado. Analise o documento abaixo e forneça:
            1. RESUMO: Um resumo executivo em 3-5 parágrafos
            2. PONTOS PRINCIPAIS: Lista dos pontos mais importantes
            3. PEDIDOS/REQUERIMENTOS: Se houver, liste os pedidos ou requerimentos
            4. PRAZOS: Se houver prazos mencionados, destaque-os
            5. PARTES ENVOLVIDAS: Identifique as partes mencionadas
            
            Documento:
            %s
            """.formatted(documentText);

        return processPrompt(escritorioId, prompt);
    }

    /**
     * Analisar jurisprudência relacionada
     */
    public AIResponse analyzeJurisprudence(UUID escritorioId, String tema, String areaJuridica) {
        String prompt = """
            Você é um assistente jurídico especializado em %s. 
            Analise o tema abaixo e forneça:
            1. TESES FAVORÁVEIS: Argumentos jurídicos que podem ser utilizados
            2. TESES CONTRÁRIAS: Possíveis argumentos da parte contrária
            3. JURISPRUDÊNCIA: Mencione decisões relevantes (STF, STJ, TRTs, TJs)
            4. SÚMULAS: Súmulas aplicáveis ao caso
            5. ESTRATÉGIA: Sugestão de estratégia processual
            
            Tema: %s
            """.formatted(areaJuridica, tema);

        return processPrompt(escritorioId, prompt);
    }

    /**
     * Gerar minuta de documento
     */
    public AIResponse generateDocument(UUID escritorioId, DocumentGenerationRequest request) {
        String prompt = """
            Você é um advogado especializado. Gere uma %s com as seguintes informações:
            
            Tipo de documento: %s
            Área do direito: %s
            Partes:
            - Autor/Requerente: %s
            - Réu/Requerido: %s
            
            Fatos: %s
            
            Pedidos/Objetivo: %s
            
            Informações adicionais: %s
            
            Gere o documento completo, com formatação adequada, fundamentação legal e pedidos específicos.
            """.formatted(
                request.getTipoDocumento(),
                request.getTipoDocumento(),
                request.getAreaDireito(),
                request.getAutor(),
                request.getReu(),
                request.getFatos(),
                request.getPedidos(),
                request.getInformacoesAdicionais()
            );

        return processPrompt(escritorioId, prompt);
    }

    /**
     * Analisar contrato e identificar cláusulas problemáticas
     */
    public AIResponse analyzeContract(UUID escritorioId, String contractText) {
        String prompt = """
            Você é um advogado especializado em contratos. Analise o contrato abaixo e forneça:
            
            1. RESUMO GERAL: Tipo de contrato e objeto principal
            2. PARTES: Identificação das partes contratantes
            3. OBRIGAÇÕES: Principais obrigações de cada parte
            4. CLÁUSULAS DE RISCO: Cláusulas que podem ser problemáticas ou abusivas
            5. CLÁUSULAS FALTANTES: Cláusulas importantes que estão ausentes
            6. MULTAS E PENALIDADES: Valores e condições
            7. PRAZO E RESCISÃO: Condições de vigência e término
            8. SUGESTÕES: Melhorias recomendadas
            9. PONTUAÇÃO DE RISCO: De 1 a 10, qual o risco deste contrato (1=baixo, 10=alto)
            
            Contrato:
            %s
            """.formatted(contractText);

        return processPrompt(escritorioId, prompt);
    }

    /**
     * Prever resultado de processo
     */
    public AIResponse predictOutcome(UUID escritorioId, CaseAnalysisRequest request) {
        String prompt = """
            Você é um analista jurídico com vasta experiência. Com base nas informações abaixo, analise:
            
            Área: %s
            Tipo de ação: %s
            Tribunal: %s
            Vara: %s
            
            Resumo do caso: %s
            
            Argumentos do autor: %s
            
            Argumentos do réu: %s
            
            Provas disponíveis: %s
            
            Forneça:
            1. PROBABILIDADE DE ÊXITO: Percentual estimado (com justificativa)
            2. PONTOS FORTES: Do caso
            3. PONTOS FRACOS: Vulnerabilidades
            4. ESTRATÉGIA RECOMENDADA: Próximos passos
            5. TEMPO ESTIMADO: Duração provável do processo
            6. VALOR ESTIMADO: Se for o caso, estimativa de condenação/acordo
            
            IMPORTANTE: Esta é apenas uma análise preditiva baseada em padrões, não uma garantia de resultado.
            """.formatted(
                request.getAreaDireito(),
                request.getTipoAcao(),
                request.getTribunal(),
                request.getVara(),
                request.getResumo(),
                request.getArgumentosAutor(),
                request.getArgumentosReu(),
                request.getProvas()
            );

        return processPrompt(escritorioId, prompt);
    }

    private IntegrationConfig getActiveAIConfig(UUID escritorioId) {
        Optional<IntegrationConfig> config = configRepository.findByEscritorioIdAndType(escritorioId, IntegrationType.OPENAI)
            .filter(IntegrationConfig::isEnabled);
        if (config.isPresent()) return config.get();

        config = configRepository.findByEscritorioIdAndType(escritorioId, IntegrationType.AZURE_OPENAI)
            .filter(IntegrationConfig::isEnabled);
        if (config.isPresent()) return config.get();

        config = configRepository.findByEscritorioIdAndType(escritorioId, IntegrationType.ANTHROPIC)
            .filter(IntegrationConfig::isEnabled);
        if (config.isPresent()) return config.get();

        if (defaultOpenAiKey != null && !defaultOpenAiKey.isBlank()) {
            return IntegrationConfig.builder()
                .type(IntegrationType.OPENAI)
                .apiKey(defaultOpenAiKey)
                .enabled(true)
                .settings(Map.of("model", "gpt-4-turbo-preview", "max_tokens", "4000", "temperature", "0.3"))
                .build();
        }

        return null;
    }

    private AIResponse demoResponse(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        String content;

        if (lower.contains("prazo") || lower.contains("contestação") || lower.contains("recurso")) {
            content = """
                **Modo demonstração (RAG + Agente de Prazos)**

                Com base no CPC/2015 indexado na base de conhecimento:

                1. **Contestação**: 15 dias úteis (art. 335)
                2. **Apelação / Recurso ordinário**: 15 dias
                3. **Embargos de declaração**: 5 dias
                4. **Agravo de instrumento**: 15 dias

                **Próximos passos recomendados:**
                - Verificar data de publicação/intimação no DJE
                - Confirmar contagem (dias úteis vs corridos)
                - Registrar alerta no módulo de Prazos do JurisFlow

                _Configure OpenAI ou Azure OpenAI em Integrações para respostas completas via LLM._
                """;
        } else if (lower.contains("contrato") || lower.contains("cláusula")) {
            content = """
                **Modo demonstração (Agente Analista)**

                Análise estruturada do contrato:

                | Aspecto | Avaliação |
                |---------|-----------|
                | Objeto | Identificar com precisão |
                | Obrigações | Mapear partes e SLA |
                | Multas | Verificar proporcionalidade |
                | Rescisão | Aviso prévio e penalidades |
                | **Risco estimado** | **6/10** (médio) |

                **Cláusulas de atenção:** limitação de responsabilidade, foro, cessão de IP.

                _Pipeline RAG recuperou contexto de "Contratos - Cláusulas de Risco"._
                """;
        } else if (lower.contains("trabalh") || lower.contains("reclamação")) {
            content = """
                **Modo demonstração (Agente Pesquisador - Trabalhista)**

                **Teses favoráveis:** verbas rescisórias, adicional noturno, intervalo intrajornada.
                **Jurisprudência:** TST - recursos repetitivos em temas trabalhistas.
                **Súmulas:** Súmula 219 TST (honorários sucumbenciais).
                **Estratégia:** documentar jornada, holerites e comunicações.

                _Configure Azure AI Foundry para embeddings e retrieval em produção._
                """;
        } else if (lower.contains("lgpd") || lower.contains("dados")) {
            content = """
                **Modo demonstração (RAG - Compliance)**

                Obrigações LGPD para escritórios:
                - Política de privacidade e consentimento
                - Registro de operações de tratamento
                - Segurança e canal do titular
                - Base legal para dados sensíveis

                _Fonte indexada: LGPD Lei 13.709/2018_
                """;
        } else {
            content = """
                **Copilot Jurídico — Modo Demonstração**

                Olá! Sou o assistente de IA do JurisFlow. Neste modo, uso **RAG** (base de conhecimento jurídica) e **agentes especializados** para responder.

                **Posso ajudar com:**
                - Pesquisa de jurisprudência e teses
                - Redação de petições e minutas
                - Análise de contratos e riscos
                - Prazos processuais (CPC)
                - Atendimento a clientes

                **Integrações disponíveis:**
                - OpenAI / Azure OpenAI
                - Azure AI Foundry
                - Microsoft Copilot Studio

                Configure sua chave em **Integrações** ou defina `OPENAI_API_KEY` no servidor para ativar o LLM completo.

                **Sua pergunta foi recebida.** Tente perguntar sobre prazos, contratos, trabalhista ou LGPD para ver o RAG em ação.
                """;
        }

        return AIResponse.success(content, Map.of("mode", "demo", "ragEnabled", true));
    }

    private AIResponse callAI(IntegrationConfig config, String prompt) {
        try {
            if (config.getType() == IntegrationType.OPENAI) {
                return callOpenAI(config, prompt);
            } else if (config.getType() == IntegrationType.AZURE_OPENAI) {
                return callAzureOpenAI(config, prompt);
            } else if (config.getType() == IntegrationType.ANTHROPIC) {
                return callAnthropic(config, prompt);
            }
            return AIResponse.error("Tipo de IA não suportado");
        } catch (Exception e) {
            log.error("Erro ao chamar IA: {}", e.getMessage(), e);
            return demoResponse(prompt);
        }
    }

    private AIResponse callAzureOpenAI(IntegrationConfig config, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", config.getApiKey());

        String resource = config.getSettings().getOrDefault("resource", "jurisflow-openai");
        String deployment = config.getSettings().getOrDefault("deployment", "gpt-4");
        String url = String.format(AZURE_OPENAI_URL_PATTERN, resource, deployment);

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = new HashMap<>();
        body.put("messages", List.of(message));
        body.put("max_tokens", Integer.parseInt(config.getSettings().getOrDefault("max_tokens", "4000")));
        body.put("temperature", Double.parseDouble(config.getSettings().getOrDefault("temperature", "0.3")));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response != null && response.containsKey("choices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
                return AIResponse.success((String) messageResponse.get("content"),
                    Map.of("provider", "azure_openai", "deployment", deployment));
            }
        }
        return AIResponse.error("Resposta inválida da Azure OpenAI");
    }

    private AIResponse callOpenAI(IntegrationConfig config, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getSettings().getOrDefault("model", "gpt-4-turbo-preview"));
        body.put("messages", List.of(message));
        body.put("max_tokens", Integer.parseInt(config.getSettings().getOrDefault("max_tokens", "4000")));
        body.put("temperature", Double.parseDouble(config.getSettings().getOrDefault("temperature", "0.3")));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(OPENAI_API_URL, request, Map.class);
        
        if (response != null && response.containsKey("choices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) messageResponse.get("content");
                return AIResponse.success(content);
            }
        }
        
        return AIResponse.error("Resposta inválida da API");
    }

    private AIResponse callAnthropic(IntegrationConfig config, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", config.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getSettings().getOrDefault("model", "claude-3-opus-20240229"));
        body.put("messages", List.of(message));
        body.put("max_tokens", Integer.parseInt(config.getSettings().getOrDefault("max_tokens", "4000")));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(ANTHROPIC_API_URL, request, Map.class);
        
        if (response != null && response.containsKey("content")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            if (!content.isEmpty()) {
                String text = (String) content.get(0).get("text");
                return AIResponse.success(text);
            }
        }
        
        return AIResponse.error("Resposta inválida da API");
    }

    // DTOs internos
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AIResponse {
        private boolean success;
        private String content;
        private String error;
        private Map<String, Object> metadata;

        public static AIResponse success(String content) {
            return AIResponse.builder().success(true).content(content).build();
        }

        public static AIResponse success(String content, Map<String, Object> metadata) {
            return AIResponse.builder().success(true).content(content).metadata(metadata).build();
        }

        public static AIResponse error(String error) {
            return AIResponse.builder().success(false).error(error).build();
        }
    }

    @lombok.Data
    public static class DocumentGenerationRequest {
        private String tipoDocumento;
        private String areaDireito;
        private String autor;
        private String reu;
        private String fatos;
        private String pedidos;
        private String informacoesAdicionais;
    }

    @lombok.Data
    public static class CaseAnalysisRequest {
        private String areaDireito;
        private String tipoAcao;
        private String tribunal;
        private String vara;
        private String resumo;
        private String argumentosAutor;
        private String argumentosReu;
        private String provas;
    }

    @lombok.Data
    public static class ChatRequest {
        private String message;
        private String agentId;
        private boolean useRag;
        private List<ChatMessage> history;
    }

    @lombok.Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}

