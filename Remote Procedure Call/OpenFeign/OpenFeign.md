# OpenFeign（声明式的伪RPC）

面向接口进行开发

OKHTTP 优化了很多 HTTP 方法，所以性能要不HTTP要高。

~~~xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>2.2.3.RELEASE</version>
</dependency>
~~~

~~~java
@FeignClient("spring-cloud-order-service")
public interface OrderServiceFeignClient {
    
    @GetMapping("/orders")
    String getAllOrder();

}
~~~

~~~java
@RestController
public class OpenFeignController {

    @Autowired
    private OrderServiceFeignClient orderServiceFeignClient; //动态代理

    @GetMapping("/test")
    public String test(){
        return orderServiceFeignClient.getAllOrder();
    }

}
~~~

~~~java
@EnableFeignClients(basePackages = "com.wolfman.netflix.clients")
@SpringBootApplication
public class UserServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceProviderApplication.class, args);
    }
}
~~~



## 示例

- competition-management
- customer-management



# 特性

默认底层通信是 httpclient 的api

使用okhttp：

~~~xml
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
</dependency>
~~~

~~~properties
feign.okhttp.enabled=true
feign.httpclient.enabled=false

# 提供压缩的方式对请求数据进行压缩
feign.compression.response.enabled=true
#请求最小数据的大小
feign.compression.request.min-request-size=2180
#针对哪些请求类型进行压缩
feign.compression.request.mime-types=

# 还有一些http的链接池相关配置
feign.httpclient.max-connections=200
feign.httpclient.connection-timeout=5000
~~~



# 思考 feign 要做的事情

- 参数的解析和装载
- 针对指定的 feignClient，生成动态代理
- 针对 feignClient 中的方法描述进行解析
- 组装出一个Request对象，发起请求

----



# Bean 的动态装载

> 如何动态装载 Bean 对象到 SpringIoC 容器中

- ImportSelector
- ImportBeanDefinitionRegistrar

## ImportBeanDefinitionRegistrar 怎么工作的

- 定义一个需要被装载到 IOC 容器中的类 HelloWorld

~~~java
public class HelloWorld {}
~~~

- 定义一个 Registrar 的实现，定义一个 bean，装载到 IoC 容器

~~~java
public class ExtendImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(HelloWorld.class.getName());
        registry.registerBeanDefinition("hello", beanDefinition);
    }

}
~~~

- 定义一个注解类

~~~java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ExtendImportBeanDefinitionRegistrar.class)
public @interface EnableHelloWorldRegistrar {
}
~~~

- 写一个测试类

~~~java

@EnableHelloWorldRegistrar
@EnableFeignClients(basePackages = {"com.wolfman.marathon"})
@SpringBootApplication
public class CompetitionManagementApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(CompetitionManagementApplication.class, args);
		System.out.println(configurableApplicationContext.getBean("hello"));
    //com.wolfman.marathon.knowledgeExtend.HelloWorld@6b063470
	}

}
~~~

通过结果演示可以发现，HelloWorld 这个 bean 已经装载到了 IoC 容器。

这就是动态装载的功能实现，它相比于 @Configuration 配置注入，会多了很多的灵活性。





# OpenFeign 源码分析

# 服务启动

## EnableFeignClients

**@EnableFeignClients(basePackages = {"com.wolfman.marathon"})**

这个注解开启了 `@FeignClient` 注解的解析过程。

~~~java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({FeignClientsRegistrar.class})	// 通过 @Import 注解，导入 FeignClientsRegistrar 配置信息
public @interface EnableFeignClients {
    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};
}
~~~



## FeignClientsRegistrar

FeignClientsRegistrar 实现了 ImportBeanDefinitionRegistrar，它是一个动态注入bean的接口， Spring Boot启动的时候，会去调用这个类中的 `registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry)` 来实现动态 Bean 的装载。 它的作用类似于ImportSelector。

~~~java
class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

 	
  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
    //注册 @EnableFeignClients 中定义 defaultConfiguration 属性下的类，包装成 FeignClientSpecification，注册到Spring容器。
		//在 @FeignClient 中有一个属性 :configuration，这个属性是表示各个FeignClient自定义的配置类，后面也会通过调用registerClientConfiguration 方法来注册成 FeignClientSpecification 到容器。 
    //所以，这里可以完全理解在 @EnableFeignClients 中配置的是做为兜底的配置，在各自 @FeignClient 配置的就是自定义的情况。
    
    //注册默认配置
		registerDefaultConfiguration(metadata, registry);
    //注册feignClient
		registerFeignClients(metadata, registry);
	}
}
~~~

- `registerDefaultConfiguration(metadata, registry);` 方法内部从 SpringBoot 启动类上检查是否有 @EnableFeignClients, 有该注解的话， 则完成 Feign 框架相关的一些配置内容注册。
- `registerFeignClients(metadata, registry);` 方法内部从 classpath 中， 扫描获得 @FeignClient 修饰的类， 将类的内容解析为 BeanDefinition , 最终通过调用 Spring 框架中的 BeanDefinitionReaderUtils.resgisterBeanDefinition 将解析处理过的 FeignClient BeanDeifinition 添加到 spring 容器中。



### FeignClientsRegistrar.registerFeignClients

这个方法主要是扫描类路径下所有的 `@FeignClient` 注解，然后进行动态 Bean 的注入。它最终会调用 registerFeignClient 方法。

~~~java
	public void registerFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>();
		Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableFeignClients.class.getName());
		final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");
		if (clients == null || clients.length == 0) {
			ClassPathScanningCandidateComponentProvider scanner = getScanner();
			scanner.setResourceLoader(this.resourceLoader);
			scanner.addIncludeFilter(new AnnotationTypeFilter(FeignClient.class));
			Set<String> basePackages = getBasePackages(metadata);
			for (String basePackage : basePackages) {
				candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
			}
		}
		else {
			for (Class<?> clazz : clients) {
				candidateComponents.add(new AnnotatedGenericBeanDefinition(clazz));
			}
		}
	
    // 循环注入带有 @FeignClient 的接口，并进行动态代理注入到 IoC 容器中。
		for (BeanDefinition candidateComponent : candidateComponents) {
			if (candidateComponent instanceof AnnotatedBeanDefinition beanDefinition) {
				// verify annotated class is an interface
				AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
				Assert.isTrue(annotationMetadata.isInterface(), "@FeignClient can only be specified on an interface");

				Map<String, Object> attributes = annotationMetadata
						.getAnnotationAttributes(FeignClient.class.getCanonicalName());

				String name = getClientName(attributes);
				String className = annotationMetadata.getClassName();
        // 注册 @FeignClient 注解的配置信息
				registerClientConfiguration(registry, name, className, attributes.get("configuration"));
				// 注册 FeignClient 类
				registerFeignClient(registry, annotationMetadata, attributes);
			}
		}
	}
~~~



### FeignClientsRegistrar.registerFeignClient

> spring.cloud.openfeign.lazy-attributes-resolution 属性，openFeignClient 是否需要进行懒加载

~~~java
private void registerFeignClient(BeanDefinitionRegistry registry, 
                                 AnnotationMetadata annotationMetadata,
                                 Map<String, Object> attributes) {
	// className = com.wolfman.marathon.feign.CustomerManagementClient
  String className = annotationMetadata.getClassName();
	if (String.valueOf(false).equals(
			environment.getProperty("spring.cloud.openfeign.lazy-attributes-resolution", 
                              String.valueOf(false)))) {
		eagerlyRegisterFeignClientBeanDefinition(className, attributes, registry);
	}
	else {
		lazilyRegisterFeignClientBeanDefinition(className, attributes, registry);
	}
}
~~~

















----































### FeignClientsRegistrar.registerFeignClient

~~~java
private void registerFeignClient(BeanDefinitionRegistry registry,
		AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
	String className = annotationMetadata.getClassName();
    // 注入一个工厂bean
    // → FeignClientFactoryBean
	BeanDefinitionBuilder definition = BeanDefinitionBuilder
			.genericBeanDefinition(FeignClientFactoryBean.class);
	validate(attributes);
	definition.addPropertyValue("url", getUrl(attributes));
	definition.addPropertyValue("path", getPath(attributes));
	String name = getName(attributes);
	definition.addPropertyValue("name", name);
	String contextId = getContextId(attributes);
	definition.addPropertyValue("contextId", contextId);
	definition.addPropertyValue("type", className);
	definition.addPropertyValue("decode404", attributes.get("decode404"));
	definition.addPropertyValue("fallback", attributes.get("fallback"));
	definition.addPropertyValue("fallbackFactory", attributes.get("fallbackFactory"));
	definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

	String alias = contextId + "FeignClient";
	AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

	boolean primary = (Boolean) attributes.get("primary"); // has a default, won't be
															// null

	beanDefinition.setPrimary(primary);

	String qualifier = getQualifier(attributes);
	if (StringUtils.hasText(qualifier)) {
		alias = qualifier;
	}

	BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
			new String[] { alias });
	BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
}
~~~

### FeignClientFactoryBean.getObject

getObject 调用的是 getTarget 方法，它从applicationContext取出FeignContext，FeignContext继承了 NamedContextFactory，它是用来来统一维护feign中各个feign客户端相互隔离的上下文。

> FeignContext注册到容器是在FeignAutoConfiguration上完成的

~~~java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties({ FeignClientProperties.class,
		FeignHttpClientProperties.class })
@Import(DefaultGzipDecoderConfiguration.class)
public class FeignAutoConfiguration {
    
    @Autowired(required = false)
	private List<FeignClientSpecification> configurations = new ArrayList<>();
    
    @Bean
	public FeignContext feignContext() {
		FeignContext context = new FeignContext();
		context.setConfigurations(this.configurations);
		return context;
	}
}
~~~



> 在初始化FeignContext时，会把configurations在容器中放入FeignContext中。configurations 的来源就是在前面 registerFeignClients 方法中将@FeignClient 的配置 configuration。

接着，构建 feign.builder，在构建时会向 FeignContext 获取配置的 Encoder，Decoder 等各种信息。 FeignContext 在上篇中已经提到会为每个 Feign 客户端分配了一个容器，它们的父容器就是 spring 容器配置完 Feign.Builder 之后，再判断是否需要 LoadBalance，如果需要，则通过 LoadBalance 的方法来设置。实际上他们最终调用的是 Target.target() 方法。

~~~java
class FeignClientFactoryBean
		implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
        
    @Override
	public Object getObject() throws Exception {
		return getTarget();
	}
    
    <T> T getTarget() {
        //实例化Feign上下文对象FeignContext
		FeignContext context = this.applicationContext.getBean(FeignContext.class);
        //构建Builder对象
		Feign.Builder builder = feign(context);
		
        //如果url为空，则走负载均衡，生成有负载均衡功能的代理类
		if (!StringUtils.hasText(this.url)) {
			if (!this.name.startsWith("http")) {
				this.url = "http://" + this.name;
			}
			else {
				this.url = this.name;
			}
			this.url += cleanPath();
			return (T) loadBalance(builder, context,
					new HardCodedTarget<>(this.type, this.name, this.url));
		}
        //如果指定了url，则生成默认的代理类
		if (StringUtils.hasText(this.url) && !this.url.startsWith("http")) {
			this.url = "http://" + this.url;
		}
		String url = this.url + cleanPath();
		Client client = getOptional(context, Client.class);
		if (client != null) {
			if (client instanceof LoadBalancerFeignClient) {
				// not load balancing because we have a url,
				// but ribbon is on the classpath, so unwrap
				client = ((LoadBalancerFeignClient) client).getDelegate();
			}
			if (client instanceof FeignBlockingLoadBalancerClient) {
				// not load balancing because we have a url,
				// but Spring Cloud LoadBalancer is on the classpath, so unwrap
				client = ((FeignBlockingLoadBalancerClient) client).getDelegate();
			}
			builder.client(client);
		}
        //生成默认代理类
		Targeter targeter = get(context, Targeter.class);
		return (T) targeter.target(this, builder, context,
				new HardCodedTarget<>(this.type, this.name, url));
	}
}
~~~

#### FeignClientFactoryBean.feign

~~~java
protected Feign.Builder feign(FeignContext context) {
	FeignLoggerFactory loggerFactory = get(context, FeignLoggerFactory.class);
	Logger logger = loggerFactory.create(this.type);

	// @formatter:off
	Feign.Builder builder = get(context, Feign.Builder.class)
			// required values
			.logger(logger)
			.encoder(get(context, Encoder.class))
			.decoder(get(context, Decoder.class))
        	//用来解析模板的
			.contract(get(context, Contract.class));
	// @formatter:on

	configureFeign(context, builder);

	return builder;
}
~~~

#### FeignClientFactoryBean.loadBalance

生成具备负载均衡能力的feign客户端，为feign客户端构建起绑定负载均衡客户端

Client client = (Client)this.getOptional(context, Client.class);

从上下文中获取一个 Client，默认是LoadBalancerFeignClient。 它是在FeignRibbonClientAutoConfiguration这个自动装配类中，通过Import实现的

~~~java
@Import({ HttpClientFeignLoadBalancedConfiguration.class,
		OkHttpFeignLoadBalancedConfiguration.class,
		DefaultFeignLoadBalancedConfiguration.class })
public class FeignRibbonClientAutoConfiguration {
    
}

@Configuration(proxyBeanMethods = false)
class DefaultFeignLoadBalancedConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
			SpringClientFactory clientFactory) {
		return new LoadBalancerFeignClient(new Client.Default(null, null), cachingFactory,
				clientFactory);
	}

}
~~~



~~~java
protected <T> T loadBalance(Feign.Builder builder, FeignContext context,
		HardCodedTarget<T> target) {
	Client client = getOptional(context, Client.class);
	if (client != null) {
        //client 跟我们解析有关系
		builder.client(client);
		Targeter targeter = get(context, Targeter.class);
		return targeter.target(this, builder, context, target);
	}

	throw new IllegalStateException(
			"No Feign Client for loadBalancing defined. Did you forget to include spring-cloud-starter-netflix-ribbon?");
}
~~~



~~~java
interface Targeter {

	<T> T target(FeignClientFactoryBean factory, Feign.Builder feign,
			FeignContext context, Target.HardCodedTarget<T> target);

}
class DefaultTargeter implements Targeter {

	@Override
	public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign,
			FeignContext context, Target.HardCodedTarget<T> target) {
		return feign.target(target);
	}

}
public abstract class Feign {

    public static class Builder {
        
        public <T> T target(Target<T> target) {
            return build().newInstance(target);
        }
   
        public Feign build() {
            SynchronousMethodHandler.Factory synchronousMethodHandlerFactory =
                new SynchronousMethodHandler.Factory(client, retryer, requestInterceptors, logger,
                                                     logLevel, decode404, closeAfterDecode, propagationPolicy);
    
            ParseHandlersByName handlersByName = new ParseHandlersByName(contract, options, encoder, decoder, queryMapEncoder,
                                                                         errorDecoder, synchronousMethodHandlerFactory);
            return new ReflectiveFeign(handlersByName, invocationHandlerFactory, queryMapEncoder);
        }
    }

}


public class ReflectiveFeign extends Feign {


    @Override
  
    public <T> T newInstance(Target<T> target) {
        Map<String, MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
        Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<Method, MethodHandler>();
        List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<DefaultMethodHandler>();
        for (Method method : target.type().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            } else if (Util.isDefault(method)) {
                DefaultMethodHandler handler = new DefaultMethodHandler(method);
                defaultMethodHandlers.add(handler);
                methodToHandler.put(method, handler);
            } else {
                methodToHandler.put(method, nameToHandler.get(Feign.configKey(target.type(), method)));
            }
        }
        // ->
        InvocationHandler handler = factory.create(target, methodToHandler);
        T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(),
                                             new Class<?>[] {target.type()}, handler);
        for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
            defaultMethodHandler.bindTo(proxy);
        }
        return proxy;
    }
}
~~~





### 在调用时

~~~java
@RestController
public class TestController {


    @Autowired
    OrderServiceFeignClient orderServiceFeignClient;    // 动态代理


    @PostMapping("/order")
    public String insert(){
        return orderServiceFeignClient.insert("123213123");
    }

}
~~~

~~~java
public class ReflectiveFeign extends Feign {
    
    static class FeignInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("equals".equals(method.getName())) {
                try {
                    Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                    return equals(otherHandler);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else if ("hashCode".equals(method.getName())) {
                return hashCode();
            } else if ("toString".equals(method.getName())) {
                return toString();
            }
            // ->
            return dispatch.get(method).invoke(args);
        }
    }
}
~~~

~~~java
final class SynchronousMethodHandler implements MethodHandler {
    @Override
    public Object invoke(Object[] argv) throws Throwable {
        // 根据请求构建一个 requestTemplate
        // template 基本请求的数据都拿到了，只差 IP 和 端口
        RequestTemplate template = buildTemplateFromArgs.create(argv);
        Options options = findOptions(argv);
        Retryer retryer = this.retryer.clone();
        while (true) {
            try {
                // 执行并进行解码
                return executeAndDecode(template, options);
            } catch (RetryableException e) {
                try {
                    retryer.continueOrPropagate(e);
                } catch (RetryableException th) {
                    Throwable cause = th.getCause();
                    if (propagationPolicy == UNWRAP && cause != null) {
                        throw cause;
                    } else {
                        throw th;	
                    }
                }
                if (logLevel != Logger.Level.NONE) {
                    logger.logRetry(metadata.configKey(), logLevel);
                }
                continue;
            }
        }
    }
}
~~~





### openFeign 开启日志级别

~~~java
@Configuration
public class FeignLogConfig {

    @Bean
    Logger.Level feignLogger(){
        return Logger.Level.FULL;// 记录所有日志
    }
}
~~~

~~~properties
logging.level.com.wolfman.openfeign.clients.OrderServiceFeignClient=DEBUG
~~~



- 项目启动时，通过 @Import(FeignClientsRegistrar.class)，分别根据 FeignClient 注入多个FeignClientFactoryBean，当通过@Autowired 注入时，则是调用 FeignClientFactoryBean.getObject 来获得具体的实例





- @Loadbalancer
- LoadbalancerClient

> RestTemplate 进行拦截

- ILoadBalancer
- IRule（负载均衡策略，权重机制（区间算法）） / 定时任务在不断的发起模拟请求() -> LoadbalancerStat
- IPing（每10秒，去访问一次目标服务的地址，如果服务不可用，则提出无效服务）
- ServerList （定时任务，30s 执行一次更新服务器列表）
- 如何自定义负载均衡算法、自定义验活算法





- Feign -> http 通信：Ribbon 集成完成负载均衡













