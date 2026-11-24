package com.desafio.comissao;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Faixas de comissão do enunciado: abaixo de R$ 100,00 → 0%; abaixo de R$ 500,00 → 1%;
 * a partir de R$ 500,00 → 5%.
 *
 * <p>O limite de cada faixa é exclusivo: exatamente R$ 100,00 já paga 1% e exatamente
 * R$ 500,00 já paga 5%.
 */
public enum FaixaComissao {

    // A ordem importa: paraValor() devolve a primeira faixa cujo limite é maior que o valor.
    SEM_COMISSAO(new BigDecimal("100.00"), new BigDecimal("0.00")),
    COMISSAO_BASICA(new BigDecimal("500.00"), new BigDecimal("0.01")),
    COMISSAO_MAXIMA(null, new BigDecimal("0.05")); // sem limite superior

    private final BigDecimal limiteExclusivo;
    private final BigDecimal percentual;

    FaixaComissao(BigDecimal limiteExclusivo, BigDecimal percentual) {
        this.limiteExclusivo = limiteExclusivo;
        this.percentual = percentual;
    }

    public static FaixaComissao paraValor(BigDecimal valorVenda) {
        Objects.requireNonNull(valorVenda, "valor da venda é obrigatório");
        for (FaixaComissao faixa : values()) {
            if (faixa.limiteExclusivo == null || valorVenda.compareTo(faixa.limiteExclusivo) < 0) {
                return faixa;
            }
        }
        throw new IllegalStateException("Nenhuma faixa de comissão configurada para " + valorVenda);
    }

    /** Comissão da faixa para o valor, sem arredondamento (feito no total por vendedor). */
    public BigDecimal calcular(BigDecimal valorVenda) {
        return valorVenda.multiply(percentual);
    }

    public BigDecimal getPercentual() {
        return percentual;
    }
}
