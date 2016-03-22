
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
package tut2;

import tut2.gui.MainFrame;

/**
 * Main class for tut2. This creates a simple Swing GUI to maintain the
 * model.
 */
public class Main {

    public static void main(String[] args) {
        try {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setSize(500, 300);
            mainFrame.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

}


