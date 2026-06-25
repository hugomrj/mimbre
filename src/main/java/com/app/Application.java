package com.app;

import io.micronaut.runtime.Micronaut;


public class Application {
    public static void main(String[] args) {
        // Lanzamos Micronaut en un hilo separado
        new Thread(() -> Micronaut.run(Application.class, args)).start();

        // Lanzamos la interfaz gráfica en el hilo principal
        new Launcher().open();
    }
}


