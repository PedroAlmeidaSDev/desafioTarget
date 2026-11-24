package com.desafio.juros;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

/**
 * Regra de negócio da questão 3: juros por atraso.
 *
 * <p>Interpretação adotada para o enunciado ("juros" e "multa" de 2,5% ao dia):
 * <ul>
 *   <li>juros simples de 2,5% sobre o valor original por dia de atraso:
 *       {@code juros = valor × 0,025 × diasAtraso};</li>
 *   <li>"juros" e "multa" são a mesma cobrança diária, sem multa fixa separada;</li>
 *   <li>dias corridos entre o vencimento e hoje; vencimento hoje ou no futuro → juros zero;</li>
 *   <li>"hoje" é a data no fuso America/Sao_Paulo (ver {@code ClockConfig});</li>
 *   <li>juros arredondados para 2 casas (HALF_UP).</li>
 * </ul>
 * Exemplo: R$ 1.000,00 vencido há 10 dias → R$ 250,00 de juros → total R$ 1.250,00.
 */
@Service
public class JurosService {

    /** 2,5% ao dia, em forma decimal. */
    static final BigDecimal TAXA_DIARIA = new BigDecimal("0.025");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Clock clock;

    public JurosService(Clock clock) {
        this.clock = clock;
    }

    public JurosResponse calcular(BigDecimal valor, LocalDate dataVencimento) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
        if (dataVencimento == null) {
            throw new IllegalArgumentException("A data de vencimento é obrigatória");
        }

        // Clock injetado para os testes poderem fixar a data de hoje.
        LocalDate hoje = LocalDate.now(clock);

        // Vencimento no futuro daria um número negativo de dias.
        long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(dataVencimento, hoje));

        BigDecimal juros = valor
                .multiply(TAXA_DIARIA)
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);

        return new JurosResponse(
                valor,
                dataVencimento,
                hoje,
                diasAtraso,
                TAXA_DIARIA.multiply(CEM).stripTrailingZeros(), // 0.025 → 2.5
                juros,
                valor.add(juros));
    }
}
