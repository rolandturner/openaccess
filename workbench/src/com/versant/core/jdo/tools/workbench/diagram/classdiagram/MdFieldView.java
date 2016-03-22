
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
import java.util.List;

import com.versant.core.jdo.tools.workbench.model.MdField;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.diagram.FieldListComponent;
import com.versant.core.jdo.tools.workbench.diagram.FieldListColModel;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;

/**
 * @keep-all
 */
public class MdFieldView extends VertexView implements CellViewRenderer {

    private static final Font FONT = new Font("Dialog", Font.PLAIN, 11);
    private static final Border BORDER = BorderFactory.createEmptyBorder(0, 3,
            0, 3);

    private FieldListComponent flc;
//    private List cols;

    public MdFieldView(Object o, JGraph jGraph, CellMapper cellMapper) {
        super(o, jGraph, cellMapper);
//        cols = getMdField().getCompositePkRefCols();
        flc = new FieldListComponent();
        flc.setFont(FONT);
        flc.setBorder(BORDER);
    }

    public void refresh(boolean createDependentViews) {
        super.refresh(createDependentViews);
        if (!(parent instanceof MdClassView)) return;
        FieldListColModel cm = ((MdClassView)parent).getColModel();
        if (flc.getColModel() == null) flc.setColModel(cm);
    }

    public CellViewRenderer getRenderer() {
        return this;
    }

    public Component getRendererComponent(JGraph graph, CellView view,
            boolean sel, boolean focus, boolean preview) {
        ClassGraph cg = (ClassGraph)graph;
        sel = cg.getInnerSelection() == this;
        Color color = sel ? Color.blue : Color.black;
        ClassDiagram.Settings s = cg.getDiagram().getSettings();
        MdField f = getMdField();
        flc.setForeground(color);
        populateFieldList(s, f, flc);
        flc.getColModel().reCalcColWidths();
        return flc;
    }

    private MdField getMdField() {
        MdFieldCell cell = (MdFieldCell)getCell();
        MdField f = cell.getMdField();
        return f;
    }

    private void populateFieldList(ClassDiagram.Settings s, MdField f,
            FieldListComponent flc) {
        flc.clear();
        if (s.isFieldName()) flc.addColumn(f.getName(), 0);
        if (s.isFieldType()) {
            String t = f.getShortTypeStr();
            t = t.substring(t.lastIndexOf('.') + 1);
            flc.addColumn(t, 0);
        }
        boolean isJDBC = f.getMdClass().getMdDataStore().isJDBC();
        if (isJDBC && s.isFieldColumnName()) {
            String t;
            switch (f.getCategory()) {
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                    t = f.getJdbcLinkTableNameStr();
                    if (t == null && f.getJdbcLinkCollectionField() != null) {
                        t = f.getJdbcLinkCollectionField().linkTable.name;
                    }
                    break;
                default:
                    if (f.isCompositePkRef()) {
                        t = f.getColumnNames();
                        if (t == null) t = "{n/a}";
                    } else {
                        t = f.getColumnNameStr();
                        if (t == null) t = f.getColumnDefName();
                        if (t == null) t = "{auto}";
                    }
                    break;
            }
            flc.addColumn(t, 1);
        }
        if (isJDBC && s.isFieldSQLType()) {
            flc.addColumn(f.getSqlDDL(), 1);
        }
        // make sure we always have at least one column
        if (flc.getColumnCount() == 0) {
            flc.addColumn(f.getName(), 0);
        }
        flc.calcColumnAlignments();
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
        int xCenter = x + width / 2;
        int yCenter = y + height / 2;
        int dx = p.x - xCenter; // Compute Angle
        int dy = p.y - yCenter;
        double alpha = Math.atan2(dy, dx);
        int xout = 0, yout = 0;
        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double beta = pi2 - alpha;
        double t = Math.atan2(height, width);
        if (alpha < -pi + t || alpha > pi - t) { // Left edge
            xout = x;
            yout = yCenter - (int)(width * Math.tan(alpha) / 2);
        } else if (alpha < -t) { // Top Edge
            return ((VertexView)getParentView()).getPerimeterPoint(source, p);
//            yout = y;
//            xout = xCenter - (int) (height * Math.tan(beta) / 2);
        } else if (alpha < t) { // Right Edge
            xout = x + width;
            yout = yCenter + (int)(width * Math.tan(alpha) / 2);
        } else { // Bottom Edge
            return ((VertexView)getParentView()).getPerimeterPoint(source, p);
//            yout = y + height;
//            xout = xCenter + (int) (height * Math.tan(beta) / 2);
        }
        return new Point(xout, yout);
    }

    static class BigerLabel extends JLabel {

        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width += 3;
            return d;
        }
    }
}
