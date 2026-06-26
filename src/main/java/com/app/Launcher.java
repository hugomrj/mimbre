package com.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Launcher {



    public void open() {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Mimbre");
        shell.setSize(1200, 800);
        shell.setLayout(new FillLayout());

        Browser browser = new Browser(shell, SWT.EDGE);
        browser.setUrl("http://localhost:8080");

        shell.open();

        // El bucle de eventos debe ser el último proceso
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }


}

