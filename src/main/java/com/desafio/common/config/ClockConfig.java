package com.desafio.common.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Clock} injetável, para os testes poderem fixar a data.
 *
 * <p>O fuso é explícito para que "hoje" não dependa do fuso do servidor: em UTC, entre 21h e 0h
 * de Brasília já seria o dia seguinte e os juros sairiam com um dia a mais.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${desafio.fuso-horario}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
