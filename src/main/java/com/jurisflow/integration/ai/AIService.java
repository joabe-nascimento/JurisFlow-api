package com.jurisflow.integration.ai;

import com.jurisflow.integration.IntegrationConfig;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.repository.IntegrationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    /**
     * Resumir peça processual usando IA
     */
    public AIResponse summarizeDocument(UUID escritorioId, String documentText) {
        IntegrationConfig config = getActiveAIConfig(escritorioId);
        if (config == null) {
            return AIResponse.error("Nenhuma integração de IA configurada");
        }

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

        return callAI(config, prompt);
    }

    /**
     * Analisar jurisprudência relacionada
     */
    public AIResponse analyzeJurisprudence(UUID escritorioId, String tema, String areaJuridica) {
        IntegrationConfig config = getActiveAIConfig(escritorioId);
        if (config == null) {
            return AIResponse.error("Nenhuma integração de IA configurada");
        }

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

        return callAI(config, prompt);
    }

    /**
     * Gerar minuta de documento
     */
    public AIResponse generateDocument(UUID escritorioId, DocumentGenerationRequest request) {
        IntegrationConfig config = getActiveAIConfig(escritorioId);
        if (config == null) {
            return AIResponse.error("Nenhuma integração de IA configurada");
        }

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

        return callAI(config, prompt);
    }

    /**
     * Analisar contrato e identificar cláusulas problemáticas
     */
    public AIResponse analyzeContract(UUID escritorioId, String contractText) {
        IntegrationConfig config = getActiveAIConfig(escritorioId);
        if (config == null) {
            return AIResponse.error("Nenhuma integração de IA configurada");
        }

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

        return callAI(config, prompt);
    }

    /**
     * Prever resultado de processo
     */
    public AIResponse predictOutcome(UUID escritorioId, CaseAnalysisRequest request) {
        IntegrationConfig config = getActiveAIConfig(escritorioId);
        if (config == null) {
            return AIResponse.error("Nenhuma integração de IA configurada");
        }

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

        return callAI(config, prompt);
    }

    private IntegrationConfig getActiveAIConfig(UUID escritorioId) {
        // Tenta OpenAI primeiro, depois Anthropic
        return configRepository.findByEscritorioIdAndType(escritorioId, IntegrationType.OPENAI)
            .filter(IntegrationConfig::isEnabled)
            .orElseGet(() -> configRepository.findByEscritorioIdAndType(escritorioId, IntegrationType.ANTHROPIC)
                .filter(IntegrationConfig::isEnabled)
                .orElse(null));
    }

    private AIResponse callAI(IntegrationConfig config, String prompt) {
        try {
            if (config.getType() == IntegrationType.OPENAI) {
                return callOpenAI(config, prompt);
            } else if (config.getType() == IntegrationType.ANTHROPIC) {
                return callAnthropic(config, prompt);
            }
            return AIResponse.error("Tipo de IA não suportado");
        } catch (Exception e) {
            log.error("Erro ao chamar IA: {}", e.getMessage(), e);
            return AIResponse.error("Erro ao processar: " + e.getMessage());
        }
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
}

