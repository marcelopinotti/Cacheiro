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
        summary = "Busca um produto por id",
        description = "Usado pela vitrine no cache miss e pelo pedido-service antes de reservar estoque.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Produto encontrado"),
                @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        })
public @interface ApiBuscarProduto {
}
