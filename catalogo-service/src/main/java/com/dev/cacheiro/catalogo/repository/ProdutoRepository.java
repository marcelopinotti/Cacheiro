package com.dev.cacheiro.catalogo.repository;

import com.dev.cacheiro.catalogo.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // UPDATE atômico: o banco serializa, sem read-modify-write na aplicação.
    // Retorna 0 se o produto não existe OU se o estoque ficaria negativo.
    @Modifying
    @Query("UPDATE Produto p SET p.estoque = p.estoque + :delta " +
            "WHERE p.id = :id AND p.estoque + :delta >= 0")
    int ajustarEstoque(@Param("id") Long id, @Param("delta") int delta);
}