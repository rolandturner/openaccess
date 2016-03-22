
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

/**
 * This persists a graph of GraphNode's for Walk to traverse. It accepts two
 * arguments: fanout and depth respectively. The fanout specifies the
 * number of children each non-leaf GraphNode in the graph has. The depth
 * specifies the number of nodes from the root node to the edge of the
 * graph including the root node. The root node has name "root".
 */
public class InsertData {

    public static void main(String[] args) {
        try {
            int fanout = Integer.parseInt(args[0]);
            int depth = Integer.parseInt(args[1]);

            System.out.println("Creating graph fanout " + fanout + " depth " + depth);
            count = 0;
            GraphNode root = createNode(1, fanout, depth);
            root.setName("root");

            PersistenceManager pm = Sys.pm();

            System.out.println("Persisting graph");
            long start = System.currentTimeMillis();

            pm.currentTransaction().begin();
            pm.makePersistent(root);
            pm.currentTransaction().commit();

            int ms = (int)(System.currentTimeMillis() - start);
            System.out.println(count + " nodes persisted in " + ms + " ms (" +
                    (float)ms / count + " ms each)");

            pm.close();

            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static int count;

    private static GraphNode createNode(int depth, int fanout, int maxDepth) {
        GraphNode ans = new GraphNode();
        if (depth < maxDepth) {
            for (int i = 0; i < fanout; i++) {
                GraphNode child = createNode(depth + 1, fanout, maxDepth);
                child.setName("N" + ++count);
                ans.addOutEdge(child);
                // only every second child has an edge back to its parent
                if ((i % 2) == 0) child.addOutEdge(ans);
            }
        }
        return ans;
    }

}
