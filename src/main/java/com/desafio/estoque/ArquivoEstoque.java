package com.desafio.estoque;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Raiz do JSON de estoque: <code>{ "estoque": [ ...produtos... ] }</code>. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArquivoEstoque(List<Produto> estoque) {
}
