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
        summary = "Lista a vitrine de produtos",
        description = "Serve do cache Redis (TTL 20s). No miss, busca no catalogo-service e regrava. "
                + "O contador vitrine.cache{result=hit|miss} sobe a cada chamada.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Produtos retornados (do cache ou do catálogo)"),
                @ApiResponse(responseCode = "429", description = "Limite de 100 req/min por IP excedido"),
                @ApiResponse(responseCode = "503", description = "Catálogo indisponível")
        })
public @interface ApiListarVitrine {
}
