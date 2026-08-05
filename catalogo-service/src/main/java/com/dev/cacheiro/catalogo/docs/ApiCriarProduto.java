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
        summary = "Cria um produto",
        description = "Publica um evento de invalidação de cache para a vitrine após gravar.",
        responses = @ApiResponse(responseCode = "201", description = "Produto criado"))
public @interface ApiCriarProduto {
}
