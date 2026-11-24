package com.desafio.estoque;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Produto do JSON de estoque. É imutável: a quantidade só muda pelo lançamento de movimentação,
 * que cria uma cópia com {@link #comEstoque(int)} depois de validar e registrar o histórico.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Produto(int codigoProduto, String descricaoProduto, int estoque) {

    public Produto comEstoque(int novoEstoque) {
        return new Produto(codigoProduto, descricaoProduto, novoEstoque);
    }
}
