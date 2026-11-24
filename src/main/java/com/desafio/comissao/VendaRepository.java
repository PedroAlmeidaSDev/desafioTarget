package com.desafio.comissao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import tools.jackson.databind.json.JsonMapper;

/**
 * Lê o JSON configurado em {@code desafio.dados.vendas} na subida da aplicação e mantém as vendas
 * em memória. Arquivo ausente ou inválido impede a aplicação de subir.
 */
@Repository
public class VendaRepository {

    private static final Logger log = LoggerFactory.getLogger(VendaRepository.class);

    private final List<Venda> vendas;

    public VendaRepository(JsonMapper jsonMapper, @Value("${desafio.dados.vendas}") Resource arquivo) {
        this.vendas = carregar(jsonMapper, arquivo);
        log.info("{} vendas carregadas de {}", vendas.size(), arquivo.getDescription());
    }

    private static List<Venda> carregar(JsonMapper jsonMapper, Resource arquivo) {
        try (InputStream entrada = arquivo.getInputStream()) {
            ArquivoVendas conteudo = jsonMapper.readValue(entrada, ArquivoVendas.class);
            if (conteudo == null || conteudo.vendas() == null) {
                throw new IllegalStateException("O arquivo não contém a chave \"vendas\": " + arquivo.getDescription());
            }
            return List.copyOf(conteudo.vendas());
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo de vendas: " + arquivo.getDescription(), e);
        }
    }

    public List<Venda> listarTodas() {
        return vendas;
    }
}
