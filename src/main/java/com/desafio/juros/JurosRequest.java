package com.desafio.juros;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Parâmetros do cálculo de juros: {@code GET /api/juros?valor=1000.00&dataVencimento=2026-11-10}.
 *
 * <p>GET porque é uma consulta sem efeito colateral e idempotente, com só dois valores simples.
 */
public record JurosRequest(
        @Schema(description = "Valor original da dívida", example = "1000.00")
        @NotNull(message = "é obrigatório")
        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 15, fraction = 2, message = "deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        @Schema(description = "Data de vencimento (aaaa-mm-dd)", example = "2026-11-10")
        @NotNull(message = "é obrigatória")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataVencimento) {
}
