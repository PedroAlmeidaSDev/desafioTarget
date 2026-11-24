package com.desafio.estoque;

import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Endpoints da questão 2. O lançamento responde 201 com {@code Location} para
 * {@code /api/estoque/movimentacoes/{id}}; os erros são tratados no GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/estoque")
@Tag(name = "2. Estoque", description = "Movimentações de entrada e saída sobre os produtos do JSON de estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping("/produtos")
    @Operation(summary = "Lista os produtos com o estoque atual")
    public List<Produto> listarProdutos() {
        return estoqueService.listarProdutos();
    }

    @GetMapping("/produtos/{codigoProduto}")
    @Operation(summary = "Busca um produto pelo código")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public Produto buscarProduto(@PathVariable int codigoProduto) {
        return estoqueService.buscarProduto(codigoProduto);
    }

    @PostMapping("/movimentacoes")
    @Operation(summary = "Lança uma movimentação de entrada ou saída",
            description = "Retorna a movimentação registrada, com ID único e o estoque final do produto.")
    @ApiResponse(responseCode = "201", description = "Movimentação lançada")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @ApiResponse(responseCode = "422", description = "Estoque insuficiente para a saída, ou entrada acima do limite")
    public ResponseEntity<Movimentacao> lancar(@Valid @RequestBody MovimentacaoRequest request) {
        Movimentacao movimentacao = estoqueService.lancar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(movimentacao.id())
                .toUri();
        return ResponseEntity.created(location).body(movimentacao);
    }

    @GetMapping("/movimentacoes")
    @Operation(summary = "Histórico de movimentações (opcionalmente de um produto)")
    public List<Movimentacao> listarMovimentacoes(
            @Parameter(description = "Filtra pelo código do produto")
            @RequestParam(required = false) Integer codigoProduto) {
        return estoqueService.listarMovimentacoes(codigoProduto);
    }

    @GetMapping("/movimentacoes/{id}")
    @Operation(summary = "Busca uma movimentação pelo ID")
    @ApiResponse(responseCode = "404", description = "Movimentação não encontrada")
    public Movimentacao buscarMovimentacao(@PathVariable long id) {
        return estoqueService.buscarMovimentacao(id);
    }
}
