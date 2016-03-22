
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.testcenter.gui;

import com.versant.testcenter.service.Context;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Launcher. This creates and displays the MainFrame on the
 * EventDispatchThread.
 *
 */
public class Main implements Runnable {

    public static void main(String[] args) {
        String url = "http://127.0.0.1:8080/testcenter/pmf";
        if (args.length == 1) {
            url = args[0];
        } else if (args.length > 1) {
            System.out.println("Expected name of remote host to connect to");
        }
        try {
            System.out.println("Connecting to " + url);
            Properties props = Context.loadJDOProperties();
            props.setProperty("versant.host", url);
            Context.initialize(props);
            Main main = new Main();
            SwingUtilities.invokeAndWait(main);
            if (main.error != null) throw main.error;
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private Throwable error;

    public void run() {
        try {
            MainFrame f = new MainFrame();
            f.setSize(400, 400);
            Dimension sz = f.getToolkit().getScreenSize();
            f.setLocation((sz.width - f.getWidth()) / 2,
                    (sz.height - f.getHeight()) / 2);
            f.setVisible(true);
            f.login();
        } catch (Throwable e) {
            error = e;
        }
    }

}

