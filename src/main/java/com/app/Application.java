package com.app;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;

public class Application {

    public static void main(String[] args) {

        EmbeddedServer server =
                ApplicationContext.run(EmbeddedServer.class);

        System.out.println(server.getURL());

        new Launcher().open();

        server.stop();
    }
}