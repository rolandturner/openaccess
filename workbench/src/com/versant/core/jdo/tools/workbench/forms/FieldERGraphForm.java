
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
package com.versant.core.jdo.tools.workbench.forms;

import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Form containing a FieldERGraph and associated ClassDiagram.
 */
public class FieldERGraphForm extends WorkbenchPanel {

    private ClassDiagram diagram;
    private FieldERGraph graph;

    private JLabel labHelp = new JLabel();
    private RightClickPopup rcp;

    private static final String HELP = "Double click elements to edit";
    private static final String HELP_MENU = HELP + ", right click for menu";

    public FieldERGraphForm() throws Exception {
        super("formGraph");

        diagram = new ClassDiagram(getProject());
        diagram.getSettings().setShowLegend(false);
        graph = new FieldERGraph(diagram);
        graph.setName("graph");
        graph.setAutoLayout(true);
        graph.addActionListener(this);
        graph.setToolTipText("piggy");

        labHelp.setName("labHelp");
        labHelp.setFont(new Font("dialog", 0, 9));
        labHelp.setText(HELP);

        add(graph);
        add(labHelp);

        setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
    }

    public ClassDiagram getDiagram() {
        return diagram;
    }

    public FieldERGraph getGraph() {
        return graph;
    }

    public void setMenu(JPopupMenu menu) {
        if (rcp == null) {
            rcp = new RightClickPopup(menu);
            graph.addMouseListener(rcp);
        } else {
            rcp.setPopupMenu(menu);
        }
        labHelp.setText(HELP_MENU);
    }

    public JPopupMenu getMenu() {
        return rcp == null ? null : rcp.getPopupMenu();
    }

    public Dimension getMinimumSize() {
        return super.getPreferredSize();
    }
    /**
     * This detects a right click on a component and displays a popup
     * menu.
     */
    public static class RightClickPopup extends MouseAdapter {

        private JPopupMenu popupMenu;

        public RightClickPopup() {
        }

        public RightClickPopup(JPopupMenu popupMenu) {
            this.popupMenu = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == 0) return;
            e.consume();
            popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
        }

        public void setPopupMenu(JPopupMenu popupMenu) {
            this.popupMenu = popupMenu;
        }

        public JPopupMenu getPopupMenu() {
            return popupMenu;
        }

    }
}

