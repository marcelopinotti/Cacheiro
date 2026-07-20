package com.dev.cacheiro.vitrine.produto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;


@RestControllerAdvice
@Slf4j
public class GlobalHandlerException {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ProblemDetail naoEncontrado() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Produto não encontrado.");
    }


    @ExceptionHandler({ResourceAccessException.class, HttpServerErrorException.class})
    public ProblemDetail catalogoIndisponivel(Exception e) {
        log.warn("Catálogo indisponível: {}", e.toString());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Catálogo indisponível. Tente novamente em instantes.");
    }
}
