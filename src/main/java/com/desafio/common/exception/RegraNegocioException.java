package com.desafio.common.exception;

/**
 * Base para "a requisição é válida, mas viola uma regra de negócio" (ex.: saída maior que o
 * estoque). O {@code GlobalExceptionHandler} converte em HTTP 422 (Unprocessable Content).
 *
 * <p>Diferente do 400, que é para requisição malformada e barrada pela validação antes do service.
 */
public abstract class RegraNegocioException extends RuntimeException {

    protected RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
