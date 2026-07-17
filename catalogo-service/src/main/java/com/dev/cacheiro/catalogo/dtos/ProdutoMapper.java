package com.dev.cacheiro.catalogo.dtos;

import com.dev.cacheiro.catalogo.entity.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {
    public ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque()
        );
    }

    public Produto toEntity(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setEstoque(request.estoque());
        return produto;
    }

}
