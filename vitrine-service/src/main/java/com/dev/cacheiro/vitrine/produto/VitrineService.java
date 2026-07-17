package com.dev.cacheiro.vitrine.produto;

import com.dev.cacheiro.vitrine.cache.CacheMetrics;
import com.dev.cacheiro.vitrine.cache.CacheProps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VitrineService {

    private final CatalogoClient catalogo;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final CacheMetrics metrics;
    private final CacheProps props;

    @SneakyThrows  // para não usar o try/catch por causa do ObjectMapper
    public ProdutoResponse buscar(Long id) {
        String chave = "produto:" + id;

        //Tenta o cache
        String json = redis.opsForValue().get(chave);
        if (json != null) {
            metrics.hit();
            return mapper.readValue(json, ProdutoResponse.class);
        }

        //Miss: busca na fonte da verdade
        metrics.miss();
        ProdutoResponse produto = catalogo.buscar(id);

        //Guarda no cache com TTL - time to life
        redis.opsForValue().set(chave,
                mapper.writeValueAsString(produto),
                props.ttlProduto());

        return produto;
    }

    @SneakyThrows
    public java.util.List<ProdutoResponse> listar() {
        String chave = "produtos:all";

        // Tenta o cache primeiro
        String json = redis.opsForValue().get(chave);
        if (json != null) {
            metrics.hit();
            return mapper.readValue(json, new TypeReference<>() {}); // typereference para converter para lista de produtos
        }


        metrics.miss();
        List<ProdutoResponse> produtos = catalogo.listar();
        redis.opsForValue().set(chave,
                mapper.writeValueAsString(produtos),
                props.ttlLista());

        return produtos;
    }


}


