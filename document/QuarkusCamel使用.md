# QuarkusCamel 使用
有种场景在实际项目中非常常见。
许多应用程序都需要集成多个服务或系统，这可能涉及到与外部API进行通信、将数据转换为不同的格式，或将数据传输到其他系统或服务。
使用Quarkus Camel框架可以使这个过程更加容易和可靠。

## 接口集成

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






