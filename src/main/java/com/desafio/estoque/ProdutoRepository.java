package com.desafio.estoque;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import tools.jackson.databind.json.JsonMapper;

/**
 * Produtos em memória, indexados por código e carregados do JSON na subida da aplicação.
 * Ao reiniciar, o estoque volta ao conteúdo do arquivo.
 */
@Repository
public class ProdutoRepository {

    private static final Logger log = LoggerFactory.getLogger(ProdutoRepository.class);

    private final JsonMapper jsonMapper;
    private final Resource arquivo;
    private final Map<Integer, Produto> produtosPorCodigo = new ConcurrentHashMap<>();

    public ProdutoRepository(JsonMapper jsonMapper, @Value("${desafio.dados.estoque}") Resource arquivo) {
        this.jsonMapper = jsonMapper;
        this.arquivo = arquivo;
    }

    @PostConstruct
    void carregar() {
        ArquivoEstoque conteudo;
        try (InputStream entrada = arquivo.getInputStream()) {
            conteudo = jsonMapper.readValue(entrada, ArquivoEstoque.class);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo de estoque: " + arquivo.getDescription(), e);
        }
        if (conteudo == null || conteudo.estoque() == null) {
            throw new IllegalStateException("O arquivo não contém a chave \"estoque\": " + arquivo.getDescription());
        }

        produtosPorCodigo.clear();
        for (Produto produto : conteudo.estoque()) {
            if (produto.estoque() < 0) {
                throw new IllegalStateException("Produto com estoque inicial negativo no arquivo: " + produto);
            }
            if (produtosPorCodigo.putIfAbsent(produto.codigoProduto(), produto) != null) {
                throw new IllegalStateException("Código de produto duplicado no arquivo: " + produto.codigoProduto());
            }
        }
        log.info("{} produtos carregados de {}", produtosPorCodigo.size(), arquivo.getDescription());
    }

    public Optional<Produto> buscarPorCodigo(int codigoProduto) {
        return Optional.ofNullable(produtosPorCodigo.get(codigoProduto));
    }

    public List<Produto> listarTodos() {
        return produtosPorCodigo.values().stream()
                .sorted(Comparator.comparingInt(Produto::codigoProduto))
                .toList();
    }

    public void salvar(Produto produto) {
        produtosPorCodigo.put(produto.codigoProduto(), produto);
    }
}
