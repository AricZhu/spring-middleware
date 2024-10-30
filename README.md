# Spring 中间件
本项目通过实现一些常见的中间件来加深对 Java 开发和 Spring 框架的理解


# 白名单拦截
白名单拦截是我们实际开发中经常遇到的业务场景：一个新功能上线后，我们通常并不会一开始就全量开放给所有用户，而是先给白名单内的用户进行开放使用，等这波用户线上验证完成后再全部开量。在这章我们实现白名单拦截的功能

## 实现思路
白名单拦截的基本实现思路是：自定义注解 + 切面 + 白名单配置
* 自定义注解：用来标记那些需要被拦截的方法。注解中需要有两个属性，第一个表示需要判断的方法参数名，第二个则是被拦截后的返回值
* 切面：用来统一处理拦截逻辑
* 白名单配置：配置白名单列表

## 实现过程
1. 先定义一个自定义注解：`WhiteListAnnotation`，里面包含方法 key，用来表示需要跟白名单进行匹配的参数
2. 然后在配置文件中添加白名单的配置，以 whitelist 作为前缀，并定义配置类：`WhiteListConfiguration`，我们使用注解 `@ConfigurationProperties(prefix = "whitelist")` 来获取配置中的值
3. 通过切面类 `WhiteListProcessAop` 来处理所有的白名单注解，处理逻辑如下：
   * 切入点是所有添加了注解 `WhiteListConfiguration` 的方法
   * 白名单开关关闭时，直接放行
   * 当前方法上没有定义 key 值，则直接放行
   * 当前方法定义了 key 值
     * key 值在白名单中，放行
     * key 值不在白名单中，返回拦截的异常


# 超时熔断
随着现在逐渐微服务化，一个功能可能上下游会依赖几十个服务，而其中如果某个服务挂了的话，那么这条链路可能就全部挂了，进而引起雪崩效应。所以为了能保证服务的高可用，我们需要有个超时熔断的机制，当某个服务不可用时能立马熔断，返回预定的错误

本章实现的超时熔断功能是在 **Netflix** 公司的开源框架 `Hystrix` 的基础上进行的封装，通过注解 + AOP 的方式，使超时熔断更加易于使用。下面简单介绍下 `Hystrix` 的原理与使用

## Hystrix 的原理与使用

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

## 实现
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


# 服务限流
服务在线上跑的时候，一般都是有性能上限的，比如某个服务 A 最大只能应对 100 QPS 的请求，但如果此时一瞬间有大流量进来，比如有 2000 QPS 的请求进来，那么服务一下子就被打崩溃了

对于上述这种问题，我们一般采用限流的方式来解决，所以本章我们就来实现限流

## 限流算法

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

## 限流实现
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

# 自定义拦截方法
本章我们实现自定义拦截方法，通过注解+切面的形式给用户提供更加方便的自定义逻辑的实现。

## 实现
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

# ORM 框架实现
数据库操作是我们实际业务中经常使用的，我们都用过 JDBC 的方式进行数据库操作，随着后面的学习，我们接触到了 iBatis、MyBatis，Hibernate 等优秀的数据库操作组件，这些都是 ORM 的具体实现。本章我们基于底层的 JDBC 自己封装一套 ORM 框架

## JDBC 介绍
在具体开发前，先来介绍下 JDBC 组件。JDBC 组件是数据库的驱动，提供了对数据库的 CRUD 操作。下面以一个示例来具体说明 JDBC 的使用：

```java
package com.aric.middleware;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcDemo {
    private static final String URL = "jdbc:mysql://localhost:3306/demo";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void insertData(String name) {
        String sql = "insert into users(name) values(?)";
        try(
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            System.out.println("Inserted: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchData() {
        String sql = "select * from users;";

        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("fetch data: id  name");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println(id + ", " + name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateData(int id, String name) {
        String sql = "update users set name = ? where id = ?";

        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();
            System.out.println("update for id = " + id + ", name = " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteData(int id) {
        String sql = "delete from users where id = ?";
        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            int i = preparedStatement.executeUpdate();
            System.out.println("delete id: " + id + ", result: " + i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JdbcDemo jdbcDemo = new JdbcDemo();
        jdbcDemo.insertData("aric");
        jdbcDemo.insertData("xiaoming");
        jdbcDemo.fetchData();
        jdbcDemo.updateData(1, "xiaoli");
        jdbcDemo.deleteData(2);
    }
}

```

通过上述的 CRUD 操作，我们知道使用 JDBC 对数据库进行操作主要分以下 4 步：
1. 连接到数据库：使用 "DriverManager.getConnection" 进行连接，并获取到连接对象 `Connection`
2. 预编译：我们调用 "connection.prepareStatement(sql)" 方法来对 SQL 语句执行预编译，并获取到预编译对象 `PreparedStatement`。执行预编译有很多好处，可以提高重复执行的效率，也可以防止 SQL 注入等
3. 设置参数：如果 SQL 语句中带有参数，那么这时候我们就需要设置参数，常见的方法如下，如果没有参数则不用执行
   * setInt(int parameterIndex, int x)：设置 int 类型参数
   * setString(int parameterIndex, String x)：设置 String 类型参数
   * setDouble(int parameterIndex, double x)：设置 double 类型参数
   * setDate(int parameterIndex, Date x)：设置 Date 类型参数
4. 执行 SQL 语句：有两种 API 来执行语句：
   * executeQuery()：执行查询，返回 ResultSet
   * executeUpdate()：用于执行 DML 语句（如 INSERT、UPDATE、DELETE），返回更新的行数

对于数据库执行返回的结果对象 `ResultSet` 是一个可以遍历的对象，允许我们逐行读取结果集中的数据

通常，我们先使用 next() 方法来移动到结果集中的下一行，返回 true 如果有下一行，返回 false 如果到达结果集的末尾

然后我们使用下面的 API 来访问列数据：
* getString(int columnIndex)：根据列的索引获取字符串类型的数据。
* getInt(String columnName)：根据列名获取整型数据。
* getDouble(int columnIndex)：根据列索引获取双精度浮点数。
* getDate(String columnLabel)：根据列名获取日期数据。

## ORM 整体设计

整个 ORM 框架需要包含以下几部分：
* xml 配置文件的解析，节点信息的保存
* 数据库的连接操作
* SQL 语句的解析执行和结果映射

上述功能中数据库的连接，SQL 语句的解析和执行我们使用 JDBC 的 API 实现，其余部分都需要我们自己实现。我们根据上述的功能进行合理划分，得到下面的类和接口：
* `Resource`、`XNode` 和 `Configuration`：分别表示资源类、xml 的节点类以及配置类
* `SqlSession` 和 `DefaultSqlSession`：接口和实现类，用来实现 SQL 的解析、执行和结果映射
* `SqlSessionFactory` 和 `DefaultSqlSessionFactory`：接口和实现类，用来实现数据库的连接操作
* `SqlSessionFactoryBuilder`：实现读取配置，解析并构造需要的类

### 类整体设计

```mermaid
classDiagram
    class SqlSession {
       <<接口>>
        + <T> T selectOne(String statement)
        + <T> T selectOne(String statement, Object parameter)
        + <T> List<T> selectList(String statement)
        + <T> List<T> selectList(String statement, Object parameter)
        + void close()
    }
    class DefaultSqlSession {
        - Connection connection
        - Map<String, XNode> mapperElement
        
        + <T> T selectOne(String statement)
        + <T> T selectOne(String statement, Object parameter)
        + <T> List<T> selectList(String statement)
        + <T> List<T> selectList(String statement, Object parameter)
        + void close()
        
        - void buildParameter(PreparedStatement preparedStatement, Object parameter, Map<Integer, String> parameterMap)
        - <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz)
    }
    class SqlSessionFactory {
       <<接口>>
       + SqlSession openSession()
    }
    class DefaultSqlSessionFactory {
        - Configuration configuration
        + SqlSession openSession()
    }
    class SqlSessionFactoryBuilder {
        + SqlSessionFactory build(Reader reader)
        - Configuration parseConfiguration(Element root)
        - Map<String, String> dataSource(List<Element> list)
        - Connection connection(Map<String, String> dataSource)
        - Map<String, XNode> mapperElement(List<Element> list)
        
    }
    class Configuration {
        - Connection connection;
        - Map<String, String> dataSource;
        - Map<String, XNode> mapperElement;
    }
    class XNode {
        - String namespace;
        - String id;
        - String parameterType;
        - String resultType;
        - String sql;
        - Map<Integer, String> parameter;
    }
    class Resource {
        + static Reader getResourceAsReader(String resource)
        - static InputStream getResourceAsStream(String resource)
        - static ClassLoader[] getClassLoaders()
    }
    
   SqlSession <|.. DefaultSqlSession
   SqlSession <.. SqlSessionFactory: 依赖
   SqlSessionFactory <|.. DefaultSqlSessionFactory
   DefaultSqlSessionFactory <-- Configuration: 关联
   SqlSessionFactoryBuilder ..> SqlSessionFactory: 依赖
   SqlSessionFactoryBuilder ..> Configuration: 依赖
   SqlSessionFactoryBuilder ..> XNode: 依赖
   Configuration --> XNode: 关联
```

### 关键步骤的实现
**xml 配置文件读取**

我们通过 `Resource` 类来实现资源的读取，具体是通过 `ClassLoader` 来加载字节流 `InputStream`，然后再转换为方便读取的字符流 `InputStreamReader`，如下:
```java
// 通过 ClassLoader 加载字节流
InputStream inputStream = classLoader.getResourceAsStream(resource);

// 字节流转换为字符流
Reader reader = new InputStreamReader(inputStream);
```

**xml 配置文件解析**

我们借助工具 dom4j 来实现对 xml 文件的解析：
1. 先从上一步中获取到的字符流 `Reader` 创建对象 `SAXReader`
2. 创建文档对象 `Document`
3. 从文档对象获取到根节点 `Element`
4. 在根节点上获取我们感兴趣的节点，使用方法 "selectNodes(nodeName)" 即可，我们也可以在节点名前增加 "//" 来直接选择子孙节点: "selectNodes("//nodeName")"
5. 通过 "content()" 方法获取到当前节点的所有子节点
6. 通过 "attributeValue(name)" 获取当前节点的属性

```java
import javax.swing.text.Element;

Reader reader = Resources.getResourceAsReader(resource);

SAXReader saxReader = new SAXReader();

Document document = saxReader.read(new InputSource(reader));

Element root = document.getRootElement();

List<Element> nodes = root.selectNodes("mappers");

List nodeChildren = nodes.get(0).content();

String id = (Element)(nodeChildren.get(0)).attributeValue("id");

String text = (Element)(nodeChildren.get(0)).getText(); // 获取节点的 text 内容
```

**XNode 节点的解析**

XNode 节点存储了我们需要的所有的 SQL 相关的信息，包括 sql 语句，输入参数，输出类型等，一个实际的例子如下：
```xml
 <select id="queryUserInfoById" parameterType="java.lang.Long" resultType="com.aric.middleware.mybatis.po.User">
     SELECT id, userId, userNickName, userHead, userPassword, createTime
     FROM user
     where id = #{id}
 </select>
```

为了存储上述的信息，XNode 类中定义了如下属性：
* String namespace: 命名空间，用来区分不同的配置文件
* String id: 当前 sql 语句的唯一标识，结合上述的 namespace，可以作为全局唯一标识: "namespace.id"
* String parameterType: 表示参数类型，一般如果只有一个参数，则可以直接使用简单对象 Long、String 等，如果 sql 中有多个参数，则使用对象或者 Map 类型的数据，存放不同的类型
* String resultType: sql 执行返回的结果类型
* String sql: 具体的 sql 语句
* Map<Integer, String> parameter: 存放 sql 中需要的参数

下面重点讲述 sql 语句的参数的解析。为了能支持参数，我们这里以特殊符号 "#{}" 来表示参数，通过正则表达式来解析出 sql 语句中的参数，并将参数名保存至 map 接口中, index 作为 map 的 key，因为在 JDBC 中参数的设置也是以 index 作为 key 值，同时当前的参数替换为 "?"，处理后，当前的 SQL 语句就是标准的 JDBC 可以解析的了。如下：
```java
public class Demo {
    public void parse() {
       String sql = node.getText();
       Map<Integer, String> parameter = new HashMap<>();
       Pattern pattern = Pattern.compile("(#\\{(.*?)\\})");
       Matcher matcher = pattern.match(sql);
       for (int i = 1; matcher.find(); i++) {
          String g1 = matcher.group(1);
          String g2 = matcher.group(2);
          // 保存参数到 map 中, 其中 i 作为 key，这样可以和 jdbc 中的 sql 参数一一对应
          parameter.put(i, g2);

          // 将自定义的参数类型 #{id} 替换为 JDBC 能处理的类型 ?
          sql = sql.replace(g1, "?");
       }
    }
}
```

**DefaultSqlSession 中的 buildParameter(PreparedStatement preparedStatement, Object parameter, Map<Integer, String> parameterMap) 实现**

"buildParameter" 方法是用来完成 SQL 中变量的替换的。实现方式也比较简单：
1. 如果输入对象 parameter 是普通的对象：Short、Integer、Long、String、Date，则直接调用 PreparedStatement 对象的 setShort、setLong 等方法进行参数替换
2. 如果输入对象 parameter 是复杂对象是，先利用反射获取到输入对象的所有字段的值，并保存到 map 中。注意：在获取字段值时，需要先调用 field.setAccessible(true) 将字段设置为公共
3. 然后遍历参数列表 parameterMap，根据参数名从上述的 map 中获取到对应的参数值，然后再根据参数值的类型去调用第一步中的对应 API 进行参数的替换

至此就完成了对 SQL 参数的替换

**DefaultSqlSession 中的 resultSet2Obj(ResultSet resultSet, Class<?> clazz) 实现**

"resultSet2Obj" 方法是将数据库查询结果转换为我们需要的对象类型。核心是遍历查询结果集，通过反射的方式将数据库中的结果映射到对象中，具体实现如下：
1. 先获取数据库结果的相关信息 `ResultSetMetaData`，从中获取到返回的列数量等信息
2. 从结果集 resultSet 中获取到当前列的值，并且通过列名拼接出对应对象的方法名，比如列名称为 "id"，则拼接出对象的方法名 "setId"
3. 根据上述的方法名，从对象上获取到实际的方法，然后调用方法实现值的设置，如下：
   ```
   // 获取数据库结果信息
   ResultSetMetaData metaData = resultSet.getMetaData();
   
   // 创建一个对象
   (T) obj = (T) clazz.newInstance();
   
   // 获取到数据库列名
   String columnName = metaData.getColumnName(i);

   // 获取到当前数据库列的值
   Object value = resultSet.getObject(i);
   
   // 拼接方法名
   String methodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1); 
   
   // 通过反射获取到方法
   if (value instanceof Timestamp) {
     Method method = clazz.getMethod(methodName, Date.class); 
   } else {
     Method method = clazz.getMethod(methodName, value.getClass()); 
   }
   
   // 调用方法设置值
   method.invoke(obj, value);
   ```

# ORM 框架与 Spring 的结合
在上一章我们实现了一个 ORM 框架，能进行基本的 CRUD 操作以及完成参数和结果的映射。这一章我们需要将它集成到 Spring 框架中便于使用。同时我们知道在 mybatis 框架提供了一种快捷方便的方式操作数据库，即只提供接口定义，不需要具体的实现类就可以实现数据库的操作，示例如下：

```java
import org.springframework.stereotype.Component;

// 定义数据库操作接口
@Component
public interface IUserDao {
   User queryUserInfoById(Long id);
}

// 直接通过接口进行数据库的操作
@Test
public void test() {
   BeanFactory beanFactory = new ClassPathXmlApplicationContext("spring-config.xml");
   IUserDao userDao = beanFactory.getBean("IUserDao", IUserDao.class);
   User user = userDao.queryUserInfoById(1L);
   logger.info("测试结果：{}", JSON.toJSONString(user));
}
```

上述例子中展示了在 mybatis 中是如何只通过一个接口，就可以进行数据库操作的方式。这个实现其实也不复杂，mybatis 框架会给这些接口都创建一个代理类，对这些接口的操作其实就是对代理类的操作，而在代理类中会调用具体的 ORM 框架去操作数据库

下面我们来实现 mybatis 的这种功能。让我们的 ORM 框架更像 mybatis

## 整体设计
上文说到了接口操作的核心是代理类，所以我们首先需要一个配置扫描类，用来扫描所有需要代理的接口，并给这些接口添加代理类，其次需要一个代理类，用来代理接口的操作，最后我们还需要一个 ORM 操作类，在代理类中我们借助 ORM 操作类来实际操作 ORM 框架

综上，mybatis-spring 框架的核心类有如下三个：
1. `MapperScannerConfigurer`：配置扫描类，通过扫描指定路径下的所有接口，并为接口添加代理类
2. `MapperFactoryBean`：接口的代理类，借助 ORM 操作类来实际操作 ORM
3. `SqlSessionFactoryBean`：完成 `SqlSessionFactory` 类的初始化，并操作 ORM

完整过程如下：
我们首先通过 Spring 配置文件，将 `MapperScannerConfigurer` 和 `SqlSessionFactoryBean` 注册到 Spring 容器中，然后在 Bean 注册的时候，`MapperScannerConfigurer` 进行自定义的扫描注册 Bean，将所有需要的接口都设置代理类 `MapperFactoryBean`，而在 `MapperFactoryBean` 中会调用 `SqlSessionFactoryBean` 来进行 ORM 操作，这样就完成了 mybatis 集成到 Spring 的过程。

![mybatis-spring架构图](doc/images/mybatis-spring.png)

## 核心实现
### `SqlSessionFactoryBean` 类的实现
`SqlSessionFactoryBean` 类是完成 `SqlSessionFactory` 类的初始化，并借此用来操作 ORM 的。我们通过实现 Spring 中的 FactoryBean 和 InitializingBean 这两个接口来完成该类的实例化
* FactoryBean：用来创建 Spring 容器中的实例对象。我们在这个接口中完成了当前实例对象指向 `SqlSessionFactory` 对象
* InitializingBean：在 Bean 对象配置完属性后的钩子操作。我们在这个接口中完成了 `SqlSessionFactory` 的实例化


### `MapperFactoryBean` 类的实现
`MapperFactoryBean` 类是接口代理类，主要是代理 mybatis 的接口，并实现 ORM 操作。这里我们还是通过实现 FactoryBean 接口来完成类的实例化，并在其中完成代理类的创建。
创建所需的被代理的接口以及 ORM 操作对象会在构造函数中注入，这个是由扫描类帮忙注入的。代理的实现逻辑也比较简单：
1. 根据当前被代理的方法，获取到方法名，和接口全名进行拼接后，就得到了完整的操作名，即上一章的 "namespace.id"
2. 根据方法是否有变量、返回是否是列表来决定选择需要的 ORM 的方法，即最终调用 SqlSession.selectOne 还是 SqlSession.selectList 

代码实现如下：
```java
@Override
public T getObject() throws Exception {
  InvocationHandler handler = (proxy, method, args) -> {
      System.out.println("你被代理了，执行SQL操作: " + method.getName());
      // 获取全限定名
      String statement = mapperInterface.getName() + "." + method.getName();

      // 开启 ORM 操作
      SqlSession sqlSession = sqlSessionFactory.openSession();

      // 根据返回值和参数类型选择调用的 ORM 的 API
      Class<?> returnType = method.getReturnType();
      try {
          // 无参数
          if (null == args || args.length == 0) {
              // 返回列表
              if (List.class.isAssignableFrom(returnType)) {
                  return sqlSession.selectList(statement);
              } else { // 返回单个对象
                  return sqlSession.selectOne(statement);
              }
          } else { // 有参
              // 返回列表
              if (List.class.isAssignableFrom(returnType)) {
                  return sqlSession.selectList(statement, args[0]);
              } else { // 返回单个对象
                  return sqlSession.selectOne(statement, args[0]);
              }
          }
      } finally {
          sqlSession.close();
      }
  };

  return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{mapperInterface}, handler);
}
```

### `MapperScannerConfigurer` 类的实现
`MapperScannerConfigurer` 是配置扫描类，用来解析配置，扫描出所有需要代理的 DAO 接口，修改 Bean 注册并添加上述的代理类，最终注册到 Spring 容器中。
注意：在给接口添加代理的时候，还需要将当前接口，以及 ORM 操作对象 `SqlSessionFactory` 注入到代理类中的构造函数的参数中

要自定义 Bean 的注册，我们需要实现 BeanDefinitionRegistryPostProcessor 接口，一个简单的例子如下，其中还包括添加构造函数的参数：
```java
// 定义一个简单的 Bean 类
public class CustomBean {
   private String name;
   private Integer age;

   public CustomBean(String name, Integer age) {
      this.name = name;
      this.age = age;
   }

   public void sayHello() {
      System.out.println("hello world: " + name + ":" + age);
   }
}

// 实现 BeanDefinitionRegistryPostProcessor
@Component
public class CustomBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

   @Override
   public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
      // 创建 BeanDefinition
      BeanDefinition beanDefinition = BeanDefinitionBuilder
              .genericBeanDefinition(CustomBean.class)
              .getBeanDefinition();
   
      // 添加构造函数的参数
      beanDefinition.getConstructorArgumentValues().addGenericArgumentValue("xiaoming");
      beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(18);
   
      registry.registerBeanDefinition("customBean", beanDefinition);
   }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 这个方法可以不实现
    }
}
```
上面的例子展示了如何自定义注册 Bean，接下来就是实现路径的扫描，并批量自定义注册 Bean。完整的实现过程如下：
1. 首先我们使用 `PathMatchingResourcePatternResolver` 类来扫描类路径，它可以支持复杂路径的扫描
2. 然后我们通过 `MetadataReader` 接口获取到类的元数据。它是 Spring 中用于读取和访问类的元数据信息的接口，是核心组件之一，可以在不加载类的情况下直接通过字节码获取到类的元数据，性能比较高
3. 将上述步骤中读取到的元数据对象通过 `ScannedGenericBeanDefinition` 来加载出 BeanDefinition 对象
4. 设置 BeanDefinition 的属性，包括设置构造参数等，供后续代理类使用，同时设置 beanClass 为我们的代理类 MapperFactoryBean
5. 最后我们注册 BeanDefinition

# 开发 ORM starter
上面两章中我们开发了 ORM，并且将 ORM 集合到 Spring 框架中，但现在使用上还是比较繁琐，不仅要添加配置文件，还需要实例化 Bean 对象，初始化 `SqlSessionFactory` 等，所以在本章我们开发一个 Spring boot starter 插件，将上述的步骤自动化装配，这样后面使用的时候，就直接在 pom 中引入我们的 starter 依赖即可开箱使用

在实际开发本章的 starter 前，先来介绍下 starter 以及它的开发流程。

## 关于 Starter
在传统的 Spring 框架使用中，我们要引入一个中间件，比如 Redis、Mybatis，需要做一系列的配置操作，以及 Bean 对象初始化等操作，引入的中间件一多，就成了配置地狱。所以为了解决这种配置繁琐的问题，官方推出了 Spring Boot，它秉承着约定大于配置的理念，通过一个个的 starter 让我们实现开箱即用，比如我们只需要在 pom 依赖中添加 spring-boot-starter-web，我们就可以使用 Spring MVC 的功能。

而 starter 其实就是内部封装了所需的所有依赖，默认的配置，并帮我们自动初始化 Bean 对象，然后通过 spring.factories 文件来告诉 Spring 框架自动装配的路径，在 Spring 框架启动的时候运行我们的 starter 完成自动装配。一句话就是 starter 帮我们做了所有的脏活累活，所以我们才能优雅的使用

## 开发一个 Starter 示例
开发一个 Starter 其实很简单，主要有以下 4 个步骤：
1. 创建一个 maven 项目，并在 pom 文件中添加需要的依赖
2. 创建一个配置类，用来保存配置。添加 @ConfigurationProperties 注解，读取配置，并且需要增加一个前缀，该前缀下的都是我们的配置。类本身包含默认配置，当然外部项目可以通过修改配置文件来覆盖默认配置
   ```java
   @ConfigurationProperties(prefix = "test")
   public class MyProperties {
        // 自动获取配置文件中前缀为test的属性，把值传入对象参数
        private String name = "hello";
   
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
   } 
   ```
3. 创建一个自动装配类，完成 Bean 对象的实例化。添加 @Configuration 注解，表示是一个配置类，添加 @EnableConfigurationProperties 注解，使上述的配置类生效。我们在实例化 Bean 方法上添加 @ConditionalOnMissingBean 注解，表示只有当前 Bean 不存在的时候，才去实例化，防止重复实例化
   ```java
   @Configuration
   @EnableConfigurationProperties(MyProperties.class)
   public class TestAutoConfiguration {
   
       @Resource
       private MyProperties properties;
   
       /**
        * 在Spring上下文中创建一个对象
        */
       @Bean
       @ConditionalOnMissingBean
       public TestService init(){
           TestService testService = new TestService();
           String name = properties.getName();
           testService.setName(name);
           return testService;
       }
   }
   ```
4. 我们在 resources 文件夹下新建目录 META-INF，在目录中新建 spring.factories 文件，并添加自动装配类的路径到 spring.factories 文件中。告诉 Spring 框架自动装配类的路径，在启动的时候自动执行我们的自动装配类来完成 Bean 对象的实例化
   ```
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.shgx.starter.TestAutoConfiguration
   ```
完成上述 4 步后，直接打包就得到了一个自定义的 Starter

## mybatis-spring-boot-starter
接下来开发我们自己的 ORM starter：mybatis-spring-boot-starter

其实 starter 要做的事情就是上一章中将 ORM 集成到 Spring 所要做的事情，包括实例化对象 `SqlSessionFactory`，以及通过 `MapperScannerConfigurer` 类来扫描接口并添加接口类。我们这里复用上一章中集成的那些类，同时因为配置从 xml 改为 yaml，所以这些类的参数有一些修改。

首先，我们定义一个配置类 `MybatisProperties`，用来保存配置信息。我们 starter 的配置如下，可以看到原先通过 xml 配置的现在都改为 yaml 配置，同时这些配置在引入的项目中可以被覆盖
```yaml
mybatis:
  driver: com.mysql.jdbc.Driver
  url: jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true
  username: root
  password: 
  mapper-locations: classpath*:mapper/*.xml
  base-package: com.aric.middleware.mybatis.dao
```

`MybatisProperties` 属性如下，跟上述的配置类一一对应，我们添加 "mybatis" 作为配置类的前缀。同时需要注意配置中使用 "-" 连接的，在配置类中使用驼峰表示
```java
@ConfigurationProperties(prefix = "mybatis")
public class MybatisProperties {
    private String driver;
    private String url;
    private String username;
    private String password;
    private String mapperLocations;
    private String basePackage;
    
    // ... getter and setter
}
```

有了配置类后，接下来我们需要一个自动装配类，来完成那些 Bean 的实例化，关键代码如下。
```java
@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
public class MybatisAutoConfiguration {
   @Bean
   @ConditionalOnMissingBean
   public SqlSessionFactory sqlSessionFactory(MybatisProperties mybatisProperties, Connection connection) throws Exception {
      return new SqlSessionFactoryBuilder().build(connection, mybatisProperties.getMapperLocations());
   }

   public static class AutoConfiguredMapperScannerRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {
      private String basePackage;

      @Override
      public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
          // 注册 MapperScannerConfigurer 类
         BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
         
         beanDefinitionBuilder.addPropertyValue("basePackage", this.basePackage);
         registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), beanDefinitionBuilder.getBeanDefinition());
      }

      @Override
      public void setEnvironment(Environment environment) {
          // 通过 EnvironmentAware 来获取配置属性
         this.basePackage = environment.getProperty("mybatis.base-package");
      }
   }

   @Configuration
   @Import({AutoConfiguredMapperScannerRegistrar.class})
   public static class MapperScannerRegistrarNotFoundConfiguration {
       // 启动上述的静态类
   }
}
```
1. 我们添加 @Configuration 注解，确保当前类在 Bean 扫描路径中，添加 @EnableConfigurationProperties 注解，启用上述的配置类
2. 在类中我们创建 `SqlSessionFactory` 的 Bean 对象，用来实例化 ORM 操作对象，这个其实和上一章的 `SqlSessionFactoryBean` 一样，只不过上一章中是添加到 xml 配置中的，这里我们在配置类中自动实例化了。 需要注意的是，这里我们需要修改下 `SqlSessionFactoryBuilder` 的参数，因为之前是从 xml 中读取的，但现在我们将配置外放，并且支持后续外部用户在项目中修改配置，因此就不适合当前的内部配置的形式了，所以我们将参数修改为直接接收 `Connection` 对象，这样内部类就不需要去读取那些配置了
3. 我们添加内部静态类 `AutoConfiguredMapperScannerRegistrar`，用来注册 `MapperScannerConfigurer` 类，这个类就是用来自动扫描并添加代理类。同时需要注意的是，Bean 注册类运行比较早，这时候还没有配置类，所以我们通过 EnvironmentAware 来感知配置
4. 最后我们添加一个类 `MapperScannerRegistrarNotFoundConfiguration` 来启动上述的静态类

最后在 resources/META-INF/spring.factories 文件中添加自动配置入口即可
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.aric.middleware.mybatis.springbootstarter.MybatisAutoConfiguration
```

# RPC 框架通信模型实现
rpc 通信被广泛的应用在不同服务之间相互调用，它可以像调用本地方法一样去调用远程服务，使用非常的便捷。如下面示例所示，我们直接在项目 A 中去调用项目 B 的服务时，直接引入对象并调用对象的方法，看起来像是直接在本地调用一般，但其实内部还是通过网络请求调用了远程的 B 服务，只不过 rpc 框架帮我们封装了底层
```java
import jakarta.annotation.Resource;

// 工程 A
public class ProjectA {
   @Resource
   private ServiceB serviceB;
   
   public void hello() {
       serviceB.hello();
   }
}

// 工程 B 中提供了 ServiceB 的服务
public class ServiceBImpl implements ServiceB {
    @Override
    public void hello() {
       System.out.println("hello world!");
    }
}
```
**注意**: 工程 B 是部署在其他服务器上的，有他自己的数据库等资源，所以要调用 B 的服务，你只能通过网络请求比如 http 的方式去调用，这个和工程 A 中直接引入工程 B 的包是不一样的，前者是独立的服务，有独立的数据库等资源，后者只是当成工具一样引入。

## 前置知识
本次 rpc 中间件的设计需要用到自定义标签的处理技术以及 netty 通信，下面先介绍这两种技术：

### 自定义标签处理
我们需要通过自定义的标签来实现接口的注册和获取，在 spring 中实现 xml 的自定义标签的解析有以下 4 步：

1. 首先我们需要创建描述我们自定义标签属性的文件，也就是 custom.xsd 文件。如下所示：xmlns 指定了自定义命名空间。targetNamespace 设定了目标命名空间，这意味着任何符合这个 XSD 的 XML 文档都会被认为是在该命名空间内。elementFormDefault="qualified" 指定在目标命名空间中的元素必须在 XML 文档中被限定，通常使用命名空间前缀。
   创建完后需要注册下，在 META-INF/spring.schemas 中添加以下内容：http\://www.example.org/schema/custom/custom.xsd=custom.xsd
上述添加的内容表示告诉 Spring 框架，遇到 xml 中的 "http\://www.example.org/schema/custom/custom.xsd" 资源验证时，直接使用 "custom.xsd"，也就是我们刚创建的 xsd 文件
   ```xsd
   <?xml version="1.0" encoding="UTF-8"?>
   <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns="http://www.example.org/schema/custom"
   targetNamespace="http://www.example.org/schema/custom"
   elementFormDefault="qualified">
   
       <xs:element name="custom-tag">
           <xs:complexType>
               <xs:attribute name="id" type="xs:string" />
               <xs:attribute name="name" type="xs:string" />
               <xs:attribute name="age" type="xs:string" />
           </xs:complexType>
       </xs:element>
   </xs:schema>
   ```
2. 有了上述的 xsd 配置文件，并注册了 xsd 的解析路径后，接下来我们就可以创建包含自定义标签的 xml 配置文件：spring-config.xml。下面的配置文件中的 xmlns:custom 就表示我们的自定义标签头为 custom, 命名空间是 "http://www.example.org/schema/custom", 然后 xsi:schemaLocation 指定了命名空间资源验证路径，可以看到我们的命名空间 "http://www.example.org/schema/custom" 对应的验证路径是 "http://www.example.org/schema/custom/custom.xsd", 而通过上述 xsd 注册知道，该验证路径正好对应我们创建的 xsd 文件，所以最终就是通过我们创建的 xsd 文件来验证 xml 配置中的 custom-tag
   ```xml
   <beans xmlns="http://www.springframework.org/schema/beans"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:custom="http://www.example.org/schema/custom"
   xsi:schemaLocation="
   http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans.xsd
   http://www.example.org/schema/custom
   http://www.example.org/schema/custom/custom.xsd">
   
       <custom:custom-tag id="customBean1" name="aric" age="18" />
       <custom:custom-tag id="customBean2" name="ke" age="28" />
   </beans>
   ```
3. 现在我们可以添加自定义命名空间处理类。并注册命名空间处理类：在 META-INF/spring.handlers 文件中添加内容：http\://www.example.org/schema/custom=com.example.CustomNamespaceHandler 。这个就表示上述我们自定义标签的命名空间 "http://www.example.org/schema/custom" 由下面的类来处理。
   ```java
   public class CustomNamespaceHandler extends NamespaceHandlerSupport {
        @Override
        public void init() {
           registerBeanDefinitionParser("custom-tag", new CustomBeanDefinitionParser(CustomBean.class));
        }
   } 
   ```
4. 创建自定义的 BeanDefinitionParser, 这是负责解析自定义标签的核心类
   ```java
   public class CustomBeanDefinitionParser implements BeanDefinitionParser {
    private Class<?> clazz;

    public CustomBeanDefinitionParser(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String age = element.getAttribute("age");

        builder.addPropertyValue("name", name);
        builder.addPropertyValue("age", Integer.valueOf(age));

        parserContext.getRegistry().registerBeanDefinition(id, builder.getBeanDefinition());
        return builder.getBeanDefinition();
    }
   }
   ```

以上 4 步就完成了自定义标签，包括 xsd 文件描述，xsd 注册，命名空间处理类、命名空间处理注册、标签解析类。接下来我们就可以按如下方式获取到我们自定义的 Bean：
```java
public class CustomBeanApplicationTest {
    @Test
    public void test_customBean() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");
        CustomBean customBean = applicationContext.getBean("customBean1", CustomBean.class);
        CustomBean customBean2 = applicationContext.getBean("customBean2", CustomBean.class);

        System.out.println(JSON.toJSONString(customBean));
        System.out.println(JSON.toJSONString(customBean2));
    }
}
```

### netty 通信
TODO

## 架构设计
rpc 的调用过程包含了 3 方：rpc 中间件、接口注册方、接口消费方。其中 rpc 中间件提供 rpc 的全部功能，接口注册方通过中间件注册接口，接口消费方通过中间件消费接口，上面例子中的项目 B 就是接口注册方，项目 A 就是接口消费方

rpc 调用的完整过程如下： 
首先接口注册方会将接口添加到注册中心（redis 实现），然后消费方调用接口时，实际通过 ConsumerBean 作为代理对象，该代理对象会先从注册中心获取实际调用的接口信息，然后通过 netty 网络通信传输到对应的接口提供方，接口提供方从 netty 中获取到信息，根据信息实际调用接口，并将接口返回结果通过 netty 返回给消费方，消费方再通过 ConsumerBean 对象返回最终的调用结果。这样就完成了一次 rpc 调用

![rpc调用流程](doc/images/rpc-process.png)

由上述的调用过程可以知道，rpc 中间件至少包含了以下的模块：
* ProviderBean：实现接口注册的 Bean
* ConsumerBean：代理对象，实现接口消费的 Bean
* Redis 注册中心：保存接口注册
* Netty 通信：网络通信层
* 编码/解码模块：网络传输时的编解码
* 注册中心配置模块：提供 redis 配置

### 模块设计
整个中间件需要包含以下几个部分：
1. 配置模块
   * 注册中心的配置：地址、端口号
   * 接口注册配置
   * 接口消费配置
2. 启动模块
   * 启动注册中心
   * 启动 netty 通信
3. 注册中心
   * redis 注册中心
   * 添加/获取接口注册
4. netty 通信
   * 消息通信
   * 编码/解码
   * 消息处理

## 功能设计
TODO

## 类设计
TODO
