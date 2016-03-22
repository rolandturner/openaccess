
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
package com.versant.core.jdbc.metadata;

import com.versant.core.common.State;
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.common.OID;
import com.versant.core.common.*;
import com.versant.core.server.StateContainer;
import com.versant.core.jdbc.*;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.util.CharBuf;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.versant.core.common.BindingSupportImpl;

/**
 * Base class for field that are Collections, Maps or arrays.
 */
public abstract class JdbcCollectionField extends JdbcField {

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final OID[] EMPTY_OID_ARRAY = new OID[0];
    public static final Object[] PRE_GEN_EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * The column(s) holding primary key of the main table for our class.
     * These could be in the link table or in the value PC class table.
     */
    public JdbcColumn[] ourPkColumns;
    /**
     * The column holding the sequence number for each value (null if the
     * collection is not ordered).
     */
    public JdbcColumn sequenceColumn;

    public void dump(PrintStream out, String indent) {
        super.dump(out, indent);
        String is = indent + "  ";
        if (ourPkColumns == null) {
            out.println(is + "ourPkColumns null");
        } else {
            for (int i = 0; i < ourPkColumns.length; i++) {
                out.println(is + "ourPkColumns[" + i + "] " + ourPkColumns[i]);
            }
        }
        out.println(is + "sequenceColumn " + sequenceColumn);
    }

    public final void prepareFetch(FetchSpec spec, FetchOptions options, SelectExp se,
                                   int refLevel, FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {
        if (options.isUseParallelQueries()) {
            prepareParFetch(spec, options, se, refLevel, src, fgField, ffPath);
        } else {
            prepareSingleFetch(spec, options, se, refLevel, src, fgField);
        }
    }

    protected void prepareSingleFetch(FetchSpec spec, FetchOptions options,
            SelectExp se, int refLevel, FetchOpData src, FetchGroupField fgField) {
        //to be implemeted by subclasses
        throw BindingSupportImpl.getInstance().internal(
                "To be implemented by subclasses");
    }

    protected void prepareParFetch(FetchSpec spec, FetchOptions options,
                                   SelectExp se, int refLevel, FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {
        //to be implemeted by subclasses
        throw BindingSupportImpl.getInstance().internal(
                "To be implemented by subclasses");
    }

    /**
     * Fetch a query in a separate query to the main query. This results in a query for each collection being fetched.
     */
    public void fetchSingleFetch(FetchResult fetchResult,
                                 StateContainer stateContainer, Object[] params, State state, SelectExp owningSe, int refLevel, FetchOpData src, FetchGroupField fgField) {
        throw BindingSupportImpl.getInstance().internal("Not implemented");
    }

    /**
     * Complete the meta data for this collection. This must use info
     * already supplied in the .jdo file and add anything else needed.
     */
    public void processMetaData(JdoElement context, JdbcMetaDataBuilder mdb,
            boolean quiet) {
        fmd.primaryField = false;
        fmd.secondaryField = true;

        // make sure there are no jdbc-XXX extensions on the field element
        // itself - they should be nested in the collection, map or array
        // element
        JdoExtension[] a = fmd.jdoField == null ? null : fmd.jdoField.extensions;
        if (a != null) {
            for (int i = 0; i < a.length; i++) {
                if (a[i].isJdbc()) {
                    fmd.addError(BindingSupportImpl.getInstance().runtime("Unexpected extension: " + a[i] + "\n" +
                            context.getContext()), quiet);
                }
            }
        }
    }

    public void deletePass2Block(DeletePacket graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batch)
            throws SQLException {
        //ignore
    }

    public static String getRsRowNo(ResultSet rs) {
        if (rs == null) return "rs.isNull";
        try {
            return "" + rs.getRow();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * This is called for the first logical row of a crossjoin resultset.
     */
    protected boolean updateForFirstRow(FetchInfo fetchInfo,
                                            boolean mustBreak, ResultSet rs,
                                            int colIndex, OID oid) throws SQLException {
        fetchInfo.onNextRow = false;
        //if this keyOid was already checked in the previous round then
        //it must not be read again
        if (fetchInfo.breakStatus == FetchInfo.BREAK_STATUS_NULL) {
            //the oid was read in previous round and was null
            mustBreak = true;
            fetchInfo.breakStatus = FetchInfo.BREAK_STATUS_DEFAULT;
        } else if (fetchInfo.breakStatus == FetchInfo.BREAK_STATUS_DEFAULT) {
            //the oid was not read in previous round so read it now
            mustBreak = checkKeyOid(rs, colIndex, fetchInfo, mustBreak, oid);
        }
        return mustBreak;
    }

    protected boolean checkKeyOid(ResultSet rs, int colIndex,
                                      FetchInfo fetchInfo, boolean mustBreak,
                                      OID oid) throws SQLException {
        OID keyOid;
        keyOid = fmd.classMetaData.createOID(false);

        if (!((JdbcOID)keyOid).copyKeyFields(rs, colIndex)) {
            fetchInfo.breakStatus = FetchInfo.BREAK_STATUS_NULL;
            fetchInfo.nextOid = null;
            mustBreak = true;
        }

        if (!oid.equals(keyOid)) {
            fetchInfo.breakStatus = FetchInfo.BREAK_STATUS_VALID;
            fetchInfo.nextOid = keyOid;
            fetchInfo.onNextRow = true;
            mustBreak = true;
        }
        return mustBreak;
    }

    /**
     * Create a empty collection data structure in the state. This is used for parallel
     * query processing.
     */
    public abstract void fillStateWithEmpty(State state);

    public abstract boolean isFilledWithEmpty(State state);

//    /**
//     * Fetch the values for this field.
//     */
//    public abstract int fetch(JdbcStorageManager sm, OID oid, State state,
//            FetchGroupField field, boolean forUpdate,
//            StateContainer container, boolean fetchPass2Fields,
//            ColFieldHolder colFHolder)
//            throws SQLException;

//    public abstract int fetchFrom(ResultSet rs, OID oid,
//            State state,
//            FetchGroupField field, boolean forUpdate,
//            StateContainer container, boolean fetchPass2Fields, int colIndex,
//            FetchInfo fetchInfo, JdbcStorageManager sm)
//            throws SQLException;

//    /**
//     * Fetch the values for this field using parallel query processing.
//     */
//    public abstract int fetchWithFilter(JdbcStorageManager sm,
//            StateContainer oidStates,
//            FetchGroupField field, ResultSet rs, boolean forUpdate,
//            OID oidToCheckOn,
//            OID[] lastReadStateOID, ClassMetaData cmd,
//            ColFieldHolder colFHolder)
//            throws SQLException;

//    /**
//     * Create the select exp for this collection.
//     */
//    public abstract SelectExp getSelectFilterExp(JdbcStorageManager sm,
//            FetchGroupField field, ColFieldHolder colFHolder);

//    public abstract SelectExp getSelectExpFrom(JdbcStorageManager sm,
//            SelectExp joinToExp, FetchGroupField field, FgDs owningFgDs);

//    /**
//     * Create a join select exp through this.
//     */
//    public abstract SelectExp getSelectFilterJoinExp(boolean value,
//            SelectExp lhSe,
//            SelectExp rootSe,
//            boolean addRootJoin);

    protected void cleanup(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException x) {
                // ignore
            }
        }
    }

    protected void cleanup(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException x) {
                // ignore
            }
        }
    }

    public void fetchParFetch(FetchResult fetchResult,
                              StateContainer stateContainer, Object[] params,
                              FetchSpec fetchSpec, SelectExp owningSe, FetchOpData src,
                              FetchGroupField fgField, int refLevel,
                              ParCollectionFetchResult parColFetchResult, FetchOp fetchOp,
                              FopParCollectionFetch fopParCollFetch, FetchFieldPath ffPath) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public SelectExp addOneToManyJoin(FetchSpec spec, FetchOpData src,
                                      SelectExp joinToExp, FetchGroupField fgField, int refLevel, FetchSpec owningSpec,
                                      FetchFieldPath ffPath) {
        return null;
    }

    /**
     * Fetch the collection with a oneToMany join from the main query.
     */
    public void fetchOneToManyParCollection(FetchSpec subSpec,
                 FetchResult subFetchResult, State state, Object[] params,
                 StateContainer container, FetchResult owningFetchResult) {
    }
}

