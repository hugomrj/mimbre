package com.app;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;

public class Application {

    public static void main(String[] args) {

        // 1. Iniciar el contexto y el servidor
        ApplicationContext context = ApplicationContext.run(EmbeddedServer.class);
        EmbeddedServer server = context.getBean(EmbeddedServer.class);

        System.out.println("Servidor iniciado en: " + server.getURL());

        // 2. Lanzar la ventana (Este método se queda bloqueado hasta que el usuario cierre la ventana)
        new Launcher().open();

        // 3. Cuando la ventana se cierre, apagar todo limpiamente
        System.out.println("Cerrando aplicación...");
        context.close();
    }
}