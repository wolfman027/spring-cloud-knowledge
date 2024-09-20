package com.wolfman.marathon.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerManagementClientFallbackFactory implements FallbackFactory<FallbackWithFactory> {

    @Override
    public FallbackWithFactory create(Throwable cause) {
        log.error("fallback reason was {}", cause.getMessage());
        return new FallbackWithFactory();
    }

}
