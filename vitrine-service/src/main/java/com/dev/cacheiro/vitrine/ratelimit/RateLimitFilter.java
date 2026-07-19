package com.dev.cacheiro.vitrine.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long LIMITE_POR_MINUTO = 100;
    private final StringRedisTemplate redis;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String ip = "ratelimit:" + request.getRemoteAddr();

        Long contagem = redis.opsForValue().increment(ip);
        if (contagem != null && contagem == 1) {
            redis.expire(ip, Duration.ofSeconds(60));
        }

        if (contagem != null && contagem > LIMITE_POR_MINUTO) {
            response.setStatus(429);  // many requests http
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"erro\":\"Limite de requisições excedido. Tente em 1 minuto.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
