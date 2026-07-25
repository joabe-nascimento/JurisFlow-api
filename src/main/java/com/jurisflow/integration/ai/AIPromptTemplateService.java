package com.jurisflow.integration.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Templates de prompts jurídicos pré-configurados (engenharia de prompts).
 */
@Service
public class AIPromptTemplateService {

    public List<PromptTemplate> listTemplates() {
        return List.of(
                PromptTemplate.builder()
                        .id("contestacao")
                        .name("Contestação — CPC")
                        .category("Processual")
                        .area("Cível")
                        .description("Estrutura de contestação com preliminares e mérito")
                        .template("Elabore contestação para o processo {{numero}}. Autor: {{autor}}. Réu: {{reu}}. Fatos: {{fatos}}. Incluir preliminares cabíveis e defesa de mérito com fundamentação CPC/legislação aplicável.")
                        .variables(List.of("numero", "autor", "reu", "fatos"))
                        .agentId("redator")
                        .build(),
                PromptTemplate.builder()
                        .id("reclamacao-trabalhista")
                        .name("Reclamação Trabalhista")
                        .category("Trabalhista")
                        .area("Trabalhista")
                        .description("Petição inicial trabalhista com pedidos e fundamentação")
                        .template("Gere reclamação trabalhista. Reclamante: {{reclamante}}. Reclamada: {{reclamada}}. Vínculo: {{vinculo}}. Pedidos: {{pedidos}}. Fundamentar CLT, súmulas TST e jurisprudência.")
                        .variables(List.of("reclamante", "reclamada", "vinculo", "pedidos"))
                        .agentId("redator")
                        .build(),
                PromptTemplate.builder()
                        .id("analise-contrato-saas")
                        .name("Análise de Contrato SaaS")
                        .category("Contratos")
                        .area("Empresarial")
                        .description("Checklist de riscos em contratos de software")
                        .template("Analise o contrato SaaS abaixo. Identifique: SLA, LGPD, rescisão, limitação de responsabilidade, propriedade de dados, foro e cláusulas abusivas.\n\n{{contrato}}")
                        .variables(List.of("contrato"))
                        .agentId("analista")
                        .build(),
                PromptTemplate.builder()
                        .id("lgpd-auditoria")
                        .name("Auditoria LGPD Escritório")
                        .category("Compliance")
                        .area("Compliance")
                        .description("Diagnóstico de conformidade LGPD")
                        .template("Realize auditoria LGPD para escritório de advocacia. Contexto: {{contexto}}. Avalie bases legais, DPO, consentimento, incidentes, contratos com operadores e políticas internas.")
                        .variables(List.of("contexto"))
                        .agentId("analista")
                        .build(),
                PromptTemplate.builder()
                        .id("jurisprudencia-tema")
                        .name("Pesquisa de Jurisprudência")
                        .category("Pesquisa")
                        .area("Geral")
                        .description("Teses favoráveis, contrárias e estratégia")
                        .template("Pesquise jurisprudência sobre: {{tema}}. Área: {{area}}. Liste STF/STJ/TJs, súmulas e estratégia processual.")
                        .variables(List.of("tema", "area"))
                        .agentId("pesquisador")
                        .build(),
                PromptTemplate.builder()
                        .id("prazos-contestacao")
                        .name("Cálculo de Prazos — Contestação")
                        .category("Prazos")
                        .area("Processual")
                        .description("Contagem de prazo para contestação")
                        .template("Calcule prazo para contestação. Data ciência: {{data_ciencia}}. Tipo contagem: {{contagem}}. Processo: {{processo}}. Alertas críticos e feriados.")
                        .variables(List.of("data_ciencia", "contagem", "processo"))
                        .agentId("prazos")
                        .build(),
                PromptTemplate.builder()
                        .id("resposta-cliente")
                        .name("Resposta ao Cliente")
                        .category("Atendimento")
                        .area("Geral")
                        .description("Comunicação clara para cliente sobre andamento")
                        .template("Responda ao cliente {{cliente}} sobre: {{assunto}}. Tom profissional, sem juridiquês excessivo. Processo: {{processo}}.")
                        .variables(List.of("cliente", "assunto", "processo"))
                        .agentId("atendimento")
                        .build(),
                PromptTemplate.builder()
                        .id("predicao-resultado")
                        .name("Predição de Resultado")
                        .category("Análise")
                        .area("Geral")
                        .description("Probabilidade e riscos do caso")
                        .template("Analise probabilidade de êxito. Área: {{area}}. Fatos: {{fatos}}. Teses: {{teses}}. Forneça % estimado, riscos e recomendações.")
                        .variables(List.of("area", "fatos", "teses"))
                        .agentId("analista")
                        .build()
        );
    }

    public PromptTemplate getTemplate(String id) {
        return listTemplates().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public String fillTemplate(String id, Map<String, String> variables) {
        PromptTemplate t = getTemplate(id);
        if (t == null) return null;
        String result = t.getTemplate();
        if (variables != null) {
            for (Map.Entry<String, String> e : variables.entrySet()) {
                result = result.replace("{{" + e.getKey() + "}}", e.getValue() != null ? e.getValue() : "");
            }
        }
        return result;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PromptTemplate {
        private String id;
        private String name;
        private String category;
        private String area;
        private String description;
        private String template;
        private List<String> variables;
        private String agentId;
    }
}
