package com.desafio.comissao;

import java.math.BigDecimal;
import java.util.List;

/** Comissão de cada vendedor e totais gerais. */
public record RelatorioComissaoResponse(
        List<ComissaoVendedorResponse> vendedores,
        long quantidadeVendas,
        BigDecimal totalVendas,
        BigDecimal totalComissao) {
}
