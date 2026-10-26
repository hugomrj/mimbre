package com.app;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

public class Application {

    private static final int PUERTO = 8000;
    private static final String URL_LOCAL = "http://localhost:" + PUERTO;

    public static void main(String[] args) {

        // 0. Detectar si estamos en modo desarrollo (IDE) o producción (JAR empaquetado)
        boolean isPackaged = Application.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getProtocol()
                .equals("jar");

        // 0.1 Pre-check: si el puerto ya está ocupado, salir limpio en vez de crashear.
        //     Esto es lo que evita el BindException cuando el usuario abre el .exe dos veces
        //     o cuando quedó un proceso colgado de una corrida anterior.
        if (puertoEnUso(PUERTO)) {
            System.err.println("==================================================");
            System.err.println(" ⚠  Ya hay una instancia de Mimbre corriendo.");
            System.err.println("    URL: " + URL_LOCAL);
            System.err.println("    Si querés reiniciar: ícono de la bandeja → 'Salir'.");
            System.err.println("==================================================");

            // Si es el .exe y el usuario hizo doble clic, le abrimos la que ya está corriendo.
            if (isPackaged) {
                abrirNavegador();
            }
            return;   // salida limpia, sin stack trace
        }

        // 1. Iniciar Micronaut en segundo plano
        final ApplicationContext context = Micronaut.run(Application.class, args);

        // 2. Auto-abrir navegador solo en la versión empaquetada
        if (isPackaged) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Esperamos 2 seg a que el servidor esté listo
                    abrirNavegador();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("==================================================");
            System.out.println(" MODO DESARROLLO: Servidor iniciado en " + URL_LOCAL);
            System.out.println(" El navegador NO se abrirá automáticamente para no molestar.");
            System.out.println(" Puedes abrirlo manualmente desde el ícono de la bandeja del sistema.");
            System.out.println(" Para detener: ícono de bandeja → 'Salir' (NO Ctrl+C).");
            System.out.println("==================================================");
        }

        // 3. Crear el icono en la bandeja del sistema
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            // Icono cuadrado con el color primario (#06B6D4)
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.setColor(new java.awt.Color(6, 182, 212));
            g2d.fillRect(0, 0, 16, 16);
            g2d.dispose();

            PopupMenu popup = new PopupMenu();

            // Abrir navegador manualmente
            MenuItem openItem = new MenuItem("Abrir en navegador");
            openItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    abrirNavegador();
                }
            });
            popup.add(openItem);

            // Salir limpiamente
            MenuItem exitItem = new MenuItem("Salir");
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    context.close();
                    System.exit(0);
                }
            });
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Mimbre - Servidor Local", popup);
            trayIcon.setImageAutoSize(true);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else {
            // Fallback: sin bandeja de sistema, el proceso queda vivo escuchando
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Intenta bindear el puerto: si falla es que está ocupado.
     * ServerSocket se libera automáticamente gracias al try-with-resources.
     */
    private static boolean puertoEnUso(int puerto) {
        try (ServerSocket ignored = new ServerSocket(puerto)) {
            return false;   // se pudo bindear → libre
        } catch (IOException e) {
            return true;    // falló → ocupado
        }
    }

    /** Abre el navegador en la URL local si el SO lo soporta. */
    private static void abrirNavegador() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(URL_LOCAL));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}