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


## 场景2:
还有种场景在实际项目中非常常见。
许多应用程序都需要集成多个服务或系统，这可能涉及到与外部API进行通信、将数据转换为不同的格式，或将数据传输到其他系统或服务。
使用Quarkus Camel框架可以使这个过程更加容易和可靠。



### 添加依赖
```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-http</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-jsonpath</artifactId>
</dependency>
```
### 添加路由类
```java
@ApplicationScoped
@RegisterForReflection
public class HttpRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer://myTimer?period=5000")
                .to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo")
                .unmarshal().json(true)
                .log("${body}");
    }
}

```
> 在 configure() 方法中，通过 from("timer://myTimer?period=5000") 创建一个定时器触发器，每 5000 毫秒执行一次路由。然后通过 to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo") 发送一个 HTTP GET 请求到指定的 URL，获取该接口返回的数据。接下来，使用 .unmarshal().json(true) 将返回的 JSON 数据反序列化为 Java 对象，并通过 .log("${body}") 将结果打印到日志中。

👇 控制台日志 ： 
```bash
INFO  [route7] (Camel (camel-1) thread #2 - timer://myTimer) {resCode=0, resMsg=Park parking lot, totalNum=1000, totalStopNum=439, totalRemainNum=561, parkingLotInfo=[{parkingLotId=1, parkingLotName=Park parking lot No:1, totalNum=1000, totalStopNum=439, totalRemainNum=561}]}
```

#### 改造1
```java
@ApplicationScoped
@RegisterForReflection
public class HttpRoute extends RouteBuilder {

    public void configure() throws Exception {
        from("timer://myTimer?period=5000")
                .to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo")
                .unmarshal().json(true)
                .enrich("direct:extractData")
                .log("内容为：${header.Data}");


        from("direct:extractData")
                .process(exchange -> {
                    // 拿值
                    String partyId = "TEST_Party" + exchange.getIn().getBody(JsonObject.class).getString("totalStopNum").toString();
                    // 定义值
                    String partyTypeId = "PERSON";
                    // 拿值
                    String partyName = "TEST_Party" + exchange.getIn().getBody(JsonObject.class).getString("totalRemainNum").toString();

                    JsonObject json = new JsonObject()
                            .put("partyId", partyId)
                            .put("partyTypeId", partyTypeId)
                            .put("partyName", partyName);

                    exchange.getIn().setHeader("Data", json.toString());
                });
    }
}
```
> 每隔5秒钟调用远程的HTTP接口获取停车场信息，然后将结果解析成JSON格式。接着通过enrich()方法将获取到的停车场信息与另外一个路由中处理的数据进行合并，合并的方式是将两个JSON对象中的属性合并到一个新的JSON对象中。最后将合并后的数据打印出来。
其中，from("timer://myTimer?period=5000")表示每隔5秒钟执行一次任务；to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo")表示调用远程的HTTP接口获取停车场信息；unmarshal().json(true)表示将获取到的信息解析成JSON格式；enrich("direct:extractData")表示调用另一个路由进行数据合并；log("内容为：${header.Data}")表示打印合并后的数据。另外一个路由定义在from("direct:extractData")中，通过process()方法对获取到的停车场信息进行处理，并将处理结果放入header中返回给原路由。

👇 控制台日志 ： 
> INFO  [route71] (Camel (camel-9) thread #18 - timer://myTimer) 内容为：{"partyId":"TEST_Party472","partyTypeId":"PERSON","partyName":"TEST_Party528"}
            

