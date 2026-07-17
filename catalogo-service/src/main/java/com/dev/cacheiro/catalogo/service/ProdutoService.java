package com.dev.cacheiro.catalogo.service;


import com.dev.cacheiro.catalogo.dtos.ProdutoMapper;
import com.dev.cacheiro.catalogo.dtos.ProdutoRequest;
import com.dev.cacheiro.catalogo.dtos.ProdutoResponse;
import com.dev.cacheiro.catalogo.entity.Produto;
import com.dev.cacheiro.catalogo.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.list;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    @Value("${catalogo.latencia-simulada-ms}")
    private long latenciaMs;

    private final ProdutoRepository repository;
    private final ProdutoMapper mapper;

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        simularBanco();
        return mapper.toResponse(findProdutoById(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(){
        simularBanco();
        return repository.findAllByOrderByIdAsc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request){
        simularBanco();
        var produto = mapper.toEntity(request);
        var produtoSalvo = repository.save(produto);
        return mapper.toResponse(produtoSalvo);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        var produtoExiste = findProdutoById(id);
        produtoExiste.setNome(request.nome());
        produtoExiste.setDescricao(request.descricao());
        produtoExiste.setPreco(request.preco());
        produtoExiste.setEstoque(request.estoque());
        return mapper.toResponse(repository.save(produtoExiste));
    }

    @Transactional
    public void deletar(Long id){
        repository.deleteById(id);
    }

    private Produto findProdutoById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    private void simularBanco() {
        try{
            Thread.sleep(latenciaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}