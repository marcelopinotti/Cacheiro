package com.dev.cacheiro.catalogo.eventos;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvalidacaoPublisher {

    public static final String CANAL = "produtos:invalidacao";

    private final StringRedisTemplate redis;

    public void publicar(Long id) {
        redis.convertAndSend(CANAL, "{\"id\":" + id + "}");
    }
}



