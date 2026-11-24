package com.desafio.estoque;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Regra de negócio da questão 2: lançamento de movimentações de estoque.
 * O estoque nunca fica negativo: uma saída maior que o disponível é recusada (422) sem alterar nada.
 */
@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final Clock clock;

    public EstoqueService(ProdutoRepository produtoRepository,
                          MovimentacaoRepository movimentacaoRepository,
                          Clock clock) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.clock = clock;
    }

    /**
     * Lança a movimentação e devolve o registro com o estoque final.
     *
     * <p>{@code synchronized} para que duas saídas simultâneas não leiam o mesmo saldo e vendam a
     * mesma unidade duas vezes. Com banco, isso viraria lock da linha ({@code SELECT ... FOR UPDATE})
     * ou {@code @Version}.
     */
    public synchronized Movimentacao lancar(MovimentacaoRequest request) {
        int quantidade = request.quantidade();
        // O @Positive do request cobre a API; aqui protege chamadas que não passam pelo controller.
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }

        Produto produto = buscarProduto(request.codigoProduto());

        int estoqueAnterior = produto.estoque();
        int estoqueFinal;
        try {
            estoqueFinal = request.tipo().aplicar(estoqueAnterior, quantidade);
        } catch (ArithmeticException estouro) {
            // O Math.addExact da ENTRADA falha se o estoque passar do maior int: vira 422 em vez de 500.
            throw new EstoqueAcimaDoLimiteException(produto, quantidade);
        }
        if (estoqueFinal < 0) {
            throw new EstoqueInsuficienteException(produto, quantidade);
        }

        produtoRepository.salvar(produto.comEstoque(estoqueFinal));

        Movimentacao movimentacao = new Movimentacao(
                movimentacaoRepository.proximoId(),
                produto.codigoProduto(),
                produto.descricaoProduto(),
                request.tipo(),
                quantidade,
                request.descricao().strip(),
                estoqueAnterior,
                estoqueFinal,
                LocalDateTime.now(clock));
        return movimentacaoRepository.salvar(movimentacao);
    }

    public Produto buscarProduto(int codigoProduto) {
        return produtoRepository.buscarPorCodigo(codigoProduto)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(codigoProduto));
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.listarTodos();
    }

    public Movimentacao buscarMovimentacao(long id) {
        return movimentacaoRepository.buscarPorId(id)
                .orElseThrow(() -> new MovimentacaoNaoEncontradaException(id));
    }

    /**
     * Histórico completo, ou só de um produto quando o código é informado.
     * Filtrar por produto inexistente dá 404 em vez de lista vazia, para o erro não passar despercebido.
     */
    public List<Movimentacao> listarMovimentacoes(Integer codigoProduto) {
        if (codigoProduto == null) {
            return movimentacaoRepository.listarTodas();
        }
        buscarProduto(codigoProduto); // lança 404 se não existir
        return movimentacaoRepository.listarPorProduto(codigoProduto);
    }
}
