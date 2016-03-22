
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
import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.jdo.tools.workbench.model.MdField;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.metadata.MDStatics;

import java.awt.*;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

/**
 * A class on a ClassGraph.
 */
public class MdClassCell extends DefaultGraphCell {

    private ClassGraph graph;
    private ClassDiagram.ClassInfo info;
    private Map linkMap = new HashMap();

    public MdClassCell(ClassGraph graph, ClassDiagram.ClassInfo info) {
        this.graph = graph;
        this.info = info;
        info.setCell(this);
        refresh();
    }

    public MdClass getMdClass() {
        return info.getMdClass();
    }

    public String getClassQName() {
        return getMdClass().getQName();
    }

    private ClassDiagram.Settings getDiagramSettings() {
        return graph.getDiagram().getSettings();
    }

    /**
     * Synchronized our state with our class.
     */
    public void refresh() {
        removeAllChildren();
        linkMap.clear();
        Map viewMap = GraphConstants.createMap();
        add(getClassNameCell(viewMap));
        boolean isJDBC = info.getMdClass().getMdDataStore().isJDBC();
        boolean pseudo = isJDBC && getDiagramSettings().isClassPseudoFields();
        boolean simple = getDiagramSettings().isClassSimpleFields();
        for (Iterator it = getMdClass().getFieldList().iterator();
             it.hasNext();) {
            MdField field = (MdField)it.next();
            int cat = field.getCategory();
            switch (cat) {
                case MDStatics.CATEGORY_CLASS_ID:
                case MDStatics.CATEGORY_DATASTORE_PK:
                case MDStatics.CATEGORY_OPT_LOCKING:
                    if (!pseudo) break;
                case MDStatics.CATEGORY_SIMPLE:
                case MDStatics.CATEGORY_EXTERNALIZED:
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    if (!simple && (cat == MDStatics.CATEGORY_SIMPLE
                            || cat == MDStatics.CATEGORY_EXTERNALIZED)) {
                        break;
                    }
                    add(getFieldCell(viewMap, field));
            }
            ;
        }
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, info.getRect());
        viewMap.put(this, map);
        graph.getModel().insert(new Object[]{this}, viewMap, null, null, null);
    }

    private DefaultGraphCell getClassNameCell(Map viewMap) {
        MdClassNameCell cell = new MdClassNameCell(getMdClass());
        Map map = GraphConstants.createMap();
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBounds(map, info.getRect());
        linkMap.put(getMdClass(), addPorts(cell));
        viewMap.put(cell, map);
        return cell;
    }

    private MdFieldCell getFieldCell(Map viewMap, MdField field) {
        MdFieldCell cell = new MdFieldCell(field);
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, info.getRect());
        linkMap.put(field, addPorts(cell));
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
        List l = getMdClass().getFieldList();
        int n = l.size();
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)l.get(i);
            switch (f.getCategory()) {
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    connectRef(f);
                    break;
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_COLLECTION:
                    connectCollection(f);
                    break;
                case MDStatics.CATEGORY_MAP:
                    connectMap(f);
                    break;
            }
        }
        connectSuper();
    }

    private void connectRef(MdField field) {
        ClassDiagram.ClassInfo ci = graph.getDiagram().findClassInfo(
                field.getTypeStr());
        if (ci == null) return;
        // do not connect references that are used as an inverse
        MdField finv = field.getFieldWeAreInverseFor();
        if (finv != null) {
            String et = finv.getElementQType();
            if (et != null) return;
        }
        DefaultEdge cell = connectToClass(field, (MdClassCell)ci.getCell());
        if(cell == null){
            return;
        }
        Map map = GraphConstants.createMap();
        setDependent(map, field.getDependentBool());
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
        addEdge(map, cell, false);
    }

    private void connectCollection(MdField field) {
        String et = field.getElementQType();
        if (et == null) return;
        ClassDiagram.ClassInfo ci = graph.getDiagram().findClassInfo(et);
        if (ci == null) return;
        String inverse = field.getInverseStr();
        if (inverse != null && inverse.length() > 0) {
            MdField inverseField = ci.getMdClass().findField(inverse);
            if (inverseField == null) return;
            DefaultEdge cell = connectToField(field,
                    (MdClassCell)ci.getCell(), inverseField);
            Map map = GraphConstants.createMap();
            switch (inverseField.getCategory()) {
                case MDStatics.CATEGORY_ARRAY:
                case MDStatics.CATEGORY_COLLECTION:
                    GraphConstants.setLineBegin(map,
                            GraphConstants.ARROW_CLASSIC);
                    GraphConstants.setLineEnd(map,
                            GraphConstants.ARROW_CLASSIC);
                    break;
                default:
                    GraphConstants.setLineBegin(map,
                            field.getDependentBool() ?
                            GraphConstants.ARROW_DIAMOND : GraphConstants.ARROW_SIMPLE);
                    GraphConstants.setLineEnd(map,
                            GraphConstants.ARROW_CLASSIC);
                    break;
            }
            addEdge(map, cell, false);
        } else {
            DefaultEdge cell = connectToClass(field,
                    (MdClassCell)ci.getCell());
            if(cell == null){
                return;
            }
            // do not connect collections that are used as an inverse
            MdField finv = field.getFieldWeAreInverseFor();
            if (finv != null) {
                return;
            }
            Map map = GraphConstants.createMap();
            setDependent(map, field.getDependentBool());
            GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
            addEdge(map, cell, false);
        }
    }

    /**
     * Add a dependend indicator to the begining of the line for map if dep
     * is true.
     */
    private static void setDependent(Map map, boolean dep) {
        GraphConstants.setLineBegin(map,
                dep ? GraphConstants.ARROW_DIAMOND : GraphConstants.ARROW_NONE);
    }

    private void connectMap(MdField field) {
        String kt = field.getKeyQType();
        String vt = field.getElementQType();
        if (kt == null || vt == null) return;
        ClassDiagram.ClassInfo ci = graph.getDiagram().findClassInfo(kt);
        if (ci != null) {
            DefaultEdge cell = connectToClass(field,
                    (MdClassCell)ci.getCell());
            if(cell == null){
                return;
            }
            Map map = GraphConstants.createMap();
            setDependent(map, field.getKeysDependentBool());
            GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
            GraphConstants.setValue(map, "key");
            addEdge(map, cell, false);
        }
        ci = graph.getDiagram().findClassInfo(vt);
        if (ci != null) {
            DefaultEdge cell = connectToClass(field,
                    (MdClassCell)ci.getCell());
            if(cell == null){
                return;
            }
            Map map = GraphConstants.createMap();
            setDependent(map, field.getDependentBool());
            GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
            GraphConstants.setValue(map, "value");
            addEdge(map, cell, false);
        }
    }

    private void connectSuper() {
        MdClass superCls = getMdClass().getPcSuperclassMdClassInh();
        if (superCls == null) return;
        ClassDiagram.ClassInfo ci = graph.getDiagram().findClassInfo(superCls);
        if (ci == null) return;
        DefaultEdge cell = connectToClass(getMdClass(),
                (MdClassCell)ci.getCell());
        if(cell == null){
            return;
        }
        Map map = GraphConstants.createMap();
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        addEdge(map, cell, true);
    }

    private DefaultEdge connectToClass(Object o, MdClassCell link) {
        DefaultEdge cell;
        if (o instanceof MdField) {
            cell = new MdFieldEdge((MdField)o, "");
        } else {
            cell = new DefaultEdge("");
        }
        Port sourcePort = (Port)linkMap.get(o);
        cell.setSource(sourcePort);
        if (sourcePort != null) {
            sourcePort.addEdge(cell);
        }else{
            return null;
        }
        Port targetPort = (Port)link.linkMap.get(link.getMdClass());
        cell.setTarget(targetPort);
        targetPort.addEdge(cell);
        return cell;
    }

    private DefaultEdge connectToField(MdField o, MdClassCell link,
            MdField field) {
        MdFieldEdge cell = new MdFieldEdge(o, "");
        Port sourcePort = (Port)linkMap.get(o);
        cell.setSource(sourcePort);
        sourcePort.addEdge(cell);
        Port targetPort = (Port)link.linkMap.get(field);
        cell.setTarget(targetPort);
        targetPort.addEdge(cell);
        return cell;
    }

    private void addEdge(Map map, DefaultGraphCell cell, boolean inheritance) {
        Map viewMap = GraphConstants.createMap();

        GraphConstants.setOpaque(map, false);
        GraphConstants.setFont(map, new Font("Dialog", Font.PLAIN, 11));
        GraphConstants.setLineStyle(map, getSettings().getStyle());
        if (inheritance) {
            GraphConstants.setRouting(map,
                    ClassEdgeRouting.CLASS_EDGE_ROUTING_INHERITANCE);
        } else {
            GraphConstants.setRouting(map,
                    ClassEdgeRouting.CLASS_EDGE_ROUTING);
        }

        viewMap.put(cell, map);

        Object[] insert = new Object[]{cell};
        graph.getModel().insert(insert, viewMap, null, null, null);
    }

    private ClassDiagram.Settings getSettings() {
        return graph.getDiagram().getSettings();
    }

}
