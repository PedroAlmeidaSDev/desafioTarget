package com.desafio.estoque;

import com.desafio.common.exception.RegraNegocioException;

/** Saída maior que o estoque disponível → HTTP 422. */
public class EstoqueInsuficienteException extends RegraNegocioException {

    public EstoqueInsuficienteException(Produto produto, int quantidadeSolicitada) {
        super("Estoque insuficiente para o produto %d (%s): disponível %d, solicitado %d"
                .formatted(produto.codigoProduto(), produto.descricaoProduto(), produto.estoque(), quantidadeSolicitada));
    }
}
