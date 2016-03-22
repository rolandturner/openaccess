
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
package graph;

import graph.model.GraphNode;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
 * <p>This demo shows how to use fetch groups to retrieve a graph of instances
 * in as few queries as possible. The same graph is retrieved using different
 * strategies. For each strategy the time required and the number of SELECT's
 * executed is displayed.</p>
 *
 * <p>Note that very large graphs may report incorrect select counts as the
 * event log ring buffer may overflow losing selects.</p>
 */
public class Walk {

    public static void main(String[] args) {
        try {
            PersistenceManager pm;
            long start;
            Query q;
            Collection col;
            GraphNode root;
            HashSet found;
            Sys.getSelectCount(); // reset SELECT counter

            System.out.println(
                "Walk the whole graph starting from the root node found with\n"+
                "a JDOQL query. This will run (number of nodes) + 1 SELECTs.");
            pm = Sys.pm();
            start = System.currentTimeMillis();
            pm.currentTransaction().begin();
            q = pm.newQuery(GraphNode.class, "name == 'root'");
            col = (Collection)q.execute();
            root = (GraphNode)col.iterator().next();
            q.closeAll();
            walkGraph(root, found = new HashSet());
            pm.currentTransaction().commit();
            printTiming(start, found);
            pm.close();

            Sys.clearLevel2Cache();

            System.out.println(
                "Walk the whole graph starting from the root node found with\n" +
                "a JDOQL query using bounded=true and fetchGroup=all that\n" +
                "recursively includes the name and outEdges of every node.\n" +
                "This will run (depth of graph) + 2 SELECTs.");
            pm = Sys.pm();
            start = System.currentTimeMillis();
            pm.currentTransaction().begin();
            q = pm.newQuery(GraphNode.class, "name == 'root'");
            q.declareParameters("String jdoGenieOptions");
            col = (Collection)q.execute("fetchGroup=all;bounded=true");
            root = (GraphNode)col.iterator().next();
            q.closeAll();
            walkGraph(root, found = new HashSet());
            pm.currentTransaction().commit();
            printTiming(start, found);
            Object rootOID = pm.getObjectId(root);
            pm.close();

            Sys.clearLevel2Cache();

            System.out.println(
                "Walk the whole graph starting from the root node found with\n" +
                "getObjectById. This will run (number of nodes) + 1 SELECTs.");
            pm = Sys.pm();
            start = System.currentTimeMillis();
            pm.currentTransaction().begin();
            root = (GraphNode)pm.getObjectById(rootOID, true);
            walkGraph(root, found = new HashSet());
            pm.currentTransaction().commit();
            printTiming(start, found);
            pm.close();

            Sys.clearLevel2Cache();

            System.out.println(
                "Walk the whole graph starting from the root node found with\n" +
                "getObjectById and fetchGroup=all. This will run\n" +
                "(depth of graph) + 2 SELECTs.");
            pm = Sys.pm();
            start = System.currentTimeMillis();
            pm.currentTransaction().begin();
            root = (GraphNode)Sys.getObjectById(rootOID, "all");
            walkGraph(root, found = new HashSet());
            pm.currentTransaction().commit();
            printTiming(start, found);
            pm.close();

            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Recursively visit all nodes reachable from node via outEdges that are
     * not already in found. Adds nodes visited to found.
     */
    private static void walkGraph(GraphNode node, Set found) {
        if (found.contains(node)) return;
        found.add(node);
        for (Iterator i = node.getOutEdges().iterator(); i.hasNext(); ) {
            walkGraph((GraphNode)i.next(), found);
        }
    }

    private static void printTiming(long start, HashSet found) {
        int ms = (int)(System.currentTimeMillis() - start);
        int count = found.size();
        int selectCount = Sys.getSelectCount();
        System.out.println("\n" + count + " nodes walked in " + ms + " ms (" +
                (float)ms / count + " ms each) using " + selectCount +
                " SELECTs\n");
    }

}

