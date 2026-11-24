package com.desafio.juros;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado do cálculo, com data de cálculo, dias e taxa para que o valor possa ser conferido.
 */
public record JurosResponse(
        BigDecimal valorOriginal,
        LocalDate dataVencimento,
        LocalDate dataCalculo,
        long diasAtraso,
        BigDecimal taxaDiariaPercentual,
        BigDecimal juros,
        BigDecimal valorTotal) {
}
