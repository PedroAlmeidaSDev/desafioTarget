package com.desafio.estoque;

import com.desafio.common.exception.RegraNegocioException;

/** Entrada que faria o estoque passar do maior valor de um {@code int} → HTTP 422. */
public class EstoqueAcimaDoLimiteException extends RegraNegocioException {

    public EstoqueAcimaDoLimiteException(Produto produto, int quantidadeSolicitada) {
        super("Estoque acima do limite para o produto %d (%s): disponível %d, entrada %d, máximo %d"
                .formatted(produto.codigoProduto(), produto.descricaoProduto(), produto.estoque(),
                        quantidadeSolicitada, Integer.MAX_VALUE));
    }
}
