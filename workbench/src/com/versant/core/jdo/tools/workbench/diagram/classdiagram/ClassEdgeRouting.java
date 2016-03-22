
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

import java.util.List;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * @keep-all
 */
public class ClassEdgeRouting implements Edge.Routing {

    public static final ClassEdgeRouting CLASS_EDGE_ROUTING
            = new ClassEdgeRouting(false);
    public static final ClassEdgeRouting CLASS_EDGE_ROUTING_INHERITANCE
            = new ClassEdgeRouting(true);

    public static final int GAP = 5;

    private boolean inheritance;

    public ClassEdgeRouting(boolean inheritance) {
        this.inheritance = inheritance;
    }

    public void route(EdgeView edge, java.util.List points) {
        int n = points.size();
        Rectangle[] s = getPoints(edge.getPoint(0), edge.getSource());
        Rectangle[] t = getPoints(edge.getPoint(n - 1), edge.getTarget());
        Point[] routed = getBestRoute(s, t);
        // Set/Add Points
        for (int i = 0; i < routed.length; i++)
            if (points.size() > i + 2)
                points.set(i + 1, routed[i]);
            else
                points.add(i + 1, routed[i]);
        // Remove spare points
        while (points.size() > routed.length + 2) {
            points.remove(points.size() - 2);
        }
    }

    private Point[] getBestRoute(Rectangle[] ps1, Rectangle[] ps2) {
        double min = Double.MAX_VALUE;
        double temp = 0;
        Rectangle pr1 = null;
        Rectangle pr2 = null;
        Rectangle p1;
        Rectangle p2;
        for (int i = 0; i < ps1.length; i++) {
            p1 = ps1[i];
            for (int j = 0; j < ps2.length; j++) {
                p2 = ps2[j];
                temp = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
                if (temp < min) {
                    pr1 = p1;
                    pr2 = p2;
                    min = temp;
                }
            }
        }
        return getRoute(pr1, pr2);
    }

    private Point[] getRoute(Rectangle r1, Rectangle r2) {
        Point p1;
        Point p2;
        if (r1.x == r2.x && r1.width == r2.width && r1.y == r2.y) {
            p1 = new Point(r2.x - 30, r1.y + 5);
            p2 = new Point(r2.x - 30, r2.y - 5);
        } else if (r1.x == r2.x && r1.width == r2.width) {
            p1 = new Point(r2.x - 30, r1.y);
            p2 = new Point(r2.x - 30, r2.y);
        } else if (r1.y == r1.height && r2.x == r2.width) {
            p1 = new Point(r2.x, r1.y);
            p2 = new Point(p1);
        } else if (r2.y == r2.height && r1.x == r1.width) {
            p1 = new Point(r1.x, r2.y);
            p2 = new Point(p1);
        } else if (r1.y == r1.height) {
            p1 = new Point(r1.x + (r2.x - r1.x) / 2, r1.y);
            p2 = new Point(r1.x + (r2.x - r1.x) / 2, r2.y);
        } else {
            p1 = new Point(r1.x, r1.y + (r2.y - r1.y) / 2);
            p2 = new Point(r2.x, r2.y + (r1.y - r2.y) / 2);
        }

        return new Point[]{r1.getLocation(), p1, p2, r2.getLocation()};
    }

    private Rectangle[] getPoints(Point p, CellView view) {
        if (view != null) {
            CellView parent = view.getParentView();
            if (parent instanceof MdFieldView || parent instanceof ColumnView) {
                Rectangle r = parent.getBounds();
                int y = r.y + r.height / 2;
                return new Rectangle[]{
                    new Rectangle(r.x - GAP, y, r.x, y),
                    new Rectangle(r.x + r.width + GAP, y, r.x + r.width, y)};
            }
            if (parent instanceof MdClassNameView) {
                Rectangle r1 = parent.getBounds();
                int y = r1.y + r1.height / 2;
                if (inheritance) {
                    Rectangle r2 = parent.getParentView().getBounds();
                    int x = r2.x + r2.width / 2;
                    return new Rectangle[]{
                        new Rectangle(x, r2.y - GAP, x, r2.y),
                        new Rectangle(x, r2.y + r2.height + GAP, x, r2.y + r2.height)};
                } else {
                    Rectangle r2 = parent.getParentView().getBounds();
                    int x = r2.x + r2.width / 2;
                    return new Rectangle[]{
                        new Rectangle(r1.x - GAP, y, r1.x, y),
                        new Rectangle(r1.x + r1.width + GAP, y, r1.x + r1.width, y),
                        new Rectangle(x, r2.y - GAP, x, r2.y),
                        new Rectangle(x, r2.y + r2.height + GAP, x, r2.y + r2.height)};
                }
            }
        }
        return new Rectangle[]{new Rectangle(p)};
    }

//    public void oldroute(EdgeView edge, java.util.List points) {
//        int n = points.size();
//        Point from = edge.getPoint(0);
//        if (edge.getSource() instanceof PortView)
//            from = ((PortView)edge.getSource()).getLocation(null);
//        else if (edge.getSource() != null)
//            from = edge.getSource().getBounds().getLocation();
//        Point to = edge.getPoint(n - 1);
//        if (edge.getTarget() instanceof PortView)
//            to = ((PortView)edge.getTarget()).getLocation(null);
//        else if (edge.getTarget() != null)
//            to = edge.getTarget().getBounds().getLocation();
//        if (from != null && to != null) {
//            Point[] routed;
//            // Handle self references
//            if (edge.getSource() == edge.getTarget()
//                    && edge.getSource() != null) {
//                Rectangle bounds =
//                        edge.getSource().getParentView().getBounds();
//                int width = 60;
//                routed = new Point[6];
//                routed[0] =
//                        new Point(
//                                bounds.x,
//                                bounds.y + bounds.height / 2);
//                routed[1] =
//                        new Point(
//                                bounds.x - width / 4,
//                                bounds.y);
//                routed[2] =
//                        new Point(
//                                bounds.x - width,
//                                bounds.y);
//                routed[3] =
//                        new Point(
//                                bounds.x - width,
//                                bounds.y + bounds.height);
//                routed[4] =
//                        new Point(
//                                bounds.x - width / 4,
//                                bounds.y + bounds.height);
//                routed[5] =
//                        new Point(
//                                bounds.x,
//                                bounds.y + bounds.height / 2);
//            } else {
//                int dx = Math.abs(from.x - to.x);
//                int dy = Math.abs(from.y - to.y);
//                int x2 = from.x + ((to.x - from.x) / 2);
//                int y2 = from.y + ((to.y - from.y) / 2);
//                routed = new Point[2];
//                if (dx == 0) {
//                    int xs = 0;
//                    int xt = 0;
//                    int w = 0;
//                    CellView source = edge.getSource();
//                    if (source != null) {
//                        if (source.getParentView() != null) {
//                            source = source.getParentView();
//                        }
//                        Rectangle r = source.getBounds();
//                        w = r.width / 2;
//                        xs = r.x;
//                    }
//                    CellView target = edge.getTarget();
//                    if (target != null) {
//                        if (target.getParentView() != null) {
//                            target = target.getParentView();
//                        }
//                        xt = target.getBounds().x;
//                    }
//                    if (xs == xt) {
//                        routed[0] = new Point(x2 - 60 - w, from.y);
//                        routed[1] = new Point(x2 - 60 - w, to.y);
//                    }
//                }
//                if (routed[0] == null) {
//                    if (dx > dy) {
//                        routed[0] = new Point(x2, from.y);
//                        //new Point(to.x, from.y)
//                        routed[1] = new Point(x2, to.y);
//                    } else {
//                        routed[0] = new Point(from.x, y2);
//                        // new Point(from.x, to.y)
//                        routed[1] = new Point(to.x, y2);
//                    }
//                }
//            }
//            // Set/Add Points
//            for (int i = 0; i < routed.length; i++)
//                if (points.size() > i + 2)
//                    points.set(i + 1, routed[i]);
//                else
//                    points.add(i + 1, routed[i]);
//            // Remove spare points
//            while (points.size() > routed.length + 2) {
//                points.remove(points.size() - 2);
//            }
//        }
//    }
}
