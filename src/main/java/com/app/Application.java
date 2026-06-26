package com.app;


import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {

        // 1. Iniciamos Micronaut (Esto levanta el servidor en el puerto 8080)
        ApplicationContext context = Micronaut.run(Application.class, args);

        // 2. Lanzamos la ventana SWT (Este método se queda bloqueado hasta que el usuario cierre la ventana)
        new Launcher().open();

        // 3. Cuando la ventana se cierre, apagamos Micronaut limpiamente
        System.out.println("Cerrando aplicación...");
        context.close();
    }
}