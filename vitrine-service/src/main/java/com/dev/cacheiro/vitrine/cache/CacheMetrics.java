package com.dev.cacheiro.vitrine.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CacheMetrics {

    private static final String HITS = "metrics:hits";
    private static final String MISSES = "metrics:misses";

    private final StringRedisTemplate redis;

    public void hit()  { redis.opsForValue().increment(HITS); }
    public void miss() { redis.opsForValue().increment(MISSES); }

    public Map<String, Object> snapshot(){
        long hits = parse(redis.opsForValue().get(HITS));
        long misses = parse(redis.opsForValue().get(MISSES));
        long total  = hits + misses;
        double ratio = total == 0 ? 0.0 : (double) hits / total;
        return Map.of("hits", hits, "misses", misses,
                "hitRatio", Math.round(ratio * 1000) / 10.0 + "%");
    }


    private long parse(String value) {
        return value == null ? 0 : Long.parseLong(value);
    }
}
