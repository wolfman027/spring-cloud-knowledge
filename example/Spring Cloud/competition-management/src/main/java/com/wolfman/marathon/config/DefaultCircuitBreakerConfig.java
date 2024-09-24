package com.wolfman.marathon.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

//@Configuration
public class DefaultCircuitBreakerConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        ContextAwareScheduledThreadPoolExecutor executor = ContextAwareScheduledThreadPoolExecutor.newScheduledThreadPool()
                .corePoolSize(5)
                .build();
        return factory -> {
            factory.configureExecutorService(executor);
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
                            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                    .minimumNumberOfCalls(3)
                                    .slidingWindowSize(10)
                                    .failureRateThreshold(20)
                                    .build())
                            .build());
        };
    }

//    @Bean
//    public Customizer<Resilience4JCircuitBreakerFactory> slowCustomizer() {
//        return factory ->
//                factory.configure(builder ->
//                        builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                                .timeLimiterConfig(
//                                        TimeLimiterConfig.custom()
//                                                .timeoutDuration(Duration.ofSeconds(2))
//                                                .build()
//                                ),"slow");
//    }

//    @Bean
//    public Customizer<Resilience4JCircuitBreakerFactory> slowCustomizer() {
//        return factory -> factory.addCircuitBreakerCustomizer(circuitBreaker -> circuitBreaker.getEventPublisher()
//                .onError(normalFluxErrorConsumer).onSuccess(normalFluxSuccessConsumer), "normalflux");
//    }


//    @Bean
//    public Customizer<Resilience4JCircuitBreakerFactory> groupExecutorServiceCustomizer() {
//        return factory -> factory.configureGroupExecutorService(group ->
//                new DelegatingSecurityContextExecutorService(Executors.newVirtualThreadPerTaskExecutor()));
//    }

}
