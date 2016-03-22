
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
package com.versant.core.jdbc.metadata;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;

import com.versant.core.common.Debug;
import com.versant.core.common.BindingSupportImpl;

import java.util.*;

/**
 * This does a topological sort of jdbc tables via its ClassMetaData. This
 * ordering is necassary to prevent tripping relation constraints.
 * This process also checks it the table is involved in circular references.
 * <p/>
 * References that lead to a cyclic depenency is ignored when the dependency
 * ordering is done.
 *
 * A class hierarchy if viewed as a node. Firstly all the references from the
 * node is collected. This is done by starting at the top most class and collecting all the
 * references. Once this is done a indirect ref graph is build up from the direct reference graph.
 * This indirect reference is build up by recursivly adding the references of
 * the direct references until we reach the end or the original node is reached again.
 * The nodes are now sorted by comparing there indirect reference graph.
 * NodeA has NodeB as indirect reference:
 * If NodeB has NodeA as indirect reference then they are equil.
 * Else NodeA is dependent on NodeB.
 *
 * Create a graph of non-cyclic refs and then start adding weight at the leaf
 * nodes working back.
 *
 * @see com.versant.core.metadata.ClassMetaData#referenceGraphIndex
 */
public final class JdbcClassReferenceGraph implements Comparator {
    
    private HashMap cmdToRefMap = new HashMap();
    private HashMap cmdToIndirectMap = new HashMap();
    private HashMap cmdToNonCyclicMap = new HashMap();
    private final ClassMetaData[] classes;

    /**
     * Extract out only the topmost classes from a.
     */
    public JdbcClassReferenceGraph(ClassMetaData[] a) {
        int n = a.length;
        classes = new ClassMetaData[n];
        for (int i = 0; i < n; i++) {
            classes[i] = a[i];
            classes[i].weight = 0;
        }
    }

    public int compare(Object o1, Object o2) {
        ClassMetaData cmd1 = ((ClassMetaData) o1).top;
        ClassMetaData cmd2 = ((ClassMetaData) o2).top;
        if (cmd1.weight != cmd2.weight) {
            return cmd2.weight - cmd1.weight;
        } else {
            return cmd2.qname.compareTo(cmd1.qname);
        }
    }

    private void addRefs(ClassMetaData cmd, HashSet set) {
        FieldMetaData[] fields = cmd.fields;
        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData field = fields[i];
            if (field.isDirectRef()) {
                set.add(field.typeMetaData.top);
            }
        }
    }

    private void addRefsRecursive(ClassMetaData cmd, HashSet set) {
        addRefs(cmd, set);
        ClassMetaData[] pcSubs = cmd.pcSubclasses;
        if (pcSubs == null) return;
        for (int i = 0; i < pcSubs.length; i++) {
            addRefsRecursive(pcSubs[i], set);
        }
    }

    private HashSet getDirectRefs(ClassMetaData cmd, HashMap refMap) {
        return (HashSet) refMap.get(cmd.top);
    }

    private HashSet getIndirectRefs(ClassMetaData cmd, HashMap indirectRefMap) {
        return (HashSet) indirectRefMap.get(cmd.top);
    }

    private void buildIndirectGraph() {
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.isBaseClass()) {
                HashSet indirectRefSet = new HashSet();
                cmdToIndirectMap.put(cmd, indirectRefSet);
                buildIndirectGraph(cmd, cmd, indirectRefSet, cmdToRefMap);
            }
        }
    }

    private void buildIndirectGraph(ClassMetaData rootCmd, ClassMetaData cmd, 
            Set indirectRefs, Map directRefMap) {
        Set directRefs = (Set)directRefMap.get(cmd);
        for (Iterator iterator = directRefs.iterator(); iterator.hasNext();) {
            ClassMetaData dRef = (ClassMetaData)iterator.next();
            if (dRef != dRef.top) {
                BindingSupportImpl.getInstance().internal(
                    "Should be top most class");
            }
            if (dRef == rootCmd) continue;
            if (indirectRefs.contains(dRef)) continue;
            indirectRefs.add(dRef);
            buildIndirectGraph(rootCmd, dRef, indirectRefs, directRefMap);
        }
    }

    private int calculateWieght(ClassMetaData cmd) {
        if (Debug.DEBUG) {
            if (cmd.top != cmd) throw BindingSupportImpl.getInstance().internal(
                    "Must be top of hierarchy");
        }
        if (cmd.weight != 0) {
            //already done
            return cmd.weight;
        }

        HashSet noncircularRefs = (HashSet)cmdToNonCyclicMap.get(cmd);
        int maxWeight = 0;
        if (noncircularRefs != null) {
            for (Iterator iterator = noncircularRefs.iterator(); iterator.hasNext();) {
                ClassMetaData classMetaData = (ClassMetaData) iterator.next();
                int refWeight = calculateWieght(classMetaData);
                if (refWeight > maxWeight) maxWeight = refWeight;
            }
        }
        return cmd.weight = maxWeight + 1;
    }

    /**
     * Sort the graph and set the referenceGraphIndex and referenceGraphCycle
     * fields on each class.
     *
     * @see com.versant.core.metadata.ClassMetaData#referenceGraphIndex
     * @see com.versant.core.metadata.ClassMetaData#referenceGraphCycle
     */
    public void sort() {
        //build the direct refs graph
        buildDirectRefGraph();

        //build the indirect ref graph
        buildIndirectGraph();

        //build the non-cyclic ref graph
        buildNonCyclicRefGraph();

        //calculate weights for the nodes
        calculateWieght();

        List classList = Arrays.asList(this.classes);
        Collections.sort(classList, this);
        for (int i = 0; i < classList.size(); i++) {
            ((ClassMetaData)classList.get(i)).setReferenceGraphIndex(i);
        }
    }

    private void calculateWieght() {
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.isBaseClass()) {
                calculateWieght(cmd);
            }
        }
    }

    private void buildNonCyclicRefGraph() {
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.isBaseClass()) {
                HashSet indirectRefSet = (HashSet) cmdToIndirectMap.get(cmd);
                HashSet nonCircularRefs = new HashSet();
                cmdToNonCyclicMap.put(cmd, nonCircularRefs);
                for (Iterator iterator = indirectRefSet.iterator(); iterator.hasNext();) {
                    ClassMetaData refNode = (ClassMetaData) iterator.next();
                    if (!isCircularRef(cmd, refNode)) {
                        nonCircularRefs.add(refNode);
                    }
                }
            }
        }
    }

    private void buildDirectRefGraph() {
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.isBaseClass()) {
                HashSet refSet = new HashSet();
                cmdToRefMap.put(cmd, refSet);
                addRefsRecursive(cmd, refSet);
            }
        }
    }

    public boolean isCircularRef(ClassMetaData cmd1, ClassMetaData cmd2) {
        cmd1 = cmd1.top;
        cmd2 = cmd2.top;
        HashSet s1 = getIndirectRefs(cmd1, cmdToIndirectMap);
        HashSet s2 = getIndirectRefs(cmd2, cmdToIndirectMap);
        return s1.contains(cmd2) && s2.contains(cmd1);
    }

    public void releaseMem() {
        cmdToIndirectMap = null;
        cmdToNonCyclicMap = null;
        cmdToRefMap = null;
    }
}
