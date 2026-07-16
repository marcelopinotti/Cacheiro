package com.dev.cacheiro.catalogo.repository;

import com.dev.cacheiro.catalogo.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
