package com.desafio.estoque;

import com.desafio.common.exception.RecursoNaoEncontradoException;

/** Código de produto inexistente → HTTP 404. */
public class ProdutoNaoEncontradoException extends RecursoNaoEncontradoException {

    public ProdutoNaoEncontradoException(int codigoProduto) {
        super("Produto não encontrado: código " + codigoProduto);
    }
}
