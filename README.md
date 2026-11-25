# Desafio — Comissão, Estoque e Juros

API REST em Spring Boot com as três questões do desafio:

| # | Questão | Endpoint principal |
|---|---------|--------------------|
| 1 | **Comissão** por vendedor a partir de um JSON de vendas | `GET /api/comissoes` |
| 2 | **Estoque**: movimentações de entrada/saída com ID único, retornando o estoque final | `POST /api/estoque/movimentacoes` |
| 3 | **Juros** de 2,5% ao dia sobre um valor vencido, calculados na data de hoje | `GET /api/juros` |

## Resultados

Saídas reais da API com os arquivos do enunciado (`vendas.json` e `estoque.json`).

### 1. Comissão: `GET /api/comissoes`

| Vendedor | Vendas | Total vendido | Comissão |
|----------|-------:|--------------:|---------:|
| João Silva | 10 | R$ 10.754,70 | **R$ 495,68** |
| Maria Souza | 9 | R$ 9.874,30 | **R$ 465,95** |
| Ana Lima | 9 | R$ 8.763,95 | **R$ 404,98** |
| Carlos Oliveira | 8 | R$ 7.928,35 | **R$ 379,37** |
| **Total** | **36** | **R$ 37.321,30** | **R$ 1.745,98** |

O teste `ComissaoArquivoOficialTest` falha se algum desses valores mudar.

### 2. Estoque: `POST /api/estoque/movimentacoes`

Saída de 10 unidades da Caneta Azul (código 101, estoque inicial 150):

```json
{
  "id": 1,
  "codigoProduto": 101,
  "descricaoProduto": "Caneta Azul",
  "tipo": "SAIDA",
  "quantidade": 10,
  "descricao": "Venda no balcao",
  "estoqueAnterior": 150,
  "estoqueFinal": 140,
  "dataHora": "2026-11-25T16:52:04.917272"
}
```

### 3. Juros: `GET /api/juros?valor=1000.00&dataVencimento=...`

R$ 1.000,00 vencido há 10 dias:

```json
{
  "valorOriginal": 1000.00,
  "dataVencimento": "2026-11-15",
  "dataCalculo": "2026-11-25",
  "diasAtraso": 10,
  "taxaDiariaPercentual": 2.5,
  "juros": 250.00,
  "valorTotal": 1250.00
}
```

`dataCalculo` é sempre o dia de hoje, por isso a resposta muda conforme a data em que a API roda.

## Tecnologias

- Java 21 · Spring Boot 4.1 (Web MVC, Bean Validation) · Jackson 3
- springdoc-openapi 3 (Swagger UI)
- JUnit 5, AssertJ, MockMvc
- Maven (com Maven Wrapper) · Docker (opcional)

## Como rodar

Pré-requisito: **JDK 21**. O Maven não precisa estar instalado, porque o wrapper (`mvnw`) baixa a versão certa.

```bash
git clone https://github.com/PedroAlmeidaSDev/desafioTarget.git
cd desafioTarget
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Ou com Docker, sem precisar de Java instalado:

```bash
docker build -t desafio .
docker run --rm -p 8080:8080 desafio
```

Depois, abra **http://localhost:8080/swagger-ui.html**.

## Como testar

### Testes automatizados

```bash
./mvnw test
```

- **Unitários** (sem Spring, rápidos): fronteiras da comissão (99,99 / 100,00 / 499,99 / 500,00), arredondamento, entrada/saída/estoque insuficiente/entrada acima do limite/produto inexistente/IDs únicos, concorrência de saídas simultâneas e juros com data congelada (vencimento futuro, hoje, 1 e 10 dias de atraso).
- **Valores do enunciado** (`ComissaoArquivoOficialTest`): com o `vendas.json` oficial, confere João Silva 495,68 · Maria Souza 465,95 · Ana Lima 404,98 · Carlos Oliveira 379,37.
- **Integração** (`ApiIntegrationTest`): sobe a aplicação e valida status HTTP, formato de erro, validações e Swagger.

### Exemplos de requisição

```bash
# 1. Comissão por vendedor (dados do arquivo)
curl http://localhost:8080/api/comissoes

# 1b. Comissão de um JSON qualquer enviado no corpo
curl -X POST http://localhost:8080/api/comissoes/calcular -H "Content-Type: application/json" \
     -d '{"vendas":[{"vendedor":"Ana","valor":500.00},{"vendedor":"Ana","valor":99.99}]}'

# 2. Estoque
curl http://localhost:8080/api/estoque/produtos
curl -X POST http://localhost:8080/api/estoque/movimentacoes -H "Content-Type: application/json" \
     -d '{"codigoProduto":101,"tipo":"SAIDA","quantidade":10,"descricao":"Venda no balcao"}'
curl "http://localhost:8080/api/estoque/movimentacoes?codigoProduto=101"

# 3. Juros
curl "http://localhost:8080/api/juros?valor=1000.00&dataVencimento=2026-11-09"
```

> No Windows, o `curl` do terminal pode enviar acentos fora de UTF-8, e a API responde 400 (JSON inválido). Nesse caso, use o Swagger ou envie o corpo de um arquivo salvo em UTF-8 (`--data-binary @arquivo.json`).

### Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/comissoes` | Comissão por vendedor + totais gerais (arquivo `vendas.json`) |
| POST | `/api/comissoes/calcular` | Mesmo cálculo para um JSON enviado no corpo |
| GET | `/api/estoque/produtos` | Produtos com estoque atual |
| GET | `/api/estoque/produtos/{codigo}` | Um produto |
| POST | `/api/estoque/movimentacoes` | Lança entrada/saída → **201** + `Location` |
| GET | `/api/estoque/movimentacoes?codigoProduto=` | Histórico (filtro opcional) |
| GET | `/api/estoque/movimentacoes/{id}` | Uma movimentação |
| GET | `/api/juros?valor=&dataVencimento=` | Juros e valor atualizado na data de hoje |

### Formato padrão de erro

```json
{
  "timestamp": "2026-11-19T10:15:30-03:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Requisição com campos inválidos",
  "caminho": "/api/estoque/movimentacoes",
  "campos": [ { "campo": "quantidade", "mensagem": "deve ser maior que zero" } ]
}
```

| Situação | Status |
|----------|--------|
| Campo inválido/ausente, JSON malformado, tipo errado (ex.: `"quantidade": 1.5`) | 400 |
| Produto ou movimentação inexistente | 404 |
| Resposta pedida em formato que não é JSON (`Accept`) | 406 |
| Corpo enviado em formato que não é JSON (`Content-Type`) | 415 |
| Saída maior que o estoque disponível, ou entrada que passaria do limite do estoque | 422 |
| Erro inesperado (vai inteiro para o log, sem stack trace na resposta) | 500 |

## Decisões e premissas

- **Organização por feature** (`comissao/`, `estoque/`, `juros/`, `common/`): as três questões são independentes. Cada pasta tem tudo de uma solução, e `common/` guarda só a infraestrutura compartilhada (erros, Clock, Swagger).
- **Dinheiro em `BigDecimal`**, nunca `double`, comparado com `compareTo`. A comissão é calculada sem arredondar em cada venda e arredondada para 2 casas (`HALF_UP`) **só no total por vendedor**, para não acumular erro de centavos.
- **Faixas de comissão em um enum** (`FaixaComissao`), com limites exclusivos: exatamente R$ 100,00 já paga 1%, e exatamente R$ 500,00 já paga 5%.
- **Estoque nunca fica negativo**: uma saída maior que o disponível é recusada com 422 e não altera nada. Quantidade deve ser um inteiro > 0 (`1.5` é recusado com 400), e o sentido vem do tipo (`ENTRADA`/`SAIDA`). Uma entrada que passaria do limite do `int` também é recusada com 422, em vez de estourar.
- **ID de movimentação** sequencial e thread-safe (`AtomicLong`). O lançamento é `synchronized`, para que duas saídas simultâneas não vendam o mesmo item duas vezes (há teste de concorrência).
- **Armazenamento em memória**: produtos e histórico são carregados do JSON na subida e voltam ao estado original ao reiniciar.
- **Juros — interpretação adotada**:
  - juros **simples** de 2,5% do valor original **por dia corrido** de atraso: `juros = valor × 0,025 × dias`;
  - "juros" e "multa" do enunciado são a mesma cobrança diária, sem multa fixa adicional;
  - vencimento hoje ou no futuro → juros zero;
  - "hoje" é a data em `America/Sao_Paulo` (configurável em `desafio.fuso-horario`).
  - Exemplo: R$ 1.000,00 com 10 dias de atraso → R$ 250,00 de juros → R$ 1.250,00.
- **`Clock` injetado** no lugar de `LocalDate.now()`, para os testes congelarem a data.
- **Juros via `GET` com query params**: é uma consulta sem efeito colateral, idempotente e com só dois parâmetros simples.
- **Caminhos dos arquivos de dados configuráveis** (`desafio.dados.vendas` / `desafio.dados.estoque`).

## Melhorias futuras

- Persistência em banco (H2/PostgreSQL com Spring Data JPA) e controle de concorrência por linha (`@Version` ou `SELECT ... FOR UPDATE`) no lugar do `synchronized`.
- Paginação no histórico de movimentações.
- Front-end simples (React) consumindo os três endpoints.
- Formato de erro padrão RFC 9457 (`ProblemDetail`).
- Pipeline de CI (GitHub Actions) rodando `mvnw test` a cada push.
