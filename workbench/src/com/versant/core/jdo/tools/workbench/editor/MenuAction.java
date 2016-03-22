
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
package com.versant.core.jdo.tools.workbench.editor;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.EventObject;

/**
 *
 * @keep-all
 */
public abstract class MenuAction implements ActionListener {
    protected String name;

    /**
     * Creates a new menu action designed by its name.
     * This name is internally used by Jext to handles scripts,
     * correctly build menu bar and tool bar.
     * @param name Internal action name
     */

    public MenuAction(String name) {
        this.name = name;
    }

    /**
     * Returns the associated action name.
     */

    public String getName() {
        return name;
    }

    /**
     * This methods returns the selected text area in the window
     * which fired the event.
     * @param evt The source event
     */

    public static JEditTextArea getTextArea(EventObject evt) {
        return getJextParent(evt);
    }

    /**
     * This methods returns the selected text area in the window
     * which fired the event, excluding the splitted one.
     * @param evt The source event
     */

    public static JEditTextArea getNSTextArea(EventObject evt) {
        return getJextParent(evt);
    }

    /**
     * Returns the window which fired the event.
     * @param evt The source event
     */

    public static JEditTextArea getJextParent(EventObject evt) {
        if (evt != null) {
            Object o = evt.getSource();
            if (o instanceof Component) {
                Component c = (Component) o;
                for (; ;) {
                    if (c instanceof JEditTextArea)
                        return (JEditTextArea) c;
                    else if (c == null)
                        break;
                    if (c instanceof JPopupMenu)
                        c = ((JPopupMenu) c).getInvoker();
                    else
                        c = c.getParent();
                }
            }
        }
        return null;
    }

    public static JEditTextArea getTextArea(Component c) {
        return getJextParent(c);
    }

    public static JEditTextArea getNSTextArea(Component c) {
        return getJextParent(c);
    }

    public static JEditTextArea getJextParent(Component comp) {
        for (; ;) {
            if (comp instanceof JEditTextArea)
                return (JEditTextArea) comp;
            else if (comp instanceof JPopupMenu)
                comp = ((JPopupMenu) comp).getInvoker();
            else if (comp != null)
                comp = comp.getParent();
            else
                break;
        }
        return null;
    }
}

// End of MenuAction.java

