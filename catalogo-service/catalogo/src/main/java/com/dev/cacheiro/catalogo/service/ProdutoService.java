package com.dev.cacheiro.catalogo.service;


import com.dev.cacheiro.catalogo.dtos.ProdutoMapper;
import com.dev.cacheiro.catalogo.dtos.ProdutoResponse;
import com.dev.cacheiro.catalogo.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    @Value("${catalogo.latencia-simulada-ms}")
    private long latenciaMs;

    private final ProdutoRepository repository;
    private final ProdutoMapper mapper;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        simularBanco();
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    private void simularBanco() {
        try{
            Thread.sleep(latenciaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}

