package com.dev.cacheiro.vitrine.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "vitrine.cache")
public record CacheProps(Duration ttlProduto, Duration ttlLista) {
}
