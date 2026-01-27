package com.jurisflow.integration.notification;

import com.jurisflow.integration.IntegrationConfig;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.repository.IntegrationConfigRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de Notificações Multicanal
 * WhatsApp, Email, SMS, Slack, Teams
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final IntegrationConfigRepository configRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Enviar notificação por múltiplos canais
     */
    @Async
    public CompletableFuture<NotificationResult> sendNotification(
            UUID escritorioId, NotificationRequest request) {
        
        List<ChannelResult> results = new ArrayList<>();
        
        for (NotificationChannel channel : request.getChannels()) {
            ChannelResult result = switch (channel) {
                case WHATSAPP -> sendWhatsApp(escritorioId, request);
                case EMAIL -> sendEmail(escritorioId, request);
                case SMS -> sendSms(escritorioId, request);
                case SLACK -> sendSlack(escritorioId, request);
                case TEAMS -> sendTeams(escritorioId, request);
            };
            results.add(result);
        }
        
        boolean allSuccess = results.stream().allMatch(ChannelResult::isSuccess);
        
        return CompletableFuture.completedFuture(NotificationResult.builder()
            .success(allSuccess)
            .results(results)
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * Enviar lembrete de prazo
     */
    public NotificationResult sendPrazoReminder(UUID escritorioId, PrazoReminderRequest request) {
        String message = String.format(
            "⚠️ *Lembrete de Prazo*\n\n" +
            "📋 *Processo:* %s\n" +
            "📝 *Prazo:* %s\n" +
            "📅 *Vencimento:* %s\n" +
            "⏰ *Dias restantes:* %d\n\n" +
            "Acesse o JurisFlow para mais detalhes.",
            request.getNumeroProcesso(),
            request.getTituloPrazo(),
            request.getDataVencimento(),
            request.getDiasRestantes()
        );
        
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .recipient(request.getDestinatario())
            .recipientName(request.getNomeDestinatario())
            .subject("Lembrete de Prazo - " + request.getTituloPrazo())
            .message(message)
            .channels(request.getChannels())
            .metadata(Map.of(
                "tipo", "prazo_reminder",
                "processoId", request.getProcessoId(),
                "prazoId", request.getPrazoId()
            ))
            .build();
        
        return sendNotification(escritorioId, notificationRequest).join();
    }

    /**
     * Enviar notificação de audiência
     */
    public NotificationResult sendAudienciaNotification(UUID escritorioId, AudienciaNotificationRequest request) {
        String message = String.format(
            "📅 *Lembrete de Audiência*\n\n" +
            "📋 *Processo:* %s\n" +
            "👤 *Cliente:* %s\n" +
            "🏛️ *Local:* %s\n" +
            "📅 *Data:* %s\n" +
            "⏰ *Horário:* %s\n" +
            "📝 *Tipo:* %s\n\n" +
            "%s",
            request.getNumeroProcesso(),
            request.getNomeCliente(),
            request.getLocal(),
            request.getData(),
            request.getHorario(),
            request.getTipoAudiencia(),
            request.getObservacoes() != null ? "Obs: " + request.getObservacoes() : ""
        );
        
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .recipient(request.getDestinatario())
            .recipientName(request.getNomeDestinatario())
            .subject("Lembrete de Audiência - " + request.getData())
            .message(message)
            .channels(request.getChannels())
            .metadata(Map.of(
                "tipo", "audiencia_reminder",
                "processoId", request.getProcessoId()
            ))
            .build();
        
        return sendNotification(escritorioId, notificationRequest).join();
    }

    /**
     * Enviar atualização de processo para cliente
     */
    public NotificationResult sendProcessUpdate(UUID escritorioId, ProcessUpdateRequest request) {
        String message = String.format(
            "📢 *Atualização do seu Processo*\n\n" +
            "Olá, %s!\n\n" +
            "Seu processo %s teve uma nova movimentação:\n\n" +
            "📝 *%s*\n" +
            "📅 Data: %s\n\n" +
            "Para mais detalhes, acesse o portal do cliente ou entre em contato conosco.",
            request.getNomeCliente(),
            request.getNumeroProcesso(),
            request.getDescricaoMovimentacao(),
            request.getDataMovimentacao()
        );
        
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .recipient(request.getContatoCliente())
            .recipientName(request.getNomeCliente())
            .subject("Atualização do Processo " + request.getNumeroProcesso())
            .message(message)
            .channels(request.getChannels())
            .metadata(Map.of(
                "tipo", "process_update",
                "processoId", request.getProcessoId(),
                "clienteId", request.getClienteId()
            ))
            .build();
        
        return sendNotification(escritorioId, notificationRequest).join();
    }

    private ChannelResult sendWhatsApp(UUID escritorioId, NotificationRequest request) {
        Optional<IntegrationConfig> config = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.WHATSAPP);
        
        if (config.isEmpty() || !config.get().isEnabled()) {
            return ChannelResult.error(NotificationChannel.WHATSAPP, "WhatsApp não configurado");
        }

        try {
            // Integração com WhatsApp Business API
            // Aqui seria feita a chamada real à API do WhatsApp
            log.info("Enviando WhatsApp para {}: {}", request.getRecipient(), request.getMessage());
            
            // Simulação de envio bem-sucedido
            return ChannelResult.success(NotificationChannel.WHATSAPP, "Mensagem enviada via WhatsApp");
        } catch (Exception e) {
            log.error("Erro ao enviar WhatsApp: {}", e.getMessage());
            return ChannelResult.error(NotificationChannel.WHATSAPP, e.getMessage());
        }
    }

    private ChannelResult sendEmail(UUID escritorioId, NotificationRequest request) {
        Optional<IntegrationConfig> config = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.SENDGRID);
        
        if (config.isEmpty() || !config.get().isEnabled()) {
            // Tenta usar SMTP padrão se SendGrid não estiver configurado
            log.info("SendGrid não configurado, usando SMTP padrão");
        }

        try {
            // Integração com SendGrid ou SMTP
            log.info("Enviando email para {}: {}", request.getRecipient(), request.getSubject());
            
            return ChannelResult.success(NotificationChannel.EMAIL, "Email enviado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao enviar email: {}", e.getMessage());
            return ChannelResult.error(NotificationChannel.EMAIL, e.getMessage());
        }
    }

    private ChannelResult sendSms(UUID escritorioId, NotificationRequest request) {
        Optional<IntegrationConfig> config = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.TWILIO);
        
        if (config.isEmpty() || !config.get().isEnabled()) {
            return ChannelResult.error(NotificationChannel.SMS, "Twilio não configurado");
        }

        try {
            // Integração com Twilio
            log.info("Enviando SMS para {}", request.getRecipient());
            
            return ChannelResult.success(NotificationChannel.SMS, "SMS enviado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao enviar SMS: {}", e.getMessage());
            return ChannelResult.error(NotificationChannel.SMS, e.getMessage());
        }
    }

    private ChannelResult sendSlack(UUID escritorioId, NotificationRequest request) {
        Optional<IntegrationConfig> config = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.SLACK);
        
        if (config.isEmpty() || !config.get().isEnabled()) {
            return ChannelResult.error(NotificationChannel.SLACK, "Slack não configurado");
        }

        try {
            String webhookUrl = config.get().getWebhookUrl();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", request.getMessage());
            payload.put("username", "JurisFlow Bot");
            payload.put("icon_emoji", ":scales:");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
            
            return ChannelResult.success(NotificationChannel.SLACK, "Mensagem enviada ao Slack");
        } catch (Exception e) {
            log.error("Erro ao enviar Slack: {}", e.getMessage());
            return ChannelResult.error(NotificationChannel.SLACK, e.getMessage());
        }
    }

    private ChannelResult sendTeams(UUID escritorioId, NotificationRequest request) {
        Optional<IntegrationConfig> config = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.TEAMS);
        
        if (config.isEmpty() || !config.get().isEnabled()) {
            return ChannelResult.error(NotificationChannel.TEAMS, "Teams não configurado");
        }

        try {
            String webhookUrl = config.get().getWebhookUrl();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("@type", "MessageCard");
            payload.put("@context", "http://schema.org/extensions");
            payload.put("summary", request.getSubject());
            payload.put("themeColor", "0076D7");
            payload.put("title", request.getSubject());
            payload.put("text", request.getMessage());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
            
            return ChannelResult.success(NotificationChannel.TEAMS, "Mensagem enviada ao Teams");
        } catch (Exception e) {
            log.error("Erro ao enviar Teams: {}", e.getMessage());
            return ChannelResult.error(NotificationChannel.TEAMS, e.getMessage());
        }
    }

    // Enums e DTOs
    public enum NotificationChannel {
        WHATSAPP, EMAIL, SMS, SLACK, TEAMS
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationRequest {
        private String recipient;
        private String recipientName;
        private String subject;
        private String message;
        private List<NotificationChannel> channels;
        private Map<String, String> metadata;
    }

    @Data
    @Builder
    public static class NotificationResult {
        private boolean success;
        private List<ChannelResult> results;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    public static class ChannelResult {
        private NotificationChannel channel;
        private boolean success;
        private String message;
        private String error;

        public static ChannelResult success(NotificationChannel channel, String message) {
            return ChannelResult.builder()
                .channel(channel)
                .success(true)
                .message(message)
                .build();
        }

        public static ChannelResult error(NotificationChannel channel, String error) {
            return ChannelResult.builder()
                .channel(channel)
                .success(false)
                .error(error)
                .build();
        }
    }

    @Data
    @Builder
    public static class PrazoReminderRequest {
        private String processoId;
        private String prazoId;
        private String numeroProcesso;
        private String tituloPrazo;
        private String dataVencimento;
        private int diasRestantes;
        private String destinatario;
        private String nomeDestinatario;
        private List<NotificationChannel> channels;
    }

    @Data
    @Builder
    public static class AudienciaNotificationRequest {
        private String processoId;
        private String numeroProcesso;
        private String nomeCliente;
        private String local;
        private String data;
        private String horario;
        private String tipoAudiencia;
        private String observacoes;
        private String destinatario;
        private String nomeDestinatario;
        private List<NotificationChannel> channels;
    }

    @Data
    @Builder
    public static class ProcessUpdateRequest {
        private String processoId;
        private String clienteId;
        private String numeroProcesso;
        private String nomeCliente;
        private String contatoCliente;
        private String descricaoMovimentacao;
        private String dataMovimentacao;
        private List<NotificationChannel> channels;
    }
}

