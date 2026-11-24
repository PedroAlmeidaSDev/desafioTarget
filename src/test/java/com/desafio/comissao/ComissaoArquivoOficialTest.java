package com.desafio.comissao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.databind.json.JsonMapper;

/** Confere as comissões do vendas.json oficial (o da aplicação) com os valores esperados no enunciado. */
class ComissaoArquivoOficialTest {

    @Test
    void comissoesBatemComOEnunciado() {
        VendaRepository repository = new VendaRepository(
                JsonMapper.builder().build(), new ClassPathResource("dados/vendas.json"));
        ComissaoService service = new ComissaoService(repository);

        Map<String, String> comissaoPorVendedor = service.gerarRelatorio().vendedores().stream()
                .collect(Collectors.toMap(
                        ComissaoVendedorResponse::vendedor,
                        v -> v.totalComissao().toPlainString()));

        assertThat(comissaoPorVendedor).containsExactlyInAnyOrderEntriesOf(Map.of(
                "João Silva", "495.68",
                "Maria Souza", "465.95",
                "Ana Lima", "404.98",
                "Carlos Oliveira", "379.37"));
    }
}
