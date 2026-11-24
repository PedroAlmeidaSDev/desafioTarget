package com.desafio.juros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

/**
 * Testes do cálculo de juros com o "hoje" fixado em 19/11/2026.
 */
class JurosServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 19);

    private final JurosService service = new JurosService(
            Clock.fixed(HOJE.atStartOfDay(SAO_PAULO).plusHours(12).toInstant(), SAO_PAULO));

    private static final BigDecimal MIL = new BigDecimal("1000.00");

    @Test
    void vencimentoFuturoNaoTemJuros() {
        JurosResponse r = service.calcular(MIL, HOJE.plusDays(5));

        assertThat(r.diasAtraso()).isZero();
        assertThat(r.juros()).isEqualByComparingTo("0");
        assertThat(r.valorTotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    void vencimentoHojeNaoTemJuros() {
        JurosResponse r = service.calcular(MIL, HOJE);

        assertThat(r.diasAtraso()).isZero();
        assertThat(r.juros()).isEqualByComparingTo("0");
    }

    @Test
    void umDiaDeAtraso() {
        JurosResponse r = service.calcular(MIL, HOJE.minusDays(1));

        assertThat(r.diasAtraso()).isEqualTo(1);
        assertThat(r.juros()).isEqualByComparingTo("25.00");     // 1000 × 2,5%
        assertThat(r.valorTotal()).isEqualByComparingTo("1025.00");
    }

    @Test
    void dezDiasDeAtrasoUsaJurosSimples() {
        JurosResponse r = service.calcular(MIL, HOJE.minusDays(10));

        assertThat(r.diasAtraso()).isEqualTo(10);
        assertThat(r.juros()).isEqualByComparingTo("250.00");    // 1000 × 2,5% × 10
        assertThat(r.valorTotal()).isEqualByComparingTo("1250.00");
        assertThat(r.dataCalculo()).isEqualTo(HOJE);
        assertThat(r.taxaDiariaPercentual()).isEqualByComparingTo("2.5");
    }

    @Test
    void atrasoAtravessandoAViradaDoMes() {
        JurosResponse r = service.calcular(MIL, LocalDate.of(2026, 10, 31)); // 31/10 → 19/11

        assertThat(r.diasAtraso()).isEqualTo(19);
        assertThat(r.juros()).isEqualByComparingTo("475.00");
    }

    @Test
    void arredondaOsJurosEmDuasCasas() {
        // 33,33 × 0,025 × 1 = 0,83325 → 0,83
        JurosResponse r = service.calcular(new BigDecimal("33.33"), HOJE.minusDays(1));

        assertThat(r.juros()).isEqualTo(new BigDecimal("0.83"));
        assertThat(r.valorTotal()).isEqualTo(new BigDecimal("34.16"));
    }

    @Test
    void rejeitaValorInvalido() {
        assertThatThrownBy(() -> service.calcular(BigDecimal.ZERO, HOJE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calcular(null, HOJE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calcular(MIL, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
