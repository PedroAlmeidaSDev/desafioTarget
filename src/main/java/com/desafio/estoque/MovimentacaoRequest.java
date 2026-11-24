package com.desafio.estoque;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corpo do POST de movimentação. Fica separado de {@link Movimentacao} porque id, data e estoque
 * final são gerados pelo sistema, não pelo cliente. Os campos numéricos são {@code Integer} para
 * que um campo omitido caia no {@code @NotNull} em vez de virar 0.
 */
public record MovimentacaoRequest(
        @Schema(example = "101")
        @NotNull(message = "é obrigatório")
        Integer codigoProduto,

        @Schema(example = "SAIDA")
        @NotNull(message = "é obrigatório (ENTRADA ou SAIDA)")
        TipoMovimentacao tipo,

        @Schema(example = "10")
        @NotNull(message = "é obrigatório")
        @Positive(message = "deve ser maior que zero")
        Integer quantidade,

        @Schema(example = "Venda no balcão - pedido 1234")
        @NotBlank(message = "é obrigatória")
        @Size(max = 255, message = "deve ter no máximo 255 caracteres")
        String descricao) {
}
