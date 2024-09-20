package com.wolfman.marathon.config;

import feign.Capability;
import feign.Logger;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FeignLogConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        System.out.println("Setting Feign logger level to FULL");
        return Logger.Level.BASIC;
    }

//    @Bean
//    Capability customCapability() {
//        System.out.println("Setting MicrometerCapability");
//        return new MicrometerCapability();
//    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new CompositeMeterRegistry();
    }

    @Bean
    public MicrometerCapability micrometerCapability(MeterRegistry meterRegistry) {
        return new MicrometerCapability(meterRegistry);
    }

}
