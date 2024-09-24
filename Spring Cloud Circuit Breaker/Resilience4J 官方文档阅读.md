# Resilience4J

> 官方网站：https://resilience4j.readme.io/docs/circuitbreaker

**RateLimiter：**统计的是所有请求的数量

**CircuitBreaker：**每个线程的断路器

**Bulkhead：**舱壁，两种实现：线程池 与 Semaphore





如果您想限制并发线程的数量，请使用Bulkhead。CircuitBreaker 和 Bulkhead 结合起来。

CircuitBreaker 并不能控制并发，针对单线程。

Bulkhead 控制并发数量



## 断路器配置属性

| Config property                                   | Default Value                                                | Description                                                  |
| ------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| failureRateThreshold                              | 50                                                           | 以百分比为单位配置失败率阈值。<br>当故障率等于或大于阈值时，断路器转换为打开并开始短路呼叫。 |
| slowCallRateThreshold                             | 100                                                          | 配置以百分比为单位的阈值。当调用持续时间大于 slowCallDurationThreshold 时，CircuitBreaker 认为调用是缓慢的<br>当故障率等于或大于阈值时，断路器转换为打开并开始短路呼叫。 |
| slowCallDurationThreshold                         | 60000 [ms]                                                   | 配置时长阈值，超过该阈值的呼叫被视为慢速调用，并增加慢速调用的比率。 |
| permittedNumberOfCalls<br/>InHalfOpenState        | 10                                                           | 配置断路器半开时允许的调用数。                               |
| maxWaitDurationInHalfOpenState                    | 0 [ms]                                                       | 配置最大等待时间，控制断路器在切换到打开状态之前保持半打开状态的最长时间。 <br/>值 0 表示断路器将无限地等待半打开状态，直到所有允许的调用都完成。 |
| slidingWindowType                                 | COUNT_BASED                                                  | 配置滑动窗口的类型，该窗口用于记录断路器关闭时调用的结果。 <br/>滑动窗口可以是基于计数或基于时间的。<br><br/>如果滑动窗口是 COUNT_BASED，那么最后的 slidingWindowSize 调用将被记录并聚合。 <br/>如果滑动窗口是 TIME_BASED，那么最后一次滑动窗口大小的调用将被记录并聚合。 |
| slidingWindowSize                                 | 100                                                          | 配置滑动窗口的大小，该窗口用于记录断路器关闭时调用的结果。   |
| minimumNumberOfCalls                              | 100                                                          | 配置在断路器可以计算错误率或慢速呼叫率之前所需的最小呼叫数(每个滑动窗口周期)。 <br/>例如，如果minimumNumberOfCalls为10，那么在计算故障率之前，必须记录至少10个呼叫。 <br/>如果只记录了9个呼叫，即使所有9个呼叫都失败了，断路器也不会过渡到打开。 |
| waitDurationInOpenState                           | 60000 [ms]                                                   | 断路器从开路过渡到半开路之前应等待的时间。                   |
| automaticTransition<br/>FromOpenToHalfOpenEnabled | false                                                        | 如果设置为true，则意味着断路器将自动从打开状态过渡到半开状态，不需要调用来触发转换。创建一个线程来监视CircuitBreakers的所有实例，以便在 waitDurationInOpenState 通过后将它们转换为HALF_OPEN。<br>然而，如果设置为false，则转换到 HALF_OPEN 仅在调用时发生，即使在传递 waitDurationInOpenState 之后也是如此。这里的优点是没有线程监视所有断路器的状态。 |
| recordExceptions                                  | empty                                                        | 记录为失败并因此增加故障率的异常列表。 <br/>除非通过 ignoreExceptions 显式忽略，否则匹配或继承列表中的任何异常都将视为失败。 <br/>如果您指定了一个异常列表，则所有其他异常都被视为成功，除非它们被ignoreExceptions显式忽略。 |
| ignoreExceptions                                  | empty                                                        | 一个被忽略的异常列表，这些异常既不能算作失败也不能算作成功。 <br/>匹配或继承列表中的任何异常都不会被视为失败或成功，即使异常是recordExceptions的一部分。 |
| recordFailurePredicate                            | throwable -> true<br>By default all exceptions are recored as failures. | 一个自定义谓词，用于评估是否应将异常记录为失败。 <br/>如果异常应被视为失败，则Predicate必须返回true。如果出现异常，谓词必须返回false <br/>应该算作成功，除非该异常被ignoreExceptions显式忽略。 |
| ignoreExceptionPredicate                          | throwable -> false<br>By default no exception is ignored.    | 一个自定义谓词，用于评估异常是否应该被忽略，并且既不算作失败也不算作成功。 <br/>如果应该忽略异常，Predicate必须返回true。 <br/>如果异常应被视为失败，则Predicate必须返回false。 |



## Bulkhead

- SemaphoreBulkhead
  - maxConcurrentCalls
  - maxWaitDuration
- FixedThreadPoolBulkhead
  - maxThreadPoolSize
  - coreThreadPoolSize
  - coreThreadPoolSize
  - keepAliveDuration
  - writableStackTraceEnabled

~~~groovy
implementation 'io.github.resilience4j:resilience4j-bulkhead'
implementation 'org.springframework.boot:spring-boot-starter-aop'
~~~




