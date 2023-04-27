package com.heyiwen.scenes;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ApplicationScoped
@RegisterForReflection
public class HttpRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // 去一个接口拿数据
        from("timer://myTimer?period=10000")
                .to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo")
                .convertBodyTo(String.class) // 将响应主体转换为字符串
                .enrich("direct:extractData")
                .log("内容为：${header.Data}")
                .process(exchange -> {
                    String requestBody = "--batch_id-1566374639722-57\r\n"
                            + "Content-Type: application/http\r\n"
                            + "Content-Transfer-Encoding: binary\r\n\r\n"
                            + "POST Parties HTTP/1.1\r\n"
                            + "Accept: application/json;odata.metadata=minimal\r\n"
                            + "Content-Type: application/json;charset=UTF-8;\r\n"
                            + "OData-MaxVersion: 4.0\r\n\r\n"
                            + "{" + exchange.getIn().getHeader("Data") + "}\r\n"
                            + "--batch_id-1566374639722-57--\r\n";
                    exchange.getIn().setBody(requestBody);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "multipart/mixed; boundary=batch_id-1566374639722-57");
                    exchange.getIn().setHeader(Exchange.HTTP_URI, "http://mdt.mdt.banff-tech.com/mdt/control/odataAppSvc/mdtManage/$batch");
                })
                .to("http://mdt.mdt.banff-tech.com/mdt/control/odataAppSvc/mdtManage/$batch")
                .convertBodyTo(String.class) // 将响应主体转换为字符串
                .log("POST请求返回的内容为：${body}");


        // 将数据进行处理
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

        // 发送到另外一个接口
        from("direct:sendToOData")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("multipart/mixed; boundary=batch_id-1566374639722-57"))
                .setBody(constant("--batch_id-1566374639722-57\r\n"
                        + "Content-Type: application/http\r\n"
                        + "Content-Transfer-Encoding: binary\r\n\r\n"
                        + "POST Parties HTTP/1.1\r\n"
                        + "Accept: application/json;odata.metadata=minimal\r\n"
                        + "Content-Type: application/json;charset=UTF-8;\r\n"
                        + "OData-MaxVersion: 4.0\r\n\r\n"
                        + "{" + "${header.Data}" + "}\r\n"
                        + "--batch_id-1566374639722-57--\r\n"))
                .to("http://mdt.mdt.banff-tech.com/mdt/control/odataAppSvc/mdtManage/$batch");
    }


}
