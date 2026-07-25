package com.jurisflow.integration.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Base de conhecimento RAG (Retrieval-Augmented Generation) em memória.
 * Indexa documentos jurídicos e recupera contexto relevante para os agentes de IA.
 */
@Service
@RequiredArgsConstructor
public class RAGKnowledgeService {

    private final PythonAIService pythonAIService;

    private final Map<UUID, List<KnowledgeDocument>> store = new ConcurrentHashMap<>();

    public List<KnowledgeDocument> listDocuments(UUID escritorioId) {
        ensureSeeded(escritorioId);
        if (pythonAIService.isAvailable()) {
            pythonAIService.seedEscritorio(escritorioId);
        }
        return new ArrayList<>(store.get(escritorioId));
    }

    public KnowledgeDocument addDocument(UUID escritorioId, String title, String content, String category, String source) {
        ensureSeeded(escritorioId);
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .content(content)
                .category(category != null ? category : "Geral")
                .source(source != null ? source : "Manual")
                .chunkCount(splitChunks(content).size())
                .createdAt(LocalDateTime.now())
                .build();
        store.get(escritorioId).add(doc);
        if (pythonAIService.isAvailable()) {
            pythonAIService.addDocument(escritorioId, title, content, category, source);
        }
        return doc;
    }

    public void removeDocument(UUID escritorioId, String documentId) {
        List<KnowledgeDocument> docs = store.get(escritorioId);
        if (docs != null) {
            docs.removeIf(d -> d.getId().equals(documentId));
        }
    }

    /**
     * Busca semântica simplificada por relevância lexical (demo RAG).
     */
    public RAGSearchResult search(UUID escritorioId, String query, int limit) {
        if (pythonAIService.isAvailable()) {
            Optional<RAGSearchResult> pythonResult = pythonAIService.search(escritorioId, query, limit);
            if (pythonResult.isPresent()) {
                return pythonResult.get();
            }
        }
        ensureSeeded(escritorioId);
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        List<String> queryTerms = tokenize(normalizedQuery);

        List<ScoredChunk> scored = new ArrayList<>();
        for (KnowledgeDocument doc : store.get(escritorioId)) {
            for (String chunk : splitChunks(doc.getContent())) {
                double score = scoreChunk(chunk, queryTerms);
                if (score > 0) {
                    scored.add(ScoredChunk.builder()
                            .documentId(doc.getId())
                            .documentTitle(doc.getTitle())
                            .category(doc.getCategory())
                            .content(chunk)
                            .score(score)
                            .build());
                }
            }
        }

        scored.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        List<ScoredChunk> top = scored.stream().limit(limit).collect(Collectors.toList());

        return RAGSearchResult.builder()
                .query(query)
                .totalMatches(scored.size())
                .chunks(top)
                .build();
    }

    public String buildContext(RAGSearchResult searchResult) {
        if (searchResult.getChunks().isEmpty()) {
            return "";
        }
        return searchResult.getChunks().stream()
                .map(c -> "[Fonte: " + c.getDocumentTitle() + "]\n" + c.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private void ensureSeeded(UUID escritorioId) {
        store.computeIfAbsent(escritorioId, id -> new ArrayList<>(defaultKnowledge()));
        if (pythonAIService.isAvailable()) {
            pythonAIService.seedEscritorio(escritorioId);
        }
    }

    private List<KnowledgeDocument> defaultKnowledge() {
        return List.of(
                KnowledgeDocument.builder()
                        .id("seed-1")
                        .title("Código de Processo Civil - Prazos")
                        .category("Processual")
                        .source("CPC/2015")
                        .chunkCount(1)
                        .createdAt(LocalDateTime.now())
                        .content("Contestação: 15 dias úteis (art. 335 CPC). Apelação: 15 dias. Embargos de declaração: 5 dias. Agravo de instrumento: 15 dias. Recurso especial e extraordinário: 15 dias. Cumprimento de sentença: prazos variam conforme a fase.")
                        .build(),
                KnowledgeDocument.builder()
                        .id("seed-2")
                        .title("Direito do Trabalho - Reclamação Trabalhista")
                        .category("Trabalhista")
                        .source("CLT + TST")
                        .chunkCount(1)
                        .createdAt(LocalDateTime.now())
                        .content("Reclamação trabalhista: ação ajuizada pelo empregado contra o empregador. Prescrição: 2 anos durante o contrato, 5 anos após extinção. Competência: Vara do Trabalho. Honorários sucumbenciais: 5% a 15% conforme Súmula 219 TST e reforma trabalhista.")
                        .build(),
                KnowledgeDocument.builder()
                        .id("seed-3")
                        .title("LGPD - Obrigações para Escritórios")
                        .category("Compliance")
                        .source("LGPD Lei 13.709/2018")
                        .chunkCount(1)
                        .createdAt(LocalDateTime.now())
                        .content("Escritórios de advocacia devem implementar política de privacidade, consentimento para dados de clientes, medidas de segurança, registro de operações de tratamento e canal de comunicação com titulares. Dados sensíveis exigem base legal específica.")
                        .build(),
                KnowledgeDocument.builder()
                        .id("seed-4")
                        .title("Pipeline de IA Jurídica - RAG e Agentes")
                        .category("Inteligência Artificial")
                        .source("JurisFlow AI")
                        .chunkCount(1)
                        .createdAt(LocalDateTime.now())
                        .content("Arquitetura recomendada: ingestão de documentos, chunking, embeddings, vector store, retrieval com reranking, geração via LLM com contexto. Agentes especializados: pesquisador jurisprudencial, redator de peças, monitor de prazos, atendimento ao cliente. Integrações: Azure OpenAI, Azure AI Foundry, Copilot Studio.")
                        .build(),
                KnowledgeDocument.builder()
                        .id("seed-5")
                        .title("Contratos - Cláusulas de Risco")
                        .category("Contratos")
                        .source("Prática advocatícia")
                        .chunkCount(1)
                        .createdAt(LocalDateTime.now())
                        .content("Cláusulas de alto risco: limitação abusiva de responsabilidade, renúncia a direitos, multas desproporcionais, foro exclusivo distante, cessão automática de IP, rescisão sem aviso prévio. Sempre revisar cláusula de confidencialidade e SLA em contratos de tecnologia.")
                        .build()
        );
    }

    private List<String> splitChunks(String content) {
        if (content == null || content.isBlank()) return List.of();
        String[] parts = content.split("(?<=[.!?])\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (current.length() + part.length() > 400) {
                if (current.length() > 0) chunks.add(current.toString().trim());
                current = new StringBuilder(part);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(part);
            }
        }
        if (current.length() > 0) chunks.add(current.toString().trim());
        return chunks.isEmpty() ? List.of(content) : chunks;
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.split("\\W+"))
                .filter(t -> t.length() > 2)
                .distinct()
                .collect(Collectors.toList());
    }

    private double scoreChunk(String chunk, List<String> queryTerms) {
        String lower = chunk.toLowerCase(Locale.ROOT);
        double score = 0;
        for (String term : queryTerms) {
            if (lower.contains(term)) score += 1.0;
        }
        return score;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class KnowledgeDocument {
        private String id;
        private String title;
        private String content;
        private String category;
        private String source;
        private int chunkCount;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ScoredChunk {
        private String documentId;
        private String documentTitle;
        private String category;
        private String content;
        private double score;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RAGSearchResult {
        private String query;
        private int totalMatches;
        private List<ScoredChunk> chunks;
    }
}
