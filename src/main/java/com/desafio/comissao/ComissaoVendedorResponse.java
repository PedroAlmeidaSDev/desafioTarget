package com.desafio.comissao;

import java.math.BigDecimal;

/** Totais de um vendedor. Quantidade e total vendido vão junto para facilitar a conferência. */
public record ComissaoVendedorResponse(
        String vendedor,
        long quantidadeVendas,
        BigDecimal totalVendas,
        BigDecimal totalComissao) {
}
