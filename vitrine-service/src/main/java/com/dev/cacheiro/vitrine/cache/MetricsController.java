package com.dev.cacheiro.vitrine.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final CacheMetrics metrics;

    @GetMapping("/cache")
    public Map<String, Object> cache() {
        return metrics.snapshot();
    }
}
