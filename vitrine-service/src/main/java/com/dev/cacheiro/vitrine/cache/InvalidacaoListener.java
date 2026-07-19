package com.dev.cacheiro.vitrine.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvalidacaoListener implements MessageListener {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        var evento = mapper.readTree(message.getBody());
        long id = evento.get("id").asLong();

        redis.delete("produto::" + id);
        redis.delete("produtos:all");

        log.info("Cache invalido para o produto: {}",id);
    }
}
