package com.desafio.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato único de erro devolvido por TODOS os endpoints.
 *
 * <pre>
 * {
 *   "timestamp": "2026-11-19T10:15:30-03:00",
 *   "status": 400,
 *   "erro": "Bad Request",
 *   "mensagem": "Requisição com campos inválidos",
 *   "caminho": "/api/estoque/movimentacoes",
 *   "campos": [ { "campo": "quantidade", "mensagem": "deve ser maior que 0" } ]
 * }
 * </pre>
 *
 * <p>{@code @JsonInclude(NON_EMPTY)} omite "campos" quando não há erros de validação.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos) {

    /** Um campo que falhou na validação e o motivo. */
    public record CampoInvalido(String campo, String mensagem) {
    }
}
