package com.heyiwen.scenes;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.builder.RouteBuilder;
import javax.enterprise.context.ApplicationScoped;

/**
 * Author: H.J
 * Date: 15:37
 * Description:
 */
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
