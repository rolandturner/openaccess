
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

import javax.swing.*;
import java.util.Map;
import java.awt.*;

/** 
 * Displays all the different sorts of lines we use with descriptions.
 * @keep-all
 */
public class LegendCell extends DefaultGraphCell {

    private static final Font FONT = new Font("Dialog", Font.PLAIN, 9);

    private static final int BLOCK_W = 10;
    private static final int BLOCK_H = 20;
    private static final int DISTANCE = 100;
    private static final int Y_SPACING = 35;

    private static final Object[] RELS = new Object[]{
        "dependent", "reference",
            new Integer(GraphConstants.ARROW_DIAMOND),
            new Integer(GraphConstants.ARROW_SIMPLE),
        "inverse", "1-n",
            new Integer(GraphConstants.ARROW_SIMPLE),
            new Integer(GraphConstants.ARROW_CLASSIC),
        "dependent", "inverse 1-n",
            new Integer(GraphConstants.ARROW_DIAMOND),
            new Integer(GraphConstants.ARROW_CLASSIC),
        "many to", "many",
            new Integer(GraphConstants.ARROW_CLASSIC),
            new Integer(GraphConstants.ARROW_CLASSIC),
    };

    public LegendCell(ClassGraph graph, int x, int y) {
        Map viewMap = GraphConstants.createMap();

        int px = x;
        int py = y;
        for (int i = 0; i < RELS.length; ) {
            addRelationShip(viewMap, px, py,
                (String)RELS[i++],
                (String)RELS[i++],
                ((Integer)RELS[i++]).intValue(),
                ((Integer)RELS[i++]).intValue());
            py += Y_SPACING;
        }

        graph.getModel().insert(new Object[]{this}, viewMap, null, null, null);
    }

    private void addRelationShip(Map viewMap,
            int x, int y, String line1, String line2, int begin, int end) {

        DefaultGraphCell src = new DefaultGraphCell("");
        viewMap.put(src, createSquare(x, y));
        DefaultPort sp = new DefaultPort("sp");
        src.add(sp);
        add(src);

        DefaultGraphCell dest = new DefaultGraphCell("");
        viewMap.put(dest, createSquare(x + DISTANCE, y));
        DefaultPort tp = new DefaultPort("tp");
        dest.add(tp);
        add(dest);

        DefaultEdge cell = new DefaultEdge("");
        cell.setSource(sp);
        sp.addEdge(cell);
        cell.setTarget(tp);
        tp.addEdge(cell);
        add(cell);
        Map map = GraphConstants.createMap();
        GraphConstants.setFont(map, FONT);
        GraphConstants.setLineBegin(map, begin);
        GraphConstants.setLineEnd(map, end);
        viewMap.put(cell, map);

        DefaultGraphCell t1 = new DefaultGraphCell(line1);
        add(t1);
        viewMap.put(t1, createLabel(x, y - 1));

        if (line2 != null) {
            DefaultGraphCell t2 = new DefaultGraphCell(line2);
            add(t2);
            viewMap.put(t2, createLabel(x, y + 10));
        }
    }

    private static Map createSquare(int x, int y) {
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, new Rectangle(x, y, BLOCK_W, BLOCK_H));
        GraphConstants.setBorder(map, BorderFactory.createLineBorder(Color.black));
        GraphConstants.setOpaque(map, true);
        return map;
    }

    private static Map createLabel(int x, int y) {
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, new Rectangle(x, y, BLOCK_W + DISTANCE, 10));
        GraphConstants.setFont(map, FONT);
        GraphConstants.setHorizontalAlignment(map, 0);
        return map;
    }

}
