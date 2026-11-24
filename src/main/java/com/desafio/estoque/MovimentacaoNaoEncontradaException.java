package com.desafio.estoque;

import com.desafio.common.exception.RecursoNaoEncontradoException;

/** ID de movimentação inexistente → HTTP 404. */
public class MovimentacaoNaoEncontradaException extends RecursoNaoEncontradoException {

    public MovimentacaoNaoEncontradaException(long id) {
        super("Movimentação não encontrada: id " + id);
    }
}
