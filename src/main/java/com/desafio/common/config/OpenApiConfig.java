package com.desafio.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados exibidos no Swagger UI (/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Desafio — Comissão, Estoque e Juros")
                .version("1.0.0")
                .description("""
                        API com a solução das três questões do desafio:
                        1. **Comissão** por vendedor a partir do JSON de vendas;
                        2. **Estoque**: lançamento de movimentações (entrada/saída) com ID único;
                        3. **Juros** de 2,5% ao dia sobre um valor vencido.
                        """));
    }
}
