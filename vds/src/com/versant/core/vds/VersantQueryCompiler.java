
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

import com.versant.core.jdo.QueryDetails;
import com.versant.core.common.CmdBitSet;
import com.versant.core.metadata.*;
import com.versant.core.util.*;
import com.versant.core.logging.LogEventStore;
import com.versant.core.jdo.query.*;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.odbms.query.OrderByExpression;

/**
 * This will compile a QueryImp into a VersantCompiledQuery for execution
 * by a VersantDataStore. This is not thread safe. An instance may only
 * be compiling one query at a time. These are designed to be pooled.
 *
 * @see VdsCompiledQuery
 * @see JdbcDataStore#compile
 * @see #reinit
 */
public class VersantQueryCompiler {

    private ModelMetaData jmd;
    private LogEventStore pes;
  	private VdsConfig config;
    // these fields must be set to null in reinit
    private ClassMetaData cmd;
    private ParamNode[] params;
    private OrderNode[] orders;
    private UnaryNode filter;
    private ResultNode resultNode;
    private GroupingNode groupingNode;
    private QueryParser qParser;

    public VersantQueryCompiler (LogEventStore pes, VdsConfig config,
            ModelMetaData jmd) {
        this.pes = pes;
        this.config = config;
        this.jmd = jmd;
    }

    /**
     * Get this compiler ready to compile more queries. This is called before
     * it is returned to the pool.
     */
    public void reinit() {
        cmd = null;
        params = null;
        orders = null;
        filter = null;
        qParser = null;
        resultNode = null;
        groupingNode = null;
    }

    public QueryParser getQParser() {
        return qParser;
    }

    /**
     * Compile a QueryImp into a JdbcCompiledQuery ready to run.
     */
    public VdsCompiledQuery compile(QueryDetails q) {
//      assert jmd != null;
//      assert q != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(jmd != null,
                    "JDOMetaData is null");
            Debug.assertInternal(q != null,
                    "QueryDetails is null");
        }
        cmd = jmd.getClassMetaData(q.getCandidateClass());
//        assert cmd != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(cmd != null,
                    "ClassMetaData is null");
        }
        return compileImp(q);
    }


    private VdsCompiledQuery compileImp(QueryDetails q) {
        VdsCompiledQuery cq = new VdsCompiledQuery(cmd, q);
        qParser = cq.queryParser = new QueryParser(jmd);

        try {
            qParser.parse(q);
            params = qParser.getParams();
            orders = qParser.getOrders();
            filter = qParser.getFilter();
            resultNode = qParser.getResultNode();
            groupingNode = qParser.getGroupingNode();
        } catch (Exception e) {
        	if( BindingSupportImpl.getInstance().isOwnException(e) )
        	{
        		throw (RuntimeException)e;	
        	}
        	else
        	{
            	throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
            }
        } catch (TokenMgrError e) {
            throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
        }

        CmdBitSet bits = qParser.getCmds();
        
        Converter converter = new Converter(cmd, qParser, params, getNamingPolicy());

        if (resultNode != null || groupingNode != null) {
        	throw BindingSupportImpl.getInstance().exception("setResult/setGrouping supported by VDS.");
        }

        if (filter != null) {
        	Object vform = new TreeWalker().walk(filter, converter);
        	cq.setExpr(vform);
        	cq.setParams(params);
        }

        if (orders != null) {
        	OrderByExpression[] vorderby = new OrderByExpression[orders.length]; 
        	for (int i = 0; i < orders.length; i++) {
        		vorderby[i] = (OrderByExpression) new TreeWalker().walk(orders[i], converter);
        	}
            cq.setOrderBy(vorderby);
        }

        if (converter.extraClasses.size() > 0) {
          Class[] eclasses = new Class[converter.extraClasses.size()];
          converter.extraClasses.toArray(eclasses);
          int[] ebits = jmd.convertToClassIndexes(eclasses, false);
          for (int i = ebits.length - 1; i >= 0; i--) {
              bits.add(jmd.classes[ebits[i]]);
          }
        }

        int[] a = q.getExtraEvictClasses();
        if (a != null) {
            for (int i = a.length - 1; i >= 0; i--) {
                bits.add(jmd.classes[a[i]]);
            }
        }
        cq.setFilterClsIndexs(bits.toArray());
        cq.setCacheable(bits.isCacheble() && !q.isRandomAccess());
        cq.setEvictionClassBits(bits.getBits());

        return cq;
    }

	private NamingPolicy getNamingPolicy() {
		 //  create and set the naming policy
		 if (config.namingPolicy != null) {
			 String cname = config.namingPolicy;
			 if (BriefNamingPolicy.ALIAS.equalsIgnoreCase(cname)) {
				 cname = BriefNamingPolicy.class.getName();
			 } else if (IdenticalNamingPolicy.ALIAS.equalsIgnoreCase(cname)) {
				 cname = IdenticalNamingPolicy.class.getName();
			 }
			 return (NamingPolicy)BeanUtils.newInstance(
					 cname, Thread.currentThread().getContextClassLoader(), NamingPolicy.class);
		 } else {
			 return new BriefNamingPolicy();
		 }
	
	}
}
