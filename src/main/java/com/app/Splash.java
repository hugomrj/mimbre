package com.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Splash {

    private final Shell shell;

    public Splash(Display display) {

        shell = new Shell(display, SWT.ON_TOP);

        shell.setText("App");
        shell.setSize(400, 200);
        shell.setLayout(new FillLayout());

        Label label = new Label(shell, SWT.CENTER);
        label.setText("Cargando ...");
    }

    public void open() {
        shell.open();
    }

    public void close() {
        shell.dispose();
    }
}