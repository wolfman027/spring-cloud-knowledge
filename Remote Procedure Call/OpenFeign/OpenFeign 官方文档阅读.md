# Spring Cloud OpenFeign 特性



# 1. 如何使用

详见 competition-management 服务

如果你想使用外部化配置，则使用 [`SimpleDiscoveryClient`](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#simplediscoveryclient)。

Spring Cloud OpenFeign 支持 Spring Cloud LoadBalancer 阻塞模式的所有可用特征。其官方文档：[project documentation](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#spring-cloud-loadbalancer)。

确保 OpenFeign Client 所在位置，有如下两个方式：

- `@EnableFeignClients(basePackages = "com.example.clients")` 
- `@EnableFeignClients(clients = InventoryServiceFeignClient.class)`.



# 2.重写 Feign 默认值

@FeignClient

Spring Cloud 根据需要使用 FeignClientsConfiguration 为每个命名的客户端创建一个新的集成作为 ApplicationContext。

包含：

- feign.Decoder
- feign.Encoder
- feign.Contract

可以通过使用 @FeignClient 注释的 contextId 属性来覆盖该集成的名称。

@FeignClient 声明额外的配置（在 FeignClientsConfiguration 之上）来完全控制feignclient。

~~~java
// FooConfiguration 配置覆盖 FeignClientsConfiguration 配置
// FooConfiguration 不需要有 @Configuration，否则他可能会成为默认配置
@FeignClient(name = "stores", configuration = FooConfiguration.class)
public interface StoreClient {
	//..
}
~~~

**注意：**之前使用 `url` 属性，不需要 `name` 属性，现在需要  `name` 属性。

~~~java
// 占位符支持在 name 与 url 属性中使用
@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface StoreClient {
	//..
}
~~~

Spring Cloud OpenFeign 默认为 feign 提供以下 bean (BeanType beanName: ClassName)：

- `Decoder` feignDecoder: `ResponseEntityDecoder` (which wraps a `SpringDecoder`)
- `Encoder` feignEncoder: `SpringEncoder`
- `Logger` feignLogger: `Slf4jLogger`/
- `MicrometerObservationCapability` micrometerObservationCapability: If `feign-micrometer` is on the classpath and `ObservationRegistry` is available
- `MicrometerCapability` micrometerCapability: If `feign-micrometer` is on the classpath, `MeterRegistry` is available and `ObservationRegistry` is not available
- `CachingCapability` cachingCapability: If `@EnableCaching` annotation is used. Can be disabled via `spring.cloud.openfeign.cache.enabled`.
- `Contract` feignContract: `SpringMvcContract`
- `Feign.Builder` feignBuilder: `FeignCircuitBreaker.Builder`
- `Client` feignClient: If Spring Cloud LoadBalancer is on the classpath, `FeignBlockingLoadBalancerClient` is used. If none of them is on the classpath, the default feign client is used.

对与使用  OkHttpClient-backed Feign 客户端和 Http2Client Feign 客户端，请确保进行依赖：

~~~properties
spring.cloud.openfeign.okhttp.enabled=true
spring.cloud.openfeign.http2client.enabled=true 
~~~

**提醒：**

Spring Cloud OpenFeign 4以后， Apache HttpClient 4 不再支持。使用 Apache HttpClient 5。

Spring Cloud OpenFeign 不提供下面类型的默认 bean：

- `Logger.Level`
- `Retryer`
- `ErrorDecoder`
- `Request.Options`
- `Collection<RequestInterceptor>`
- `SetterFactory`
- `QueryMapEncoder`
- `Capability` (`MicrometerObservationCapability` and `CachingCapability` are provided by default)

创建其中一种类型的 bean，在 `@FeignClient` configuration 属性。例如：

~~~java
@FeignClient(name = "stores", configuration = FooConfiguration.class)

@Configuration
public class FooConfiguration {
	@Bean
	public Contract feignContract() {
		return new feign.Contract.Default();
	}

	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
		return new BasicAuthRequestInterceptor("user", "password");
	}
}
~~~



@FeignClient 也可以使用配置属性进行配置。

~~~yaml
spring:
	cloud:
		openfeign:
			client:
				config:
					feignName:
            url: http://remote-service.com
						connectTimeout: 5000
						readTimeout: 5000
						loggerLevel: full
						errorDecoder: com.example.SimpleErrorDecoder
						retryer: com.example.SimpleRetryer
						defaultQueryParameters:
							query: queryValue
						defaultRequestHeaders:
							header: headerValue
						requestInterceptors:
							- com.example.FooRequestInterceptor
							- com.example.BarRequestInterceptor
						responseInterceptor: com.example.BazResponseInterceptor
						dismiss404: false
						encoder: com.example.SimpleEncoder
						decoder: com.example.SimpleDecoder
						contract: com.example.SimpleContract
						capabilities:
							- com.example.FooCapability
							- com.example.BarCapability
						queryMapEncoder: com.example.SimpleQueryMapEncoder
						micrometer.enabled: false
~~~

feignName 指的是 `@FeignClient` 值，`@FeignClient` name 属性 和 `@FeignClient` contextId 属性。



默认配置可以在 @EnableFeignClients 注解 defaultConfiguration 属性中具体说明。这将会应用到所有 Feign Client。

您可以使用 spring.cloud.openfeign.client.config.feignName.defaultQueryParameters 和 spring.cloud.openfeign.client.config.feignName.defaultRequestHeaders 来指定查询参数和头，这些参数和头将随客户端名为feignName 的每个请求一起发送。

~~~properties
spring:
	cloud:
		openfeign:
			client:
				config:
					default:
						connectTimeout: 5000
						readTimeout: 5000
						loggerLevel: basic
~~~

`@Configuration` bean 和 configuration properties 都配置了，则 configuration properties 会生效。除非：`spring.cloud.openfeign.client.default-to-properties` to `false`.



如果我们想要创建多个具有相同名称或url的 feign client，以便它们指向相同的服务器，但每个客户端都有不同的自定义配置，那么我们必须使用 @FeignClient 的 contextId 属性，以避免这些配置 bean 的名称冲突。

~~~java
@FeignClient(contextId = "fooClient", name = "stores", configuration = FooConfiguration.class)
public interface FooClient {
	//..
}

@FeignClient(contextId = "barClient", name = "stores", configuration = BarConfiguration.class)
public interface BarClient {
	//..
}
~~~



也可以将 FeignClient 配置为不从父上下文继承 bean。你可以通过重写 FeignClientConfigurer bean 中的 inheritParentConfiguration()来返回 false：

~~~java
@Configuration
public class CustomConfiguration {
	@Bean
	public FeignClientConfigurer feignClientConfigurer() {
		return new FeignClientConfigurer() {
			@Override
			public boolean inheritParentConfiguration() {
				 return false;
			}
		};
	}
}
~~~

默认情况下，Feign客户端不编码斜杠/字符。你可以通过将 spring.cloud.openfeign.client.decodeSlash 的值设置为 false 来改变这种行为。



**`SpringEncoder` 配置**

在我们提供的 SpringEncoder 中，我们为二进制内容类型设置了空字符集，为所有其他内容类型设置了 UTF-8。

你可以通过设置 `spring.cloud.openfeign.encoder.charset-from-content-type` 的值为 `true`，来改变这种行为，从而根据 Content-Type 标头中的字符集来提取字符集。







































































































