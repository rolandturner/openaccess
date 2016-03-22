
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
package tut1.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Base class for JDialogs in this demo. This is a generic ActionListener
 * that dispatches events to methods using reflection. The actionCommand
 * is used as the method name.
 *
 */
public abstract class DialogBase extends JDialog implements ActionListener {
    protected MainFrame mainFrame;

    public DialogBase(MainFrame owner, String title, boolean modal) {
        super(owner, title, modal);
        this.mainFrame = owner;
    }

    public DialogBase(DialogBase owner, String title, boolean modal) {
        super(owner, title, modal);
        this.mainFrame = owner.mainFrame;
    }

    protected JButton createButton(String text, String method) {
        JButton b = new JButton(text);
        b.addActionListener(this);
        b.setActionCommand(method);
        return b;
    }

    public void actionPerformed(ActionEvent e) {
        mainFrame.invoke(this, e.getActionCommand());
    }

    public String showInputDialog(String msg, Object value) {
        return (String) JOptionPane.showInputDialog(this, msg, "Input",
                JOptionPane.QUESTION_MESSAGE, null, null,
                value);
    }

}

