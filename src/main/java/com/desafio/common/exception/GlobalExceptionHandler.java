package com.desafio.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converte as exceções da API em respostas HTTP no formato {@link ErroResponse}, sem expor stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------- erros de negócio (nossos)

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), req, null);
    }

    // ---------------------------------------------------------------- erros de entrada (400)

    /**
     * Bean Validation falhou em um objeto anotado com {@code @Valid} (body JSON ou query params
     * agrupados num record). Também cobre campos que não puderam ser convertidos (ex.: data "2026-13-40").
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::paraCampoInvalido)
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Requisição com campos inválidos", req, campos);
    }

    /** Validação de parâmetros soltos do método (ex.: {@code @PathVariable @Positive}). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> validacaoParametros(HandlerMethodValidationException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ErroResponse.CampoInvalido(
                                resultado.getMethodParameter().getParameterName(),
                                erro.getDefaultMessage())))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Requisição com parâmetros inválidos", req, campos);
    }

    /** JSON malformado, tipo errado no body ou valor de enum inexistente (ex.: "tipo": "TROCA"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou malformado. Confira o JSON e os valores permitidos.", req, null);
    }

    /** Path variable ou query param com tipo errado (ex.: /produtos/abc quando se espera número). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String mensagem = "Valor inválido para '%s': '%s'".formatted(ex.getName(), ex.getValue());
        return responder(HttpStatus.BAD_REQUEST, mensagem, req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String mensagem = "Parâmetro obrigatório ausente: '%s'".formatted(ex.getParameterName());
        return responder(HttpStatus.BAD_REQUEST, mensagem, req, null);
    }

    // ---------------------------------------------------------------- erros de rota e de formato

    // Erros que o próprio Spring lança antes de chegar ao controller. Sem estes handlers, eles cairiam
    // no handler genérico (Exception) e virariam 500, como se a falha fosse do servidor.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req, null);
    }

    /** Corpo enviado em formato que não é JSON (ex.: {@code Content-Type: text/plain}). */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> formatoNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type não suportado. Envie o corpo como application/json.", req, null);
    }

    /**
     * O cliente só aceita uma resposta que a API não gera (ex.: {@code Accept: application/xml}).
     * Vai sem corpo, porque o erro em JSON também seria recusado.
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Void> formatoNaoAceito() {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    // ---------------------------------------------------------------- rede de segurança (500)

    /**
     * Erro não previsto: vai inteiro para o log, com stack trace; o cliente recebe só uma mensagem genérica.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", req, null);
    }

    // ---------------------------------------------------------------- auxiliares

    private static ErroResponse.CampoInvalido paraCampoInvalido(FieldError erro) {
        // Em falha de conversão, troca a mensagem técnica do Spring ("Failed to convert...") por uma legível.
        String mensagem = erro.isBindingFailure()
                ? "valor inválido: '%s'".formatted(erro.getRejectedValue())
                : erro.getDefaultMessage();
        return new ErroResponse.CampoInvalido(erro.getField(), mensagem);
    }

    private static ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem,
                                                          HttpServletRequest req,
                                                          List<ErroResponse.CampoInvalido> campos) {
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
