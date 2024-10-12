# Spring 中间件
本项目通过实现一些常见的中间件来加深对 Java 开发和 Spring 框架的理解


## 白名单拦截
白名单拦截是我们实际开发中经常遇到的业务场景：一个新功能上线后，我们通常并不会一开始就全量开放给所有用户，而是先给白名单内的用户进行开放使用，等这波用户线上验证完成后再全部开量。在这章我们实现白名单拦截的功能

### 实现思路
白名单拦截的基本实现思路是：自定义注解 + 切面 + 白名单配置
* 自定义注解：用来标记那些需要被拦截的方法。注解中需要有两个属性，第一个表示需要判断的方法参数名，第二个则是被拦截后的返回值
* 切面：用来统一处理拦截逻辑
* 白名单配置：配置白名单列表

### 实现过程
1. 先定义一个自定义注解：`WhiteListAnnotation`，里面包含方法 key，用来表示需要跟白名单进行匹配的参数
2. 然后在配置文件中添加白名单的配置，以 whitelist 作为前缀，并定义配置类：`WhiteListConfiguration`，我们使用注解 `@ConfigurationProperties(prefix = "whitelist")` 来获取配置中的值
3. 通过切面类 `WhiteListProcessAop` 来处理所有的白名单注解，处理逻辑如下：
   * 切入点是所有添加了注解 `WhiteListConfiguration` 的方法
   * 白名单开关关闭时，直接放行
   * 当前方法上没有定义 key 值，则直接放行
   * 当前方法定义了 key 值
     * key 值在白名单中，放行
     * key 值不在白名单中，返回拦截的异常


## 超时熔断
随着现在逐渐微服务化，一个功能可能上下游会依赖几十个服务，而其中如果某个服务挂了的话，那么这条链路可能就全部挂了，进而引起雪崩效应。所以为了能保证服务的高可用，我们需要有个超时熔断的机制，当某个服务不可用时能立马熔断，返回预定的错误

本章实现的超时熔断功能是在 **Netflix** 公司的开源框架 `Hystrix` 的基础上进行的封装，通过注解 + AOP 的方式，使超时熔断更加易于使用。下面简单介绍下 `Hystrix` 的原理与使用

### Hystrix 的原理与使用

maven 依赖包地址如下：
```xml
<dependency>
    <groupId>com.netflix.hystrix</groupId>
    <artifactId>hystrix-core</artifactId>
    <version>1.5.18</version>
</dependency>
```

`Hystrix` 工作时有以下三个状态：
* 关闭：所有请求直接通过
* 全开：当服务错误达到阈值时，进入全开状态，产生熔断，此时所有请求均降级返回
* 半开：处于全开状态下，经过阈值窗口时间后，会先处于半开状态，在本状态下，会放过一个请求，如果请求能正常返回，则后续变为关闭状态，否则回到全开状态

![hystrix 状态机](./doc/images/hystrix-state.png)

`Hystrix` 的使用主要有命令模式和注解模式两种。下面详细介绍命令模式的使用：

通过继承 `HystrixCommand` 来包裹实际需要调用的服务，在 `run()` 中调用服务，在 `getFallback()` 中实现降级逻辑，并且可以通过以下两种方式执行：
- execute(): 同步执行，直接返回结果，发生错误时抛异常
- queue(): 异步执行，返回一个 `Future` 对象

`HystrixCommand` 支持如下的配置： 
- GroupKey:  该命令属于哪一个组，可以帮助我们更好的组织命令。
- CommandKey: 该命令的名称
- ThreadPoolKey: 该命令所属线程池的名称，同样配置的命令会共享同一线程池，若不配置，会默认使用GroupKey作为线程池名称。
- CommandProperties: 该命令的一些设置，包括断路器的配置，隔离策略，降级设置以及一些监控指标等。
- ThreadPoolProerties: 关于线程池的配置，包括线程池大小，排队队列的大小等。

使用示例见：`HystrixDemo`

### 实现
我们最终的实现效果是通过注解的方式来方便的给当前方法添加超时熔断，可以指定超时时长，也可以使用默认的1s超时熔断。为此，我们分以下步骤实现：

1. 定义超时熔断注解：`HystrixAnnotation`，包含一个参数 timeoutMs，主要用来标记哪些方法需要超时熔断
2. 定义超时熔断处理类 `HystrixProcess`，接收切面类的方法，以及超时熔断时间，并在 run 方法中调用切面方法，完成最终方法的执行。在降级方法中返回熔断异常
3. 定义切面处理类 `HystrixProcessAop`，处理所有有超时注解的方法，实例化上述超时熔断处理类，并将当前的方法以及超时参数传入。注意：在本方法中通过 @Around("pointcut() && @annotation(hystrixAnnotation)") 来直接获取方法中的注解值

**注意点:**

在超时时长的设置中，由于同一个 key 只能设置一次，因此为了能支持对不同方法设置不同时长，我们将 "类名.方法名" 称作为 key 值传入，如下：
```
super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                // 通过 "类名.方法名" 来实现不同方法的超时时长的设置: String commandKey = jp.getTarget().getClass().getName() + "." + jp.getSignature().getName();
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(timeoutMs)));
```


## 服务限流
服务在线上跑的时候，一般都是有性能上限的，比如某个服务 A 最大只能应对 100 QPS 的请求，但如果此时一瞬间有大流量进来，比如有 2000 QPS 的请求进来，那么服务一下子就被打崩溃了

对于上述这种问题，我们一般采用限流的方式来解决，所以本章我们就来实现限流

### 限流算法

常见的限流算法有以下三种：
1. 计数器法
2. 漏桶算法
3. 令牌桶

**计数器法**: 计数器法限流比较简单粗暴，比如我们现在要限流 100QPS/s，那么从接收到第一个请求开始计数，每接收一个请求，计数加 1，当在 1s 内接收到 100 个请求后，后续所有的请求则全部直接丢弃，直到下一个 1s 的到来又开始重新计数
这种方式的缺点是后续的请求会被全部放弃掉，就像一个突刺一样，我们称之为突刺现象

**漏桶算法**：所有请求到来时先进入漏桶中（可以用队列实现桶），然后以指定速率从桶中获取请求来处理。虽然请求到来的时间不确定，但由于处理的速度是确定的，因此也能达到限流的目的，当然在桶装满的情况下，后续的请求也会被丢弃掉。漏桶算法可以有效的解决突刺问题，但缺点是难以应对一开始的大流量问题

![漏桶算法](./doc/images/leak-bucket.png)

**令牌桶**: 令牌桶是在桶中以固定速率放入令牌，然后服务每次先从令牌痛中获取令牌，当获取到时则服务继续运行，如果获取不到则原地等待或者直接返回异常，通过控制令牌放入桶中的速率，可以达到限流的目的。由于令牌痛一开始是装满令牌的，因此可以应对一开始的大流量问题

![令牌桶](./doc/images/token-bucket.png)

### 限流实现
本章实现的限流功能是基于 guava 的 `RateLimiter`限流工具的封装，它本身使用令牌桶算法，依赖包地址如下：

```xml
<dependency>
   <groupId>com.google.guava</groupId>
   <artifactId>guava</artifactId>
   <version>18.0</version>
</dependency>
```

`RateLimiter` 的使用很简单，只需要通过 create 方法创建一个实例对象，然后调用 acquire（没有令牌时原地阻塞） 或者 tryAcquire（没有令牌直接返回 false） 方法获取令牌即可，示例如下：

```java
package com.aric.middleware;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterDemo {
    //每秒只发出 1 个令牌
    private final RateLimiter rateLimiter = RateLimiter.create(1);

    public void testLimit() {
        int count = 0;
        for (;;) {
            if (rateLimiter.tryAcquire()) {
                System.out.println(System.currentTimeMillis() / 1000);
                count++;
                if (count > 10) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        RateLimiterDemo rateLimiterDemo = new RateLimiterDemo();
        rateLimiterDemo.testLimit();
    }
}

```

基于上述的 `RateLimiter`，我们现在可以封装自己的限流中间件。具体实现步骤如下：

1. 定义注解类 `RateLimiterAnnotation`，用来给需要添加限流的方法使用，同时包含一个参数 permitPerSecond 来表示具体的限流值
2. 定义限流封装类 `RateLimiterProcess`，实现核心的限流逻辑。
    - 接收切面方法来调用具体的服务
    - 维护一个 map，"类名.方法名" 作为 key，限流对象作为 value，限流参数由注解提供，最终实现针对不同方法具有不同的限流值
3. 定义切面类 `RateLimiterProcessAop`，用来拦截所有有限流注解的方法，并传入上述的限流封装类中

## 自定义拦截方法
本章我们实现自定义拦截方法，通过注解+切面的形式给用户提供更加方便的自定义逻辑的实现。

### 实现
1. 添加注解 `MethodExtAnnotation`，用来表示自定义方法的信息，其中参数 method 表示自定义的方法名称
2. 添加切面类 `MethodExtProcessAop`，处理自定义逻辑：
    * 首先从注解中获取到自定义方法的名称
    * 根据名称获取到自定义方法，这里已经假设自定义方法的参数和当前方法的参数一致：`jp.getTarget().getClass().getMethod(methodName, method.getParameterTypes());`
    * 判断方法返回时是否是 boolean，不是则抛出异常: `method.getReturnType().getName().equals("boolean")`
    * 运行方法，再根据返回值是否为 true 决定继续运行还是直接返回
3. 自定义逻辑使用如下：在运行方法 `queryUser` 时，会先运行方法 `doFilter`，当 `doFilter` 返回为 true 时，则 `queryUser` 正常返回，否则直接返回被拦截异常

```java
import com.aric.middleware.common.Result;

class MethodExtDemo {
   @MethodExtAnnotation(method = "doFilter")
   public Result queryUser(String userId) {
      return Result.success("查询用户: " + userId);
   }

   /**
    * 自定义拦截方法
    */
   public boolean doFilter(String userId) {
      if ("1234".equals(userId)) {
         logger.info("拦截自定义用户 userId：{}", userId);
         return false;
      }
      return true;
   }
}

```