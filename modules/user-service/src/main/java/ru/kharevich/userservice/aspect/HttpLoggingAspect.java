package ru.kharevich.userservice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HttpLoggingAspect {

    private final ObjectMapper jsonMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController) && execution(* *(..))")
    public Object logRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();

        // Логируем только запросы к API v1
        if (request.getRequestURI().startsWith("/api/v1")) {
            logRequest(request);

            long startTime = System.currentTimeMillis();
            Object response = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            logResponse(request, duration);
            return response;
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(
                RequestContextHolder.getRequestAttributes())).getRequest();
    }

    private void logRequest(HttpServletRequest request) {
        try {
            log.info("""
                    API REQUEST:
                    Method: {}
                    URI: {}
                    Headers:
                    Authorization: {}
                    Content-Type: {}
                    """,
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("Authorization"),
                    request.getContentType());
        } catch (Exception e) {
            log.warn("Failed to log request", e);
        }
    }

    private void logResponse(HttpServletRequest request, long duration) {
        try {
            log.info("""
                    API RESPONSE:
                    URI: {}
                    Status: {}
                    Duration: {} ms
                    """,
                    request.getRequestURI(),
                    ((ServletRequestAttributes) RequestContextHolder
                            .getRequestAttributes()).getResponse().getStatus(),
                    duration);
        } catch (Exception e) {
            log.warn("Failed to log response", e);
        }
    }
}