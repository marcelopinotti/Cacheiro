package com.dev.cacheiro.catalogo.service;


import com.dev.cacheiro.catalogo.dtos.ProdutoRequest;
import com.dev.cacheiro.catalogo.dtos.ProdutoResponse;
import com.dev.cacheiro.catalogo.entity.Produto;
import com.dev.cacheiro.catalogo.eventos.InvalidacaoPublisher;
import com.dev.cacheiro.catalogo.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProdutoService {

    @Value("${catalogo.latencia-simulada-ms}")
    private long latenciaMs;

    private final ProdutoRepository repository;
    private final InvalidacaoPublisher publisher;

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        simularBanco();
        return toResponse(findProdutoById(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(){
        simularBanco();
        return repository.findAll(Sort.by("id").ascending())
                .stream()
                .map(ProdutoService::toResponse)
                .toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request){
        simularBanco();
        var produto = toEntity(request);
        var produtoSalvo = repository.save(produto);
        publisher.publicar(produtoSalvo.getId());
        return toResponse(produtoSalvo);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        var produtoExiste = findProdutoById(id);
        produtoExiste.setNome(request.nome());
        produtoExiste.setDescricao(request.descricao());
        produtoExiste.setPreco(request.preco());
        // estoque NÃO se altera pelo CRUD: só pelo PATCH /estoque (usado pelo pedido-service)
        publisher.publicar(id);
        return toResponse(produtoExiste);
    }

    @Transactional
    public void ajustarEstoque(Long id, int delta) {
        int linhas = repository.ajustarEstoque(id, delta);
        if (linhas == 0) {
            if (repository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Estoque insuficiente");
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }
        publisher.publicar(id);
    }

    @Transactional
    public void deletar(Long id){
        repository.deleteById(id);
        publisher.publicar(id);
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

    private static ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque()
        );
    }

    private static Produto toEntity(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setEstoque(request.estoque());
        return produto;
    }

}
