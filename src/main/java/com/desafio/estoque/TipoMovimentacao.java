package com.desafio.estoque;

/**
 * Tipo da movimentação. Cada constante implementa {@link #aplicar(int, int)}, então o service não
 * precisa de if/else por tipo e um tipo novo é obrigado a definir sua regra. As operações
 * {@code Math.*Exact} falham em overflow em vez de estourar silenciosamente.
 */
public enum TipoMovimentacao {

    ENTRADA {
        @Override
        public int aplicar(int estoqueAtual, int quantidade) {
            return Math.addExact(estoqueAtual, quantidade);
        }
    },

    SAIDA {
        @Override
        public int aplicar(int estoqueAtual, int quantidade) {
            return Math.subtractExact(estoqueAtual, quantidade);
        }
    };

    /** Calcula o novo estoque. A validação de saldo negativo fica no service. */
    public abstract int aplicar(int estoqueAtual, int quantidade);
}
