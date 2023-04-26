package com.heyiwen.scenes;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterForReflection
public class HttpRoute extends RouteBuilder {

//    @Override
//    public void configure() throws Exception {
//        from("timer://myTimer?period=5000")
//                .to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo")
//                .unmarshal().json(true)
//                .log("${body}");
//    }
    @Override
    public void configure() throws Exception {
        from("timer://myTimer?period=5000")
                .to("http://parkinglot.banff-tech.com/Parking/Handheld/GetParkingLotInfo")
                .unmarshal().json(true)
                .log("${body}");

    }
}
