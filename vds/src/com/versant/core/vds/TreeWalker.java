
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
package com.versant.core.vds;

import java.util.*;
import com.versant.core.jdo.query.Node;
import com.versant.core.jdo.query.NodeVisitor;

public final class TreeWalker 
{
  public Object walk(Node node, NodeVisitor visitor) {
    if(node != null) {
      // node.arrive( visitor );
      ArrayList results = new ArrayList();
      Node children = node.childList;
      if (children != null) {   
        for (Node c = children; c != null; c = c.next) 
          results.add(walk(c, visitor));
      }
      return node.accept(visitor, results.toArray(new Object[results.size()]));
    }
    return null;
  }
}
