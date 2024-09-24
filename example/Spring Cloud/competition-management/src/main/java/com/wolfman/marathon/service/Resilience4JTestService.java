package com.wolfman.marathon.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Resilience4JTestService {

    @RateLimiter(name = "rateA", fallbackMethod = "getOKFallback")
    public String getOK() {
        return "OK";
    }

    @SneakyThrows
    public String getOKFallback(Throwable excetpion){
        log.info("getOKFallback:{}", excetpion.getMessage());
        return "Fail";
    }

    @RateLimiter(name = "default", fallbackMethod = "getOKFallback")
    public String getDefaultOK() {
        return "OK";
    }

    public String getConcurrentOK() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "OK";
    }


}
