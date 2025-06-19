package ru.kharevich.apigateway.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class GatewayLoggingAspect {

    private final ObjectMapper jsonMapper;

    @Pointcut("execution(* org.springframework.cloud.gateway.handler.FilteringWebHandler.handle(..))")
    public void gatewayRequestHandling() {}

    @Around("gatewayRequestHandling()")
    public Object logGatewayRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        ServerWebExchange exchange = extractExchange(joinPoint.getArgs());

        if (exchange == null /*|| !exchange.getRequest().getPath().value().startsWith("/api/v1")*/) {
            return joinPoint.proceed();
        }

        // Логируем запрос
        logRequest(exchange.getRequest());

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        // Обрабатываем реактивный (Mono) и синхронный случаи
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                    .doOnSuccess(response -> logResponse(exchange, response, startTime))
                    .doOnError(error -> log.error("Gateway error: {}", error.getMessage()));
        } else {
            logResponse(exchange, result, startTime);
            return result;
        }
    }

    private ServerWebExchange extractExchange(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ServerWebExchange) {
                return (ServerWebExchange) arg;
            }
        }
        return null;
    }

    private void logRequest(ServerHttpRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaders().forEach((key, values) ->
                headers.put(key, String.join(",", values))
        );

        log.info("""
                GATEWAY REQUEST:
                Method: {}
                Path: {}
                Headers: {}
                Query: {}
                """,
                request.getMethod(),
                request.getPath(),
                headers,
                request.getQueryParams()
        );
    }

    private void logResponse(ServerWebExchange exchange, Object response, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        log.info("""
                GATEWAY RESPONSE:
                Path: {}
                Status: {}
                Duration: {} ms
                Response: {}
                """,
                exchange.getRequest().getPath(),
                exchange.getResponse().getStatusCode(),
                duration,
                serializeToJson(response)
        );
    }

    private String serializeToJson(Object object) {
        try {
            return object != null ? jsonMapper.writeValueAsString(object) : "null";
        } catch (Exception e) {
            log.warn("Failed to serialize response to JSON", e);
            return "[serialization error]";
        }
    }
}