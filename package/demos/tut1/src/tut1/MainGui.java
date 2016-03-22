
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
package tut1;

import tut1.util.JDOSupport;
import tut1.gui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Launcher. This creates and displays the MainFrame on the
 * EventDispatchThread.
 * It can be run in two modes depending on the supplied command line argument.
 *
 * <ol>
 * <li>(none): Connects directly to the database.
 * <li><code>remote_host</code>: Run against a JDO Genie server on remote_host.
 * </ol>
 *
 * To test remote operation start a server in one console (ant run-server) and
 * then run the demo in remote mode from another console (ant run-remote).
 * If you want to connect from a different machine then edit the run-remote
 * task in build.xml to specify the host name as a argument.
 *
 */
public class MainGui implements Runnable {

    public static void main(String[] args) {
        try {
            JDOSupport.init(args.length > 0 ? args[0] : null);
            MainGui main = new MainGui();
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
            f.setSize(800, 600);
            Dimension sz = f.getToolkit().getScreenSize();
            f.setLocation((sz.width - f.getWidth()) / 2,
                    (sz.height - f.getHeight()) / 2);
            f.setVisible(true);
        } catch (Throwable e) {
            error = e;
        }
    }

}

