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
        summary = "Remove um produto",
        description = "Idempotente: remover um id inexistente também responde 204. "
                + "Publica invalidação de cache.",
        responses = @ApiResponse(responseCode = "204", description = "Produto removido"))
public @interface ApiDeletarProduto {
}
