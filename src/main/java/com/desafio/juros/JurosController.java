package com.desafio.juros;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint da questão 3. Os parâmetros chegam como query params e são agrupados em {@link JurosRequest}.
 */
@RestController
@RequestMapping("/api/juros")
@Tag(name = "3. Juros", description = "Juros simples de 2,5% ao dia sobre um valor vencido")
public class JurosController {

    private final JurosService jurosService;

    public JurosController(JurosService jurosService) {
        this.jurosService = jurosService;
    }

    @GetMapping
    @Operation(summary = "Calcula os juros na data de hoje",
            description = "juros = valor × 2,5% × dias de atraso (juros simples, dias corridos). "
                    + "Vencimento hoje ou futuro → juros zero.")
    public JurosResponse calcular(@Valid @ParameterObject JurosRequest request) {
        return jurosService.calcular(request.valor(), request.dataVencimento());
    }
}
