package com.desafio.comissao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Comissão por vendedor (questão 1).
 *
 * <p>A comissão de cada venda é calculada sem arredondamento e só o total de cada vendedor é
 * arredondado para 2 casas, para não acumular erro de centavos entre as vendas.
 */
@Service
public class ComissaoService {

    private static final int CASAS_DECIMAIS = 2;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    private final VendaRepository vendaRepository;

    public ComissaoService(VendaRepository vendaRepository) {
        this.vendaRepository = vendaRepository;
    }

    /** Relatório usando as vendas do arquivo JSON da aplicação. */
    public RelatorioComissaoResponse gerarRelatorio() {
        return gerarRelatorio(vendaRepository.listarTodas());
    }

    /** Relatório para uma lista qualquer de vendas. */
    public RelatorioComissaoResponse gerarRelatorio(List<Venda> vendas) {
        List<ComissaoVendedorResponse> porVendedor = calcularPorVendedor(vendas);

        // Totais gerais somam os totais já arredondados, para que a soma das linhas bata com o total.
        BigDecimal totalVendas = somar(porVendedor.stream().map(ComissaoVendedorResponse::totalVendas).toList());
        BigDecimal totalComissao = somar(porVendedor.stream().map(ComissaoVendedorResponse::totalComissao).toList());
        long quantidade = porVendedor.stream().mapToLong(ComissaoVendedorResponse::quantidadeVendas).sum();

        return new RelatorioComissaoResponse(porVendedor, quantidade, totalVendas, totalComissao);
    }

    /**
     * Totais por vendedor, da maior para a menor comissão. Em caso de empate, prevalece a ordem
     * em que o vendedor aparece nas vendas.
     */
    public List<ComissaoVendedorResponse> calcularPorVendedor(List<Venda> vendas) {
        Map<String, List<Venda>> vendasPorVendedor = vendas.stream()
                .collect(Collectors.groupingBy(Venda::vendedor, LinkedHashMap::new, Collectors.toList()));

        return vendasPorVendedor.entrySet().stream()
                .map(entrada -> resumirVendedor(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparing(ComissaoVendedorResponse::totalComissao).reversed())
                .toList();
    }

    /** Comissão de uma venda, sem arredondamento. */
    public BigDecimal calcularComissao(BigDecimal valorVenda) {
        if (valorVenda == null || valorVenda.signum() < 0) {
            throw new IllegalArgumentException("Valor da venda inválido: " + valorVenda);
        }
        return FaixaComissao.paraValor(valorVenda).calcular(valorVenda);
    }

    private ComissaoVendedorResponse resumirVendedor(String vendedor, List<Venda> vendasDoVendedor) {
        BigDecimal totalVendas = somar(vendasDoVendedor.stream().map(Venda::valor).toList());
        BigDecimal totalComissao = somar(vendasDoVendedor.stream().map(v -> calcularComissao(v.valor())).toList());

        return new ComissaoVendedorResponse(
                vendedor,
                vendasDoVendedor.size(),
                arredondar(totalVendas),
                arredondar(totalComissao));
    }

    private static BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(CASAS_DECIMAIS, ARREDONDAMENTO);
    }
}
