package com.desafio;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testes de integração: sobem a aplicação e validam status HTTP, formato de erro, validações e Swagger.
 *
 * <p>O contexto do Spring é compartilhado entre os testes, e com ele o estoque em memória. Por isso
 * só se verifica o que não depende da ordem de execução.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(RelogioFixoConfig.class)
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Nested
    class Comissao {

        @Test
        void calculaDoArquivo() throws Exception {
            mvc.perform(get("/api/comissoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vendedores.length()").value(4))
                    .andExpect(jsonPath("$.vendedores[0].totalComissao").exists())
                    .andExpect(jsonPath("$.totalComissao").exists());
        }

        @Test
        void calculaDoCorpo() throws Exception {
            mvc.perform(post("/api/comissoes/calcular").contentType(MediaType.APPLICATION_JSON).content("""
                            { "vendas": [ { "vendedor": "Ana", "valor": 500.00 }, { "vendedor": "Ana", "valor": 99.99 } ] }
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vendedores[0].vendedor").value("Ana"))
                    .andExpect(jsonPath("$.vendedores[0].totalComissao").value(25.00));
        }

        @Test
        void validaCadaVendaDoCorpo() throws Exception {
            mvc.perform(post("/api/comissoes/calcular").contentType(MediaType.APPLICATION_JSON).content("""
                            { "vendas": [ { "vendedor": "", "valor": -1 } ] }
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos[*].campo", hasItem("vendas[0].valor")))
                    .andExpect(jsonPath("$.campos[*].campo", hasItem("vendas[0].vendedor")));
        }
    }

    @Nested
    class Estoque {

        @Test
        void lancaMovimentacaoERetornaCreatedComLocation() throws Exception {
            String location = mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 101, "tipo": "ENTRADA", "quantidade": 5, "descricao": "Compra de fornecedor" }
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/estoque/movimentacoes/")))
                    .andExpect(jsonPath("$.id").value(greaterThan(0)))
                    .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                    .andExpect(jsonPath("$.estoqueFinal").isNumber())
                    .andReturn().getResponse().getHeader("Location");

            mvc.perform(get(location))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descricao").value("Compra de fornecedor"));
        }

        @Test
        void produtoInexistenteRetorna404Padronizado() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 999999, "tipo": "ENTRADA", "quantidade": 1, "descricao": "x" }
                            """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.erro").value("Not Found"))
                    .andExpect(jsonPath("$.mensagem").value(containsString("999999")))
                    .andExpect(jsonPath("$.caminho").value("/api/estoque/movimentacoes"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.campos").doesNotExist());
        }

        @Test
        void estoqueInsuficienteRetorna422() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 102, "tipo": "SAIDA", "quantidade": 1000000, "descricao": "Venda" }
                            """))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.mensagem").value(containsString("Estoque insuficiente")));
        }

        @Test
        void entradaAcimaDoLimiteRetorna422() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 104, "tipo": "ENTRADA", "quantidade": 2147483647, "descricao": "Compra" }
                            """))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.mensagem").value(containsString("acima do limite")));
        }

        @Test
        void quantidadeComCasasDecimaisRetorna400() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 101, "tipo": "ENTRADA", "quantidade": 1.5, "descricao": "x" }
                            """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void camposInvalidosRetornam400ComDetalhes() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 101, "quantidade": 0, "descricao": "  " }
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.length()").value(3))
                    .andExpect(jsonPath("$.campos[*].campo", hasItem("tipo")))
                    .andExpect(jsonPath("$.campos[*].campo", hasItem("quantidade")))
                    .andExpect(jsonPath("$.campos[*].campo", hasItem("descricao")));
        }

        @Test
        void tipoInexistenteRetorna400() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.APPLICATION_JSON).content("""
                            { "codigoProduto": 101, "tipo": "TROCA", "quantidade": 1, "descricao": "x" }
                            """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void codigoNaoNumericoNaUrlRetorna400() throws Exception {
            mvc.perform(get("/api/estoque/produtos/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(containsString("codigoProduto")));
        }

        @Test
        void filtraHistoricoPorProdutoInexistenteRetorna404() throws Exception {
            mvc.perform(get("/api/estoque/movimentacoes").param("codigoProduto", "999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Juros {

        @Test
        void calculaComOHojeDoClock() throws Exception {
            mvc.perform(get("/api/juros").param("valor", "1000.00").param("dataVencimento", "2026-11-09"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataCalculo").value("2026-11-19"))
                    .andExpect(jsonPath("$.diasAtraso").value(10))
                    .andExpect(jsonPath("$.juros").value(250.00))
                    .andExpect(jsonPath("$.valorTotal").value(1250.00));
        }

        @Test
        void valorNegativoRetorna400ComOCampo() throws Exception {
            mvc.perform(get("/api/juros").param("valor", "-5").param("dataVencimento", "2026-11-01"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos[0].campo").value("valor"))
                    .andExpect(jsonPath("$.campos[0].mensagem").value("deve ser maior que zero"));
        }

        // Data que não converte: só o erro de conversão é devolvido, as demais validações não rodam.
        @Test
        void dataImpossivelRetorna400ComOCampo() throws Exception {
            mvc.perform(get("/api/juros").param("valor", "100").param("dataVencimento", "2026-13-40"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos[0].campo").value("dataVencimento"))
                    .andExpect(jsonPath("$.campos[0].mensagem").value("valor inválido: '2026-13-40'"));
        }

        @Test
        void parametrosAusentesRetornam400() throws Exception {
            mvc.perform(get("/api/juros"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Infraestrutura {

        @Test
        void documentacaoOpenApiEstaDisponivel() throws Exception {
            mvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paths['/api/juros']").exists());
        }

        @Test
        void rotaInexistenteRetorna404EnaoErro500() throws Exception {
            mvc.perform(get("/api/nao-existe"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void corpoQueNaoEhJsonRetorna415() throws Exception {
            mvc.perform(post("/api/estoque/movimentacoes").contentType(MediaType.TEXT_PLAIN).content("oi"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status").value(415));
        }

        @Test
        void respostaEmFormatoQueNaoEhJsonRetorna406() throws Exception {
            mvc.perform(get("/api/juros").param("valor", "100").param("dataVencimento", "2026-11-01")
                            .accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isNotAcceptable());
        }
    }
}
