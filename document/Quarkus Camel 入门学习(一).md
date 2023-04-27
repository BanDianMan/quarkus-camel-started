# Quarkus Camel 入门学习

---

​			Quarkus Camel 是基于 Quarkus 框架和 Apache Camel 集成框架的整合，用于构建轻量级、高度可扩展的微服务应用程序。Apache Camel 是一个基于企业级集成模式（Enterprise Integration Patterns）的开源集成框架，它提供了一组用于构建消息驱动的应用程序的 API 和组件。而 Quarkus 是一个针对云原生应用程序设计的 Java 框架，它支持快速启动、低内存占用、快速开发等特点，可以与现有的 Java 生态系统协同工作。

## 环境要求

- git客户端
- JDK11+
- Apache Maven 3.8.2+(建议3.8.6)
- [可选] GraalVM (用于构建)

## 项目搭建

### 创建项目

- 可以通过 [Quarkus 自带应用程序生成器](https://code.quarkus.io/)，它可以帮助开发人员快速创建和配置基于 Quarkus 框架的应用程序。该应用程序生成器是一个 Web 界面，通过简单的界面交互，可以选择所需的 Quarkus 扩展、依赖库、编程语言等，并自动生成项目骨架代码和相关配置文件。

- 也可以通过IDEA 创建新项目时指定是 Quarkus项目 , 一步一步的配置依赖和环境

- 还有一张方式是通过Maven 插件来创建Quarkus项目 , 也是我比较常用的方式

  - ```bash
     mvn io.quarkus:quarkus-maven-plugin:2.16.0.Final:create \
        -DprojectGroupId=com.heyiwen \
        -DprojectArtifactId=quarkus-camel-started \
        -Dextensions=camel-quarkus-log,camel-quarkus-timer
    ```

  - ![image-20230426132434262](https://www.img.heyiwen.com/blog/image-20230426132434262.png)

### 下载插件

- 可以下载Intellij IDEA的插件 , 提供了与Apache Camel相关的功能。 [Apache Camel IDEA plugin](https://plugins.jetbrains.com/plugin/9371-apache-camel-idea-plugin) 

## 下一步

- 添加依赖

```xml
        <dependency>
            <groupId>org.apache.camel.quarkus</groupId>
            <artifactId>camel-quarkus-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.camel.quarkus</groupId>
            <artifactId>camel-quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.camel.quarkus</groupId>
            <artifactId>camel-quarkus-direct</artifactId>
        </dependency>
```

- 添加实体类 `Fruit`

```java
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class Fruit {
    private String name;
    private String description;

    public Fruit() {
    }

    public Fruit(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Fruit)) {
            return false;
        }

        Fruit other = (Fruit) obj;

        return Objects.equals(other.name, this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
```

- 添加实体类 `Legume`

```java
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class Legume {
    private String name;
    private String description;

    public Legume() {
    }

    public Legume(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Legume)) {
            return false;
        }

        Legume other = (Legume) obj;

        return Objects.equals(other.name, this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
```

- 添加路由类`Routes`

```java
public class Routes extends RouteBuilder {
    private final Set<Fruit> fruits = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<Legume> legumes = Collections.synchronizedSet(new LinkedHashSet<>());

    public Routes() {

        /* Let's add some initial fruits */
        this.fruits.add(new Fruit("Apple", "Winter fruit"));
        this.fruits.add(new Fruit("Pineapple", "Tropical fruit"));

        /* Let's add some initial legumes */
        this.legumes.add(new Legume("Carrot", "Root vegetable, usually orange"));
        this.legumes.add(new Legume("Zucchini", "Summer squash"));
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().bindingMode(RestBindingMode.json);

        rest("/fruits")
                .get()
                .to("direct:getFruits")

                .post()
                .type(Fruit.class)
                .to("direct:addFruit");

        rest("/legumes")
                .get()
                .to("direct:getLegumes");

        from("direct:getFruits")
                .setBody().constant(fruits);

        from("direct:addFruit")
                .process().body(Fruit.class, fruits::add)
                .setBody().constant(fruits);

        from("direct:getLegumes")
                .setBody().constant(legumes);
    }
}	
```

> 这段代码使用 Camel DSL（领域特定语言）来配置 Camel Quarkus 应用程序。在 `configure()` 方法中，使用 `restConfiguration()` 方法来配置 REST 组件，指定 REST 绑定模式为 JSON 格式，即 `.bindingMode(RestBindingMode.json)`。
>
> 接下来，使用 `rest()` 方法来定义两个 REST API 端点，即 `/fruits` 和 `/legumes`。对于 `/fruits`，定义了 GET 和 POST 两种请求方法，分别对应获取水果数据和添加水果数据。对于 GET 方法，将请求路由到 `direct:getFruits` 端点，从而获取水果数据并返回 JSON 格式的响应；对于 POST 方法，先使用 `type()` 方法定义请求体类型为 `Fruit` 类型，再将请求路由到 `direct:addFruit` 端点，从而将请求体中的水果数据添加到数据源中。
>
> 对于 `/legumes`，只定义了 GET 请求方法，将请求路由到 `direct:getLegumes` 端点，从而获取蔬菜数据并返回 JSON 格式的响应。
>
> 最后，使用 `from()` 方法来定义 `direct:getFruits`、`direct:addFruit` 和 `direct:getLegumes` 这三个端点的实现。其中，`direct:getFruits` 和 `direct:getLegumes` 直接将数据源中的数据设置为响应体，以返回 JSON 格式的响应。而 `direct:addFruit` 则将请求体中的数据添加到数据源中，并将数据源作为响应体返回。

- 添加配置  : 在`src\main\resources\application.properties`中添加

  - ```properties
    camel.context.name = quarkus-camel-example-rest-json
    ```

    - camel.context.name 是 Camel Quarkus 中的一个属性，用于设置 Camel 上下文的名称。

## 启动开发模式

```shell
mvn clean compile quarkus:dev
```

## 验证

可以在浏览器中检查应用程序的访问地址，例如 `http://localhost:8080/fruits`，对于示例的 `rest-json` 应用程序，这里是检查水果数据接口。

可以修改代码，并通过刷新浏览器查看应用程序中的更改生效。

**`GET`**

```bash
$ curl -l http://localhost:8080/fruits
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   141    0   141    0     0    582      0 --:--:-- --:--:-- --:--:--   585
[{"name":"Apple","description":"Winter fruit"},{"name":"Pineapple","description":"Tropical fruit"},{"name":"西瓜","description":"好吃!"}]
```

**`POST`**

```bash
curl -X POST -H "Content-Type: application/json" -d '{"name":"banana", "description":"Banana plants in the family Banana" }' http://localhost:8080/fruits
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   280    0   210  100    70    937    312 --:--:-- --:--:-- --:--:--  1250[{"name":"Apple","description":"Winter fruit"},{"name":"Pineapple","description":"Tropical fruit"},{"name":"西瓜","description":"好吃!"},{"name":"banana","description":"Banana plants in the family Banana"}]
```

再次发生 **`GET请求`**

```bash
$ curl -l http://localhost:8080/fruits
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   210    0   210    0     0    941      0 --:--:-- --:--:-- --:--:--   945[{"name":"Apple","description":"Winter fruit"},{"name":"Pineapple","description":"Tropical fruit"},{"name":"西瓜","description":"好吃!"},{"name":"banana","description":"Banana plants in the family Banana"}]

```

## 打包

```bash
mvn clean package -P native
```

> 通过Quarkus Native Image构建可执行文件。Quarkus是一个基于GraalVM的Java框架，它可以将Java应用程序编译成本地可执行文件。
>
> 打包后可以直接在Windows操作系统上运行的可执行文件，双击运行它，看看能否启动应用程序。

![image-20230426150750532](https://www.img.heyiwen.com/blog/image-20230426150750532.png)

- 运行

![image-20230426150800174](https://www.img.heyiwen.com/blog/image-20230426150800174.png)

- 测试

```bash
curl -l http://localhost:8080/fruits
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   141    0   141    0     0  20142      0 --:--:-- --:--:-- --:--:-- 23500[{"name":"Apple","description":"Winter fruit"},{"name":"Pineapple","description":"Tropical fruit"},{"name":"西瓜","description":"好吃!"}]
```

## 总结

这个代码的核心是使用 Camel Quarkus 框架实现一个 RESTful 风格的 Web 应用程序，该应用程序提供了一个名为 `/fruits` 的 HTTP GET API，返回 JSON 格式的水果列表数据。

在实现过程中，使用了 Camel Quarkus 中的许多组件和特性，例如 `camel-rest` 组件来实现 RESTful 风格的 API，`camel-jackson` 组件来支持 JSON 格式数据的序列化和反序列化，`camel-infinispan` 组件来提供一个基于 Infinispan 缓存的数据存储，以及 `camel-context` 组件来管理 Camel 上下文的创建和销毁等。

此外，该示例还使用了 Quarkus 框架的特性，例如 DevMode 开发模式的支持，即开发者可以在开发过程中对代码进行修改，并且自动重启应用程序以反映更改，以及 Quarkus 的构建和部署特性等。

因此，`camel-quarkus-examples/rest-json` 这个示例代码的核心是展示如何使用 Camel Quarkus 和 Quarkus 框架的特性和组件来开发一个具有 RESTful 风格的 Web 应用程序。

## 相关文档

- [QuarkusCamel官方文档](https://quarkus.io/guides/camel)
- [ApacheCamel官方文档](https://camel.apache.org/camel-quarkus/2.16.x/index.html)
- [Camel Quarkus 示例](https://camel.apache.org/camel-quarkus/2.16.x/user-guide/examples.html)
- [Camel-Quarkus支持的扩展](https://camel.apache.org/camel-quarkus/2.16.x/reference/index.html)

