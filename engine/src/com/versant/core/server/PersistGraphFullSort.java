
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
package com.versant.core.server;

import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.util.IntArray;

import javax.jdo.JDOFatalUserException;		//only appears in throws clause, to discuss, whether useful there at all
import java.util.Date;

/**
 * This class does a full topological sort of the graph and must be used for
 * any graph containing new instances using post-insert key generators.
 *
 * @see PersistGraph
 */
public final class PersistGraphFullSort extends PersistGraph {

    private int[] oidLevels;
    private int[] oidDepth;

    public PersistGraphFullSort(ModelMetaData jmd, int maxSize) {
        super(jmd, maxSize);
        oidDepth = new int[maxSize];
    }

    /**
     * Empty the graph so it can be reused.
     */
    public void clear() {
        super.clear();
        oidLevels = null;
    }

    /**
     * Sort the graph so that the nodes that do not depend on (reference) any
     * other nodes are first, then those that depend on them and so on. This
     * is the correct ordering for persisting a graph with new instances
     * using post-insert key generators.
     *
     * @throws javax.jdo.JDOFatalUserException
     *          if a cycle is detected
     */
    public void sort() {
        int size = this.size;
        int[] oidEdgeStart = new int[size];
        int[] oidEdgeCount = new int[size];
        boolean[] rootVertex = new boolean[size];
        boolean[] isVisited = new boolean[size];
        boolean[] isInserted = new boolean[size];

        for (int i = 0; i < size; i++) {
            rootVertex[i] = true;
            isVisited[i] = false;
            isInserted[i] = false;
        }

        int[] oidEdges = findEdges(size, oidEdgeStart, oidEdgeCount,
                rootVertex);

        for (int i = 0; i < size; i++) {
            if (rootVertex[i]) {
                topsort(i, oidEdges, oidEdgeStart, oidEdgeCount,
                        isVisited, isInserted);
            }
        }

        super.sort();

        oidLevels = new int[oidDepth[size - 1] + 1];
        for (int i = 0; i < oidLevels.length; oidLevels[i++] = 0) ;
        oidLevels[0] = 0;

        for (int i = 0, j = 0; i < size; i++) {
            if (j != oidDepth[i]) oidLevels[++j] = i;
        }

        oidIndexMap.clear(); // no longer valid
    }

    /**
     * Do a full topological sort of the graph.
     *
     * @throws javax.jdo.JDOFatalUserException
     *          if there are cycles
     */
    private int topsort(int index, int[] oidEdges, int[] oidEdgeStart,
            int[] oidEdgeCount, boolean[] isVisited,
            boolean[] isInserted) throws JDOFatalUserException {
        if (isVisited[index]) return 0;
        int edgeCount = oidEdgeCount[index];
        if (edgeCount == 0) { // aleaf
            if (!isInserted[index]) {
                oidDepth[index] = 0;
                isInserted[index] = true;
            }
            return 0;
        }
        isVisited[index] = true; // push index on stack of current path
        int depth = 0;
        int t = 0;
        while (edgeCount > 0) {
            t = topsort(oidEdges[oidEdgeStart[index] + (--edgeCount)],
                    oidEdges, oidEdgeStart, oidEdgeCount,
                    isVisited, isInserted);
            depth = t > depth ? t : depth;
        }
        depth = depth + 1; //Depth = max( depth of children) + 1
        if (!isInserted[index]) {
            oidDepth[index] = depth;
            isInserted[index] = true;
        }
        isVisited[index] = false; // pop out index from the stack
        return depth;
    }

    /**
     * Find all the edges in the graph. This also updates autoSet fields.
     */
    private int[] findEdges(int size, int[] oidEdgeStart,
            int[] oidEdgeCount, boolean[] rootVertex) {
        IntArray edges = new IntArray(size);
        int start;
        int fin;
        Date now = new Date();
        for (int i = 0; i < size; i++) {
            start = oidEdgeStart[i] = edges.size();
            State ns = newStates[i];
            OID oid = oids[i];
            if (oid.isNew()) {
                ns.updateAutoSetFieldsCreated(now);
            } else {
                ns.updateAutoSetFieldsModified(now, oldStates[i]);
            }
            ns.findDirectEdges(this, edges);
            fin = edges.size();
            oidEdgeCount[i] = fin - start;
            for (int j = start; j < fin; j++) {
                rootVertex[edges.get(j)] = false;
            }
        }
        return edges.toArray();
    }

    /**
     * Compare graph entries at and a and b. Return 0 if equal, less than 0
     * if a is less than b or greater than 0 if a is greater than b. This
     * orders entries by depth, by class index, by new objects first,
     * by field numbers.
     */
    protected int compare(int a, int b) {
        int diff = oidDepth[a] - oidDepth[b];
        if (diff != 0) return diff;
        return super.compare(a, b);
    }

    /**
     * Swap entries.
     */
    protected void swap(int index1, int index2) {
        super.swap(index1, index2);
        int tempDepth = oidDepth[index1];
        oidDepth[index1] = oidDepth[index2];
        oidDepth[index2] = tempDepth;
    }
}
