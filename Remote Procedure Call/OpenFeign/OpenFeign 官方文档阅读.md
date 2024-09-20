# Spring Cloud OpenFeign 特性

> 基于 4.1.3 版本学习



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



# 3. 超时处理

默认和指定客户端上都能配置超时。

有两个超时参数：

- connectTimeout：防止由于较长的服务器处理时间而阻塞调用方。
- readTimeout：从连接建立时起应用，并在返回响应花费太长时间时触发。

> **注意：**
>
> 如果服务器没有运行或不可用，数据包会导致连接被拒绝。通信最终会以错误消息结束，或者退回到备用方案。这种情况可能会发生在连接超时之前，特别是如果连接超时时间设置得非常短的情况下。执行查找和接收这样的数据包所需的时间是造成这种延迟的重要部分，而这一时间是根据远程主机涉及的DNS查找情况而变化的。



# 4. 手动创建 Feign Client

下面是一个示例，它创建了两个具有相同接口的 Feign 客户端，但为每个客户端配置了一个单独的请求拦截器。

~~~java
@Import(FeignClientsConfiguration.class)
class FooController {

	private FooClient fooClient;

	private FooClient adminClient;

	@Autowired
	public FooController(Client client, Encoder encoder, Decoder decoder, Contract contract, MicrometerObservationCapability micrometerObservationCapability) {
		this.fooClient = Feign.builder().client(client)
				.encoder(encoder)
				.decoder(decoder)
				.contract(contract)
				.addCapability(micrometerObservationCapability)
				.requestInterceptor(new BasicAuthRequestInterceptor("user", "user"))
				.target(FooClient.class, "https://PROD-SVC");

		this.adminClient = Feign.builder().client(client)
				.encoder(encoder)
				.decoder(decoder)
				.contract(contract)
				.addCapability(micrometerObservationCapability)
				.requestInterceptor(new BasicAuthRequestInterceptor("admin", "admin"))
				.target(FooClient.class, "https://PROD-SVC");
	}
}
~~~

> **注意：**
>
> - Spring Cloud OpenFeign 提供默认配置：`FeignClientsConfiguration.class`。
> - PROD-SVC 是客户端将向其发出请求的服务的名称。
> - Feign 的 Contract 对象定义了接口上有效的注解和值。自动装配的 Contract Bean 提供了对 SpringMVC 注解的支持，而不是默认的 Feign 本地注解。

你也可以使用 Builder 来配置 FeignClient 不从父上下文继承 Bean。你可以通过调用 `Builder` 的 `inheritParentContext(false)` 方法来实现这一点。



# 5. Feign Spring Cloud CircuitBreaker 支持

如果 Spring Cloud CircuitBreaker 在类路径上并且 `spring.cloud.openfeign.circuitbreaker.enabled=true`。Feign将用 circuit breaker 封装所有方法。

要在每个客户端的基础上禁用 Spring Cloud CircuitBreaker 支持，可以创建一个具有 “prototype” 作用域的基本 Feign.Builder，例如：

~~~java
@Configuration
public class FooConfiguration {
	@Bean
	@Scope("prototype")
	public Feign.Builder feignBuilder() {
		return Feign.builder();
	}
}
~~~

circuit breaker 名字遵循 `<feignClientClassName>#<calledMethod>(<parameterTypes>)` 模式。

当调用带有 `FooClient` 接口的 `@FeignClient` 并且被调用的接口方法没有参数是 `bar` 时，circuit breaker 名称将是  `FooClient#bar()`。

提供了 `CircuitBreakerNameResolver`，可以改变断路器名字模式。

~~~java
import feign.Target;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.lang.reflect.Method;

@Configuration
public class CustomCircuitBreakerNameConfiguration {
    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (String feignClientName, Target<?> target, Method method) -> feignClientName + "_" + method.getName();
    }
}
~~~

要启用 Spring Cloud CircuitBreaker 组，设置 `spring.cloud.openfeign.circuitbreaker.group.enabled=true` (默认为false)。



# 6. 配置文件配置 Configuring CircuitBreakers

例如，你有下面的 Feign Client：

~~~java
@FeignClient(url = "http://localhost:8080")
public interface DemoClient {
    @GetMapping("demo")
    String getDemo();
}
~~~

通过下面的配置文件配置：

~~~properties
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
        alphanumeric-ids:
          enabled: true
resilience4j:
  circuitbreaker:
    instances:
      DemoClientgetDemo:
        minimumNumberOfCalls: 69
  timelimiter:
    instances:
      DemoClientgetDemo:
        timeoutDuration: 10s
~~~

> **注意：**
>
> 如果你想切换回在 Spring Cloud 2022.0.0 之前使用的断路器名称，设置 `spring.cloud.openfeign.circuitbreaker.alphanumeric-ids.enabled=false`。



# 7. Feign Spring Cloud CircuitBreaker Fallbacks

Spring Cloud CircuitBreaker 支持 fallback 的概念：当 circuit 打开或出现错误时执行的默认代码路径。

~~~properties
spring:
  application:
    name: competition-management
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
      alphanumeric-ids:
        enabled: true
resilience4j:
  circuitbreaker:
    instances:
      DemoClientgetDemo:
        minimumNumberOfCalls: 69
  timelimiter:
    instances:
      DemoClientgetDemo:
        timeoutDuration: 10s
~~~



~~~java
@FeignClient(name = "customer-management", contextId = "customerManagementClient", fallback = CustomerManagementClientFallback.class)
public interface CustomerManagementClient {

    @RequestMapping("/customer/{id}")
    CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id);

}

@Slf4j
@Component
public class CustomerManagementClientFallback implements CustomerManagementClient {
    @Override
    public CustomerInfoDTO getCustomerInfo(String id) {
        log.info("fallback");
        return null;
    }
}
~~~

如果需要访问导致回退触发的原因，可以使用 @FeignClient 中的 fallbackFactory 属性。

~~~java
@FeignClient(name = "customer-management", contextId = "customerManagementClient",
        fallbackFactory = CustomerManagementClientFallbackFactory.class)
public interface CustomerManagementClient {
    @RequestMapping("/customer/{id}")
    CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id);
}

@Slf4j
@Component
public class CustomerManagementClientFallbackFactory implements FallbackFactory<FallbackWithFactory> {
    @Override
    public FallbackWithFactory create(Throwable cause) {
        log.error("fallback reason was {}", cause.getMessage());
        return new FallbackWithFactory();
    }
}

public class FallbackWithFactory implements CustomerManagementClient {
    @Override
    public CustomerInfoDTO getCustomerInfo(String id) {
        return new CustomerInfoDTO();
    }
}
~~~



# 8. Feign and `@Primary`

当使用Spring Cloud CircuitBreaker fallbacks 的 Feign 时，ApplicationContext 中有多个相同类型的 bean。这将导致@Autowired无法工作，因为没有一个bean，也没有一个标记为 primary 的 bean。为了解决这个问题，Spring Cloud OpenFeign 将所有 Feign 实例标记为 @Primary，这样Spring框架就知道要注入哪个bean。在某些情况下，这可能是不可取的。要关闭此行为，请将 @FeignClient 的主要属性设置为 false。

~~~java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface FeignClient {
	boolean primary() default true;
}

@FeignClient(name = "hello", primary = false)
public interface HelloClient {
	// methods here
}
~~~



# 9. Feign 继承支持

Feign 通过单继承接口支持样板 api。这允许将常见的操作组合到方便的基础接口中。

~~~java
public interface UserService {
	@GetMapping("/users/{id}")
	User getUser(@PathVariable("id") long id);
}

@RestController
public class UserResource implements UserService {

}

@FeignClient("users")
public interface UserClient extends UserService {
}
~~~

> [!WARNING]
>
> `@FeignClient` 接口不应该在服务端和客户端之间共享，并且在类级别上使用 `@RequestMapping` 注解 `@FeignClient` 接口的做法已不再支持。

[[feign-request/response-compression]] === Feign request/response compression

通过下面配置，启动 request 或 response 进行 GZIP 压缩：

~~~properties
spring.cloud.openfeign.compression.request.enabled=true
spring.cloud.openfeign.compression.response.enabled=true
~~~

Feign 请求压缩提供类似 web 服务器设置：

~~~properties
spring.cloud.openfeign.compression.request.enabled=true
spring.cloud.openfeign.compression.request.mime-types=text/xml,application/xml,application/json
spring.cloud.openfeign.compression.request.min-request-size=2048
~~~

这些属性允许您选择压缩 media types 和 minimum 请求阈值长度。

> [!TIP]
>
> 由于 OkHttpClient 使用 "transparent" 的压缩机制，如果存在 `content-encoding` 或 `accept-encoding` 头，则会禁用压缩。因此，当 `feign.okhttp.OkHttpClient` 存在于类路径中并且 `spring.cloud.openfeign.okhttp.enabled` 被设置为 `true` 时，我们不会启用压缩。



# 10. Feign logging

为创建的每个 Feign 客户机创建一个日志记录器。默认情况下，logger 的名称是用于创建 Feign 客户端的接口的完整类名。Feign 日志只响应 DEBUG 级别。

~~~yaml
logging:
  level:
    com.wolfman.marathon.feign.CustomerManagementClient: DEBUG
~~~

可以为每个客户端配置 Logger.Level。级别有：

- `NONE`：No logging (**DEFAULT**).
- `BASIC`：记录仅为请求方法和 URL，响应状态码和执行时间。
- `HEADERS`：记录基本信息以及请求和响应头。
- `FULL`：记录请求和响应的头，body，元数据。

下面代码会设置日志级别为 FULL，需要与上边的 yaml 中注解一起使用：

~~~java
@Configuration
public class FooConfiguration {
	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.BASIC;
	}
}

// [CustomerManagementClient#getCustomerInfo] ---> GET http://customer-management/customer/1 HTTP/1.1
// [CustomerManagementClient#getCustomerInfo] <--- HTTP/1.1 200  (73ms)
~~~





# 11. Feign Capability 支持

Feign 的功能（capabilities）暴露了核心的 Feign 组件，这样这些组件就可以被修改。例如，功能可以接收 Client，对其进行装饰，然后将装饰后的实例返回给 Feign。对 Micrometer 的支持就是一个很好的实际例子。

创建一个或多个 `Capability` bean 并将它们放在 `@FeignClient` 配置中，这样您就可以注册它们并修改所涉及的客户端的行为。

```java
@Configuration
public class FooConfiguration {
	@Bean
	Capability customCapability() {
		return new CustomCapability();
	}
}
```



# 12. Micrometer 支持

下列条件都为 `true`，`MicrometerObservationCapability` bean 被创建和注册，以便 Micrometer 可以观察到您的 Feign 客户端：

- `feign-micrometer` is on the classpath
- `ObservationRegistry` bean 是可用的
- feign micrometer properties are set to `true` (by default)
  - `spring.cloud.openfeign.micrometer.enabled=true` (for all clients)
  - `spring.cloud.openfeign.client.config.feignName.micrometer.enabled=true` (for a single client)

> [!NOTE]
>
> 如果您的应用程序已经使用了 Micrometer，那么启用这个特性就像在类路径中添加 `feign-micrometer` 一样简单。

你可以使这个特性失效：

- excluding `feign-micrometer` from your classpath
- setting one of the feign micrometer properties to `false`
  - `spring.cloud.openfeign.micrometer.enabled=false`
  - `spring.cloud.openfeign.client.config.feignName.micrometer.enabled=false`

> [!NOTE]
>
> `spring.cloud.openfeign.micrometer.enabled=false` 是对所有 Feign Client 的配置。
>
> 如果想要设置每个 client，使用 `spring.cloud.openfeign.client.config.feignName.micrometer.enabled`。

你也可以通过注册你自己的 bean 来定制 `MicrometerObservationCapability` :

~~~java
@Configuration
public class FooConfiguration {
	@Bean
	public MicrometerObservationCapability micrometerObservationCapability(ObservationRegistry registry) {
		return new MicrometerObservationCapability(registry);
	}
}
~~~

仍然可以使用 `MicrometerCapability` 与 Feign 一起工作（仅支持指标），你需要禁用 Micrometer 支持（设置 `spring.cloud.openfeign.micrometer.enabled=false`），并创建一个 `MicrometerCapability` 的 Bean：

~~~java
@Configuration
public class FooConfiguration {
	@Bean
	public MicrometerCapability micrometerCapability(MeterRegistry meterRegistry) {
		return new MicrometerCapability(meterRegistry);
	}
}
~~~



# 13. Feign Caching

如果使用了 `@EnableCaching` 注释，就会创建并注册一个 `CachingCapability`  bean，这样你的 Feign 客户端就可以在它的接口上识别 `@Cache*`  注释：

~~~java
// 需要配置 -parameters 参数
public interface DemoClient {
  
  @GetMapping("/customer/default-customer-name")
  @Cacheable(cacheNames = "demo-cache", key = "#name") 
  String defaultCustomerName(@RequestParam("name") String name);
  
}
~~~

你也可以通过属性  `spring.cloud.openfeign.cache.enabled=false` 禁用该特性。



# 14. Spring @RequestMapping 支持

定义一个接口使用参数属性：

~~~java
@FeignClient("demo")
public interface DemoTemplate {
        
  @PostMapping(value = "/stores/{storeId}", params = "mode=upsert")
  Store update(@PathVariable("storeId") Long storeId, Store store);
  
}
~~~

上述例子，请求 url 解析为：`/stores/storeId?mode=upsert`。

params 属性还支持使用多个 `key=value` 或只使用一个 `key`：

- 当 `params = { "key1=v1", "key2=v2" }`，请求 url 解析为：`/stores/storeId?key1=v1&key2=v2`
- 当 `params = "key"`，请求 url 解析为：`/stores/storeId?key`



# 15. Feign @QueryMap 支持

Spring Cloud OpenFeign 提供了一个等效的 `@SpringQueryMap` 注解，用于将 POJO 或 Map 参数注释为查询参数映射。

例如：

~~~java
// Params.java
public class CustomerRequestParams {
	private String name;
	private String phone;
	// [Getters and setters omitted for brevity]
}
~~~

使用 `@SpringQueryMap` 注解：

~~~java
@FeignClient("demo")
public interface DemoTemplate {

  @GetMapping(path = "/customer/check-existed")
  String checkExisted(@SpringQueryMap CustomerRequestParams params);

}

// [CustomerManagementClient#checkExisted] ---> GET http://customer-management/customer/check-existed?phone=132&name=rose HTTP/1.1
~~~

如果需要对生成的查询参数映射进行更多的控制，可以实现自定义的 `QueryMapEncoder` bean。



# 16. HATEOAS support

Spring 提供了一些api来创建遵循 [HATEOAS](https://en.wikipedia.org/wiki/HATEOAS) 原则、[Spring Hateoas](https://spring.io/projects/spring-hateoas) 和 [Spring Data REST](https://spring.io/projects/spring-data-rest) 的 REST 表示。

`org.springframework.boot:spring-boot-starter-hateoas`  或 `org.springframework.boot:spring-boot-starter-data-rest` ，默认情况下启用 Feign HATEOAS 支持。

当 HATEOAS 支持开启，Feign clients 允许序列化和反序列化 HATEOAS 表示的模型：EntityModel、CollectionModel、PagedModel。

~~~java
@FeignClient("demo")
public interface DemoTemplate {

	@GetMapping(path = "/stores")
	CollectionModel<Store> getStores();
  
}
~~~



# 17. Spring @MatrixVariable 支持

如果将一个映射（map）作为方法参数传递，那么 `@MatrixVariable` 路径段是通过将映射中的键值对用 `=` 连接起来创建的。

~~~java
@GetMapping("/objects/links/{matrixVars}")
Map<String, List<String>> getObjects(@MatrixVariable Map<String, List<String>> matrixVars);
~~~



# 18. Feign `CollectionFormat` 支持

在下面的示例中，使用CSV格式而不是默认的 `EXPLODED` 式格式来处理该方法。

~~~java
@FeignClient(name = "demo")
protected interface DemoFeignClient {

    @CollectionFormat(feign.CollectionFormat.CSV)
    @GetMapping(path = "/test")
    ResponseEntity performRequest(String test);

}
~~~



# 19. Reactive Support

由于 OpenFeign 项目目前不支持响应式客户端(如Spring WebClient)，因此 Spring Cloud OpenFeign 也不支持。





# 20. Spring Data Support

如果 Jackson Databind 和 Spring Data Commons 在类路径上，那么会自动添加针对 `org.springframework.data.domain.Page` 和 `org.springframework.data.domain.Sort` 的转换器。

要禁用此行为，请设置

```properties
spring.cloud.openfeign.autoconfiguration.jackson.enabled=false
```

详情请参见 `org.springframework.cloud.openfeign.FeignAutoConfiguration.FeignJacksonConfiguration`。





# 转换 load-balanced HTTP 请求

您可以使用选定的 ServiceInstance 来转换负载均衡的HTTP请求。

对于 Request，你需要实现和定义 LoadBalancerFeignRequestTransformer，如下所示：

~~~java
@Bean
public LoadBalancerFeignRequestTransformer transformer() {
	return new LoadBalancerFeignRequestTransformer() {
		@Override
		public Request transformRequest(Request request, ServiceInstance instance) {
			Map<String, Collection<String>> headers = new HashMap<>(request.headers());
			headers.put("X-ServiceId", Collections.singletonList(instance.getServiceId()));
			headers.put("X-InstanceId", Collections.singletonList(instance.getInstanceId()));
			return Request.create(request.httpMethod(), request.url(), headers, request.body(), request.charset(),
					request.requestTemplate());
		}
	};
  
}
~~~

如果定义了多个转换器，则按照定义 bean 的顺序应用它们。或者，您可以使用 `LoadBalancerFeignRequestTransformer.DEFAULT_ORDER` 指定顺序。



# X-Forwarded Headers Support

~~~properties
spring.cloud.loadbalancer.x-forwarded.enabled=true
~~~

















