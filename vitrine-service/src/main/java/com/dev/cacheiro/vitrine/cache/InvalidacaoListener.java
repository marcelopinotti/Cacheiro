package com.dev.cacheiro.vitrine.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvalidacaoListener implements MessageListener {

    private final StringRedisTemplate redis;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        long id = Long.parseLong(new String(message.getBody()));

        redis.delete("produto:" + id);
        redis.delete("produtos:all");

        log.info("Cache invalido para o produto: {}",id);
    }
}
