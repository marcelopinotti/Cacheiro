package com.dev.cacheiro.catalogo.eventos;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class InvalidacaoPublisher {

    public static final String CANAL = "produtos:invalidacao";

    private final StringRedisTemplate redis;

    // Publica só após o commit: se sair antes, a vitrine pode re-cachear o dado antigo
    public void publicar(Long id) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redis.convertAndSend(CANAL, id.toString());
                }
            });
        } else {
            redis.convertAndSend(CANAL, id.toString());
        }
    }
}



