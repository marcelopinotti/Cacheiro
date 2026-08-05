package com.dev.cacheiro.vitrine.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Detalha um produto da vitrine",
        description = "Serve do cache Redis (TTL 45s). No miss, um lock NX de 5s garante que só uma "
                + "requisição vá ao catálogo (anti-stampede); as demais esperam e releem o cache, "
                + "com fallback para chamada direta.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Produto retornado (do cache ou do catálogo)"),
                @ApiResponse(responseCode = "404", description = "Produto não encontrado no catálogo"),
                @ApiResponse(responseCode = "429", description = "Limite de 100 req/min por IP excedido"),
                @ApiResponse(responseCode = "503", description = "Catálogo indisponível")
        })
public @interface ApiDetalharVitrine {
}
