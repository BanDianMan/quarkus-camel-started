package com.heyiwen.scenes;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Author: H.J
 * Date: 16:02
 * Description:
 */
@ApplicationScoped
@RegisterForReflection
public class FileToAmqpRoute extends RouteBuilder {

    @Inject
    AMQPComponent amqp;

    @Override
    public void configure() throws Exception {
        from("file:data/input?noop=true")
                .to("amqp:queue:myQueue");
    }
}
