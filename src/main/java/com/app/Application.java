package com.app;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URI;

public class Application {

    public static void main(String[] args) {
        // 1. Iniciar Micronaut en segundo plano
        final ApplicationContext context = Micronaut.run(Application.class, args);

        // 2. Abrir el navegador predeterminado después de un breve segundo
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Esperamos 2 seg a que el puerto 8080 esté listo
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 3. Crear el icono en la bandeja del sistema (abajo a la derecha)
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            // Creamos un icono cuadrado azul básico (16x16 pixeles)
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.setColor(Color.BLUE);
            g2d.fillRect(0, 0, 16, 16);
            g2d.dispose();

            // Creamos el menú al hacer clic derecho
            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Salir");

            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Cerrar Micronaut y salir del programa
                    context.close();
                    System.exit(0);
                }
            });

            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Servidor Mimbre", popup);
            trayIcon.setImageAutoSize(true);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else {
            // Si por alguna razón no hay bandeja, el programa se queda vivo infinitamente
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}