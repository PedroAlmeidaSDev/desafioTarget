package com.desafio.estoque;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

/**
 * Histórico de movimentações em memória. Os IDs são sequenciais e gerados de forma atômica,
 * então não se repetem mesmo com requisições simultâneas.
 */
@Repository
public class MovimentacaoRepository {

    private final AtomicLong sequencia = new AtomicLong();
    private final List<Movimentacao> movimentacoes = new CopyOnWriteArrayList<>();

    public long proximoId() {
        return sequencia.incrementAndGet();
    }

    public Movimentacao salvar(Movimentacao movimentacao) {
        movimentacoes.add(movimentacao);
        return movimentacao;
    }

    public Optional<Movimentacao> buscarPorId(long id) {
        return movimentacoes.stream().filter(m -> m.id() == id).findFirst();
    }

    public List<Movimentacao> listarTodas() {
        return List.copyOf(movimentacoes);
    }

    public List<Movimentacao> listarPorProduto(int codigoProduto) {
        return movimentacoes.stream().filter(m -> m.codigoProduto() == codigoProduto).toList();
    }
}
