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

        // 2. Detectar si estamos en modo desarrollo (IDE) o producción (JAR empaquetado)
        boolean isPackaged = Application.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getProtocol()
                .equals("jar");

        // Solo abrir el navegador automáticamente si es la versión empaquetada (ejecutable)
        if (isPackaged) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Esperamos 2 seg a que el servidor esté listo
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI("http://localhost:8000"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("==================================================");
            System.out.println(" MODO DESARROLLO: Servidor iniciado en http://localhost:8000");
            System.out.println(" El navegador NO se abrirá automáticamente para no molestar.");
            System.out.println(" Puedes abrirlo manualmente desde el icono de la bandeja del sistema.");
            System.out.println("==================================================");
        }

        // 3. Crear el icono en la bandeja del sistema (abajo a la derecha)
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            // Creamos un icono cuadrado con el color primario de la app (#06B6D4)
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.setColor(new java.awt.Color(6, 182, 212));
            g2d.fillRect(0, 0, 16, 16);
            g2d.dispose();

            // Creamos el menú al hacer clic derecho
            PopupMenu popup = new PopupMenu();

            // Opción para abrir manualmente (muy útil en desarrollo o si se cerró la pestaña)
            MenuItem openItem = new MenuItem("Abrir en navegador");
            openItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI("http://localhost:8000"));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            popup.add(openItem);

            MenuItem exitItem = new MenuItem("Salir");
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Cerrar Micronaut y salir del programa limpiamente
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
            // Si por alguna razón no hay bandeja de sistema, el programa se queda vivo infinitamente
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}