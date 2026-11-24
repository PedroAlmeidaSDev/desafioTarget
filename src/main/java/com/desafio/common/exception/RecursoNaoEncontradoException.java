package com.desafio.common.exception;

/**
 * Base das exceções de recurso inexistente; o {@code GlobalExceptionHandler} converte em HTTP 404.
 * As features estendem esta classe com exceções específicas, sem depender de HTTP.
 */
public abstract class RecursoNaoEncontradoException extends RuntimeException {

    protected RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
