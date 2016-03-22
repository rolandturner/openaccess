
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

import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.*;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.jdbc.query.JdbcJDOQLCompiler;
import com.versant.core.server.*;
import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.*;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.common.Utils;
import com.versant.core.util.CharBuf;
import com.versant.core.common.*;
import com.versant.core.jdo.query.Node;
import com.versant.core.jdo.query.OrderNode;
import com.versant.core.common.Debug;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.sql.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a Map field stored using a link table.
 */
public class JdbcMapField extends JdbcLinkCollectionField {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final OID[] EMPTY_OID_ARRAY = new OID[0];

    /**
     * The column(s) holding the keys. This array will have length 1 unless
     * the keys are of a PC class with a composite primary key.
     */
    public JdbcColumn keyColumns[];
    /**
     * Should the keys be considered a dependent? If they are
     * they will be deleted if removed from the map or if our
     * instance is deleted. This only makes sense if the keys are instances
     * of a PC class.
     */
    public boolean keysDependent;
    /**
     * Are the keys OID's?
     */
    public boolean keysAreOIDs;
    /**
     * Should a join be done to pick up the fields for keys when they are
     * read? This only makes sense if the key is a PC class.
     */
    public int useKeyJoin;

    private transient boolean createKeyConstraint;
    private transient String keyConstraintName;
    private MapEntries preGenEntries;

    public void dump(PrintStream out, String indent) {
        super.dump(out, indent);
        String is = indent + "  ";
        if (keyColumns == null) {
            out.println(is + "keyColumns null");
        } else {
            for (int i = 0; i < keyColumns.length; i++) {
                out.println(is + "keyColumns[" + i + "] " + keyColumns[i]);
            }
        }
        out.println(is + "keysDependent " + keysDependent);
        out.println(is + "keysAreOIDs " + keysAreOIDs);
        out.println(is + "useKeyJoin " + toUseJoinString(useKeyJoin));
    }

    /**
     * Get the useKeyJoin value for this field. This is only valid for maps.
     */
    public int getUseKeyJoin() {
        return useKeyJoin;
    }

    /**
     * Complete the meta data for this collection. This must use info
     * already supplied in the .jdo file and add anything else needed.
     */
    public void processMetaData(JdoElement context, JdbcMetaDataBuilder mdb,
            boolean quiet) {
        keysAreOIDs = fmd.keyTypeMetaData != null;
        if (keysAreOIDs) {
            useKeyJoin = JdbcField.USE_JOIN_INNER;
        } else {
            useKeyJoin = JdbcField.USE_JOIN_NO;
        }
        super.processMetaData(context, mdb, quiet);
        preGenEntries = new MapEntries(
                keysAreOIDs ? EMPTY_OID_ARRAY: EMPTY_OBJECT_ARRAY,
                valuesAreOIDs ? EMPTY_OID_ARRAY : EMPTY_OBJECT_ARRAY);
    }

    /**
     * Set the PK of the link table.
     */
    protected void createLinkTablePK() {
        linkTable.setPk(JdbcColumn.concat(ourPkColumns, keyColumns));
    }

    /**
     * Complete the key and value column related meta data.
     */
    protected void completeKeyAndValueColumnMetaData(JdbcClass jdbcClass,
            ArrayList cols,
            JdoElement context, JdoExtension[] linkNested,
            JdbcMetaDataBuilder mdb, boolean quiet) {

        // create the key column(s)
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.JDBC_KEY,
                linkNested);
        if (fmd.keyTypeMetaData != null) {  // values are OIDs
            JdbcRefMetaDataBuilder rdb = new JdbcRefMetaDataBuilder(
                    fmd.classMetaData,
                    mdb, fmd.keyTypeMetaData, context,
                    JdbcMetaDataBuilder.KEY_FIELDNAME,
                    ext == null ? null : ext.nested, quiet);
            keyColumns = rdb.getCols();
            cols.addAll(rdb.getColsList());
            createKeyConstraint = !rdb.isDoNotCreateConstraint();
            keyConstraintName = rdb.getConstraintName();
        } else {
            if (fmd.keyType == /*CHFC*/Object.class/*RIGHTPAR*/) {
                throw BindingSupportImpl.getInstance().runtime("You must specify the key-type for maps\n" +
                        fmd + "\n" + context.getContext());
            }
            JdbcColumn kc = mdb.createColumn(ext == null ? null : ext.nested,
                    JdbcMetaDataBuilder.KEY_FIELDNAME, fmd.keyType);
            keyColumns = new JdbcColumn[]{kc};
            cols.add(kc);
        }

        int n = keyColumns.length;
        for (int i = 0; i < n; i++) keyColumns[i].setNulls(false);

        super.completeKeyAndValueColumnMetaData(jdbcClass, cols, context,
                linkNested, mdb, quiet);
    }

    public SelectExp addParColJoin(SelectExp joinTo, boolean keyJoin) {
        //create a join to the linktable
        SelectExp linkTableSe = new SelectExp();
        linkTableSe.table = linkTable;
        linkTableSe.appendOrderByForColumns(ourPkColumns);
        joinTo.addJoin(joinTo.table.pk, ourPkColumns, linkTableSe);

        if (keyJoin) {
            SelectExp se = new SelectExp();
            JdbcClass keyJdbcClass = (JdbcClass)fmd.keyTypeMetaData.storeClass;
            se.table = keyJdbcClass.table;
            linkTableSe.addJoin(keyColumns, se.table.pk, se);
            return se;
        } else {
            SelectExp se = new SelectExp();
            JdbcClass valueJdbcClass = (JdbcClass)fmd.elementTypeMetaData.storeClass;
            se.table = valueJdbcClass.table;
            linkTableSe.addJoin(valueColumns, se.table.pk, se);
            return se;
        }
    }

    /**
     * Name the key and value columns.
     */
    protected void nameKeyAndValueColumns(JdbcNameGenerator namegen,
            String linkTableNameForNamegen) {
        // name the keycolumn(s)
        if (keysAreOIDs) {
            String[] keyPkNames = JdbcColumn.getColumnNames(
                    ((JdbcClass)fmd.keyTypeMetaData.storeClass).table.pk);
            String[] linkKeyRefNames = JdbcColumn.getColumnNames(keyColumns);
            namegen.generateLinkTableValueRefNames(linkTable.name,
                    keyPkNames, fmd.keyType.getName(), linkKeyRefNames, true);
            JdbcColumn.setColumnNames(keyColumns, linkKeyRefNames);
        } else {
            JdbcColumn c = keyColumns[0];
            if (c.name == null) {
                c.name = namegen.generateLinkTableValueName(linkTable.name,
                        fmd.keyType, true);
            }
        }

        super.nameKeyAndValueColumns(namegen, linkTableNameForNamegen);
    }

    /**
     * Name our linkTable.
     */
    protected void nameLinkTable(JdbcNameGenerator namegen,
            JdbcClass jdbcClass) {
        linkTable.name = namegen.generateLinkTableName(jdbcClass.table.name,
                fmd.name, null);
    }

    /**
     * Create all the constraints for our link table.
     */
    protected List createConstraints(boolean pkConstraint,
            String pkConstraintName) {
        List constraints = super.createConstraints(pkConstraint,
                pkConstraintName);

        if (createKeyConstraint && keysAreOIDs
                && fmd.keyTypeMetaData.storeClass != null) {
            JdbcConstraint keyCon = new JdbcConstraint();
            keyCon.src = linkTable;
            keyCon.srcCols = keyColumns;
            keyCon.dest = ((JdbcClass)fmd.keyTypeMetaData.storeClass).table;
            keyCon.name = keyConstraintName;
            constraints.add(keyCon);
        }

        return constraints;
    }

    /**
     * Persist pass 2 field for a block of graph entries all with
     * the same class. The same ps'es can be used for all entries in the block.
     */
    public void persistPass2Block(PersistGraph graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batchInserts,
            boolean batchUpdates) throws SQLException {
        PreparedStatement psdel = null;
        PreparedStatement psdelAll = null;
        PreparedStatement psins = null;
        int delCount = 0;
        try {
            String psdelSql = null;
            String psdelAllSql = null;
            String psinsSql = null;
            for (int pos = blockStart; pos < blockEnd; pos++) {
                State ns = graph.getNewState(pos);
                if (!ns.containsField(stateFieldNo)) continue;

                MapDiff diff = (MapDiff)ns.getInternalObjectField(stateFieldNo);

                OID oid = graph.getOID(pos);

                if (diff == null || diff.status == CollectionDiff.STATUS_NEW) {
                    if (!oid.isNew()) {
                        if (psdelAll == null) {
                            psdelAllSql = getDeleteAllLinkTableRowsSql(s);
                            psdelAll = con.prepareStatement(psdelAllSql);
                        }
                        ((JdbcOID)oid).setParams(psdelAll, 1);
                        if (batchUpdates) {
                            psdelAll.addBatch();
                        } else {
                            try {
                                psdelAll.execute();
                            } catch (Exception e) {
								throw mapException(e,
                                        "Delete all link table rows failed: " +
                                        JdbcUtils.toString(e) + "\n" +
                                        "Field: " + fmd.getQName() + "\n" +
                                        "Instance: " + oid.toSString() + "\n" +
                                        JdbcUtils.getPreparedStatementInfo(
                                                psdelAllSql, psdelAll));
                            }
                        }
                    }
                } else {
                    Object[] deleted = diff.deletedKeys;
                    if (deleted != null && deleted.length > 0) {
                        if (psdel == null) {
                            psdelSql = getDeleteLinkTableRowSql(s);
                            psdel = con.prepareStatement(psdelSql);
                        }
                        deleteMapLinkTableRows(oid, deleted, psdel,
                                batchUpdates, psdelSql);
                        delCount += deleted.length;
                    }
                }

                if (diff != null) {
                    Object[] inserted = diff.insertedKeys;
                    if (inserted != null && inserted.length > 0) {
                        if (psins == null) {
                            psinsSql = getInsertLinkTableRowSql(s);
                            psins = con.prepareStatement(psinsSql);
                        }
                        insertMapLinkTableRows(oid, inserted, diff.insertedValues,
                                psins, batchInserts, psinsSql);
                    }
                }
            }

            if (batchUpdates) {
                execLinkTableBatchDeletes(delCount, psdel, psdelSql,
                        psdelAll, psdelAllSql);
            }
            if (batchInserts && psins != null) {
                execLinkTableBatchInserts(psins, psinsSql);
            }
        } finally {
            cleanup(psdel);
            cleanup(psdelAll);
            cleanup(psins);
        }
    }

    /**
     * Delete keys from an map link table.
     */
    private void deleteMapLinkTableRows(OID oid, Object[] deleted,
            PreparedStatement psdel, boolean batch, String sql)
            throws SQLException {
        if (keysAreOIDs) {
            for (int j = deleted.length - 1; j >= 0; j--) {
                int pp = ((JdbcOID)oid).setParams(psdel, 1);
                ((JdbcOID)deleted[j]).setParams(psdel, pp);
                if (batch) {
                    psdel.addBatch();
                } else {
                    int uc;
                    try {
                        uc = psdel.executeUpdate();
                    } catch (Exception e) {
						throw mapException(e,
                                "Delete map link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Key: " + ((OID)deleted[j]).toSString() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel));
                    }
                    if (uc == 0) {
                        throw BindingSupportImpl.getInstance().concurrentUpdate("Map link table row not found: " +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Key: " + ((OID)deleted[j]).toSString() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel), deleted[j]);
                    }
                }
            }
        } else {
            JdbcColumn kc = keyColumns[0];
            for (int j = deleted.length - 1; j >= 0; j--) {
                int pp = ((JdbcOID)oid).setParams(psdel, 1);
                kc.set(psdel, pp, deleted[j]);
                if (batch) {
                    psdel.addBatch();
                } else {
                    int uc;
                    try {
                        uc = psdel.executeUpdate();
                    } catch (Exception e) {
						throw mapException(e,
                                "Delete map link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Key: " + Utils.toString(deleted[j]) + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel));
                    }
                    if (uc == 0) {
                        throw BindingSupportImpl.getInstance().concurrentUpdate("Map link table row not found: " +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Key: " + Utils.toString(deleted[j]) + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel), oid);
                    }
                }
            }
        }
    }

    /**
     * Insert rows into an map link table.
     */
    private void insertMapLinkTableRows(OID oid, Object[] insertedKeys,
            Object[] insertedValues, PreparedStatement psins,
            boolean batch, String sql)
            throws SQLException {

        JdbcColumn kc = keyColumns[0];
        JdbcColumn vc = valueColumns[0];

        // do the inserts
        int ilen = insertedKeys.length;
        for (int j = 0; j < ilen; j++) {
            int pp = ((JdbcOID)oid).setParams(psins, 1);

            // set key
            if (keysAreOIDs) {
                pp = ((JdbcOID)insertedKeys[j]).setParams(psins, pp);
            } else {
                kc.set(psins, pp++, insertedKeys[j]);
            }

            // set value
            if (valuesAreOIDs) {
                if (insertedValues[j] == null) {
                    JdbcGenericOID.setNullParams(psins, pp, fmd.elementTypeMetaData);
                } else {
                    ((JdbcOID)insertedValues[j]).setParams(psins, pp);
                }
            } else {
                vc.set(psins, pp, insertedValues[j]);
            }

            if (batch) {
                psins.addBatch();
            } else {
                try {
                    psins.execute();
                } catch (Exception e) {
                    String keyStr = keysAreOIDs
                            ? ((OID)insertedKeys[j]).toSString()
                            : Utils.toString(insertedKeys[j]);
                    String valueStr = valuesAreOIDs
                            ? ((OID)insertedValues[j]).toSString()
                            : Utils.toString(insertedValues[j]);
					throw mapException(e,
                            "Insert link table row failed: " +
                            JdbcUtils.toString(e) + "\n" +
                            "Field: " + fmd.getQName() + "\n" +
                            "Instance: " + oid.toSString() + "\n" +
                            "Key: " + keyStr + "\n" +
                            "Value: " + valueStr + "\n" +
                            JdbcUtils.getPreparedStatementInfo(sql, psins));
                }
            }
        }
    }

    protected void prepareParFetch(FetchSpec spec, FetchOptions options, SelectExp owningSe,
                                   int refLevel, FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {
        FopParCollectionFetch fgc2 = new FopParCollectionFetch(spec, src, this, owningSe,
                refLevel, fgField, ffPath);
        spec.addFetchOp(fgc2, false);
    }

    protected FetchSpec createSingleFetch(FetchSpec spec, SelectExp owningSe,
            int refLevel, FetchOpData src, FetchGroupField fgField) {
        SelectExp root = new SelectExp();
        root.table = linkTable;
        root.whereExp = JdbcColumn.createEqualsParamExp(ourPkColumns, root);

        FetchSpec fSpec = new FetchSpec(root,
                ((JdbcClass)fmd.classMetaData.storeClass).sqlDriver, true);
//        FopGetCollection fopGetCollection = new FopGetCollection(spec, fSpec,
//                src, this);
//        spec.addFetchOp(fopGetCollection, false);
        if (keysAreOIDs) {
            if (fgField.jdbcUseKeyJoin != JdbcField.USE_JOIN_NO) {
                SelectExp se = new SelectExp();
                JdbcClass keyJdbcClass = (JdbcClass)fmd.keyTypeMetaData.storeClass;
                se.table = keyJdbcClass.table;
                se.outer = fgField.jdbcUseKeyJoin == JdbcField.USE_JOIN_OUTER;
                root.addJoin(keyColumns, se.table.pk, se);

                FopGetOID fopGetOid = new FopGetOID(fSpec,
                        FetchOpDataMainRS.INSTANCE, keyJdbcClass.cmd,
                        se);
                FopGetState fopGetState = new FopGetState(fSpec,
                        fopGetOid.getOutputData(), fgField.nextKeyFetchGroup,
                        true, se, refLevel, fmd.keyTypeMetaData, null);

                fSpec.addFetchOp(fopGetOid, true);
                fSpec.addFetchOp(fopGetState, false);
            }
        } else {
            fSpec.addFetchOp(new FopGetColumn(fSpec, keyColumns[0],
                    root ), true);
        }

        if (valuesAreOIDs) {
            if (fgField.jdbcUseJoin != JdbcField.USE_JOIN_NO || fmd.ordering != null) {
                SelectExp se = new SelectExp();
                JdbcClass valueJdbcClass = (JdbcClass)fmd.elementTypeMetaData.storeClass;
                se.table = valueJdbcClass.table;
                se.outer = fgField.jdbcUseJoin != JdbcField.USE_JOIN_INNER;

                FopGetOID fopGetOid = new FopGetOID(fSpec,
                        FetchOpDataMainRS.INSTANCE, valueJdbcClass.cmd, se);
                FopGetState fopGetState = new FopGetState(fSpec,
                        fopGetOid.getOutputData(), fgField.nextFetchGroup,
                        true, se, refLevel, valueJdbcClass.cmd, null);

                fSpec.addFetchOp(fopGetOid, true);
                fSpec.addFetchOp(fopGetState, true);

                root.addJoin(valueColumns, se.table.pk, se);

                if (fmd.ordering != null) {
                    se.addOrderBy(fmd.ordering, false);
                    root.orderByList = se.orderByList;
                    se.orderByList = null;
                }
            }
        } else {
            FopGetColumn fopGetColumn = new FopGetColumn(fSpec,
                    valueColumns[0], root );
            fSpec.addFetchOp(fopGetColumn, true);

            if (fmd.ordering != null) {
                // the ordering can only be 'this ascending' or 'this descending'
                boolean desc = fmd.ordering[0].order == OrderNode.ORDER_DESCENDING;
                root.orderByList = new OrderExp(valueColumns[0].toSqlExp(root),
                        desc);
                // there will be only one entry in valueColumns as this is
                // not a collection of PC instances
            }
        }

        if (fmd.ordered) {
            if (fmd.ordering != null) {
                throw BindingSupportImpl.getInstance().internal(
                        "ordered == true && ordering != null, " + fmd.getTypeQName());
            }
            root.orderByList = linkTable.createOrderByPKList(root);
        }
        fSpec.finish(1);
        return fSpec;
    }

    protected void prepareSingleFetch(FetchSpec spec, FetchOptions options,
            SelectExp owningSe, int refLevel, FetchOpData src,
            FetchGroupField fgField) {
        FopGetCollection fopGetCollection = new FopGetCollection(spec, src,
                this, owningSe, refLevel, fgField);
        spec.addFetchOp(fopGetCollection, false);
    }

    public void fetchSingleFetch(FetchResult fetchResult,
            StateContainer stateContainer, Object[] params, State state,
            SelectExp owningSe, int refLevel, FetchOpData src,
            FetchGroupField fgField) {
        FetchSpec subSpec = createSingleFetch(fetchResult.getFetchSpec(),
                owningSe, refLevel, src, fgField);

        FetchResult fResultImp = subSpec.createFetchResult(
                fetchResult.getStorageManager(), fetchResult.getConnection(),
                params, fetchResult.isForUpdate(), fetchResult.isForCount(), 0, 0,
                0, false, 0);
        try {
            if (fResultImp.hasNext()) {
                ArrayList keys = new ArrayList();
                ArrayList values = new ArrayList();
                while(fResultImp.hasNext()) {
                    Object[] row = (Object[])fResultImp.next(stateContainer);
                    keys.add(row[0]);
                    values.add(row[1]);
                }
                fResultImp.close();
                updateStateFilter(keys, values, state);
                //fetch the pass2 fields
                subSpec.fetchPass2(fResultImp, params, stateContainer);
            }
        } finally {
            fResultImp.close();
        }
    }

    public void fetchParFetch(FetchResult fetchResult,
                              StateContainer stateContainer, Object[] params, FetchSpec fetchSpec,
                              SelectExp owningSe, FetchOpData src, FetchGroupField fgField,
                              int refLevel, ParCollectionFetchResult parColFetchResult,
                              FetchOp fetchOp, FopParCollectionFetch fopParCollFetch, FetchFieldPath ffPath) {

        OID prevOID = null;
        ArrayList keys = new ArrayList();
        ArrayList values = new ArrayList();

        FetchResult fResultImp = null;
        boolean toContinue = false;
        boolean updated = false;
        if (parColFetchResult == null) {
            FetchSpec subSpec = createCollectionFetchSpec(fetchSpec, owningSe, src,
                            fgField, refLevel, fopParCollFetch);
            if (Debug.DEBUG) subSpec.printPlan(System.out, "  ");

            fResultImp = subSpec.createFetchResult(
                    fetchResult.getStorageManager(), fetchResult.getConnection(),
                    params, fetchResult.isForUpdate(), fetchResult.isForCount(),
                    0, 0, 0, false, 0);

            parColFetchResult = new ParCollectionFetchResult(this, fResultImp);
            fetchResult.setPass2Data(fetchOp, parColFetchResult);

            if (!fResultImp.hasNext()) {
                fResultImp.close();
            }
        } else {
            fResultImp = parColFetchResult.getFetchResult();
            //check if the result is closed
            if (fResultImp.isClosed()) return;
        }

        if (fResultImp.hasNext()) {
            toContinue = true;
        }

        //advance to the first valid row
        while(fResultImp.hasNext()) {
            OID currentOIDFetched = (OID) fResultImp.getDiscriminator(stateContainer);
            if (stateContainer.get(currentOIDFetched) == null) {
                fResultImp.skip(1);
            } else {
                break;
            }
        }

        while(fResultImp.hasNext()) {
            /**
             * Read the first fetchOp
             */
            OID owner = (OID) fResultImp.getDiscriminator(stateContainer);
            if (stateContainer.get(owner) == null) {
                if (keys.size() == 0) {
                    throw BindingSupportImpl.getInstance().internal(
                            "There was no items added to the collection.");
                }
                break;
            }

            Object[] row = (Object[])fResultImp.next(stateContainer);
            Object key = row[0];
            Object val = row[1];

            if (prevOID == null) {
                prevOID = owner;
            } else if (!prevOID.equals(owner)) {
                //owner changed
                State state = stateContainer.get(prevOID);
                if (state == null) {
                    throw BindingSupportImpl.getInstance().internal("State not found");
                }
                if (updateStateFilter(keys, values, state)) updated = true;
                prevOID = owner;

                keys.clear();
                values.clear();
            }
            keys.add(key);
            values.add(val);
        }

        if (prevOID != null) {
            State state = stateContainer.get(prevOID);
            if (state == null) {
                throw BindingSupportImpl.getInstance().internal("State not found");
            }
            if (updateStateFilter(keys, values, state)) updated = true;
        }

        //fetch the pass2 fields
        if (updated && toContinue) fResultImp.getFetchSpec().fetchPass2(fResultImp, params, stateContainer);
    }

    private FetchSpec createCollectionFetchSpec(FetchSpec spec, SelectExp owningSe,
            FetchOpData src, FetchGroupField fgField, int refLevel, FopParCollectionFetch fopParCollFetch) {

        /**
         * Fetch by filter. This is a scheme whereby the collection is fetched by
         * using the filter of the original query. This result set must be
         * ordered by the root oids and then by the owning oid.
         *
         * If this is not a query but a fetch operation then we do not need to
         * order by the root oid, only on our owing oid.
         */

        //must create a filter exp as per original query
        SelectExp root = spec.createQueryFilter();
        //follow the join path back to the root exp
        SelectExp joinToSExp = fopParCollFetch.createJoinPath(root);

        //create a join to the linktable
        SelectExp linkTableSe = new SelectExp();
        linkTableSe.table = linkTable;
        linkTableSe.appendOrderByForColumns(ourPkColumns);
        joinToSExp.addJoin(joinToSExp.table.pk, ourPkColumns, linkTableSe);

        FetchSpec fSpec = new FetchSpec(root,
                ((JdbcClass)fmd.classMetaData.storeClass).sqlDriver);
        fSpec.setParentFetchSpec(spec);

        //add the owning oid of this collection.
        FopGetOID stateOIDFop = new FopGetOID(fSpec,
                FetchOpDataMainRS.INSTANCE, fmd.classMetaData,
                linkTableSe, ourPkColumns);
        fSpec.addDiscriminator(new FetchOpDiscriminator(fSpec, stateOIDFop), false);

        if (keysAreOIDs) {
            if (fgField.jdbcUseKeyJoin != JdbcField.USE_JOIN_NO) {
                SelectExp se = new SelectExp();
                JdbcClass keyJdbcClass = (JdbcClass)fmd.keyTypeMetaData.storeClass;
                se.table = keyJdbcClass.table;
                se.outer = fgField.jdbcUseKeyJoin == JdbcField.USE_JOIN_OUTER;
                linkTableSe.addJoin(keyColumns, se.table.pk, se);

                FopGetOID fopGetOid = new FopGetOID(fSpec,
                        FetchOpDataMainRS.INSTANCE, keyJdbcClass.cmd,
                        se);
                FopGetState fopGetState = new FopGetState(fSpec,
                        fopGetOid.getOutputData(), fgField.nextKeyFetchGroup,
                        true, se, refLevel, fmd.keyTypeMetaData, fopParCollFetch.getFetchFieldPathCopy().add(this, true));

                fSpec.addFetchOp(fopGetOid, true);
                fSpec.addFetchOp(fopGetState, false);
            }
        } else {
            fSpec.addFetchOp(new FopGetColumn(fSpec, keyColumns[0],
                    linkTableSe ), true);
        }

        if (valuesAreOIDs) {
            if (fgField.jdbcUseJoin != JdbcField.USE_JOIN_NO || fmd.ordering != null) {
                SelectExp se = new SelectExp();
                JdbcClass valueJdbcClass = (JdbcClass)fmd.elementTypeMetaData.storeClass;
                se.table = valueJdbcClass.table;
                se.outer = fgField.jdbcUseJoin != JdbcField.USE_JOIN_INNER;

                linkTableSe.addJoin(valueColumns, se.table.pk, se);

                FopGetOID fopGetOid = new FopGetOID(fSpec,
                        FetchOpDataMainRS.INSTANCE, valueJdbcClass.cmd,
                        se);
                FopGetState fopGetState = new FopGetState(fSpec,
                        fopGetOid.getOutputData(), fgField.nextFetchGroup,
                        true, se, refLevel, valueJdbcClass.cmd, fopParCollFetch.getFetchFieldPathCopy().add(this, false));

                fSpec.addFetchOp(fopGetOid, true);
                fSpec.addFetchOp(fopGetState, false);



                if (fmd.ordering != null) {
                    se.addOrderBy(fmd.ordering, false);
                    linkTableSe.orderByList = se.orderByList;
                    se.orderByList = null;
                }
            }
        } else {
            fSpec.addFetchOp(new FopGetColumn(fSpec, valueColumns[0],
                    linkTableSe ), true);
        }

        if (fmd.ordered) {
            if (fmd.ordering != null) {
                throw BindingSupportImpl.getInstance().internal(
                        "ordered == true && ordering != null, " + fmd.getTypeQName());
            }
            linkTableSe.orderByList = linkTable.createOrderByPKList(linkTableSe);
        }
        root.appendOrderByExp(linkTableSe.orderByList);
        fSpec.finish(1);
        return fSpec;
    }

    public void fillStateWithEmpty(State state) {
        if (!state.containsField(fmd.stateFieldNo)) {
            state.setInternalObjectField(fmd.stateFieldNo, preGenEntries);
        }
    }

    public boolean isFilledWithEmpty(State state) {
        return (state.getInternalObjectField(fmd.stateFieldNo) == preGenEntries);
    }


    /**
     * Update the supplied state with a MapEntries intsance. This method expects
     * the field to be filled with a non-null value.
     */
    private boolean updateStateFilter(ArrayList keys, ArrayList values,
            State state) {
        if (Debug.DEBUG) {
            //TODO START IF(DEBUG)
            if (!state.containsField(fmd.stateFieldNo)) {
                throw BindingSupportImpl.getInstance().internal("The mapField '" + fmd.name
                        + "' is not filled");
            }
            if (state.getInternalObjectField(fmd.stateFieldNo) == null) {
                throw BindingSupportImpl.getInstance().internal("The mapField '" + fmd.name
                        + "' is filled with a null value");
            }
            //TODO END IF(DEBUG)
        }

        /**
         * The field is already resolved for client and this should be a state
         * that was retrieved from the localPMCache
         */
        if ((state.getInternalObjectField(fmd.stateFieldNo) instanceof Map)) {
            return false;
        }
        
        if (state.getInternalObjectField(fmd.stateFieldNo) != preGenEntries) {
            return false;
        }

        final Object[] keysArray;
        final Object[] valuesArray;
        if (keysAreOIDs) {
            keysArray = new OID[keys.size()];
            keys.toArray(keysArray);
        } else {
            keysArray = keys.toArray();
        }
        if (valuesAreOIDs) {
            valuesArray = new OID[values.size()];
            values.toArray(valuesArray);
        } else {
            valuesArray = values.toArray();
        }
        state.setInternalObjectField(fmd.stateFieldNo,
                new MapEntries(keysArray, valuesArray));
        return true;
    }

    /**
     * Convert this field into a containsKey expression.
     */
    public SqlExp toContainsKeySqlExp(JdbcJDOQLCompiler comp, SelectExp root,
            Node args) {
        return toContainsSqlExp(keyColumns, fmd.keyTypeMetaData, comp, root,
                args);
    }

    /**
     * Add our key columns to the row.
     */
    protected void addFetchAllRowsKey(SqlExp e, SelectExp se) {
        for (; e.getNext() != null; e = e.getNext()) ;
        e.setNext(JdbcColumn.toSqlExp(keyColumns, se));
    }

    /**
     * Fetch a row of values for this field. This is used when bulk copying
     * one database to another to read all the rows in a given link table.
     * Return the index of the last column read + 1.
     */
    public int readRow(ResultSet rs, JdbcLinkCollectionField.LinkRow row)
            throws SQLException {
        int pos = super.readRow(rs, row);
        if (keysAreOIDs) {
            OID keyOid = fmd.keyTypeMetaData.createOID(false);
            ((JdbcOID)keyOid).copyKeyFields(rs, pos);
            row.key = keyOid;
            pos += keyColumns.length;
        } else {
            row.key = keyColumns[0].get(rs, pos++);
        }
        return pos;
    }

    /**
     * Set a row of values for this field on a PreparedStatement.
     * This is used when bulk copying one database to another.
     */
    public void writeRow(PreparedStatement ps, LinkRow row)
            throws SQLException {
        row.owner.setCmd(fmd.classMetaData);
        int pos = row.owner.setParams(ps, 1);
        if (keysAreOIDs) {
            JdbcGenericOID k = (JdbcGenericOID)row.key;
            k.setCmd(fmd.classMetaData);
            pos = k.setParams(ps, pos);
        } else {
            keyColumns[0].set(ps, pos++, row.key);
        }
        if (valuesAreOIDs) {
            JdbcGenericOID v = (JdbcGenericOID)row.value;
            v.setCmd(fmd.classMetaData);
            v.setParams(ps, pos);
        } else {
            valueColumns[0].set(ps, pos, row.value);
        }
    }

}
