
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

import com.jgraph.graph.EdgeRenderer;
import com.jgraph.graph.EdgeView;
import com.jgraph.graph.GraphConstants;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * @keep-all
 */
public class ClassEdgeRenderer extends EdgeRenderer{

    protected Shape createShape() {
        beginShape = lineShape = endShape = null;
        int n = view.getPointCount();
        if (n > 1) {
            // Following block may modify global vars as side effect (Flyweight Design)
            EdgeView tmp = view;
            Point[] p = new Point[n];
            for (int i = 0; i < n; i++)
                p[i] = new Point(tmp.getPoint(i));
            // End of Side-Effect Block
            // Undo Global Side Effects
            if (view != tmp) {
                view = tmp;
                installAttributes(view);
            }
            // End of Undo
            sharedPath.reset();
            beginShape = lineShape = endShape = null;
            Point p0 = p[0];
            Point pe = p[n - 1];
            Point p1 = p[1];
            Point p2 = p[n - 2];
            if (beginDeco != GraphConstants.ARROW_NONE) {
                beginShape = createLineEnd(beginSize, beginDeco, p1, p0);
            }
            if (endDeco != GraphConstants.ARROW_NONE) {
                endShape = createLineEnd(endSize, endDeco, p2, pe);
            }
                sharedPath.moveTo(p0.x, p0.y);
                if (lineStyle == GraphConstants.STYLE_QUADRATIC && n > 2){
                    sharedPath.lineTo(p1.x, p1.y);
                    sharedPath.quadTo(p[2].x, p[2].y, p2.x, p2.y);
                    sharedPath.lineTo(pe.x, pe.y);
                }else if (lineStyle == GraphConstants.STYLE_BEZIER && n > 3){
                    sharedPath.lineTo(p1.x, p1.y);
                    sharedPath.curveTo(p[2].x, p[2].y, p[3].x, p[3].y, p2.x, p2.y);
                    sharedPath.lineTo(pe.x, pe.y);
                }else {
                    for (int i = 1; i < n - 1; i++)
                        sharedPath.lineTo(p[i].x, p[i].y);
                    sharedPath.lineTo(pe.x, pe.y);
                }
                lineShape = (GeneralPath) sharedPath.clone();
                if (endShape != null)
                    sharedPath.append(endShape, true);
                if (beginShape != null)
                    sharedPath.append(beginShape, true);
            return sharedPath;
        }
        return null;
    }
}

