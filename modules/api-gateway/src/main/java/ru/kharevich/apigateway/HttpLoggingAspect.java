package ru.kharevich.apigateway;

import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class HttpLoggingAspect {
//
//    private final ObjectMapper jsonMapper;
//
//    // Точка среза для всех методов в контроллерах с путями /api/v1/**
//    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) && " +
//            "execution(* ru.kharevich.apigateway..*.*(..)) && " +
//            "@annotation(org.springframework.web.bind.annotation.RequestMapping) && " +
//            "args(..,jakarta.servlet.http.HttpServletRequest)")
//    public void apiV1Requests() {}
//
//    @Around("apiV1Requests()")
//    public Object logApiV1Request(ProceedingJoinPoint joinPoint) throws Throwable {
//        HttpServletRequest request = getCurrentHttpRequest();
//        if (request.getRequestURI().startsWith("/api/v1")) {
//            logRequestWithHeaders(request);
//            long startTime = System.currentTimeMillis();
//            Object response = joinPoint.proceed();
//            logResponseWithDetails(request, response, startTime);
//            return response;
//        }
//        return joinPoint.proceed();
//    }
//
//    private void logRequestWithHeaders(HttpServletRequest request) {
//        Map<String, String> headers = new HashMap<>();
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            headers.put(headerName, request.getHeader(headerName));
//        }
//
//        log.info("""
//                API V1 REQUEST:
//                Method: {}
//                URI: {}
//                Headers: {}
//                Query: {}
//                """,
//                request.getMethod(),
//                request.getRequestURI(),
//                headers,
//                request.getQueryString());
//    }
//
//    private void logResponseWithDetails(HttpServletRequest request, Object response, long startTime) {
//        long duration = System.currentTimeMillis() - startTime;
//        log.info("""
//                API V1 RESPONSE:
//                Method: {}
//                URI: {}
//                Status: {}
//                Duration: {} ms
//                Response: {}
//                """,
//                request.getMethod(),
//                request.getRequestURI(),
//                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
//                        .getResponse().getStatus(),
//                duration,
//                serializeToJson(response));
//    }
//
//    private HttpServletRequest getCurrentHttpRequest() {
//        return ((ServletRequestAttributes) Objects.requireNonNull(
//                RequestContextHolder.getRequestAttributes())).getRequest();
//    }
//
//    private String serializeToJson(Object object) {
//        try {
//            return object != null ? jsonMapper.writeValueAsString(object) : "null";
//        } catch (Exception e) {
//            log.warn("Failed to serialize object to JSON", e);
//            return "[serialization error]";
//        }
//    }
}