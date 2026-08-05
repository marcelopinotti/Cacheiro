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
        summary = "Ajusta o estoque de um produto",
        description = "delta negativo reserva estoque, positivo devolve (cancelamento). "
                + "O UPDATE é condicional no banco, então concorrência não gera estoque negativo. "
                + "Chamado pelo pedido-service.",
        responses = {
                @ApiResponse(responseCode = "204", description = "Estoque ajustado"),
                @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
                @ApiResponse(responseCode = "409", description = "Estoque insuficiente para o delta pedido")
        })
public @interface ApiAjustarEstoque {
}
