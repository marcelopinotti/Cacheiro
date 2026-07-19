package com.dev.cacheiro.vitrine.produto;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CatalogoClient {

    private final RestClient restClient;

    public CatalogoClient(@Value("${vitrine.catalogo-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ProdutoResponse buscar(Long id) {
        return restClient.get().uri("/api/produtos/{id}", id)
                .retrieve().body(ProdutoResponse.class);
    }

    public List<ProdutoResponse> listar() {
        return restClient.get().uri("/api/produtos")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }


}
