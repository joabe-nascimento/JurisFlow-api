package com.jurisflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JurisFlow - Sistema Inteligente de Gestão Jurídica
 * 
 * @author Joabe Fonseca do Nascimento
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class JurisflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(JurisflowApplication.class, args);
    }
}


