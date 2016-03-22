
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
package com.versant.core.metadata.parser;

import com.versant.core.common.Debug;

import java.io.PrintStream;

/**
 * A named query in the meta data.
 */
public final class JdoQuery extends JdoElement {

    // query
    public String name;
    public String language;
    public int ignoreCache;
    public int includeSubclasses;
    public String ordering;
    public int rangeStart;
    public int rangeEnd;

    // filter
    public String filter;

    // sql
    public String sql;

    // declare
    public String imports;
    public String parameters;
    public String variables;

    // result
    public String result;
    public String resultClass;
    public String grouping;
    public int unique;

    public JdoExtension[] extensions;

    public JdoClass parent;

    public JdoElement getParent() {
        return parent;
    }

    /**
     * Get the fully qualified name of the candidate class for the query.
     */
    public String getCandidateClass() {
        return parent.parent.name + "." + parent.name;
    }

    /**
     * Get information for this element to be used in building up a
     * context string.
     *
     * @see #getContext
     */
    public String getSubContext() {
        return "query[" + name + "]";
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("query[");
        s.append(name);
        s.append("] language=");
        s.append(language);
        s.append(" ignore-cache=");
        s.append(ignoreCache);
        s.append(" include-subclasses=");
        s.append(includeSubclasses);
        s.append("\nfilter[");
        s.append(filter);
        s.append("]");
        s.append("\ndeclarations imports=");
        s.append(imports);
        s.append(" parameters=");
        s.append(parameters);
        s.append(" variables=");
        s.append(variables);
        s.append(" ordering=");
        s.append(ordering);
        s.append("\nresult class=");
        s.append(resultClass);
        s.append(" grouping=");
        s.append(grouping);
        s.append(" unique=");
        s.append(unique);

        return s.toString();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                extensions[i].dump(out, is);
            }
        }
    }
}
