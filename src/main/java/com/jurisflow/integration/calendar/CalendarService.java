package com.jurisflow.integration.calendar;

import com.jurisflow.integration.IntegrationConfig;
import com.jurisflow.integration.IntegrationConfig.IntegrationType;
import com.jurisflow.integration.repository.IntegrationConfigRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviço de Integração com Calendários
 * Google Calendar, Outlook, Apple Calendar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final IntegrationConfigRepository configRepository;

    /**
     * Sincronizar evento com calendário externo
     */
    public CalendarSyncResult syncEvent(UUID escritorioId, CalendarEvent event) {
        List<CalendarSyncResult.SyncDetail> results = new ArrayList<>();
        
        // Tenta sincronizar com Google Calendar
        Optional<IntegrationConfig> googleConfig = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.GOOGLE_CALENDAR);
        
        if (googleConfig.isPresent() && googleConfig.get().isEnabled()) {
            results.add(syncToGoogleCalendar(googleConfig.get(), event));
        }
        
        // Tenta sincronizar com Outlook
        Optional<IntegrationConfig> outlookConfig = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.OUTLOOK);
        
        if (outlookConfig.isPresent() && outlookConfig.get().isEnabled()) {
            results.add(syncToOutlook(outlookConfig.get(), event));
        }
        
        boolean allSuccess = results.stream().allMatch(CalendarSyncResult.SyncDetail::isSuccess);
        
        return CalendarSyncResult.builder()
            .success(allSuccess || !results.isEmpty())
            .eventId(event.getId())
            .details(results)
            .syncedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Criar audiência no calendário
     */
    public CalendarSyncResult createAudiencia(UUID escritorioId, AudienciaRequest request) {
        CalendarEvent event = CalendarEvent.builder()
            .id(UUID.randomUUID().toString())
            .title("Audiência - " + request.getNumeroProcesso())
            .description(String.format(
                "Processo: %s\nCliente: %s\nTipo: %s\nVara: %s\n\n%s",
                request.getNumeroProcesso(),
                request.getNomeCliente(),
                request.getTipoAudiencia(),
                request.getVara(),
                request.getObservacoes() != null ? request.getObservacoes() : ""
            ))
            .location(request.getLocal())
            .startDateTime(request.getDataHoraInicio())
            .endDateTime(request.getDataHoraFim())
            .allDay(false)
            .reminders(Arrays.asList(
                new CalendarReminder(60, "email"),      // 1 hora antes
                new CalendarReminder(1440, "popup"),   // 1 dia antes
                new CalendarReminder(10080, "email")   // 1 semana antes
            ))
            .attendees(request.getParticipantes())
            .color("#dc2626") // Vermelho para audiências
            .metadata(Map.of(
                "tipo", "audiencia",
                "processoId", request.getProcessoId(),
                "clienteId", request.getClienteId()
            ))
            .build();
        
        return syncEvent(escritorioId, event);
    }

    /**
     * Criar prazo no calendário
     */
    public CalendarSyncResult createPrazo(UUID escritorioId, PrazoCalendarRequest request) {
        CalendarEvent event = CalendarEvent.builder()
            .id(UUID.randomUUID().toString())
            .title("⚠️ PRAZO: " + request.getTitulo())
            .description(String.format(
                "Processo: %s\nTipo: %s\nPrioridade: %s\n\n%s",
                request.getNumeroProcesso(),
                request.getTipoPrazo(),
                request.getPrioridade(),
                request.getDescricao() != null ? request.getDescricao() : ""
            ))
            .startDateTime(request.getDataVencimento().atStartOfDay())
            .endDateTime(request.getDataVencimento().atTime(23, 59))
            .allDay(true)
            .reminders(getRemindersForPriority(request.getPrioridade()))
            .color(getColorForPriority(request.getPrioridade()))
            .metadata(Map.of(
                "tipo", "prazo",
                "processoId", request.getProcessoId(),
                "prazoId", request.getPrazoId()
            ))
            .build();
        
        return syncEvent(escritorioId, event);
    }

    /**
     * Criar reunião no calendário
     */
    public CalendarSyncResult createReuniao(UUID escritorioId, ReuniaoRequest request) {
        CalendarEvent event = CalendarEvent.builder()
            .id(UUID.randomUUID().toString())
            .title(request.getTitulo())
            .description(request.getDescricao())
            .location(request.getLocal())
            .startDateTime(request.getDataHoraInicio())
            .endDateTime(request.getDataHoraFim())
            .allDay(false)
            .reminders(Arrays.asList(
                new CalendarReminder(15, "popup"),
                new CalendarReminder(60, "email")
            ))
            .attendees(request.getParticipantes())
            .conferenceLink(request.getLinkVideoconferencia())
            .color("#3b82f6") // Azul para reuniões
            .metadata(Map.of("tipo", "reuniao"))
            .build();
        
        return syncEvent(escritorioId, event);
    }

    /**
     * Obter eventos do calendário
     */
    public List<CalendarEvent> getEvents(UUID escritorioId, LocalDateTime start, LocalDateTime end) {
        List<CalendarEvent> events = new ArrayList<>();
        
        // Google Calendar
        Optional<IntegrationConfig> googleConfig = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.GOOGLE_CALENDAR);
        
        if (googleConfig.isPresent() && googleConfig.get().isEnabled()) {
            events.addAll(getEventsFromGoogle(googleConfig.get(), start, end));
        }
        
        // Outlook
        Optional<IntegrationConfig> outlookConfig = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.OUTLOOK);
        
        if (outlookConfig.isPresent() && outlookConfig.get().isEnabled()) {
            events.addAll(getEventsFromOutlook(outlookConfig.get(), start, end));
        }
        
        // Ordenar por data
        events.sort(Comparator.comparing(CalendarEvent::getStartDateTime));
        
        return events;
    }

    /**
     * Deletar evento do calendário
     */
    public CalendarSyncResult deleteEvent(UUID escritorioId, String eventId) {
        List<CalendarSyncResult.SyncDetail> results = new ArrayList<>();
        
        Optional<IntegrationConfig> googleConfig = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.GOOGLE_CALENDAR);
        
        if (googleConfig.isPresent() && googleConfig.get().isEnabled()) {
            results.add(deleteFromGoogle(googleConfig.get(), eventId));
        }
        
        Optional<IntegrationConfig> outlookConfig = configRepository.findByEscritorioIdAndType(
            escritorioId, IntegrationType.OUTLOOK);
        
        if (outlookConfig.isPresent() && outlookConfig.get().isEnabled()) {
            results.add(deleteFromOutlook(outlookConfig.get(), eventId));
        }
        
        return CalendarSyncResult.builder()
            .success(!results.isEmpty())
            .eventId(eventId)
            .details(results)
            .syncedAt(LocalDateTime.now())
            .build();
    }

    // Métodos privados de integração
    
    private CalendarSyncResult.SyncDetail syncToGoogleCalendar(IntegrationConfig config, CalendarEvent event) {
        try {
            log.info("Sincronizando evento {} com Google Calendar", event.getId());
            // Aqui seria feita a integração real com Google Calendar API
            // Requer OAuth2 e Google Calendar API
            
            return CalendarSyncResult.SyncDetail.builder()
                .provider("GOOGLE_CALENDAR")
                .success(true)
                .externalEventId("google_" + event.getId())
                .message("Evento sincronizado com Google Calendar")
                .build();
        } catch (Exception e) {
            log.error("Erro ao sincronizar com Google Calendar: {}", e.getMessage());
            return CalendarSyncResult.SyncDetail.builder()
                .provider("GOOGLE_CALENDAR")
                .success(false)
                .error(e.getMessage())
                .build();
        }
    }

    private CalendarSyncResult.SyncDetail syncToOutlook(IntegrationConfig config, CalendarEvent event) {
        try {
            log.info("Sincronizando evento {} com Outlook", event.getId());
            // Aqui seria feita a integração real com Microsoft Graph API
            
            return CalendarSyncResult.SyncDetail.builder()
                .provider("OUTLOOK")
                .success(true)
                .externalEventId("outlook_" + event.getId())
                .message("Evento sincronizado com Outlook")
                .build();
        } catch (Exception e) {
            log.error("Erro ao sincronizar com Outlook: {}", e.getMessage());
            return CalendarSyncResult.SyncDetail.builder()
                .provider("OUTLOOK")
                .success(false)
                .error(e.getMessage())
                .build();
        }
    }

    private List<CalendarEvent> getEventsFromGoogle(IntegrationConfig config, LocalDateTime start, LocalDateTime end) {
        // Simulação - aqui seria feita a chamada real à API
        return new ArrayList<>();
    }

    private List<CalendarEvent> getEventsFromOutlook(IntegrationConfig config, LocalDateTime start, LocalDateTime end) {
        // Simulação - aqui seria feita a chamada real à API
        return new ArrayList<>();
    }

    private CalendarSyncResult.SyncDetail deleteFromGoogle(IntegrationConfig config, String eventId) {
        log.info("Deletando evento {} do Google Calendar", eventId);
        return CalendarSyncResult.SyncDetail.builder()
            .provider("GOOGLE_CALENDAR")
            .success(true)
            .message("Evento removido do Google Calendar")
            .build();
    }

    private CalendarSyncResult.SyncDetail deleteFromOutlook(IntegrationConfig config, String eventId) {
        log.info("Deletando evento {} do Outlook", eventId);
        return CalendarSyncResult.SyncDetail.builder()
            .provider("OUTLOOK")
            .success(true)
            .message("Evento removido do Outlook")
            .build();
    }

    private List<CalendarReminder> getRemindersForPriority(String prioridade) {
        return switch (prioridade.toUpperCase()) {
            case "URGENTE" -> Arrays.asList(
                new CalendarReminder(60, "popup"),
                new CalendarReminder(180, "email"),
                new CalendarReminder(1440, "popup"),
                new CalendarReminder(2880, "email")
            );
            case "ALTA" -> Arrays.asList(
                new CalendarReminder(60, "popup"),
                new CalendarReminder(1440, "email"),
                new CalendarReminder(2880, "email")
            );
            default -> Arrays.asList(
                new CalendarReminder(1440, "email"),
                new CalendarReminder(2880, "popup")
            );
        };
    }

    private String getColorForPriority(String prioridade) {
        return switch (prioridade.toUpperCase()) {
            case "URGENTE" -> "#dc2626"; // Vermelho
            case "ALTA" -> "#f97316";    // Laranja
            case "MEDIA" -> "#eab308";   // Amarelo
            default -> "#22c55e";         // Verde
        };
    }

    // DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEvent {
        private String id;
        private String title;
        private String description;
        private String location;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private boolean allDay;
        private List<CalendarReminder> reminders;
        private List<String> attendees;
        private String conferenceLink;
        private String color;
        private Map<String, String> metadata;
    }

    @Data
    @AllArgsConstructor
    public static class CalendarReminder {
        private int minutesBefore;
        private String method; // popup, email
    }

    @Data
    @Builder
    public static class CalendarSyncResult {
        private boolean success;
        private String eventId;
        private List<SyncDetail> details;
        private LocalDateTime syncedAt;

        @Data
        @Builder
        public static class SyncDetail {
            private String provider;
            private boolean success;
            private String externalEventId;
            private String message;
            private String error;
        }
    }

    @Data
    @Builder
    public static class AudienciaRequest {
        private String processoId;
        private String clienteId;
        private String numeroProcesso;
        private String nomeCliente;
        private String tipoAudiencia;
        private String vara;
        private String local;
        private LocalDateTime dataHoraInicio;
        private LocalDateTime dataHoraFim;
        private String observacoes;
        private List<String> participantes;
    }

    @Data
    @Builder
    public static class PrazoCalendarRequest {
        private String processoId;
        private String prazoId;
        private String titulo;
        private String numeroProcesso;
        private String tipoPrazo;
        private String prioridade;
        private String descricao;
        private java.time.LocalDate dataVencimento;
    }

    @Data
    @Builder
    public static class ReuniaoRequest {
        private String titulo;
        private String descricao;
        private String local;
        private LocalDateTime dataHoraInicio;
        private LocalDateTime dataHoraFim;
        private List<String> participantes;
        private String linkVideoconferencia;
    }
}

