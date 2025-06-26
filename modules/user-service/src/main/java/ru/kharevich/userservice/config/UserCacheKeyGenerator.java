package ru.kharevich.userservice.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import ru.kharevich.userservice.util.JwtUtils;

import java.lang.reflect.Method;

@Component
public class UserCacheKeyGenerator implements KeyGenerator {

    private final JwtUtils jwtUtils;

    public UserCacheKeyGenerator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return jwtUtils.getUserId();
    }
}