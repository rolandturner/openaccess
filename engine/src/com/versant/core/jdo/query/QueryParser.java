
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
package com.versant.core.jdo.query;

import com.versant.core.common.Debug;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.common.CmdBitSet;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.util.classhelper.ClassHelper;

import java.io.StringReader;

import com.versant.core.common.BindingSupportImpl;

/**
 * Wrapper around the JavaCC JDOQL parser.
 */
public class QueryParser {

    private JavaCharStream stream;
    protected ImportNode[] imports;
    protected ParamNode[] params;
    protected VarNode[] vars;
    protected OrderNode[] orders;
    protected UnaryNode filter;
    protected ResultNode resultNode;
    protected GroupingNode groupingNode;

    private JDOQLParser parser;
    private JDOQLParserTokenManager tokenManager;

    /**
     * The ClassMetaData of the Candidate class.
     */
    protected ClassMetaData cmd;
    protected ModelMetaData jmd;

    public QueryParser(ModelMetaData jmd) {
        this.jmd = jmd;
    }

	public ClassMetaData getCmd() {
		return cmd;
	}

    public ImportNode[] getImports() {
        return imports;
    }

    public ParamNode[] getParams() {
        return params;
    }

    public void setParams(ParamNode[] params) {
        this.params = params;
    }
	
    public VarNode[] getVars() {
        return vars;
    }

    public OrderNode[] getOrders() {
        return orders;
    }

    public UnaryNode getFilter() {
        return filter;
    }

    public ResultNode getResultNode() {
        return resultNode;
    }

    public GroupingNode getGroupingNode() {
        return groupingNode;
    }

    public void setGroupingNode(GroupingNode groupingNode) {
        this.groupingNode = groupingNode;
    }

    public void parse(QueryDetails q) throws Exception {

			parseJDOQL(q);
			
		parseCommon(q);
	}

    public void parseJDOQL(QueryDetails q) throws Exception {
        cmd = jmd.getClassMetaData(q.getCandidateClass());
        String s = q.getImports();
        if (s != null) {
            initParser(s);
            try {
                imports = parser.declareImports();
            } catch (ParseException e) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid imports:\n" + s + "\n" +
                        e.getMessage());
            }
            if (Debug.DEBUG) {
                Debug.OUT.println("imports: ");
                dump(imports);
            }
        } else {
            imports = null;
        }

        // parse the parameters
        s = q.getParameters();
        if (s != null) {
            initParser(s);
            try {
                params = parser.declareParameters();
            } catch (ParseException e) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid parameter declarations:\n" + s + "\n" +
                        e.getMessage());
            }
            for (int i = params.length - 1; i >= 0; i--) params[i].setIndex(i);
            if (Debug.DEBUG) {
                Debug.OUT.println("params: ");
                dump(params);
            }
        } else {
            params = null;
        }

        // parse the variables
        s = q.getVariables();
        if (s != null) {
            initParser(s);
            try {
                vars = parser.declareVariables();
            } catch (ParseException e) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid variable declarations:\n" + s + "\n" +
                        e.getMessage());
            }
            if (Debug.DEBUG) {
                Debug.OUT.println("vars: ");
                dump(vars);
            }
        } else {
            vars = null;
        }

        // parse the ordering
        s = q.getOrdering();
        if (s != null) {
            initParser(s);
            try {
                orders = parser.setOrderings();
            } catch (ParseException e) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid ordering:\n" + s + "\n" +
                        e.getMessage());
            }
            for (int i = 0; i < orders.length; i++) orders[i].normalize();
            if (Debug.DEBUG) {
                Debug.OUT.println("normalized orders: ");
                dump(orders);
            }
        } else {
            orders = null;
        }

        // parse the filter
        s = q.getFilter();
        if (s != null) {
            initParser(q.getFilter());
            try {
                Node e = parser.filterExpression();
                if (e != null) {
                    filter = new UnaryNode(e);
                } else {
                    filter = null;
                }
            } catch (ParseException x) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid filter:\n" + s + "\n" +
                        x.getMessage());
            }
        } else {
            filter = null;
        }

        //parse the result
        s = q.getResult();
        if (s != null && s.trim().length() > 0) {
            initParser(s);
            try {
                resultNode = parser.setResults();
            } catch (ParseException x) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid result:\n" + s + "\n" +
                        x.getMessage());
            }
        } else {
            resultNode = null;
        }

        //parse the grouping
        s = q.getGrouping();
        if (s != null) {
            initParser(s);
            try {
                groupingNode = parser.setGrouping();
            } catch (ParseException x) {
                throw BindingSupportImpl.getInstance().invalidOperation("Invalid grouping:\n" + s + "\n" +
                        x.getMessage());
            }
        } else {
            groupingNode = null;
        }
    }

    private void parseCommon(QueryDetails q) throws Exception {

        // check the types of all vars and lookup their meta data
        if (vars != null) {
            for (int i = vars.length - 1; i >= 0; i--) vars[i].resolve(this);
        }

        if (filter != null) {
            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Filter: " + filter);
                Debug.OUT.println("\n* Parsed tree:");
                filter.dump("");
            }

            // simplify some tree constructs
            filter.normalize();
            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Normalized tree:");
                filter.dump("");
            }

            // resolve field and parameter names etc
            filter.resolve(this, cmd, false);
            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Resolved tree:");
                filter.dump("");
            }

            // simplify some tree constructs again as some operations
            // require resolved nodes
            filter.normalize();
            if (Debug.DEBUG) {
                Debug.OUT.println("\n* Second normalized tree:");
                filter.dump("");
            }

        } else {
            if (Debug.DEBUG) {
                Debug.OUT.println("filter is null");
            }
        }
    }


	
    /**
     * Parse just an ordering specification. This is used to parse orderings
     * on their own in the meta data.
     */
    public OrderNode[] parseOrdering(ClassMetaData candidateClass, String s)
            throws ParseException {
        try {
            if (s != null) {
                cmd = candidateClass;
                initParser(s);
                return parser.setOrderings();
            } else {
                return null;
            }
        } catch (TokenMgrError e) {
            throw new ParseException(e.toString());
        }
    }

    /**
     * Find the parameter with name or null if none.
     */
    public ParamNode findParam(String name) {
        if (params == null) return null;
        for (int i = params.length - 1; i >= 0; i--) {
            ParamNode p = params[i];
            if (p.getIdentifier().equals(name)) return p;
        }
        return null;
    }

    /**
     * Find the variable with name or null if none.
     */
    public VarNode findVar(String name) {
        if (vars == null) return null;
        for (int i = vars.length - 1; i >= 0; i--) {
            VarNode v = vars[i];
            if (v.getIdentifier().equals(name)) return v;
        }
        return null;
    }

    public ClassMetaData getCMD(Class cls) {
        return jmd.getClassMetaData(cls);
    }

    /**
     * Convert a variable type name into a class. This makes sure it is a
     * legal type.
     */
    public Class resolveVarType(String type) {
        Class cls;
        try {
            cls = resolveType(type);
        } catch (ClassNotFoundException e) {
            throw BindingSupportImpl.getInstance().runtime("Variable class not found: '" +
                    type + "'", e);
        }
        return cls;
    }

    /**
     * Convert a parameter type name into a class. This makes sure it is a
     * legal type.
     */
    public Class resolveParamType(String type) {
        Class cls;
        try {
            cls = resolveType(type);
        } catch (ClassNotFoundException e) {
            throw BindingSupportImpl.getInstance().runtime("Parameter class not found: '" +
                    type + "'", e);
        }
        return cls;
    }

    private Class resolveType(String type) throws ClassNotFoundException {
        try {
            return resolveTypeImp(type);
        } catch (ClassNotFoundException e) {
            int i = type.lastIndexOf('.');
            if (i >= 0) {
                StringBuffer s = new StringBuffer(type);
                s.setCharAt(i, '$');
                try {
                    return resolveTypeImp(s.toString());
                } catch (ClassNotFoundException e1) {
                    // ignore
                }
            }
            throw e;
        }
    }

    private Class resolveTypeImp(String type) throws ClassNotFoundException {
        ClassLoader loader = cmd.getClassLoader();
        try {
            return ClassHelper.get().classForName(type, true, loader);
        } catch (ClassNotFoundException e) {
            if (imports != null) {
                int len = imports.length;
                for (int i = 0; i < len; i++) {
                    ImportNode im = imports[i];
                    if (im.all) {
                        try {
                            return ClassHelper.get().classForName(im.name + type, true, loader);
                        } catch (ClassNotFoundException x) {
                            // ignore
                        }
                    } else {
                        if (type.equals(im.getClassName())) {
                            return ClassHelper.get().classForName(im.name, true, loader);
                        }
                    }
                }
            }
            try {
                return ClassHelper.get().classForName(cmd.packageNameWithDot + 
													  type, true, loader);
            } catch (ClassNotFoundException x) {
                return ClassHelper.get().classForName("java.lang." + type, true, loader);
            }
        }
    }

    /**
     * Convert a cast expression into ClassMetaData or throw a
     * JDOFatalUserException if not found. If the cast is to an interface
     * then the array will contain all the possible implementing classes.
     * TODO: Complete the interface support.
     */
    public ClassMetaData[] resolveCastType(String type) {
        ClassMetaData c = jmd.getClassMetaData(type);
        if (c == null) {
            if (imports != null) {
                int len = imports.length;
                for (int i = 0; i < len && c == null; i++) {
                    ImportNode im = imports[i];
                    if (im.all) {
                        c = jmd.getClassMetaData(im.name + type);
                    } else if (type.equals(im.getClassName())) {
                        c = jmd.getClassMetaData(im.name);
                    }
                }
            }
            if (c == null) {
                c = jmd.getClassMetaData(cmd.packageNameWithDot + type);
            }
        }
        if (c == null) {
            throw BindingSupportImpl.getInstance().runtime("No persistent class found for cast expression: (" + type +
                    "): check the query imports");
        }
        return new ClassMetaData[]{c};
    }

    /**
     * Get the parser ready to parse s.
     */
    private void initParser(String s) {
        StringReader reader = new StringReader(s);
        if (stream == null) {
            stream = new JavaCharStream(reader);
        } else {
            stream.ReInit(reader);
        }
        if (tokenManager == null) {
            tokenManager = new JDOQLParserTokenManager(stream);
        } else {
            tokenManager.ReInit(stream);
        }
        if (parser == null) {
            parser = new JDOQLParser(tokenManager);
        } else {
            parser.ReInit(tokenManager);
        }
    }

    private void dump(Node[] a) {
        for (int i = 0; i < a.length; i++) {
            Debug.OUT.print("[" + i + "] ");
            a[i].dump(" ");
        }
    }

    /**
     * This will return the CmdBitSet filled with the classMetadata's
     * that this query depends on.
     */
    public CmdBitSet getCmds() {
        final CmdBitSet bitSet = new CmdBitSet(jmd);
        bitSet.addPlus(cmd);
        if (filter != null) {
            doCmdDependency(filter, bitSet);
        }
        if (orders != null) {
            for (int i = 0; i < orders.length; i++) {
                doCmdDependency(orders[i], bitSet);
            }
        }
        return bitSet;
    }

    private void doCmdDependency(Node node, CmdBitSet bitSet) {
        if (node == null) return;
        node.updateEvictionDependency(bitSet);
        for (Node n = node.childList; n != null; n = n.next) {
            doCmdDependency(n, bitSet);
        }
    }

}
