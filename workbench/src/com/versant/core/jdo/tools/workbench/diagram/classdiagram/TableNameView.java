
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

import com.versant.core.jdo.tools.workbench.diagram.InfoListComponent;
import com.versant.core.jdo.tools.workbench.model.GraphTable;

/**
 * View for the name of a JdbcTable.
 *
 * @keep-all
 */
public class TableNameView extends VertexView implements CellViewRenderer {

    private static final Font FONT_TITLE = new Font("Dialog", Font.BOLD, 11);
    private static final Font FONT_COMMENT = new Font("Dialog", Font.PLAIN, 9);
    private static final Font FONT_INFO = new Font("Dialog", Font.PLAIN, 10);

    private static final Border BORDER =
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.black, 1),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1));

    private InfoListComponent info = new InfoListComponent();

    public TableNameView(Object o, JGraph jGraph, CellMapper cellMapper) {
        super(o, jGraph, cellMapper);
        info.setFont(FONT_INFO);
        info.setAlignmentX(0.5f);
        info.setBorder(BORDER);
    }

    public CellViewRenderer getRenderer() {
        return this;
    }

    public Component getRendererComponent(JGraph graph, CellView view,
            boolean sel, boolean focus, boolean preview) {
        DefaultGraphCell cell = (DefaultGraphCell)getCell();
        GraphTable table = (GraphTable)cell.getUserObject();
        info.clear();
        StringBuffer title = new StringBuffer();
        title.append(table.getName());
        if (table.isDoNotCreate()) title.append(" [DNC]");
        info.addLine(title.toString(), FONT_TITLE);
        info.addLine(table.toString(), FONT_COMMENT);
        return info;
    }

    /**
     * Returns the intersection of the bounding rectangle and the
     * straight line between the source and the specified point p.
     * The specified point is expected not to intersect the bounds.
     */
    public Point getPerimeterPoint(Point source, Point p) {
        Rectangle bounds = getBounds();
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;
        int xCenter = (x + width / 2);
        int yCenter = (y + height / 2);
        int dx = p.x - xCenter; // Compute Angle
        int dy = p.y - yCenter;
        double alpha = Math.atan2(dy, dx);
        int xout = 0, yout = 0;
        double pi = Math.PI;
        double t = Math.atan2(height, width);
        if (alpha < -pi + t || alpha > pi - t) { // Left edge
            xout = x;
            yout = yCenter - (int)(width * Math.tan(alpha) / 2);
        } else if (alpha < -t) { // Top Edge
            return ((VertexView)getParentView()).getPerimeterPoint(source, p);
        } else if (alpha < t) { // Right Edge
            xout = x + width;
            yout = yCenter + (int)(width * Math.tan(alpha) / 2);
        } else { // Bottom Edge
            return ((VertexView)getParentView()).getPerimeterPoint(source, p);
        }
        return new Point(xout, yout);
    }
}
