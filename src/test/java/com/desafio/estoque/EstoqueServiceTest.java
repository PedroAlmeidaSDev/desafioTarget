package com.desafio.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import tools.jackson.databind.json.JsonMapper;

/**
 * Testes unitários do estoque, sem Spring. Usa um JSON próprio para não depender do arquivo da
 * aplicação, e cada teste começa com o estoque recarregado.
 */
class EstoqueServiceTest {

    private static final String JSON = """
            { "estoque": [
                { "codigoProduto": 101, "descricaoProduto": "Caneta", "estoque": 10 },
                { "codigoProduto": 102, "descricaoProduto": "Caderno", "estoque": 0 }
            ] }
            """;

    private static final Clock RELOGIO_FIXO =
            Clock.fixed(Instant.parse("2026-11-19T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private ProdutoRepository produtoRepository;
    private EstoqueService service;

    @BeforeEach
    void setUp() {
        produtoRepository = new ProdutoRepository(
                JsonMapper.builder().build(), new ByteArrayResource(JSON.getBytes(StandardCharsets.UTF_8)));
        produtoRepository.carregar(); // fora do Spring, o @PostConstruct não roda
        service = new EstoqueService(produtoRepository, new MovimentacaoRepository(), RELOGIO_FIXO);
    }

    @Test
    void entradaSomaAoEstoque() {
        Movimentacao mov = service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.ENTRADA, 5, "Compra"));

        assertThat(mov.estoqueAnterior()).isEqualTo(10);
        assertThat(mov.estoqueFinal()).isEqualTo(15);
        assertThat(service.buscarProduto(101).estoque()).isEqualTo(15);
        assertThat(mov.dataHora()).hasToString("2026-11-19T10:00"); // 13h UTC = 10h em São Paulo
    }

    @Test
    void saidaSubtraiDoEstoque() {
        Movimentacao mov = service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.SAIDA, 3, "Venda"));

        assertThat(mov.estoqueFinal()).isEqualTo(7);
        assertThat(service.buscarProduto(101).estoque()).isEqualTo(7);
    }

    @Test
    void saidaDeTodoOEstoqueZeraSemErro() {
        Movimentacao mov = service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.SAIDA, 10, "Venda total"));

        assertThat(mov.estoqueFinal()).isZero();
    }

    @Test
    void saidaMaiorQueOEstoqueEhRecusadaENaoAlteraNada() {
        assertThatThrownBy(() -> service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.SAIDA, 11, "Venda")))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("disponível 10, solicitado 11");

        // A tentativa recusada não pode alterar o estoque nem o histórico.
        assertThat(service.buscarProduto(101).estoque()).isEqualTo(10);
        assertThat(service.listarMovimentacoes(null)).isEmpty();
    }

    @Test
    void saidaDeProdutoSemEstoqueEhRecusada() {
        assertThatThrownBy(() -> service.lancar(new MovimentacaoRequest(102, TipoMovimentacao.SAIDA, 1, "Venda")))
                .isInstanceOf(EstoqueInsuficienteException.class);
    }

    @Test
    void entradaQuePassariaDoLimiteEhRecusadaENaoAlteraNada() {
        assertThatThrownBy(() -> service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.ENTRADA, Integer.MAX_VALUE, "Compra")))
                .isInstanceOf(EstoqueAcimaDoLimiteException.class)
                .hasMessageContaining("disponível 10");

        assertThat(service.buscarProduto(101).estoque()).isEqualTo(10);
        assertThat(service.listarMovimentacoes(null)).isEmpty();
    }

    @Test
    void produtoInexistenteLancaExcecao() {
        assertThatThrownBy(() -> service.lancar(new MovimentacaoRequest(999, TipoMovimentacao.ENTRADA, 1, "Compra")))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    void quantidadeZeroOuNegativaEhRecusadaMesmoSemPassarPeloController() {
        assertThatThrownBy(() -> service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.ENTRADA, 0, "x")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.SAIDA, -5, "x")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void idsSaoUnicosESequenciaisEHistoricoFiltraPorProduto() {
        Movimentacao m1 = service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.ENTRADA, 1, "a"));
        Movimentacao m2 = service.lancar(new MovimentacaoRequest(102, TipoMovimentacao.ENTRADA, 1, "b"));
        Movimentacao m3 = service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.SAIDA, 1, "c"));

        assertThat(List.of(m1.id(), m2.id(), m3.id())).containsExactly(1L, 2L, 3L);
        assertThat(service.listarMovimentacoes(101)).extracting(Movimentacao::id).containsExactly(1L, 3L);
        assertThat(service.buscarMovimentacao(2).descricao()).isEqualTo("b");
    }

    /**
     * 100 saídas simultâneas de 1 unidade sobre um produto com 10: só 10 podem ser aceitas,
     * o estoque termina em 0 e nenhum ID se repete.
     */
    @Test
    void saidasSimultaneasNuncaDeixamEstoqueNegativo() throws Exception {
        int tentativas = 100;
        AtomicInteger sucessos = new AtomicInteger();
        Set<Long> ids = ConcurrentHashMap.newKeySet();

        try (ExecutorService executor = Executors.newFixedThreadPool(16)) {
            List<Future<?>> futuros = new java.util.ArrayList<>();
            for (int i = 0; i < tentativas; i++) {
                futuros.add(executor.submit(() -> {
                    try {
                        ids.add(service.lancar(new MovimentacaoRequest(101, TipoMovimentacao.SAIDA, 1, "concorrente")).id());
                        sucessos.incrementAndGet();
                    } catch (EstoqueInsuficienteException esperado) {
                        // esperado depois que o estoque zera
                    }
                }));
            }
            for (Future<?> futuro : futuros) {
                futuro.get(); // propaga erros inesperados das threads
            }
        }

        assertThat(sucessos).hasValue(10);
        assertThat(ids).hasSize(10);
        assertThat(service.buscarProduto(101).estoque()).isZero();
    }
}
