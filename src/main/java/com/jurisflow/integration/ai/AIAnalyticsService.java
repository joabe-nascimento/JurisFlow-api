package com.jurisflow.integration.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas de uso de IA por escritório (demo / observabilidade).
 */
@Service
public class AIAnalyticsService {

    private final Map<UUID, EscritorioMetrics> metrics = new ConcurrentHashMap<>();

    public void record(UUID escritorioId, String operation, boolean success, String provider) {
        EscritorioMetrics m = metrics.computeIfAbsent(escritorioId, k -> new EscritorioMetrics());
        m.totalRequests.incrementAndGet();
        if (success) m.successfulRequests.incrementAndGet();
        m.operations.merge(operation, 1L, Long::sum);
        if (provider != null) {
            m.providers.merge(provider, 1L, Long::sum);
        }
        m.recentActivity.addFirst(ActivityEntry.builder()
                .operation(operation)
                .success(success)
                .provider(provider != null ? provider : "demo")
                .timestamp(LocalDateTime.now())
                .build());
        while (m.recentActivity.size() > 50) {
            m.recentActivity.removeLast();
        }
    }

    public AIMetricsDTO getMetrics(UUID escritorioId, int ragDocCount, int agentCount) {
        EscritorioMetrics m = metrics.getOrDefault(escritorioId, new EscritorioMetrics());
        long total = m.totalRequests.get();
        long success = m.successfulRequests.get();
        double successRate = total > 0 ? (success * 100.0 / total) : 100.0;

        List<OperationStat> topOps = m.operations.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(e -> OperationStat.builder().operation(e.getKey()).count(e.getValue()).build())
                .toList();

        return AIMetricsDTO.builder()
                .totalRequests(total)
                .successfulRequests(success)
                .successRate(Math.round(successRate * 10) / 10.0)
                .ragDocumentsIndexed(ragDocCount)
                .activeAgents(agentCount)
                .providersInUse(m.providers.isEmpty() ? Map.of("demo", total) : m.providers)
                .topOperations(topOps)
                .recentActivity(new ArrayList<>(m.recentActivity))
                .stackCapabilities(List.of(
                        "Azure OpenAI",
                        "Azure AI Foundry",
                        "Copilot Studio",
                        "OpenAI GPT-4",
                        "RAG",
                        "Agentes especializados",
                        "LangChain-style pipelines"
                ))
                .build();
    }

  private static class EscritorioMetrics {
        final AtomicLong totalRequests = new AtomicLong(0);
        final AtomicLong successfulRequests = new AtomicLong(0);
        final Map<String, Long> operations = new ConcurrentHashMap<>();
        final Map<String, Long> providers = new ConcurrentHashMap<>();
        final LinkedList<ActivityEntry> recentActivity = new LinkedList<>();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AIMetricsDTO {
        private long totalRequests;
        private long successfulRequests;
        private double successRate;
        private int ragDocumentsIndexed;
        private int activeAgents;
        private Map<String, Long> providersInUse;
        private List<OperationStat> topOperations;
        private List<ActivityEntry> recentActivity;
        private List<String> stackCapabilities;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OperationStat {
        private String operation;
        private long count;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ActivityEntry {
        private String operation;
        private boolean success;
        private String provider;
        private LocalDateTime timestamp;
    }
}
