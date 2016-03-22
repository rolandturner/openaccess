
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
package com.versant.core.jdo.tools.workbench.forms.overview;

import za.co.hemtech.gui.util.GuiUtils;
import com.versant.core.jdo.tools.workbench.BrowserControl;
import com.versant.core.jdo.tools.workbench.WorkbenchSettings;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URL;

/**
 * Form with an overview of the Workbench. This is displayed on the tree view
 * as help.
 */
public class WorkbenchOverviewForm extends WorkbenchPanel
        implements HyperlinkListener {

    public WorkbenchOverviewForm(String htmlResName) throws Exception {
        URL url = getClass().getResource(htmlResName);
        JEditorPane ed = new JEditorPane(url);
        ed.setEditable(false);
        ed.addHyperlinkListener(this);
        ed.setBorder(BorderFactory.createMatteBorder(20, 32, 20, 32, Color.white));
        JScrollPane scroller = new JScrollPane(ed);
        scroller.setName("scroller");
        add(scroller);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            status("");
            return;
        }
        URL url = e.getURL();
        String q = url.toExternalForm();
        status(q);
        if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
        try {
            info("Opening " + q);
            WorkbenchSettings s = getSettings();
            BrowserControl.displayURL(s.getWinBrowser(), s.getUnixBrowser(), q);
        } catch (Exception x) {
            GuiUtils.dispatchException(this, x);
        }
    }

    public void dispose() throws Exception {
    }

    public void projectClosed() throws Exception {
    }

}

