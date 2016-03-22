
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
package com.versant.core.ejb.query;

import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.PrintStream;

/**
 * Information collected and used during a Node.resolve call (e.g.
 * identification variables and so on).
 */
public class ResolveContext {

    private final ModelMetaData mmd;
    private final Map idVarMap = new HashMap(16);
    private NavRoot[] roots = new NavRoot[2];
    private int rootCount;

    private Map paramMap;   // param name or Integer number -> ParamUsage list
    private boolean usingPositionalParameters;

    private ParamUsage[] NO_PARAMS = new ParamUsage[0];

    /**
     * Tracks usage of a parameter within the query String. Each time
     * a parameter is used one of these is added to its list in
     * paramMap.
     */
    public static class ParamUsage {

        private ParameterNode paramNode;
        private ParamUsage next;
        private int index;
        public Object storeObject;

        public ParamUsage(ParameterNode paramNode, int index) {
            this.paramNode = paramNode;
            this.index = index;
        }

        public ParameterNode getParamNode() {
            return paramNode;
        }

        public ParamUsage getNext() {
            return next;
        }

        /**
         * This defines the order that the parameters where addded in with
         * the first having index 0.
         */
        public int getIndex() {
            return index;
        }
    }

    public ResolveContext(ModelMetaData mmd) {
        this.mmd = mmd;
    }

    public ModelMetaData getModelMetaData() {
        return mmd;
    }

    /**
     * Throw a user exception of some kind. This is invoked for user errors
     * in the query (e.g. duplicate identification variables). The node
     * parameter is used to provide a line and column reference to the
     * original query string.
     */
    public RuntimeException createUserException(String msg, Node node) {
        return BindingSupportImpl.getInstance().invalidOperation(msg);
    }

    /**
     * Get the identification variable for the identifier or null if
     * not found.
     */
    public NavBase getIdVar(String identifier) {
        return (NavBase)idVarMap.get(identifier);
    }

    /**
     * Add an identification variable for an identifier.
     */
    public void addIdVar(String identifier, NavBase navBase) {
        idVarMap.put(identifier, navBase);
        if (navBase instanceof NavRoot) {
            if (rootCount == roots.length) {
                NavRoot[] a = new NavRoot[rootCount * 2];
                System.arraycopy(roots, 0, a, 0, rootCount);
                roots = a;
            }
            roots[rootCount++] = (NavRoot)navBase;
        }
    }

    /**
     * Get the number of roots.
     */
    public int getRootCount() {
        return rootCount;
    }

    /**
     * Get the root with index.
     */
    public NavRoot getRoot(int index) {
        if (Debug.DEBUG) {
            if (index >= rootCount) {
                throw BindingSupportImpl.getInstance().internal(
                        "index >= rootCount: " + index + " >= " + rootCount);
            }
        }
        return roots[index];
    }

    /**
     * Throw a duplicate ID var exception if identifier already exists.
     */
    public void checkIdVarDoesNotExist(String identifier, Node node) {
        NavBase dup = getIdVar(identifier);
        if (dup != null) {
            throw createUserException("Duplicate identification variable: " +
                    identifier, node);
        }
    }

    /**
     * Get the identification variable for the identifier or throw an
     * exception if not found.
     */
    public NavBase checkIdVarExists(String identifier, Node node) {
        NavBase ans = getIdVar(identifier);
        if (ans == null) {
            throw createUserException("Unknown identification variable: " +
                    identifier, node);
        }
        return ans;
    }

    /**
     * Resolve a path into a NavField. A new path is created in the tree
     * starting at the identification variable if necessary.
     */
    public NavBase resolveJoinPath(PathNode path, boolean outer, boolean fetch) {
        return resolveJoinPath(path, outer, fetch, path.size());
    }

    /**
     * Resolve a path sz identifiers deep into a NavBase. A new path is
     * created in the tree starting at the identification variable if
     * necessary.
     */
    public NavBase resolveJoinPath(PathNode path, boolean outer, boolean fetch,
            int sz) {
        String idVarName = path.get(0);
        NavBase pos = checkIdVarExists(idVarName, path);
        for (int i = 1; i < sz; i++) {
            String name = path.get(i);
            NavField f = pos.findChild(name, outer, fetch);
            if (f == null) {
                ClassMetaData cmd = pos.getNavClassMetaData();
                if (cmd == null) {
                    throw createUserException("Field " + f.getFmd().getQName() +
                            " may not be navigated", path);
                }
                FieldMetaData fmd = cmd.getFieldMetaData(name);
                if (fmd == null) {
                    throw createUserException("No such field '" + name +
                            "' on " + cmd.qname, path);
                }
                f = new NavField(fmd, pos, outer, fetch);
            }
            pos = f;
        }
        return pos;
    }

    public ParamUsage addParameterNode(ParameterNode param) {
        if (paramMap == null) {
            usingPositionalParameters = param.isPositional();
            paramMap = new HashMap(8);
        } else {
            if (usingPositionalParameters != param.isPositional()) {
                throw createUserException(
                        "Positional and named parameters may not be mixed", param);
            }
        }
        ParamUsage ans = new ParamUsage(param, paramMap.size());
        Object key = param.getName();
        ParamUsage u = (ParamUsage)paramMap.get(key);
        if (u != null) {
            for (; u.next != null; u = u.next);
            u.next = ans;
        } else {
            paramMap.put(key, ans);
        }
        return ans;
    }

    /**
     * Is this query using positional parameters?
     */
    public boolean isUsingPositionalParameters() {
        return usingPositionalParameters;
    }

    /**
     * Get the parameters used in the query sorted in the order that they
     * appear. Returns empty array if  the query uses no parameters.
     */
    public ParamUsage[] getParameters() {
        if (paramMap == null) {
            return NO_PARAMS;
        }
        ParamUsage[] ans = new ParamUsage[paramMap.size()];
        for (Iterator i = paramMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            ParamUsage u = (ParamUsage)e.getValue();
            ans[u.index] = u;
        }
        return ans;
    }

    /**
     * Find the first usage of a parameter in the query String. Throws
     * an exception if none found (should not be possible).
     */
    public ParamUsage getFirstParamUsage(Object nameOrPosition) {
        ParamUsage ans = (ParamUsage)paramMap.get(nameOrPosition);
        if (ans == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "No usage found for " + nameOrPosition);
        }
        return ans;
    }

    /**
     * Dump debugging info.
     */
    public void dump(PrintStream out) {
        out.println("Roots (" + rootCount + ")");
        for (int i = 0; i < rootCount; i++) {
            out.println("[" + i + "] " + getRoot(i));
        }
    }

}

