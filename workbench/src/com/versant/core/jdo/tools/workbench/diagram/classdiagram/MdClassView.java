
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
package com.versant.core.jdo.tools.workbench.diagram.classdiagram;

import com.jgraph.graph.*;
import com.jgraph.JGraph;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import za.co.hemtech.gui.exp.ExpTableModel;
import com.versant.core.jdo.tools.workbench.diagram.FieldListColModel;

/**
 * @keep-all
 */
public class MdClassView extends VertexView implements CellViewRenderer, CellHandle {

    private static final Border BORDER = BorderFactory.createLineBorder(Color.black);

    private JPanel panel = new JPanel();
    private Color colorSelected = UIManager.getColor("textHighlight");
    private FieldListColModel colModel = new FieldListColModel();

    public MdClassView(Object o, JGraph jGraph, CellMapper cellMapper) {
        super(o, jGraph, cellMapper);
        panel.setBorder(BORDER);
    }

    public CellViewRenderer getRenderer() {
        return this;
    }

    public FieldListColModel getColModel() {
        return colModel;
    }

    public void setColModel(FieldListColModel colModel) {
        this.colModel = colModel;
    }

    public void refresh(boolean createDependentViews) {
//        MdClassCell cell = (MdClassCell) getCell();
//        cell.getChildren()
        super.refresh(createDependentViews);
    }

    public Component getRendererComponent(JGraph graph, CellView view,
            boolean sel, boolean focus, boolean preview) {
        panel.setBackground(sel ? colorSelected : Color.white);
        return panel;
    }

    public CellHandle getHandle(GraphContext context) {
//            cellHandle = super.getHandle(context);
        return this;
    }

    public void paint(Graphics g) {
    }

    public void overlay(Graphics g) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mousePressed(MouseEvent event) {
        if (event.getClickCount() == graph.getEditClickCount()) {
            graph.startEditingAtCell(this);
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent event) {
    }
}
