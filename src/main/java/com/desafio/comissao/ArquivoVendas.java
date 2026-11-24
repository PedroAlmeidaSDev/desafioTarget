package com.desafio.comissao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Raiz do JSON de vendas, que é um objeto e não uma lista:
 * <code>{ "vendas": [ { "vendedor": "...", "valor": ... } ] }</code>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArquivoVendas(
        @NotNull(message = "é obrigatório")
        List<@Valid @NotNull Venda> vendas) {
}
