package com.desafio;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Fixa o "hoje" em 19/11/2026 nos testes de integração. */
@TestConfiguration
class RelogioFixoConfig {

    @Bean
    @Primary
    Clock relogioFixo() {
        return Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    }
}
