package ru.kharevich.chatservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import ru.kharevich.chatservice.controller.exception.DriverFeignErrorDecoder;

@FeignClient(
        name = "user-service",
        configuration = DriverFeignErrorDecoder.class
)
@Profile("!test")
public interface UserFeignClientDev extends UserFeignClient {
}
