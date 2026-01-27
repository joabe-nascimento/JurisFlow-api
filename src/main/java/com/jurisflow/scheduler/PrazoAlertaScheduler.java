package com.jurisflow.scheduler;

import com.jurisflow.domain.prazo.entity.Prazo;
import com.jurisflow.domain.prazo.repository.PrazoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler para envio de alertas de prazos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrazoAlertaScheduler {

    private final PrazoRepository prazoRepository;
    // private final NotificacaoService notificacaoService; // TODO: implementar

    /**
     * Executa diariamente às 8h para verificar prazos e enviar alertas.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void verificarPrazos() {
        log.info("Iniciando verificação de prazos...");

        LocalDate hoje = LocalDate.now();
        
        // Alertas D-7
        List<Prazo> prazos7Dias = prazoRepository.findPrazosParaAlerta7Dias(hoje.plusDays(7));
        for (Prazo prazo : prazos7Dias) {
            enviarAlerta(prazo, "7 dias");
            prazo.setAlerta7Dias(true);
            prazoRepository.save(prazo);
        }
        log.info("Alertas D-7 enviados: {}", prazos7Dias.size());

        // Alertas D-3
        List<Prazo> prazos3Dias = prazoRepository.findPrazosParaAlerta3Dias(hoje.plusDays(3));
        for (Prazo prazo : prazos3Dias) {
            enviarAlerta(prazo, "3 dias");
            prazo.setAlerta3Dias(true);
            prazoRepository.save(prazo);
        }
        log.info("Alertas D-3 enviados: {}", prazos3Dias.size());

        // Alertas D-1
        List<Prazo> prazos1Dia = prazoRepository.findPrazosParaAlerta1Dia(hoje.plusDays(1));
        for (Prazo prazo : prazos1Dia) {
            enviarAlerta(prazo, "amanhã");
            prazo.setAlerta1Dia(true);
            prazoRepository.save(prazo);
        }
        log.info("Alertas D-1 enviados: {}", prazos1Dia.size());

        log.info("Verificação de prazos concluída");
    }

    private void enviarAlerta(Prazo prazo, String tempoRestante) {
        // TODO: Integrar com serviço de notificações (email, WhatsApp, push)
        log.info("Enviando alerta: Prazo '{}' vence em {} - Processo: {}", 
                prazo.getTitulo(), 
                tempoRestante,
                prazo.getProcesso() != null ? prazo.getProcesso().getNumero() : "N/A");
    }
}


