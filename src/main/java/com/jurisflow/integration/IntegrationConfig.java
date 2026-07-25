package com.jurisflow.integration;

import jakarta.persistence.*;
import lombok.*;
import com.jurisflow.common.BaseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração de integrações do escritório
 */
@Entity
@Table(name = "integration_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    public enum IntegrationType {
        // IA
        OPENAI,
        ANTHROPIC,
        AZURE_OPENAI,
        AZURE_AI_FOUNDRY,
        COPILOT_STUDIO,
        // Tribunais
        PJE,
        ESAJ,
        PROJUDI,
        DATAJUD,
        
        // Órgãos Públicos
        RECEITA_FEDERAL,
        BACENJUD,
        RENAJUD,
        SISBAJUD,
        
        // Comunicação
        WHATSAPP,
        TWILIO,
        SENDGRID,
        SLACK,
        TEAMS,
        
        // Calendário
        GOOGLE_CALENDAR,
        OUTLOOK,
        
        // Armazenamento
        GOOGLE_DRIVE,
        ONEDRIVE,
        AWS_S3,
        
        // Assinatura Digital
        DOCUSIGN,
        CLICKSIGN,
        ICP_BRASIL,
        
        // Pagamentos
        STRIPE,
        MERCADO_PAGO,
        PIX
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationType type;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean enabled;

    @Column(columnDefinition = "TEXT")
    private String apiKey;

    @Column(columnDefinition = "TEXT")
    private String apiSecret;

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private String webhookUrl;

    private String callbackUrl;

    @ElementCollection
    @CollectionTable(name = "integration_config_settings")
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> settings = new HashMap<>();

    @Column(name = "escritorio_id", nullable = false)
    private java.util.UUID escritorioId;

    private java.time.LocalDateTime lastSync;

    private String lastSyncStatus;

    private String lastSyncError;
}

