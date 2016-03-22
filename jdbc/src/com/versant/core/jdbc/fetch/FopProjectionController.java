
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
package com.versant.core.jdbc.fetch;

import com.versant.core.server.StateContainer;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.ProjectionQueryDecoder;
import com.versant.core.jdbc.query.JDOQLNodeToSqlExp;
import com.versant.core.jdo.query.ResultNode;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.OID;

import java.sql.SQLException;

/**
 * This fetchOp is responsible for fetching user projection queries.
 */
public class FopProjectionController extends FetchOp {
    private final ProjectionQueryDecoder decoder;
    private final FetchOpData src;
    private final JDOQLNodeToSqlExp visitor;
    private final ResultNode resultNode;
    private final ClassMetaData cmd;
    private final FetchGroup fg;
    private final boolean includeSubClasses;
    private final Data data;
    private FopGetOID oidFop;

    public FopProjectionController(FetchSpec spec, FetchOpData src,
            ProjectionQueryDecoder queryDecoder, ClassMetaData cmd,
            FetchGroup fg, boolean includeSubClasses, JDOQLNodeToSqlExp visitor,
            ResultNode resultNode) {
        super(spec);
        this.decoder = queryDecoder;
        this.src = src;
        this.cmd = cmd;
        this.fg = fg;
        this.includeSubClasses = includeSubClasses;
        this.data = new Data(src);
        this.visitor = visitor;
        this.resultNode = resultNode;
    }

    public SqlExp init(SelectExp root) {
        if (decoder.containsThis()) {
            oidFop = new FopGetOID(spec,
                                src, cmd, root);
            spec.addFetchOp(oidFop, false);

//            FopGetState stateFop = new FopGetState(spec,
//                    oidFop.getOutputData(), fg, includeSubClasses,
//                    root, 0, cmd);
//            spec.addFetchOp(stateFop, false);

            if (!decoder.isContainsThisOnly()) {
                FopGetProjection projFop = new FopGetProjection(spec, decoder,
                        visitor, resultNode, data);
                spec.addFetchOp(projFop, false);
            }
        } else {
            FopGetProjection projFop = new FopGetProjection(spec, decoder,
                    visitor, resultNode, data);
            spec.addFetchOp(projFop, false);
        }
        return null;
    }

    public void fetch(FetchResult fetchResult,
            StateContainer stateContainer) throws SQLException {
        if (decoder.isContainsThisOnly()) return;
        Object[] result = new Object[decoder.getResultTypeArray().length];
        data.setProjectionResultArray(fetchResult, result);
    }

    public String getDescription() {
        return cmd.qname + data.getDescription();
    }

    public FetchOpData getOutputData() {
        return null;
    }

    public Object getResult(FetchResult fetchResult) {
        if (decoder.isContainsThisOnly()) {
            //will return the oid
            return fetchResult.getData(oidFop);
        } else {
            Object[] resultArray = data.getProjectionResultArray(fetchResult);
//            if (decoder.containsThis()) {
//                resultArray[decoder.getFirstThisIndex()] = fetchResult.getData(oidFop);
//            }

//            System.out.println("\n\n\n AFTER resultArray");
//            for (int i = 0; i < resultArray.length; i++) {
//                Object o = resultArray[i];
//                System.out.println("o = " + o);
//            }
//            System.out.println("\n");

            if (resultArray.length == 1) {
                return resultArray[0];
            } else {
                return resultArray;
            }
        }
    }

    public class Data extends FetchOpDataProxy {

        public Data(FetchOpData src) {
            super(src);
        }

        public void setProjectionResultArray(FetchResult fetchResult, Object[] val) {
            fetchResult.setData(FopProjectionController.this, val);
        }

        public Object[] getProjectionResultArray(FetchResult fetchResult) {
            return (Object[]) fetchResult.getData(FopProjectionController.this);
        }

        public OID getOID(FetchResult fetchResult) {
            return (OID) fetchResult.getData(oidFop);
        }

        public String getDescription() {
            return " [" + getIndex() + "]";
        }
    }
}
