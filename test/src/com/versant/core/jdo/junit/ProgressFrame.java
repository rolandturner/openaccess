
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
package com.versant.core.jdo.junit;

import javax.swing.*;
import java.awt.*;

/**
 * Displays realtime progress of tests. Ant only collects whole lines of
 * output from JUnitTestRunner so it is not easy to see which test is
 * currently busy when one gets stuck.
 */
public class ProgressFrame extends JFrame {

    private JLabel label = new JLabel();

    public ProgressFrame(String title) {
        super(title);
        label.setText("No test");
        getContentPane().add(label, BorderLayout.CENTER);
        pack();
        int w = 600;
        int h = getHeight();
        setSize(w, h);
        Dimension sz = getToolkit().getScreenSize();
        setLocation(sz.width - w - 16, 0);
        setVisible(true);
    }

    public void setLabelText(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                label.setText(s);
            }
        });
    }

}

