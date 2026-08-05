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
        summary = "Atualiza nome, descrição e preço de um produto",
        description = "O campo estoque é ignorado aqui: só muda via PATCH /api/produtos/{id}/estoque. "
                + "Publica invalidação de cache.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Produto atualizado"),
                @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        })
public @interface ApiAtualizarProduto {
}
