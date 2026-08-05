package com.dev.cacheiro.catalogo.controller;

import com.dev.cacheiro.catalogo.docs.ApiAjustarEstoque;
import com.dev.cacheiro.catalogo.docs.ApiAtualizarProduto;
import com.dev.cacheiro.catalogo.docs.ApiBuscarProduto;
import com.dev.cacheiro.catalogo.docs.ApiCriarProduto;
import com.dev.cacheiro.catalogo.docs.ApiDeletarProduto;
import com.dev.cacheiro.catalogo.docs.ApiListarProdutos;
import com.dev.cacheiro.catalogo.dtos.EstoqueRequest;
import com.dev.cacheiro.catalogo.dtos.ProdutoRequest;
import com.dev.cacheiro.catalogo.dtos.ProdutoResponse;
import com.dev.cacheiro.catalogo.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "CRUD do catálogo — fonte da verdade dos produtos e do estoque")
public class ProdutoController {

    private final ProdutoService service;

    @GetMapping
    @ApiListarProdutos
    public List<ProdutoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @ApiBuscarProduto
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiCriarProduto
    public ProdutoResponse criar(@RequestBody ProdutoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @ApiAtualizarProduto
    public ProdutoResponse atualizar(@PathVariable Long id, @RequestBody ProdutoRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/estoque")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiAjustarEstoque
    public void ajustarEstoque(@PathVariable Long id, @RequestBody EstoqueRequest request) {
        service.ajustarEstoque(id, request.delta());
    }

    @DeleteMapping("/{id}")
    @ApiDeletarProduto
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
