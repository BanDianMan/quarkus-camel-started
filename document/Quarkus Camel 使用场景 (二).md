# Quarkus Camel 使用 (二)

---

​	首先，需要创建一个基于Quarkus的应用程序，并添加Quarkus Camel扩展。

## 场景1：

**从文件夹读取文件并将内容输出到控制台**

实现步骤：

- 使用file组件读取指定的文件夹
- 使用log组件将读取到的文件内容输出到控制台

### 添加依赖

```xml
    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-file</artifactId>
    </dependency>
```

### 添加路由类

**`FileRoute`**

```java
@ApplicationScoped
@RegisterForReflection
public class FileRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("file://data/input/?noop=true")
                .routeId("file-route")
                .log("${body}");
    }
}
```

> 使用了`from("file://data/input?noop=true")`来从`data/input`文件夹中读取文件。`noop=true`表示不删除已经读取的文件。然后，我们将文件内容使用`log()`输出到控制台。

### 添加数据并启动项目

![image-20230426155520023](https://www.img.heyiwen.com/blog/image-20230426155520023.png)

