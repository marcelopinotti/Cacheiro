package com.dev.cacheiro.vitrine.produto;

import com.dev.cacheiro.vitrine.cache.CacheProps;
import com.dev.cacheiro.vitrine.cache.Keys;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VitrineService {

    private final CatalogoClient catalogo;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final MeterRegistry registry;
    private final CacheProps props;

    private void hit()  { registry.counter("vitrine.cache", "result", "hit").increment(); }
    private void miss() { registry.counter("vitrine.cache", "result", "miss").increment(); }

    @SneakyThrows
    public ProdutoResponse buscar(Long id) {
        String chave = Keys.produto(id);
        String chaveLock = Keys.lockProduto(id);

        String json = redis.opsForValue().get(chave);
        if (json != null) {
            hit();
            return mapper.readValue(json, ProdutoResponse.class);
        }
        miss();

        // Tenta adquirir o lock (NX = só se não existir, EX 5s de segurança)
        Boolean ganheiLock = redis.opsForValue()
                .setIfAbsent(chaveLock, "1", Duration.ofSeconds(5));

        if (Boolean.TRUE.equals(ganheiLock)) {
            try {
                ProdutoResponse produto = catalogo.buscar(id);
                redis.opsForValue().set(chave,
                        mapper.writeValueAsString(produto), props.ttlProduto());
                return produto;
            } finally {
                redis.delete(chaveLock);
            }
        }

        // Perdi o lock: espero um pouco e tento o cache de novo
        Thread.sleep(100);
        String recarregado = redis.opsForValue().get(chave);
        if (recarregado != null) {
            return mapper.readValue(recarregado, ProdutoResponse.class);
        }
        // Fallback: busca direta (melhor responder lento que falhar)
        return catalogo.buscar(id);
    }

    @SneakyThrows
    public List<ProdutoResponse> listar() {
        String chave = Keys.PRODUTOS_ALL;

        // Tenta o cache primeiro
        String json = redis.opsForValue().get(chave);
        if (json != null) {
            hit();
            return mapper.readValue(json, new TypeReference<>() {}); // typereference para converter para lista de produtos
        }


        miss();
        List<ProdutoResponse> produtos = catalogo.listar();
        redis.opsForValue().set(chave,
                mapper.writeValueAsString(produtos),
                props.ttlLista());

        return produtos;
    }


}


