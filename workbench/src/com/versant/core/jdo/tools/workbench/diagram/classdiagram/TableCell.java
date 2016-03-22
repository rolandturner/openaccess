
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
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.GraphTable;
import com.versant.core.jdo.tools.workbench.model.GraphColumn;
import com.versant.core.jdo.tools.workbench.model.GraphColumnRef;
import com.versant.core.common.Debug;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * A JdbcTable on a ClassGraph.
 *
 * @keep-all
 */
public class TableCell extends DefaultGraphCell implements HasComment {

    private ClassGraph graph;
    private ClassDiagram.TableInfo info;
    private Map linkMap = new HashMap();

    private static final Font REF_FONT = new Font("Dialog", Font.PLAIN, 11);

    public TableCell(ClassGraph graph, ClassDiagram.TableInfo info) {
        this.graph = graph;
        this.info = info;
        this.info.setCell(this);
        refresh();
    }

    public GraphTable getGraphTable() {
        return info.getTable();
    }

    /**
     * Synchronized our state with our table.
     */
    public void refresh() {
        removeAllChildren();
        linkMap.clear();
        Map viewMap = GraphConstants.createMap();
        add(getTableNameCell(viewMap));
        GraphTable table = getGraphTable();
        GraphColumn[] cols = table.getCols();
        if (cols != null) {
            for (int i = 0; i < cols.length; i++) {
                add(getColumnCell(viewMap, cols[i]));
            }
        }
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, info.getRect());
        viewMap.put(this, map);
        graph.getModel().insert(new Object[]{this}, viewMap, null, null, null);
    }

    private DefaultGraphCell getTableNameCell(Map viewMap) {
        TableNameCell cell = new TableNameCell(getGraphTable());
        Map map = GraphConstants.createMap();
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBounds(map, info.getRect());
//        linkMap.put(getGraphTable().getColumnName(), addPorts(cell));
        viewMap.put(cell, map);
        return cell;
    }

    private ColumnCell getColumnCell(Map viewMap, GraphColumn col) {
        ColumnCell cell = new ColumnCell(col);
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, info.getRect());
        linkMap.put(col.getColumnName(), addPorts(cell));
        viewMap.put(cell, map);
        return cell;
    }

    private DefaultPort addPorts(DefaultGraphCell cell) {
        cell.removeAllChildren();
        DefaultPort port = null;
        port = new DefaultPort("Center");
        cell.add(port);
        return port;
    }

    /**
     * Refresh our connects only.
     */
    public void refreshConnects() {
        GraphColumnRef[] refs = info.getTable().getRefs();
        if (refs == null) return;
        ClassDiagram diagram = graph.getDiagram();
        for (int i = 0; i < refs.length; i++) {
            GraphColumnRef r = refs[i];
            GraphColumn[] destColumns = r.getDestColumns();
            if (destColumns == null || destColumns.length == 0) {
                if (Debug.DEBUG) System.out.println("no destColumns: " + r);
                continue;
            }
            GraphColumn destCol = destColumns[0];
            ClassDiagram.TableInfo ti = diagram.findTableInfo(
                    destCol.getTable());
            if (ti == null) {
                if (Debug.DEBUG) {
                    System.out.println("destCol.table does not exist: " + r);
                }
                continue;
            }
            GraphColumn[] srcColumns = r.getSrcColumns();
            if (srcColumns == null || srcColumns.length == 0) {
                if (Debug.DEBUG) System.out.println("no srcColumns: " + r);
                continue;
            }
            DefaultEdge cell = connectGraphColumnRef(r,
                    (TableCell)ti.getCell());
            if (cell == null) {
                if (Debug.DEBUG) {
                    System.out.println("no cell for destCol.table: " + r);
                }
                continue;
            }
            Map map = GraphConstants.createMap();
            GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
            addEdge(map, cell);
        }
    }

    private DefaultEdge connectGraphColumnRef(GraphColumnRef ref,
            TableCell link) {
        Port sourcePort = (Port)linkMap.get(ref.getSrcColumns()[0].getColumnName());
        Port targetPort = (Port)link.linkMap.get(ref.getDestColumns()[0].getColumnName());
        if (sourcePort == null || targetPort == null) {
            if (Debug.DEBUG) {
                System.out.println("sourcePort = " + sourcePort +
                        " targetPort = " + targetPort);
            }
            return null;
        }
        ColumnRefEdge cell = new ColumnRefEdge(ref);
        cell.setSource(sourcePort);
        sourcePort.addEdge(cell);
        cell.setTarget(targetPort);
        targetPort.addEdge(cell);
        return cell;
    }

    private void addEdge(Map map, DefaultGraphCell cell) {
        Map viewMap = GraphConstants.createMap();

        GraphConstants.setOpaque(map, false);
        GraphConstants.setFont(map, REF_FONT);
        GraphConstants.setLineStyle(map, getSettings().getStyle());
        GraphConstants.setRouting(map, ClassEdgeRouting.CLASS_EDGE_ROUTING);
        GraphConstants.setForeground(map, Color.blue);

        viewMap.put(cell, map);

        Object[] insert = new Object[]{cell};
        graph.getModel().insert(insert, viewMap, null, null, null);
    }

    private ClassDiagram.Settings getSettings() {
        return graph.getDiagram().getSettings();
    }

    public String getComment() {
        return info.getTable().getComment();
    }
}
