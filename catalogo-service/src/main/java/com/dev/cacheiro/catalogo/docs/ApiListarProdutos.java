package com.dev.cacheiro.catalogo.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Lista todos os produtos",
        description = "Fonte da verdade do catálogo, lido direto do Postgres (sem cache). "
                + "Ordenado por id ascendente.",
        responses = @ApiResponse(responseCode = "200", description = "Produtos retornados"))
public @interface ApiListarProdutos {
}
