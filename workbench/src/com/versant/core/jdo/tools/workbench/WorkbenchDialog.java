
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
package com.versant.core.jdo.tools.workbench;

import za.co.hemtech.config.Config;
import za.co.hemtech.config.DelimList;
import za.co.hemtech.config.DelimListParser;
import za.co.hemtech.gui.ExceptionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

import com.versant.core.jdo.tools.workbench.model.ExceptionListenerManager;

/**
 * JDialog wrapper for a WorkbenchPanel.
 */
public class WorkbenchDialog extends JDialog
        implements ExceptionListener {
    private Config userConfig;

    public WorkbenchDialog(Dialog dialog, boolean modal) {
        super(dialog, modal);
        initWorkbenchDialog();
    }

    public WorkbenchDialog(Frame frame, boolean modal) {
        super(frame, modal);
        initWorkbenchDialog();
    }

    private void initWorkbenchDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ((JComponent)getContentPane()).setMinimumSize(new Dimension(1, 1));
        setResizable(true);
    }

    public boolean handleException(Object source, Throwable x) {
        return ExceptionListenerManager.getDefaultExceptionListenerManager().handleException(source, x);
    }

    public Config getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(Config userConfig) {
        this.userConfig = userConfig;
        restoreState();
    }

    public void setName(String name) {
        super.setName(name);
        restoreState();
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            saveState();
        }
    }

    public void restoreState() {
        if (userConfig == null || getName() == null) return;
        String s = userConfig.getString(getName());
        if (s == null) {
            pack();
            setLocationRelativeTo(getParent());
        } else {
            DelimListParser p = new DelimListParser(s);
            setLocation(p.nextInt(), p.nextInt());
            setSize(p.nextInt(), p.nextInt());
        }
    }

    public void saveState() {
        if (userConfig == null || getName() == null) return;
        DelimList l = new DelimList();
        Rectangle r = getBounds();
        l.append(r.x);
        l.append(r.y);
        l.append(r.width);
        l.append(r.height);
        userConfig.setString(getName(), l.toString());
    }
}

