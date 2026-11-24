package com.desafio.comissao;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Uma venda do arquivo JSON: <code>{ "vendedor": "João Silva", "valor": 1200.50 }</code>. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Venda(
        @NotBlank(message = "é obrigatório")
        String vendedor,

        @NotNull(message = "é obrigatório")
        @PositiveOrZero(message = "não pode ser negativo")
        BigDecimal valor) {
}
