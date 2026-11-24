package com.desafio.comissao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comissoes")
@Tag(name = "1. Comissão", description = "Comissão por vendedor: < R$100 → 0%; < R$500 → 1%; ≥ R$500 → 5%")
public class ComissaoController {

    private final ComissaoService comissaoService;

    public ComissaoController(ComissaoService comissaoService) {
        this.comissaoService = comissaoService;
    }

    @GetMapping
    @Operation(summary = "Comissão por vendedor a partir do JSON de vendas da aplicação",
            description = "Ordenado da maior para a menor comissão. Valores arredondados em 2 casas (HALF_UP) só no total.")
    public RelatorioComissaoResponse calcularDoArquivo() {
        return comissaoService.gerarRelatorio();
    }

    // Permite aplicar a mesma regra a qualquer JSON no formato do arquivo. É POST por receber
    // um corpo, mas não altera estado.
    @PostMapping("/calcular")
    @Operation(summary = "Calcula a comissão de um JSON de vendas enviado no corpo",
            description = "Mesmo formato do arquivo: { \"vendas\": [ { \"vendedor\": \"...\", \"valor\": 0.00 } ] }")
    public RelatorioComissaoResponse calcularDoCorpo(@Valid @RequestBody ArquivoVendas arquivo) {
        return comissaoService.gerarRelatorio(arquivo.vendas());
    }
}
