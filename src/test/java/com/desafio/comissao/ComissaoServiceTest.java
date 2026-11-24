package com.desafio.comissao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ComissaoServiceTest {

    private final ComissaoService service = new ComissaoService(null);

    @Nested
    @DisplayName("Comissão de uma venda")
    class ComissaoDeUmaVenda {

        @ParameterizedTest(name = "venda de {0} → comissão {1}")
        @CsvSource({
                "0.00,    0.00",   // menor valor possível
                "99.99,   0.00",   // fronteira: último valor sem comissão
                "100.00,  1.00",   // fronteira: exatamente 100 já é 1%
                "250.00,  2.50",
                "499.99,  4.9999", // fronteira: último valor a 1% (sem arredondar!)
                "500.00,  25.00",  // fronteira: exatamente 500 já é 5%
                "1200.50, 60.025"
        })
        void aplicaAFaixaCorreta(String valor, String comissaoEsperada) {
            assertThat(service.calcularComissao(new BigDecimal(valor)))
                    .isEqualByComparingTo(comissaoEsperada);
        }

        @Test
        void rejeitaValorNegativoOuNulo() {
            assertThatThrownBy(() -> service.calcularComissao(new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> service.calcularComissao(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Totais por vendedor")
    class TotaisPorVendedor {

        @Test
        void agrupaSomaEOrdenaPelaMaiorComissao() {
            List<Venda> vendas = List.of(
                    new Venda("Ana", new BigDecimal("50.00")),    // 0
                    new Venda("Bruno", new BigDecimal("600.00")), // 30.00
                    new Venda("Ana", new BigDecimal("200.00")),   // 2.00
                    new Venda("Ana", new BigDecimal("500.00")));  // 25.00

            List<ComissaoVendedorResponse> resultado = service.calcularPorVendedor(vendas);

            assertThat(resultado).hasSize(2);

            ComissaoVendedorResponse bruno = resultado.get(0);
            assertThat(bruno.vendedor()).isEqualTo("Bruno");
            assertThat(bruno.quantidadeVendas()).isEqualTo(1);
            assertThat(bruno.totalComissao()).isEqualByComparingTo("30.00");

            ComissaoVendedorResponse ana = resultado.get(1);
            assertThat(ana.vendedor()).isEqualTo("Ana");
            assertThat(ana.quantidadeVendas()).isEqualTo(3);
            assertThat(ana.totalVendas()).isEqualByComparingTo("750.00");
            assertThat(ana.totalComissao()).isEqualByComparingTo("27.00");
        }

        @Test
        @DisplayName("arredonda só o total, não cada venda")
        void arredondaApenasNoFinal() {
            // Cada venda: 100,50 × 1% = 1,005. Arredondando cada uma: 1,01 + 1,01 = 2,02 (errado).
            // Somando antes de arredondar: 2,010 → 2,01 (= 1% de 201,00, correto).
            List<Venda> vendas = List.of(
                    new Venda("Ana", new BigDecimal("100.50")),
                    new Venda("Ana", new BigDecimal("100.50")));

            ComissaoVendedorResponse ana = service.calcularPorVendedor(vendas).get(0);

            assertThat(ana.totalComissao()).isEqualTo(new BigDecimal("2.01")); // isEqualTo também confere a escala
        }

        @Test
        void relatorioTrazTotaisGerais() {
            List<Venda> vendas = List.of(
                    new Venda("Ana", new BigDecimal("500.00")),
                    new Venda("Bruno", new BigDecimal("100.00")));

            RelatorioComissaoResponse relatorio = service.gerarRelatorio(vendas);

            assertThat(relatorio.quantidadeVendas()).isEqualTo(2);
            assertThat(relatorio.totalVendas()).isEqualByComparingTo("600.00");
            assertThat(relatorio.totalComissao()).isEqualByComparingTo("26.00");
        }

        @Test
        void listaVaziaGeraRelatorioZerado() {
            RelatorioComissaoResponse relatorio = service.gerarRelatorio(List.of());

            assertThat(relatorio.vendedores()).isEmpty();
            assertThat(relatorio.totalComissao()).isEqualByComparingTo("0");
        }
    }
}
