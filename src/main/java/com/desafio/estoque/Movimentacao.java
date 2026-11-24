package com.desafio.estoque;

import java.time.LocalDateTime;

/**
 * Movimentação já lançada; também é a resposta do POST. Guarda o estoque anterior e o final
 * para que cada registro possa ser conferido isoladamente.
 */
public record Movimentacao(
        long id,
        int codigoProduto,
        String descricaoProduto,
        TipoMovimentacao tipo,
        int quantidade,
        String descricao,
        int estoqueAnterior,
        int estoqueFinal,
        LocalDateTime dataHora) {
}
