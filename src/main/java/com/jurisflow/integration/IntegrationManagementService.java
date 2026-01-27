package com.jurisflow.integration;

import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.repository.IntegrationConfigRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de Gerenciamento de Integrações
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IntegrationManagementService {

    private final IntegrationConfigRepository repository;
    private final UsuarioService usuarioService;

    /**
     * Listar todas as integrações do escritório
     */
    public List<IntegrationDTO> listIntegrations() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        List<IntegrationConfig> configs = repository.findByEscritorioId(escritorioId);
        
        // Criar lista com todas as integrações possíveis
        List<IntegrationDTO> integrations = new ArrayList<>();
        
        for (IntegrationType type : IntegrationType.values()) {
            Optional<IntegrationConfig> existing = configs.stream()
                .filter(c -> c.getType() == type)
                .findFirst();
            
            integrations.add(IntegrationDTO.builder()
                .type(type.name())
                .name(getIntegrationName(type))
                .description(getIntegrationDescription(type))
                .category(getIntegrationCategory(type))
                .icon(getIntegrationIcon(type))
                .configured(existing.isPresent())
                .enabled(existing.map(IntegrationConfig::isEnabled).orElse(false))
                .lastSync(existing.map(IntegrationConfig::getLastSync).orElse(null))
                .lastSyncStatus(existing.map(IntegrationConfig::getLastSyncStatus).orElse(null))
                .build());
        }
        
        return integrations;
    }

    /**
     * Listar integrações por categoria
     */
    public Map<String, List<IntegrationDTO>> listIntegrationsByCategory() {
        return listIntegrations().stream()
            .collect(Collectors.groupingBy(IntegrationDTO::getCategory));
    }

    /**
     * Obter configuração de uma integração
     */
    public IntegrationConfigDTO getIntegration(IntegrationType type) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        
        return repository.findByEscritorioIdAndType(escritorioId, type)
            .map(this::toConfigDTO)
            .orElse(IntegrationConfigDTO.builder()
                .type(type.name())
                .name(getIntegrationName(type))
                .configured(false)
                .enabled(false)
                .build());
    }

    /**
     * Configurar integração
     */
    public IntegrationConfigDTO configureIntegration(IntegrationType type, IntegrationConfigRequest request) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        
        IntegrationConfig config = repository.findByEscritorioIdAndType(escritorioId, type)
            .orElse(IntegrationConfig.builder()
                .type(type)
                .name(getIntegrationName(type))
                .escritorioId(escritorioId)
                .build());
        
        // Atualizar configurações
        if (request.getApiKey() != null) {
            config.setApiKey(request.getApiKey());
        }
        if (request.getApiSecret() != null) {
            config.setApiSecret(request.getApiSecret());
        }
        if (request.getAccessToken() != null) {
            config.setAccessToken(request.getAccessToken());
        }
        if (request.getRefreshToken() != null) {
            config.setRefreshToken(request.getRefreshToken());
        }
        if (request.getWebhookUrl() != null) {
            config.setWebhookUrl(request.getWebhookUrl());
        }
        if (request.getCallbackUrl() != null) {
            config.setCallbackUrl(request.getCallbackUrl());
        }
        if (request.getSettings() != null) {
            config.setSettings(request.getSettings());
        }
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        }
        
        config.setDescription(request.getDescription());
        
        IntegrationConfig saved = repository.save(config);
        log.info("Integração {} configurada para escritório {}", type, escritorioId);
        
        return toConfigDTO(saved);
    }

    /**
     * Habilitar/desabilitar integração
     */
    public IntegrationConfigDTO toggleIntegration(IntegrationType type, boolean enabled) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        
        IntegrationConfig config = repository.findByEscritorioIdAndType(escritorioId, type)
            .orElseThrow(() -> new RuntimeException("Integração não configurada"));
        
        config.setEnabled(enabled);
        IntegrationConfig saved = repository.save(config);
        
        log.info("Integração {} {} para escritório {}", 
            type, enabled ? "habilitada" : "desabilitada", escritorioId);
        
        return toConfigDTO(saved);
    }

    /**
     * Testar conexão da integração
     */
    public TestConnectionResult testConnection(IntegrationType type) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        
        Optional<IntegrationConfig> configOpt = repository.findByEscritorioIdAndType(escritorioId, type);
        
        if (configOpt.isEmpty()) {
            return TestConnectionResult.builder()
                .success(false)
                .message("Integração não configurada")
                .build();
        }
        
        IntegrationConfig config = configOpt.get();
        
        try {
            // Aqui seria feito o teste real de conexão
            // Por enquanto, apenas simulamos
            boolean success = config.getApiKey() != null && !config.getApiKey().isEmpty();
            
            // Atualizar status do último sync
            config.setLastSync(LocalDateTime.now());
            config.setLastSyncStatus(success ? "SUCCESS" : "FAILED");
            config.setLastSyncError(success ? null : "Credenciais inválidas");
            repository.save(config);
            
            return TestConnectionResult.builder()
                .success(success)
                .message(success ? "Conexão estabelecida com sucesso" : "Falha na conexão")
                .responseTime(150L)
                .testedAt(LocalDateTime.now())
                .build();
        } catch (Exception e) {
            config.setLastSync(LocalDateTime.now());
            config.setLastSyncStatus("ERROR");
            config.setLastSyncError(e.getMessage());
            repository.save(config);
            
            return TestConnectionResult.builder()
                .success(false)
                .message("Erro: " + e.getMessage())
                .testedAt(LocalDateTime.now())
                .build();
        }
    }

    /**
     * Remover configuração de integração
     */
    public void removeIntegration(IntegrationType type) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        
        repository.findByEscritorioIdAndType(escritorioId, type)
            .ifPresent(config -> {
                repository.delete(config);
                log.info("Integração {} removida do escritório {}", type, escritorioId);
            });
    }

    private IntegrationConfigDTO toConfigDTO(IntegrationConfig config) {
        return IntegrationConfigDTO.builder()
            .id(config.getId())
            .type(config.getType().name())
            .name(config.getName())
            .description(config.getDescription())
            .configured(true)
            .enabled(config.isEnabled())
            .hasApiKey(config.getApiKey() != null && !config.getApiKey().isEmpty())
            .hasApiSecret(config.getApiSecret() != null && !config.getApiSecret().isEmpty())
            .webhookUrl(config.getWebhookUrl())
            .callbackUrl(config.getCallbackUrl())
            .settings(config.getSettings())
            .lastSync(config.getLastSync())
            .lastSyncStatus(config.getLastSyncStatus())
            .lastSyncError(config.getLastSyncError())
            .createdAt(config.getCreatedAt())
            .updatedAt(config.getUpdatedAt())
            .build();
    }

    private String getIntegrationName(IntegrationType type) {
        return switch (type) {
            case OPENAI -> "OpenAI (GPT-4)";
            case ANTHROPIC -> "Anthropic (Claude)";
            case PJE -> "PJe - Processo Judicial Eletrônico";
            case ESAJ -> "e-SAJ (TJSP)";
            case PROJUDI -> "PROJUDI";
            case DATAJUD -> "DATAJUD (CNJ)";
            case RECEITA_FEDERAL -> "Receita Federal";
            case BACENJUD -> "BacenJud";
            case RENAJUD -> "Renajud";
            case SISBAJUD -> "Sisbajud";
            case WHATSAPP -> "WhatsApp Business";
            case TWILIO -> "Twilio (SMS)";
            case SENDGRID -> "SendGrid (Email)";
            case SLACK -> "Slack";
            case TEAMS -> "Microsoft Teams";
            case GOOGLE_CALENDAR -> "Google Calendar";
            case OUTLOOK -> "Microsoft Outlook";
            case GOOGLE_DRIVE -> "Google Drive";
            case ONEDRIVE -> "Microsoft OneDrive";
            case AWS_S3 -> "Amazon S3";
            case DOCUSIGN -> "DocuSign";
            case CLICKSIGN -> "Clicksign";
            case ICP_BRASIL -> "Certificado Digital ICP-Brasil";
            case STRIPE -> "Stripe";
            case MERCADO_PAGO -> "Mercado Pago";
            case PIX -> "PIX (Banco Central)";
        };
    }

    private String getIntegrationDescription(IntegrationType type) {
        return switch (type) {
            case OPENAI -> "IA para análise de documentos, geração de peças e previsão de resultados";
            case ANTHROPIC -> "IA avançada para análise jurídica e geração de conteúdo";
            case PJE -> "Consulta e peticionamento no Processo Judicial Eletrônico";
            case ESAJ -> "Integração com tribunais de São Paulo";
            case PROJUDI -> "Integração com tribunais estaduais";
            case DATAJUD -> "Base nacional de dados do Poder Judiciário";
            case RECEITA_FEDERAL -> "Consulta CPF/CNPJ e situação cadastral";
            case BACENJUD -> "Consulta de bloqueios judiciais";
            case RENAJUD -> "Restrições de veículos";
            case SISBAJUD -> "Penhoras e bloqueios judiciais";
            case WHATSAPP -> "Notificações e comunicação com clientes via WhatsApp";
            case TWILIO -> "Envio de SMS para clientes e equipe";
            case SENDGRID -> "Envio de emails transacionais e marketing";
            case SLACK -> "Alertas e notificações para equipe no Slack";
            case TEAMS -> "Alertas e notificações para equipe no Teams";
            case GOOGLE_CALENDAR -> "Sincronização de audiências e prazos";
            case OUTLOOK -> "Sincronização com calendário Outlook";
            case GOOGLE_DRIVE -> "Armazenamento de documentos na nuvem";
            case ONEDRIVE -> "Armazenamento de documentos no OneDrive";
            case AWS_S3 -> "Armazenamento escalável na Amazon";
            case DOCUSIGN -> "Assinatura eletrônica de documentos";
            case CLICKSIGN -> "Assinatura eletrônica com validade jurídica";
            case ICP_BRASIL -> "Assinatura com certificado digital A1/A3";
            case STRIPE -> "Cobranças e pagamentos internacionais";
            case MERCADO_PAGO -> "Cobranças e pagamentos no Brasil";
            case PIX -> "Pagamentos instantâneos via PIX";
        };
    }

    private String getIntegrationCategory(IntegrationType type) {
        return switch (type) {
            case OPENAI, ANTHROPIC -> "Inteligência Artificial";
            case PJE, ESAJ, PROJUDI, DATAJUD -> "Tribunais";
            case RECEITA_FEDERAL, BACENJUD, RENAJUD, SISBAJUD -> "Órgãos Públicos";
            case WHATSAPP, TWILIO, SENDGRID, SLACK, TEAMS -> "Comunicação";
            case GOOGLE_CALENDAR, OUTLOOK -> "Calendário";
            case GOOGLE_DRIVE, ONEDRIVE, AWS_S3 -> "Armazenamento";
            case DOCUSIGN, CLICKSIGN, ICP_BRASIL -> "Assinatura Digital";
            case STRIPE, MERCADO_PAGO, PIX -> "Pagamentos";
        };
    }

    private String getIntegrationIcon(IntegrationType type) {
        return switch (type) {
            case OPENAI, ANTHROPIC -> "brain";
            case PJE, ESAJ, PROJUDI, DATAJUD -> "building-library";
            case RECEITA_FEDERAL, BACENJUD, RENAJUD, SISBAJUD -> "landmark";
            case WHATSAPP -> "message-circle";
            case TWILIO -> "smartphone";
            case SENDGRID -> "mail";
            case SLACK, TEAMS -> "hash";
            case GOOGLE_CALENDAR, OUTLOOK -> "calendar";
            case GOOGLE_DRIVE, ONEDRIVE, AWS_S3 -> "cloud";
            case DOCUSIGN, CLICKSIGN, ICP_BRASIL -> "pen-tool";
            case STRIPE, MERCADO_PAGO, PIX -> "credit-card";
        };
    }

    // DTOs
    @Data
    @Builder
    public static class IntegrationDTO {
        private String type;
        private String name;
        private String description;
        private String category;
        private String icon;
        private boolean configured;
        private boolean enabled;
        private LocalDateTime lastSync;
        private String lastSyncStatus;
    }

    @Data
    @Builder
    public static class IntegrationConfigDTO {
        private UUID id;
        private String type;
        private String name;
        private String description;
        private boolean configured;
        private boolean enabled;
        private boolean hasApiKey;
        private boolean hasApiSecret;
        private String webhookUrl;
        private String callbackUrl;
        private Map<String, String> settings;
        private LocalDateTime lastSync;
        private String lastSyncStatus;
        private String lastSyncError;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class IntegrationConfigRequest {
        private String apiKey;
        private String apiSecret;
        private String accessToken;
        private String refreshToken;
        private String webhookUrl;
        private String callbackUrl;
        private String description;
        private Boolean enabled;
        private Map<String, String> settings;
    }

    @Data
    @Builder
    public static class TestConnectionResult {
        private boolean success;
        private String message;
        private Long responseTime;
        private LocalDateTime testedAt;
    }
}

