package com.dev.cacheiro.catalogo.dtos;

import java.math.BigDecimal;

public record ProdutoRequest(
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque
) {}
