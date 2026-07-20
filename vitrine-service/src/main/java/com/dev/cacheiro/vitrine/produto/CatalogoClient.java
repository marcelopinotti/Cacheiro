package com.dev.cacheiro.vitrine.produto;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
public class CatalogoClient {

    private final RestClient restClient;

    public CatalogoClient(@Value("${vitrine.catalogo-url}") String baseUrl,
                          @Value("${vitrine.catalogo-timeout-conexao}") Duration timeoutConexao,
                          @Value("${vitrine.catalogo-timeout-leitura}") Duration timeoutLeitura) {

        // Timeouts somados ficam abaixo do TTL de 5s do lock anti-stampede,
        // então o lock sempre é liberado pelo finally, nunca por expiração.
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeoutConexao).build());
        factory.setReadTimeout(timeoutLeitura);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
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
