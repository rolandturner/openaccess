
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
import com.jgraph.event.GraphModelListener;
import com.jgraph.event.GraphModelEvent;
import com.jgraph.graph.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.Printable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.versant.core.jdo.tools.workbench.WorkbenchSettings;
import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.MdUtils;

import javax.swing.*;

/**
 * Model, View and controller for a class diagram.
 */
public class ClassGraph extends JGraph implements Printable,
        GraphModelListener {

    private ClassDiagram diagram;
    private CellView innerSelection;
    private LegendCell legend;
    private boolean autoLayout;
    private int autoOriginX = 0;
    private int autoOriginY = 0;
    private int autoSpacingX = 100;
    private int autoSpacingY = 30;

    public ClassGraph(ClassDiagram diagram) {
        super(new DefaultGraphModel());
        this.diagram = diagram;
        diagram.finishLoad();
        setEditable(true);
        setCloneable(false);
        setMoveable(true);
        setSizeable(false);
        setBendable(false);
        setConnectable(false);
        setDisconnectable(false);
        setAntiAliased(!WorkbenchSettings.getInstance().isSlowDisplay());
        setDisconnectOnMove(false);
        refresh();
    }

    public ClassDiagram getDiagram() {
        return diagram;
    }

    /**
     * Redo everything based on our diagram.
     */
    public void refresh() {
        clearSelection();
        if (graphModel != null) graphModel.removeGraphModelListener(this);
        setModel(new DefaultGraphModel());
        if (diagram.getSettings().isShowLegend()) {
            legend = new LegendCell(this, diagram.getLegendX(),
                    diagram.getLegendY());
        } else {
            legend = null;
        }
        ClassDiagram.Info prev = null;
        for (Iterator i = diagram.getInfos().iterator(); i.hasNext();) {
            ClassDiagram.Info info = (ClassDiagram.Info)i.next();
            if (autoLayout) {
                int x = autoOriginX;
                int y = autoOriginY;
                if (prev != null) {
                    Rectangle bounds = getCellBounds(prev.getCell());
                    if (info.isUnder()) {
                        y = bounds.y + bounds.height + autoSpacingY;
                        x = bounds.x;
                    } else {
                        x = bounds.x + bounds.width + autoSpacingX;
                        y = autoOriginY;
                    }
                }
                info.setX(x);
                info.setY(y);
            }
            createCell(info);
            prev = info;
        }
        refreshConnects();
        graphModel.addGraphModelListener(this);
    }

    private void createCell(ClassDiagram.Info info) {
        if (info instanceof ClassDiagram.ClassInfo) {
            new MdClassCell(this, (ClassDiagram.ClassInfo)info);
        } else if (info instanceof ClassDiagram.TableInfo) {
            new TableCell(this, (ClassDiagram.TableInfo)info);
        }
    }

    /**
     * Redo only our connects.
     */
    public void refreshConnects() {
        clearSelection();
        int c = graphModel.getRootCount();
        ArrayList list = new ArrayList(c);
        for (int x = 0; x < c; x++) {
            Object o = graphModel.getRootAt(x);
            if (o instanceof Edge) list.add(o);
        }
        graphModel.remove(list.toArray());
        for (Iterator i = diagram.getInfos().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof ClassDiagram.ClassInfo) {
                ((MdClassCell)((ClassDiagram.ClassInfo)o).getCell()).refreshConnects();
            } else if (o instanceof ClassDiagram.TableInfo) {
                ((TableCell)((ClassDiagram.TableInfo)o).getCell()).refreshConnects();
            }
        }
    }

    /**
     * Remove the classes from the diagram. This will refresh the connects.
     * The array must contain cells (e.g. MdClassCell).
     */
    public void removeCells(Object[] a) {
        for (int i = 0; i < a.length; i++) {
            Object o = a[i];
            if (o instanceof MdClassCell) {
                removeClassImp((MdClassCell)o);
            } else if (o instanceof LegendCell) {
                graphModel.remove(getDescendants(new Object[]{o}));
                diagram.getSettings().setShowLegend(false);
            }
        }
        refreshConnects();
    }

    private void removeClassImp(MdClassCell cell) {
        graphModel.remove(getDescendants(new Object[]{cell}));
        diagram.removeClass(cell.getMdClass());
    }

    /**
     * Remove a list of classes from the diagram and add another list of
     * classes. This will refresh the connects.
     */
    public void removeAndAddClasses(List removed, List added) {
        for (int i = removed.size() - 1; i >= 0; i--) {
            MdClass mdc = (MdClass)removed.get(i);
            ClassDiagram.ClassInfo ci = diagram.findClassInfo(mdc);
            removeClassImp((MdClassCell)ci.getCell());
        }
        Object[] roots = getRoots();
        if (legend != null) {
            int n = roots.length;
            ArrayList a = new ArrayList(n);
            for (int i = 0; i < n; i++) {
                if (roots[i] != legend) a.add(roots[i]);
            }
            roots = a.toArray();
        }
        Rectangle r = getCellBounds(roots);
        int ox = 8;
        int x = 8;
        int y = 8;
        if (r != null) {
            if (r.width > r.height) {
                y = r.y + r.height + 8;
            } else {
                x = ox = r.x + r.width + 8;
            }
        }
        for (int i = 0; i < added.size(); i++) {
            MdClass mdc = (MdClass)added.get(i);
            ClassDiagram.ClassInfo ci = diagram.addClass(mdc);
            ci.setX(x);
            ci.setY(y);
            ci.setCell(new MdClassCell(this, ci));
            x += 200;
            if (x >= ox + 800) {
                x = ox;
                y += 150;
            }
        }
        refresh();
    }

    public void updateAutoSize(CellView view) {
        if (view != null && !isEditing()) {
            if (view instanceof MdClassView || view instanceof TableView) {
                Rectangle bounds = view.getBounds();
                if (bounds != null) {
                    CellView[] childViews = view.getChildViews();
                    int childCount = childViews.length;
                    int[] ch = new int[childCount];
                    int w = 0;
                    int h = 0;
                    for (int i = 0; i < childCount; i++) {
                        CellView childView = childViews[i];
                        Dimension ps = getViewPreferredSize(childView);
                        if (ps.width > w) w = ps.width;
                        h += ch[i] = ps.height;
                    }
                    bounds.width = w;
                    bounds.height = h;
                    int x = bounds.x;
                    int y = bounds.y;
                    for (int i = 0; i < childCount; i++) {
                        CellView childView = childViews[i];
                        Rectangle r = childView.getBounds();
                        r.x = x;
                        r.y = y;
                        r.width = w;
                        y += r.height = ch[i];
                    }
                }
            } else {
                super.updateAutoSize(view);
            }
        }
    }

    private Dimension getViewPreferredSize(CellView view) {
        Component component =
                view.getRendererComponent(this, false, false, false);
        if (component != null) {
            add(component);
            component.validate();
            return component.getPreferredSize();
        }
        return new Dimension(0, 0);
    }

    protected VertexView createVertexView(Object cell, CellMapper cellMapper) {
        if (cell instanceof MdFieldCell) {
            VertexView view = new MdFieldView(cell, this, cellMapper);
            cellMapper.putMapping(cell, view);
            view.refresh(true); // Create Dependent Views
            view.update();
            return view;
        }
        if (cell instanceof MdClassCell) {
            VertexView view = new MdClassView(cell, this, cellMapper);
            cellMapper.putMapping(cell, view);
            view.refresh(true); // Create Dependent Views
            view.update();
            return view;
        }
        if (cell instanceof MdClassNameCell) {
            VertexView view = new MdClassNameView(cell, this, cellMapper);
            cellMapper.putMapping(cell, view);
            view.refresh(true); // Create Dependent Views
            view.update();
            return view;
        }
        if (cell instanceof ColumnCell) {
            VertexView view = new ColumnView(cell, this, cellMapper);
            cellMapper.putMapping(cell, view);
            view.refresh(true); // Create Dependent Views
            view.update();
            return view;
        }
        if (cell instanceof TableCell) {
            VertexView view = new TableView(cell, this, cellMapper);
            cellMapper.putMapping(cell, view);
            view.refresh(true); // Create Dependent Views
            view.update();
            return view;
        }
        if (cell instanceof TableNameCell) {
            VertexView view = new TableNameView(cell, this, cellMapper);
            cellMapper.putMapping(cell, view);
            view.refresh(true); // Create Dependent Views
            view.update();
            return view;
        }
        return super.createVertexView(cell, cellMapper);
    }

    protected EdgeView createEdgeView(Object e, CellMapper cm) {
        return new ClassEdgeView(e, this, cm);
    }

    /**
     * Make sure only top level things can be selected and store the next
     * level down selection in innerSelection (e.g. the class is selected
     * but we also know which field).
     */
    public CellView getNextViewAt(CellView cellView, int x, int y) {
        CellView view = super.getNextViewAt(null, x, y);
        if (view != null) {
            innerSelection = findInnerSelection(view, x, y);
        } else {
            innerSelection = null;
        }
        return view;
    }

    private CellView findInnerSelection(CellView current, int x, int y) {
        CellView[] cv = current.getChildViews();
        Object[] a = new Object[cv.length];
        for (int i = 0; i < a.length; i++) a[i] = cv[i].getCell();
        Object[] sel = graphLayoutCache.order(a);
        CellView[] cells = graphLayoutCache.getMapping(sel);
        CellView cell = getNextViewAt(cells, current, x, y);
        return cell;
    }

    public CellView getInnerSelection() {
        return innerSelection;
    }

    public int print(Graphics g, PageFormat pF, int index)
            throws PrinterException {
        if (index > 0) return NO_SUCH_PAGE;
        boolean aa = isAntiAliased();
        boolean gv = isGridVisible();
        RepaintManager currentManager = RepaintManager.currentManager(this);
        boolean db = currentManager.isDoubleBufferingEnabled();
        Color bg = getBackground();
        try {
            setAntiAliased(true);
            setGridVisible(false);
            setBackground(Color.white);
            clearSelection();
            currentManager.setDoubleBufferingEnabled(false);

            Rectangle imageRectangle = new Rectangle((int)pF.getImageableX(),
                    (int)pF.getImageableY(), (int)pF.getImageableWidth(),
                    (int)pF.getImageableHeight());

            Graphics2D grx = (Graphics2D)g;
            grx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            grx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            grx.translate(imageRectangle.x, imageRectangle.y);
            grx.setColor(Color.black);

            int spaceUsed = 0;

            if (diagram.getSettings().isPrintHeader()) {
                String title = diagram.getName();
                if (title == null || title.length() == 0) title = "<Untitled>";
                Font font = new Font("Dialog", Font.PLAIN, 8);
                grx.setFont(font);
                FontMetrics fm = getFontMetrics(font);
                int fHeight = fm.getHeight() + 2;
                int fAscent = fm.getMaxAscent() + 1;
                grx.drawString(title, 0, fAscent);

                SimpleDateFormat df = new SimpleDateFormat(
                        WorkbenchSettings.getInstance().getLastUsedDateFormatString());
                String ver = MdUtils.getVersion();
                title = df.format(new Date()) + " - Open Access " + (ver == null ? "" : ver);
                grx.drawString(title, imageRectangle.width - fm.stringWidth(
                        title),
                        fAscent);

                spaceUsed += fHeight;
                spaceUsed += 8;
            }

            grx.translate(0, spaceUsed);
            grx.setClip(0, 0, imageRectangle.width,
                    imageRectangle.height - spaceUsed);

            Rectangle realRectangle = getCellBounds(getRoots());
            imageRectangle.height -= spaceUsed;
            float factorWidth = (float)imageRectangle.width / (float)realRectangle.width;
            float factorHeight = (float)imageRectangle.height / (float)realRectangle.height;
            float factor = Math.min(1.0f, Math.min(factorHeight, factorWidth));

            AffineTransform atrans = new AffineTransform();
            grx.setColor(Color.white);
            atrans.scale(factor, factor);
            grx.transform(atrans);
            grx.translate(
                    (grx.getClipBounds().width - realRectangle.width) / 2 - realRectangle.x,
                    -realRectangle.y);
            print(grx);
        } finally {
            setBackground(bg);
            setAntiAliased(aa);
            setGridVisible(gv);
            currentManager.setDoubleBufferingEnabled(db);
        }
        return PAGE_EXISTS;
    }

    public BufferedImage getImage() {
        boolean aa = isAntiAliased();
        boolean gv = isGridVisible();
        RepaintManager currentManager = RepaintManager.currentManager(this);
        boolean db = currentManager.isDoubleBufferingEnabled();
        BufferedImage img;
        try {
            setAntiAliased(true);
            setGridVisible(false);
            clearSelection();
            currentManager.setDoubleBufferingEnabled(false);
            Rectangle bounds = getCellBounds(getRoots());

            Dimension d = bounds.getSize();
            img = new BufferedImage(d.width + 20, d.height + 20,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D grx = img.createGraphics();
            grx.fillRect(0, 0, bounds.width + 20, bounds.height + 20);
            int x = -bounds.x + 10;
            int y = -bounds.y + 10;
            grx.translate(x, y);
            grx.setColor(Color.white);
            print(grx);
        } finally {
            setAntiAliased(aa);
            setGridVisible(gv);
            currentManager.setDoubleBufferingEnabled(db);
        }
        return img;
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    public void startEditingAtCell(Object cell) {
        fireActionEvent();
    }

    private void fireActionEvent() {
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                null);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener)listeners[i + 1]).actionPerformed(e);
            }
        }
    }

    /**
     * Update our diagram to match the change in the model. This is done
     * using invoke later so that the positions of the classes are correct.
     */
    public void graphChanged(GraphModelEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Iterator i = diagram.getClassInfos(); i.hasNext();) {
                    ClassDiagram.ClassInfo ci = (ClassDiagram.ClassInfo)i.next();
                    MdClassCell cell = (MdClassCell)ci.getCell();
                    VertexView view = (VertexView)getGraphLayoutCache().getMapping(
                            cell, false);
                    if (view == null) continue;
                    Rectangle r = view.getBounds();
                    ci.setX(r.x);
                    ci.setY(r.y);
                }
                if (legend != null) {
                    VertexView view = (VertexView)getGraphLayoutCache().getMapping(
                            legend, false);
                    if (view != null) {
                        Rectangle r = view.getBounds();
                        diagram.setLegendX(r.x);
                        diagram.setLegendY(r.y);
                    }
                }
            }
        });
    }

    /**
     * Gobble control C, control X and control V.
     */
    protected void processKeyEvent(KeyEvent e) {
        if (e.isControlDown()) {
            int vk = e.getKeyCode();
            if (vk == KeyEvent.VK_C || vk == KeyEvent.VK_X || vk == KeyEvent.VK_V) {
                return;
            }
        }
        super.processKeyEvent(e);
    }

    public void updateUI() {
        super.updateUI();
        setBackground(UIManager.getColor("Panel.background"));
    }

    public boolean isAutoLayout() {
        return autoLayout;
    }

    public void setAutoLayout(boolean autoLayout) {
        this.autoLayout = autoLayout;
    }

    public int getAutoOriginX() {
        return autoOriginX;
    }

    public void setAutoOriginX(int autoOriginX) {
        this.autoOriginX = autoOriginX;
    }

    public int getAutoOriginY() {
        return autoOriginY;
    }

    public void setAutoOriginY(int autoOriginY) {
        this.autoOriginY = autoOriginY;
    }

    public int getAutoSpacingX() {
        return autoSpacingX;
    }

    public void setAutoSpacingX(int autoSpacingX) {
        this.autoSpacingX = autoSpacingX;
    }

    public int getAutoSpacingY() {
        return autoSpacingY;
    }

    public void setAutoSpacingY(int autoSpacingY) {
        this.autoSpacingY = autoSpacingY;
    }

    /**
     * Display label of cell under cursor as tooltip or its comment if it
     * implements HasComment.
     */
    public String getToolTipText(MouseEvent e) {
        if (e != null) {
            int x = e.getX();
            int y = e.getY();
            CellView view = super.getNextViewAt(null, x, y);
            if (view == null) return null;
            CellView inner = findInnerSelection(view, x, y);
            if (inner != null) view = inner;
            Object c = view.getCell();
            if (c != null) {
                String s;
                if (c instanceof HasComment) {
                    s = ((HasComment)c).getComment();
                } else {
                    s = c.toString();
                }
                return wrapToolipInHtml(s);
            }
        }
        return null;
    }

    private String tos(Object o) {
        if (o == null) return "<null>";
        return o.getClass().getName() + ": " + o;
    }

    /**
     * Wrap a tooltip in an HTML table so it can be multiline. If the tooltip
     * is short then it is returned as is.
     */
    private String wrapToolipInHtml(String info) {
        if (info == null || info.length() <= 40) return info;
        StringBuffer s = new StringBuffer();
        s.append("<html>" +
                "<body bgcolor=\"#FFFFE6\">" +
                "<table width=\"300\"><tr><td><p>" +
                "<font face=\"dialog\" size=\"-1\">");
        s.append(info);
        if (info.charAt(info.length() - 1) != '.') s.append('.');
        s.append("</font></p></td></tr></table></body></html>");
        return s.toString();
    }
}
