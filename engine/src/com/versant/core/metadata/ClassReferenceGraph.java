
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
package com.versant.core.metadata;

import com.versant.core.common.SortableBase;
import com.versant.core.util.IntArray;

import javax.jdo.JDOFatalUserException;

/**
 * This does a topological sort of a graph of ClassMetaData using direct
 * references between the classes as edges. Each class has its
 * referenceClassDepth field filled by sort. Cycles are detected and all
 * classes involved in a cycle have their referenceGraphCycle flags set. This
 * is used to not generate constraints for these fields. The
 * referenceClassDepth is used to order deletes to avoid tripping
 * constraints.<p>
 *
 * Only top level classes are actually sorted and the results are applied to
 * all subclasses. This is because heirachies are mapped into the table for
 * the base class. References in subclasses are considered as edges of the
 * base class in the graph.<p>
 *
 * @see ClassMetaData#referenceGraphIndex
 * @see ClassMetaData#referenceGraphCycle
 */
public final class ClassReferenceGraph extends SortableBase {

    /**
     * Fill in the referenceGraphIndex and referenceGraphCycle fields for all
     * classes in jmd.
     * @see ClassMetaData#referenceGraphIndex
     * @see ClassMetaData#referenceGraphCycle
     */
    public static void sort(ClassMetaData[] classes) {
        new ClassReferenceGraph(classes).sort();
    }

    private final ClassMetaData[] classes; // top level only
    private final int[] depth;
    private final int[] edgeStart;
    private final int[] edgeCount;

    /**
     * Extract out only the topmost classes from a.
     */
    private ClassReferenceGraph(ClassMetaData[] a) {
        int n = a.length;
        classes = new ClassMetaData[n];
        for (int i = 0; i < n; i++) {
            ClassMetaData cmd = a[i];
            if (cmd.pcSuperMetaData == null) classes[size++] = cmd;
        }
        depth = new int[size];
        edgeStart = new int[size];
        edgeCount = new int[size];
    }

    /**
     * Compare entries at and a and b. Return 0 if equal, less than 0
     * if a is less than b or greater than 0 if a is greater than b.
     */
    protected int compare(int a, int b) {
        int diff = depth[b] - depth[a];
        if (diff != 0) return diff;
        return classes[a].index - classes[b].index;
    }

    /**
     * Swap entries.
     */
    protected void swap(int i1, int i2) {
        int t = depth[i1];
        depth[i1] = depth[i2];
        depth[i2] = t;
        ClassMetaData cmd = classes[i1];
        classes[i1] = classes[i2];
        classes[i2] = cmd;
    }

    /**
     * Sort the graph and set the referenceGraphIndex and referenceGraphCycle
     * fields on each class.
     * @see ClassMetaData#referenceGraphIndex
     * @see ClassMetaData#referenceGraphCycle
     */
    public void sort() {
        for (int i = size - 1; i >= 0; i--) {
            classes[i].setReferenceGraphCycle(false);
        }
        int[] visited = new int[size];
        int[] edges = findEdges();
//        if (Debug.DEBUG) dumpEdges(edges);
        for (int i = 0; i < size; i++) {
            topsort(i, edges, visited, 0);
        }
        super.sort();
        for (int i = size - 1; i >= 0; i--) {
            classes[i].setReferenceGraphIndex(i);
        }
//        if (Debug.DEBUG) dumpGraph();
    }

    private void dumpGraph() {
        for (int i = 0; i < size; i++) {
            ClassMetaData cmd = classes[i];
            System.out.println("[" + i + "] = " + cmd.qname +
                " depth " + depth[i] +
                " cycle " + cmd.referenceGraphCycle);
        }
        System.out.println("---");
    }

    private void dumpEdges(int[] edges) {
        for (int i = 0; i < size; i++) {
            StringBuffer s = new StringBuffer();
            s.append("[" + i + "] = " + classes[i].qname + " edges");
            for (int j = 0; j < edgeCount[i]; j++) {
                s.append(' ');
                s.append(edges[edgeStart[i] + j]);
            }
            System.out.println(s);
        }
        System.out.println("---");
    }

    /**
     * Do a topological sort of the graph.
     */
    private int topsort(int index, int[] edges, int[] visited,
            int pathlen) throws JDOFatalUserException {
        if (visited[index] > 0) {
            // us and all classes visited after our first visit form a cycle
            int v = visited[index];
            for (int i = size - 1; i >= 0; i--) {
                if (visited[i] >= v) classes[i].setReferenceGraphCycle(true);
            }
            return 0;
        }
        int ec = edgeCount[index];
        if (ec == 0) return 0; // a leaf
        visited[index] = ++pathlen; // push index on stack of current path
        int d = 0;
        int t = 0;
        while (ec > 0) {
            t = topsort(edges[edgeStart[index] + (--ec)],
                    edges, visited, pathlen);
            d = t > d ? t : d;
        }
        d = d + 1; //Depth = max( depth of children) + 1
        if (depth[index] < d) depth[index] = d;
        visited[index] = 0; // pop out index from the stack
        return d;
    }

    /**
     * Find all the edges in the graph.
     */
    private int[] findEdges() {
        IntArray edges = new IntArray(size);
        for (int i = 0; i < size; i++) {
            int start = edgeStart[i] = edges.size();
            findEdges(classes[i], edges);
            edgeCount[i] = edges.size() - start;
        }
        return edges.toArray();
    }

    /**
     * Find all direct references from cmd and its subclasses to other
     * classes and add them to edges.
     */
    private void findEdges(ClassMetaData cmd, IntArray edges) {
        FieldMetaData[] a = cmd.fields;
        for (int j = a.length - 1; j >= 0; j--) {
            FieldMetaData f = a[j];
            if (!f.isDirectRef()) continue;
            edges.add(indexOf(f.typeMetaData));
        }
        if (cmd.pcSubclasses != null) {
            for (int i = cmd.pcSubclasses.length - 1; i >= 0; i--) {
                findEdges(cmd.pcSubclasses[i], edges);
            }
        }
    }

    private int indexOf(ClassMetaData cmd) {
        for (; cmd.pcSuperMetaData != null; ) cmd = cmd.pcSuperMetaData;
        for (int i = size - 1; i >= 0; i--) {
            if (classes[i] == cmd) return i;
        }
        return -1;
    }

}
