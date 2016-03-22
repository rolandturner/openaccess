
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
package gui;

import util.JDOSupport;

import javax.swing.*;
import java.awt.*;

/**
 * Launcher. This creates and displays the MainFrame on the
 * EventDispatchThread.
 *
 */
public class Main implements Runnable {

    public static void main(String[] args) {
        try {
            JDOSupport.init(args.length > 0 ? args[0] : null);
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
            f.setSize(800, 600);
            Dimension sz = f.getToolkit().getScreenSize();
            f.setLocation((sz.width - f.getWidth()) / 2,
                    (sz.height - f.getHeight()) / 2);
            f.setVisible(true);
            f.chooseBranch();
        } catch (Throwable e) {
            error = e;
        }
    }

}

