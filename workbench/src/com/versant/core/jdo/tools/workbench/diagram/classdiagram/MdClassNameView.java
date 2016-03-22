
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

import com.jgraph.JGraph;
import com.jgraph.graph.*;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.diagram.InfoListComponent;
import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class MdClassNameView extends VertexView implements CellViewRenderer {

    private static final Font FONT_TITLE = new Font("Dialog", Font.BOLD, 11);
    private static final Font FONT_PACKAGE = new Font("Dialog", Font.PLAIN, 9);
    private static final Font FONT = new Font("Dialog", Font.PLAIN, 11);
    private static final Font FONT_INFO = new Font("Dialog", Font.PLAIN, 10);

    private static final Border BORDER =
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.black, 1),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1));

    private InfoListComponent info = new InfoListComponent();

    public MdClassNameView(Object o, JGraph jGraph, CellMapper cellMapper) {
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
        MdClass mdc = (MdClass)cell.getUserObject();
        info.clear();
        ClassDiagram.Settings s = ((ClassGraph)graph).getDiagram().getSettings();
        boolean isJDBC = mdc.getMdDataStore().isJDBC();
        if (isJDBC && s.isClassTableFirst()) {
            if (s.isClassTable()) addTableName(mdc, FONT_TITLE);
            if (s.isClassName()) info.addLine(mdc.getName(), FONT);
            if (s.isClassPackage()) {
                info.addLine(mdc.getMdPackage().getName(),
                        FONT_PACKAGE);
            }
        } else {
            if (s.isClassName()) info.addLine(mdc.getName(), FONT_TITLE);
            if (s.isClassPackage()) {
                info.addLine(mdc.getMdPackage().getName(),
                        FONT_PACKAGE);
            }
            if (isJDBC && s.isClassTable()) addTableName(mdc, FONT);
        }

        if (s.isAnyClassExtraSet()) {
            info.addSeparator();
            if (s.isClassDatastore()) info.addLine(mdc.getMdDataStore().getName());
            if (s.isClassDeleteOrphans() && mdc.isDeleteOrphansBool()) {
                info.addLine("delete-orphans");
            }
            if (isJDBC && s.isClassObjectIdClass()) {
                info.addLine(mdc.getObjectIdClassStr());
            }
            if (s.isClassCache()) {
                info.addLine("cache: " + getCacheStrategy(mdc));
            }
            if (s.isClassOptLocking()) {
                info.addLine("opt-locking: " + getOptLocking(mdc));
            }
            if (isJDBC && s.isClassDoNotCreateTable()) {
                info.addLine("do-not-create-table: ",
                        mdc.getJdbcDoNotCreateTableStr());
            }
            if (isJDBC && s.isClassKeyGenerator()) {
                info.addLine("key-generator: " + getKeyGenerator(mdc));
            }
            if (isJDBC && s.isClassClassID()) {
                info.addLine("class-id: " + mdc.getDefJdbcClassId());
            }
            if (isJDBC && s.isClassJDBCClassID()) {
                info.addLine("jdbc-class-id: " + getJdbcClassId(mdc));
            }
            if (isJDBC && s.isClassUseJoin()) {
                info.addLine("use-join: " + getUseJoin(mdc));
            }
            if (s.isClassJdoFile()) {
                info.addLine(mdc.getMdJdoFile().getResourceName());
            }
        }
        return info;
    }

    private void addTableName(MdClass mdc, Font font) {
        info.addLine(mdc.getJdbcTableFinalName(), font);
    }

    private String getCacheStrategy(MdClass c) {
        String s = c.getCacheStrategyStr();
        if (s == null) s = c.getMdDataStore().getCacheStrategyStr();
        return s;
    }

    private String getOptLocking(MdClass c) {
        String s = c.getJdbcOptimisticLockingStr();
        if (s == null) s = c.getMdDataStore().getJdbcOptimisticLockingStr();
        return s;
    }

    private String getKeyGenerator(MdClass c) {
        String s = c.getJdbcKeyGeneratorStr();
        if (s == null) s = c.getMdDataStore().getJdbcKeyGeneratorStr();
        return s;
    }

    private String getJdbcClassId(MdClass c) {
        String s = c.getJdbcClassIdStr();
        if (s == null) s = c.getDefJdbcClassId();
        return s;
    }

    private String getUseJoin(MdClass c) {
        String s = c.getJdbcUseJoinStr();
        if (s == null) s = "no";
        return s;
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
