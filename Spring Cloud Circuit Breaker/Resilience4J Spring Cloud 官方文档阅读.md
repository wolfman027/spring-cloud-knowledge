# Configuring Resilience4J Circuit Breakers

> 基于 3.1.2 版本学习
>
> 官方网站：https://resilience4j.readme.io/docs/getting-started-6



## Starters

reactive 应用：

~~~groovy
org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j
~~~

non-reactive 应用：

~~~groovy
org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j
~~~

取消 Resilience4J 自动配置：

~~~properties
spring.cloud.circuitbreaker.resilience4j.enabled=false
~~~



## Default Configuration

要为所有断路器提供默认配置，请创建一个 `Customizer` bean，该 bean 将传递 `Resilience4JCircuitBreakerFactory` 或 `ReactiveResilience4JCircuitBreakerFactory`。`configureDefault` 方法用于提供默认配置。

**Non-Reactive Example：**

~~~java
// 通过注解代码配置全局默认配置
@Configuration
public class DefaultCircuitBreakerConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> 
          factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4)).build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                      .slidingWindowSize(10)
                                      .failureRateThreshold(50)
                                      .build())
                .build());
    }
}
~~~

**Reactive Example：**

~~~java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
	return factory -> 
    factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                             .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                             .timeLimiterConfig(
                               TimeLimiterConfig
                               .custom()      
                               .timeoutDuration(Duration.ofSeconds(4))
                               .build())       
                             .build());
}
~~~



### 自定义线程池 ExecutorService

如果你想配置执行断路器的 `ExecutorService`，你可以使用 `Resilience4JCircuitBreakerFactory`。

例如，如果你想使用上下文感知的 `ExecutorService`，你可以这样做。

~~~java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
	return factory -> {
		ContextAwareScheduledThreadPoolExecutor executor = ContextAwareScheduledThreadPoolExecutor.newScheduledThreadPool().corePoolSize(5)
			.build();
		factory.configureExecutorService(executor);
	};
}
~~~



## Specific Circuit Breaker Configuration

类似于提供默认配置，你可以创建一个 `Customizer` bean，该 bean 会接收一个 `Resilience4JCircuitBreakerFactory` 或 `ReactiveResilience4JCircuitBreakerFactory`。

~~~java
@Bean
public Customizer<Resilience4JCircuitBreakerFactory> slowCustomizer() {
    return factory ->
            factory.configure(builder ->
                    builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                            .timeLimiterConfig(
                                    TimeLimiterConfig.custom()
                                            .timeoutDuration(Duration.ofSeconds(2))
                                            .build()
                            ),"slow");
}
~~~

除了配置已创建的断路器之外，您还可以在创建断路器之后但在将其返回给调用者之前自定义断路器。要做到这一点，你可以使用 `addCircuitBreakerCustomizer` 方法。这对于向 Resilience4J 断路器添加事件处理程序非常有用。

~~~java
@Bean
public Customizer<Resilience4JCircuitBreakerFactory> slowCustomizer() {
	return factory -> factory.addCircuitBreakerCustomizer(circuitBreaker -> circuitBreaker.getEventPublisher()
	.onError(normalFluxErrorConsumer).onSuccess(normalFluxSuccessConsumer), "normalflux");
}
~~~

如果你需要将安全上下文传播给断路器，你必须配置断路器使用 `DelegatingSecurityContextExecutorService`。

~~~java
@Bean
public Customizer<Resilience4JCircuitBreakerFactory> groupExecutorServiceCustomizer() {
    return factory -> factory.configureGroupExecutorService(group -> 
            new DelegatingSecurityContextExecutorService(Executors.newVirtualThreadPerTaskExecutor()));
}
~~~



**Reactive Example：**

~~~java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> slowCustomizer() {
	return factory -> {
		factory.configure(builder -> builder
		.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(2)).build())
		.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults()), "slow", "slowflux");
		factory.addCircuitBreakerCustomizer(circuitBreaker -> circuitBreaker.getEventPublisher()
        	.onError(normalFluxErrorConsumer).onSuccess(normalFluxSuccessConsumer), "normalflux");
     };
}
~~~



## Circuit Breaker Properties Configuration

Property configuration 优先级要比 Java `Customizer` 高。

优先级从上到下递减：

- Method(id) config —— 具体的方法或操作
- Service(group) config —— 特定的应用程序服务或操作
- Global default config —— 全局默认配置

```java
ReactiveResilience4JCircuitBreakerFactory.create(String id, String groupName)
Resilience4JCircuitBreakerFactory.create(String id, String groupName)
```



### Global Default Properties Configuration

~~~yml
resilience4j.circuitbreaker:
    configs:
        default:
            registerHealthIndicator: true
            slidingWindowSize: 50

resilience4j.timelimiter:
    configs:
        default:
            timeoutDuration: 5s
            cancelRunningFuture: true
~~~



### Configs Properties Configuration

~~~yaml
resilience4j.circuitbreaker:
    configs:
        groupA:
            registerHealthIndicator: true
            slidingWindowSize: 200

resilience4j.timelimiter:
    configs:
        groupC:
            timeoutDuration: 3s
            cancelRunningFuture: true
~~~



### Instances Properties Configuration

~~~yaml
resilience4j.circuitbreaker:
 instances:
     backendA:
         registerHealthIndicator: true
         slidingWindowSize: 100
     backendB:
         registerHealthIndicator: true
         slidingWindowSize: 10
         permittedNumberOfCallsInHalfOpenState: 3
         slidingWindowType: TIME_BASED
         recordFailurePredicate: io.github.robwin.exception.RecordFailurePredicate

resilience4j.timelimiter:
 instances:
     backendA:
         timeoutDuration: 2s
         cancelRunningFuture: true
     backendB:
         timeoutDuration: 1s
         cancelRunningFuture: false
~~~

- `ReactiveResilience4JCircuitBreakerFactory.create("backendA")` or `Resilience4JCircuitBreakerFactory.create("backendA")` will apply `instances backendA properties`
- `ReactiveResilience4JCircuitBreakerFactory.create("backendA", "groupA")` or `Resilience4JCircuitBreakerFactory.create("backendA", "groupA")` will apply `instances backendA properties`
- `ReactiveResilience4JCircuitBreakerFactory.create("backendC")` or `Resilience4JCircuitBreakerFactory.create("backendC")` will apply `global default properties`
- `ReactiveResilience4JCircuitBreakerFactory.create("backendC", "groupC")` or `Resilience4JCircuitBreakerFactory.create("backendC", "groupC")` will apply `global default CircuitBreaker properties and config groupC TimeLimiter properties`











































































