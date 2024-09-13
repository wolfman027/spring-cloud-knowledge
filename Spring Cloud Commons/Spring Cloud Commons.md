# Cloud Native Applications

> 云原生应用 - 基于 4.1.4



## 介绍

> https://12factor.net/ 12 因素 APP

Cloud Native 是一种应用程序开发风格，它鼓励在持续交付和价值驱动开发领域轻松采用最佳实践。一个相关的规程是构建[12-factor Applications](https://12factor.net/)，其中开发实践与交付和操作目标保持一致 —— 例如，通过使用声明式编程、管理和监控。Spring Cloud 以许多特定的方式促进了这些开发风格。起点是一组特性，分布式系统中的所有组件都需要易于访问这些特性。

其中许多特性都由 Spring Boot 提供，Spring Cloud 就是在这个平台上构建的。Spring Cloud 还提供了两个库：Spring Cloud Context和 Spring Cloud Commons。

Spring Cloud Context 为 Spring Cloud 应用程序的 ApplicationContext（引导上下文、加密、刷新范围和环境端点）提供实用工具和特殊服务。

Spring Cloud Commons 是一组抽象和通用类，用于不同的 Spring Cloud 实现（如 Spring Cloud Netflix 和 Spring Cloud Consul）。



## Spring Cloud Context: 应用上下文服务

Spring Boot 对如何使用 Spring 构建应用程序有一种固执己见的看法。例如，它具有用于公共配置文件的常规位置，并具有用于公共管理和监视任务的端点。Spring Cloud 在此基础上构建，并添加了系统中许多组件会使用或偶尔需要的一些特性。



### The Bootstrap Application Context

Spring Cloud 应用程序通过创建一个 “bootstrap” 上下文来运行，该上下文是主应用程序的父上下文。该上下文负责从外部源加载配置属性，并解密本地外部配置文件中的属性。这两个上下文共享一个 `Environment`，它是任何 Spring 应用程序的外部属性的来源。默认情况下，bootstrap 属性（不是指 `bootstrap.properties` 文件，而是指在引导阶段加载的属性）具有较高的优先级，因此它们不能被本地配置所覆盖。

bootstrap 上下文使用与主应用程序上下文不同的约定来定位外部配置。除了使用 `application.yml`（或 `.properties`）之外，你可以使用 `bootstrap.yml`，这样可以将外部配置清晰地分离为 bootstrap 配置和主上下文配置。

下面的清单显示了一个示例：

**bootstrap.yml**

```yaml
spring:
  application:
    name: foo
  cloud:
    config:
      uri: ${SPRING_CONFIG_URI:http://localhost:8888}
```

如果您的应用程序需要从服务器获得任何特定于应用程序的配置，那么最好设置 `spring.application.name` (在  `bootstrap.yml`  或 `application.yml`)。为了将属性 `spring.application.name` 用作应用程序的上下文ID，您必须在 `bootstrap.[properties | yml]` 中设置它。

如果你想获取特定配置文件的配置，你也应该在 `bootstrap.[properties | yml]` 中设置 `spring.profiles.active`。

你可以通过设置 `spring.cloud.bootstrap.enabled=false`（例如，在系统属性中）来完全禁用引导加载过程。



### 应用程序上下文层次结构

如果你通过 `SpringApplication` 或 `SpringApplicationBuilder` 构建一个应用上下文，那么 Bootstrap 上下文会被添加为该上下文的父级。这是 Spring 的一个特性，子上下文会从它们的父上下文中继承属性源和配置文件，因此与没有使用 Spring Cloud Config 构建相同的上下文相比，“主”应用上下文包含了额外的属性源。

额外的属性源是：

















