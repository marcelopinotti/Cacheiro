package com.dev.cacheiro.catalogo.dtos;

import java.math.BigDecimal;

public record ProdutoResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    Integer estoque
) { }
