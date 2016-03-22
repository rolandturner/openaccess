
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

import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.server.StateContainer;

import java.sql.SQLException;
import java.io.PrintStream;

/**
 * This is a fetchOp that acts as a Descriminator. It delegates all calls
 * to a real FetchOp and adds logic to determine if it has been fetched or not.
 */
public class FetchOpDiscriminator extends FetchOp {
    private FetchOp delegateFetchOp;

    public FetchOpDiscriminator(FetchSpec spec, FetchOp del) {
        super(spec);
        this.delegateFetchOp = del;
    }

    public SqlExp init(SelectExp root) {
        return delegateFetchOp.init(root);
    }

    public FetchOpData getOutputData() {
        return delegateFetchOp.getOutputData();
    }

    public void fetch(FetchResult fetchResult, StateContainer stateContainer) throws SQLException {
        fetchResult.fetchCachedDiscr(this, stateContainer);
    }

    public void fetchImp(FetchResult fetchResult, StateContainer stateContainer) throws SQLException {
        delegateFetchOp.fetch(fetchResult, stateContainer);
    }

    public void setFirstColIndex(int firstColIndex) {
        delegateFetchOp.setFirstColIndex(firstColIndex);
    }

    public void offsetFirstColIndex(int offset) {
        delegateFetchOp.offsetFirstColIndex(offset);
    }

    public int getFirstColIndex() {
        return delegateFetchOp.getFirstColIndex();
    }

    public void fetchPass2(FetchResult fetchResult, Object[] params,
            StateContainer stateContainer)
            throws SQLException {
        delegateFetchOp.fetchPass2(fetchResult, params, stateContainer);
    }

    public String getDescription() {
        return delegateFetchOp.getDescription();
    }

    public void printPlan(PrintStream p, String indent) {
        delegateFetchOp.printPlan(p, indent);
    }

    public String getName() {
        return delegateFetchOp.getName();
    }

    public void generateSQL() {
        delegateFetchOp.generateSQL();
    }

    public void fetchResultClosed(FetchResult fetchResult) {
        delegateFetchOp.fetchResultClosed(fetchResult);
    }

    public Object getResult(FetchResult fetchResult) {
        return delegateFetchOp.getResult(fetchResult);
    }

    public int getResultType() {
        return delegateFetchOp.getResultType();
    }

    public FetchSpec getSpec() {
        return delegateFetchOp.getSpec();
    }

    public int getIndex() {
        return delegateFetchOp.getIndex();
    }

    public void setIndex(int index) {
        delegateFetchOp.setIndex(index);
    }

    public RuntimeException notImplemented() {
        return delegateFetchOp.notImplemented();
    }

    public RuntimeException notImplemented(String msg) {
        return delegateFetchOp.notImplemented(msg);
    }

    public RuntimeException internal(String msg) {
        return delegateFetchOp.internal(msg);
    }

    public String toString() {
        return delegateFetchOp.toString();
    }

    public String toStringImp() {
        return delegateFetchOp.toStringImp();
    }


}
