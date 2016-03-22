
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

import com.versant.core.common.Debug;
import com.versant.core.common.*;
import com.versant.core.metadata.*;
import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.jdo.query.*;
import com.versant.core.server.StateContainer;
import com.versant.core.server.PersistGraph;
import com.versant.core.jdbc.*;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.jdbc.query.JdbcJDOQLCompiler;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.util.CharBuf;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A field that is a Collection, Map or array stored in a link table.
 */
public class JdbcLinkCollectionField extends JdbcCollectionField {

    /**
     * If this field is in a many-to-many relationship then this is the
     * other side.
     */
    public JdbcLinkCollectionField inverse;
    /**
     * Is this the read-only half of a many-to-many?
     */
    public boolean readOnly;
    /**
     * The link table.
     */
    public JdbcTable linkTable;
    /**
     * The column(s) holding the values. This array will have length 1
     * unless the values are of a PC class with a composite primary key.
     */
    public JdbcColumn[] valueColumns;
    /**
     * Are the values OID's?
     */
    public boolean valuesAreOIDs;

    private transient boolean createValueConstraint;
    private transient String valueConstraintName;
    private transient boolean doNotCreateTable;

    // Cache for SQL to delete a value from the collection.
    private transient String deleteRowSql;
    // Cache for SQL to delete all values from the collection.
    private transient String deleteAllRowsSql;
    // Cache for SQL to insert a value into the collection.
    private transient String insertRowSql;

    // Size of the moving window used to calculate avgRowCount.
    private static final int WINDOW_SIZE = 20;
    // The initial array size is avgRowCount multiplied by this.
    private static final float FUDGE_FACTOR = 1.5f;
    // This is the minimum initial array size.
    private static final int MIN_LEN = 4;

    // Total number of fetches done so far.
    protected transient int fetchCount;
    // Average number of rows retrieved with each fetch.
    protected transient float avgRowCount;
    // Total number of times the values array for a fetch has had to be
    // expanded (i.e. we guessed the size incorrectly. This is the number
    // of extra objects and array copies we have had to do.
    protected transient int expansionCount;
    private Object emptyArray;


    /**
     * Add all tables that belong to this field to the set.
     */
    public void getTables(HashSet tables) {
        if (!readOnly && !doNotCreateTable && linkTable != null) {
            tables.add(linkTable);
        }
    }

    public void dump(PrintStream out, String indent) {
        super.dump(out, indent);
        String is = indent + "  ";
        out.println(is + "inverse " + inverse);
        out.println(is + "readOnly " + readOnly);
        out.println(is + "valuesAreOIDs " + valuesAreOIDs);
        out.println(is + "linkTable " + linkTable);
        if (valueColumns == null) {
            out.println(is + "valueColumns null");
        } else {
            for (int i = 0; i < valueColumns.length; i++) {
                out.println(is + "valueColumns[" + i + "] " + valueColumns[i]);
            }
        }
    }

    /**
     * Create the fetchSpec to fetch all the collection in this level.
     */
    private FetchSpec createCollectionFetchSpec(FetchSpec spec, SelectExp owningSe,
            FetchGroupField fgField,
            int refLevel, FopParCollectionFetch fopParCollFetch) {
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
        //add orderby for collections owner
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
                        true, se, refLevel, valueJdbcClass.cmd, fopParCollFetch.getFetchFieldPathCopy().add(this));

                fSpec.addFetchOp(fopGetOid, true);
                fSpec.addFetchOp(fopGetState, false);



                if (fmd.ordering != null) {
                    se.addOrderBy(fmd.ordering, false);
                    linkTableSe.appendOrderByExp(se.orderByList);
                    se.orderByList = null;
                }
            }
        } else {
            FopGetColumn fopGetColumn = new FopGetColumn(fSpec, valueColumns[0],
                    linkTableSe);
            fSpec.addFetchOp(fopGetColumn, true);

            if (fmd.ordering != null) {
                // the ordering can only be 'this ascending' or 'this descending'
                boolean desc = fmd.ordering[0].order == OrderNode.ORDER_DESCENDING;
                linkTableSe.appendOrderByExp(new OrderExp(valueColumns[0].toSqlExp(linkTableSe),
                        desc));
                // there will be only one entry in valueColumns as this is
                // not a collection of PC instances
            }
        }
        if (fmd.ordered) {
            if (fmd.ordering != null) {
                throw BindingSupportImpl.getInstance().internal(
                        "ordered == true && ordering != null, " + fmd.getTypeQName());
            }
//            linkTableSe.appendOrderByExp(linkTable.createOrderByPKList(linkTableSe));
            linkTableSe.appendOrderByForColumns(sequenceColumn);
        }
        root.appendOrderByExp(linkTableSe.orderByList);
        fSpec.finish(1);
        return fSpec;
    }

    public void fetchParFetch(FetchResult owningFResult,
                              StateContainer stateContainer, Object[] params,
                              FetchSpec owningFSpec, SelectExp owningSe, FetchOpData src,
                              FetchGroupField fgField, int refLevel,
                              ParCollectionFetchResult parColFetchResult, FetchOp fetchOp, FopParCollectionFetch fopParCollFetch, FetchFieldPath ffPath) {
        Struct struct = new Struct();
        struct.init();

        OID prevOIDFetched = null;
        OID currentOIDFetched = null;

        /**
         * Want to re-read the first op result in the result many times
         *
         * Execute query
         * read the discriminator
         * check that the state for the discr oid is already fetched and in the container
         * if not then skip until we find an oid in the container that matches the discriminator
         * This can happen if the user skipped over some results.
         *
         * if we find the owner in the container then read the rows for the collection
         * the next row must be peeked first to check if the discr is the same
         * if not then add the already fetched rows for the collection.
         *
         */

        //if this is the first time then create the infrastructure
        if (parColFetchResult == null) {
            FetchSpec subFSpec = createCollectionFetchSpec(owningFSpec, owningSe,
                    fgField, refLevel, fopParCollFetch);
            if (Debug.DEBUG) subFSpec.printPlan(System.out, "  ");

            FetchResult subFResult = subFSpec.createFetchResult(
                owningFResult.getStorageManager(), owningFResult.getConnection(),
                params, owningFResult.isForUpdate(), owningFResult.isForCount(),
                    0, 0, 0, false, 0);

            parColFetchResult = new ParCollectionFetchResult(this, subFResult);
            owningFResult.setPass2Data(fetchOp, parColFetchResult);
        }

        FetchResult fResultImp = parColFetchResult.getFetchResult();
        final boolean toContinue = fResultImp.hasNext();
        boolean updateDated = false;

        //advance to the first valid row
        while(fResultImp.hasNext()) {
            currentOIDFetched = (OID) fResultImp.getDiscriminator(stateContainer);
            if (stateContainer.get(currentOIDFetched) == null) {
                fResultImp.skip(1);
            } else {
                break;
            }
        }

        while(fResultImp.hasNext()) {
            if (prevOIDFetched == null) {
                //first row
                prevOIDFetched = currentOIDFetched;
            } else if (!currentOIDFetched.equals(prevOIDFetched)) {
                //owner changed
                if (updateStateImp(stateContainer, prevOIDFetched, struct)) updateDated = true;
                prevOIDFetched = currentOIDFetched;
                struct.init();
            }
            struct.add(((Object[])fResultImp.next(stateContainer))[0]);

            if (fResultImp.hasNext()) {
                currentOIDFetched = (OID) fResultImp.getDiscriminator(stateContainer);
                if (stateContainer.get(currentOIDFetched) == null) {
                    if (Debug.DEBUG) {
                        System.out.println("\n\n\nBREAKING ON OWNER NOT IN CONTAINER");
                        System.out.println("currentOIDFetched = " + currentOIDFetched.toStringImp());
                        System.out.println("getRsRowNo(fResultImp.getResultSet()) = " + getRsRowNo(fResultImp.getResultSet()));
                    }
                    //must break/pause here for this batch
                    //if we have'nt done any then there is a bug
                    if (struct.size == 0) {
                        throw BindingSupportImpl.getInstance().internal("There was no items added to the collection.");
                    }
                    break;
                }
            }
        }

        if (prevOIDFetched != null) {
            if (updateStateImp(stateContainer, prevOIDFetched, struct)) updateDated = true;
        }
        //fetch the pass2 fields
        if (updateDated && toContinue) fResultImp.getFetchSpec().fetchPass2(fResultImp, params, stateContainer);
    }



    public void fetchSingleFetch(FetchResult fetchResult, StateContainer stateContainer,
                 Object[] params, State state, SelectExp owningSe, int refLevel,
                 FetchOpData src, FetchGroupField fgField) {

        FetchSpec subSpec = createSingleFetch(fetchResult.getFetchSpec(),
                owningSe, refLevel, src, fgField);

        FetchResult fResultImp = subSpec.createFetchResult(
                fetchResult.getStorageManager(), fetchResult.getConnection(),
                params, fetchResult.isForUpdate(),
                fetchResult.isForCount(), 0, 0, 0, false, 0);

        try {
            if (fResultImp.hasNext()) {
                Struct struct = new Struct();
                struct.init();
                while(fResultImp.hasNext()) {
                    struct.add(((Object[])fResultImp.next(stateContainer))[0]);
                }
                fResultImp.close();
                updateStateFilter(struct, state);
                //fetch the pass2 fields
                subSpec.fetchPass2(fResultImp, params, stateContainer);
            }
        } finally {
            fResultImp.close();
        }
    }

    private boolean updateStateImp(StateContainer stateContainer, OID owner, Struct struct) {
        State state = stateContainer.get(owner);
        if (state == null) {
            throw BindingSupportImpl.getInstance().internal("State not found");
        }
        return updateStateFilter(struct, state);
    }

    protected void prepareParFetch(FetchSpec spec, FetchOptions options, SelectExp
            owningSe, int refLevel,
                      FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {

            FopParCollectionFetch fgc2 = new FopParCollectionFetch(spec, src, this, owningSe,
                    refLevel, fgField, ffPath);
            spec.addFetchOp(fgc2, false);
    }

    protected FetchSpec createSingleFetch(FetchSpec spec, SelectExp owningSe,
                                          int refLevel, FetchOpData src, FetchGroupField fgField) {
        SelectExp root = new SelectExp();
        root.table = linkTable;
        root.selectList = JdbcColumn.toSqlExp(valueColumns, root);
        root.whereExp = JdbcColumn.createEqualsParamExp(ourPkColumns, root);


        FetchSpec fSpec = new FetchSpec(root,
                ((JdbcClass)fmd.classMetaData.storeClass).sqlDriver, true);

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
                    valueColumns[0], root);
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

    public SelectExp addParColJoin(SelectExp joinTo, boolean keyJoin) {
        //create a join to the linktable
        SelectExp linkTableSe = new SelectExp();
        linkTableSe.table = linkTable;
        linkTableSe.appendOrderByForColumns(ourPkColumns);
        joinTo.addJoin(joinTo.table.pk, ourPkColumns, linkTableSe);

        SelectExp se = new SelectExp();
        JdbcClass valueJdbcClass = (JdbcClass)fmd.elementTypeMetaData.storeClass;
        se.table = valueJdbcClass.table;
        linkTableSe.addJoin(valueColumns, se.table.pk, se);
        return se;
    }

    protected void prepareSingleFetch(FetchSpec spec, FetchOptions options,
            SelectExp owningSe, int refLevel, FetchOpData src, FetchGroupField fgField) {
        FopGetCollection fopGetCollection = new FopGetCollection(spec, src, this,
                owningSe, refLevel, fgField);
        spec.addFetchOp(fopGetCollection, false);
    }

    /**
     * Complete the meta data for this collection. This must use info
     * already supplied in the .jdo file and add anything else needed.
     */
    public void processMetaData(JdoElement context, JdbcMetaDataBuilder mdb,
            boolean quiet) {
        super.processMetaData(context, mdb, quiet);

        JdoExtension[] extensions = getExtensions();

        // see if this is the inverse side of a many-to-many
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.INVERSE,
                extensions);
        if (ext != null) {
            processInverse(mdb, ext);
            return;
        }

        valuesAreOIDs = fmd.elementTypeMetaData != null;
        if (valuesAreOIDs) {
            if (fmd.ordered || allowNulls()) {
                useJoin = JdbcField.USE_JOIN_OUTER;
            } else {
                useJoin = JdbcField.USE_JOIN_INNER;
            }
        } else {
            useJoin = JdbcField.USE_JOIN_NO;
        }

        ext = JdoExtension.find(JdoExtensionKeys.MANAGED, extensions);
        if (ext != null) {
            if (fmd.category == MDStatics.CATEGORY_ARRAY && ext.getBoolean()) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "The managed option is not supported for arrays: " + fmd.name);
            }
            fmd.managed = ext.getBoolean();
        } else {
            if (fmd.category != MDStatics.CATEGORY_ARRAY) {
                fmd.managed = mdb.getJdbcConfig().managedManyToMany;
            } else {
                fmd.managed = false;
            }
        }

        ClassMetaData cmd = fmd.classMetaData;
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        ArrayList cols = new ArrayList();
        JdoExtension link = JdoExtension.find(JdoExtensionKeys.JDBC_LINK_TABLE,
                extensions);
        JdoExtension[] linkNested = link == null ? null : link.nested;

        // create our pk columns
        ext = JdoExtension.find(JdoExtensionKeys.JDBC_OWNER_REF, linkNested);
        JdbcRefMetaDataBuilder rdb = new JdbcRefMetaDataBuilder(cmd, mdb,
                fmd.classMetaData, context,
                JdbcMetaDataBuilder.OWNER_REF_FIELDNAME,
                ext == null ? null : ext.nested, quiet);
        ourPkColumns = rdb.getCols();
        cols.addAll(rdb.getColsList());
        boolean pkConstraint = !rdb.isDoNotCreateConstraint();
        String pkConstraintName = rdb.getConstraintName();

        // create the sequence column if required
        if (fmd.ordered) {
            ext = JdoExtension.find(JdoExtensionKeys.JDBC_SEQUENCE, linkNested);
            sequenceColumn = mdb.createColumn(ext == null ? null : ext.nested,
                    JdbcMetaDataBuilder.SEQUENCE_FIELDNAME, /*CHFC*/Integer.TYPE/*RIGHTPAR*/);
            cols.add(sequenceColumn);
        }

        completeKeyAndValueColumnMetaData(jdbcClass, cols, context, linkNested,
                mdb, quiet);

        // create the link table
        linkTable = new JdbcTable();
        linkTable.comment = fmd.getTypeQName();
        createLinkTablePK();
        linkTable.sqlDriver = mdb.getSqlDriver();
        linkTable.cols = new JdbcColumn[cols.size()];
        cols.toArray(linkTable.cols);
        linkTable.setTableOnCols();

        // Create a constraint for the main table pk columns and for the value
        // (and key for map) columns if the value (or key) is an OID
        List constraints = createConstraints(pkConstraint, pkConstraintName);
        linkTable.constraints = new JdbcConstraint[constraints.size()];
        constraints.toArray(linkTable.constraints);

        // see if the link table must be left out of the schema or not
        ext = JdoExtension.find(JdoExtensionKeys.JDBC_DO_NOT_CREATE_TABLE,
                linkNested);
        doNotCreateTable = ext != null && ext.getBoolean();
        String linkTableNameForNamegen = null;

        // Make sure the link table has a name. A dummy name is used for
        // name generation purposes if the table is being left out of the
        // schema and there is already a table with the same name so that
        // any generated column names will match those of the table included
        // in the schema (if any).
        JdbcNameGenerator namegen = mdb.getNameGenerator();
        ext = JdoExtension.find(JdoExtensionKeys.JDBC_TABLE_NAME, linkNested);
        if (ext == null) {
            nameLinkTable(namegen, jdbcClass);
        } else {
            try {
                namegen.addTableName(linkTable.name = ext.getString());
            } catch (IllegalArgumentException e) {
                if (!doNotCreateTable) {
                    throw BindingSupportImpl.getInstance().runtime(e.getMessage() + "\n" +
                            context.getContext(), e);
                }
                // Duplicate names are ok if the table is not being created.
                // Use a dummy name so generated column names are the same as
                // the original. This dummy name is removed at the end of this
                // method.
                linkTableNameForNamegen = linkTable.name + System.currentTimeMillis();
                namegen.addTableName(linkTableNameForNamegen);
            }
        }
        String linkTableName = linkTable.name;
        if (linkTableNameForNamegen == null) linkTableNameForNamegen = linkTableName;

        // name its pk constraint
        if (!doNotCreateTable) {
            if (linkTable.pkConstraintName == null) {
                linkTable.pkConstraintName =
                        namegen.generatePkConstraintName(linkTableName);
            } else {
                namegen.addPkConstraintName(linkTableName,
                        linkTable.pkConstraintName);
            }
        }

        // register the names of all columns that already have names
        for (int i = 0; i < linkTable.cols.length; i++) {
            try {
                linkTable.cols[i].addColumnNames(linkTableNameForNamegen,
                        namegen);
            } catch (IllegalArgumentException e) {
                if (!doNotCreateTable) {
                    throw BindingSupportImpl.getInstance().runtime(e.getMessage() + "\n" +
                            context.getContext(), e);
                }
                // duplicates are ok if table is not being created
            }
        }

        // name our pk columns
        String[] pkNames = jdbcClass.table.getPkNames();
        String[] ourPkNames = JdbcColumn.getColumnNames(ourPkColumns);
        namegen.generateLinkTableMainRefNames(linkTableNameForNamegen, pkNames,
                ourPkNames);
        JdbcColumn.setColumnNames(ourPkColumns, ourPkNames);

        // name the sequence column (if any)
        if (sequenceColumn != null && sequenceColumn.name == null) {
            sequenceColumn.name = namegen.generateLinkTableSequenceName(
                    linkTableNameForNamegen);
        }

        // name the key and value columns
        nameKeyAndValueColumns(namegen, linkTableNameForNamegen);

        // name all the constraints
        if (!doNotCreateTable) {
            for (int i = 0; i < linkTable.constraints.length; i++) {
                JdbcConstraint c = linkTable.constraints[i];
                if (c.name != null) {
                    namegen.addRefConstraintName(linkTableName, c.name);
                } else {
                    String[] fkNames = JdbcColumn.getColumnNames(c.srcCols);
                    String[] refPkNames = JdbcColumn.getColumnNames(c.dest.pk);
                    c.name = namegen.generateRefConstraintName(linkTableName,
                            c.dest.name, fkNames, refPkNames);
                }
            }
        }

        // remove the table if it is not going to be in the schema so other
        // fields can be mapped to the same name and get the same generated
        // column names
        if (doNotCreateTable) namegen.removeTableName(linkTableNameForNamegen);

        // sycn with our inverse (if any)
        syncWithInverse(mdb);
        emptyArray = mdb.getEmptyArray(getStructArrayType());
    }

    /**
     * Create all the constraints for our link table.
     */
    protected List createConstraints(boolean pkConstraint,
            String pkConstraintName) {
        ArrayList constraints = new ArrayList();

        if (pkConstraint) {
            JdbcConstraint mainCon = new JdbcConstraint();
            mainCon.src = linkTable;
            mainCon.srcCols = ourPkColumns;
            mainCon.dest = ((JdbcClass)fmd.classMetaData.storeClass).table;
            mainCon.name = pkConstraintName;
            constraints.add(mainCon);
        }

        if (createValueConstraint && valuesAreOIDs
                && fmd.elementTypeMetaData.storeClass != null) {
            JdbcConstraint valueCon = new JdbcConstraint();
            valueCon.src = linkTable;
            valueCon.srcCols = valueColumns;
            valueCon.dest = ((JdbcClass)fmd.elementTypeMetaData.storeClass).table;
            valueCon.name = valueConstraintName;
            constraints.add(valueCon);
        }

        return constraints;
    }

    /**
     * Get our extensions or null if none.
     */
    private JdoExtension[] getExtensions() {
        switch (fmd.category) {
            case MDStatics.CATEGORY_ARRAY:
                if (fmd.jdoArray != null) return fmd.jdoArray.extensions;
                return null;
            case MDStatics.CATEGORY_COLLECTION:
                if (fmd.jdoCollection != null) return fmd.jdoCollection.extensions;
                return null;
            case MDStatics.CATEGORY_MAP:
                if (fmd.jdoMap != null) return fmd.jdoMap.extensions;
                return null;
        }
        throw BindingSupportImpl.getInstance().internal(
                "invalid category: " + fmd.category);
    }

    /**
     * Set the PK of the link table.
     */
    protected void createLinkTablePK() {
        if (sequenceColumn != null) {
            linkTable.setPk(JdbcColumn.concat(ourPkColumns, sequenceColumn));
        } else {
            linkTable.setPk(JdbcColumn.concat(ourPkColumns, valueColumns));
        }
    }

    /**
     * Name the key and value columns.
     */
    protected void nameKeyAndValueColumns(JdbcNameGenerator namegen,
            String linkTableNameForNamegen) {
        // name the value column(s)
        if (valuesAreOIDs) {
            String[] valuePkNames = JdbcColumn.getColumnNames(
                    ((JdbcClass)fmd.elementTypeMetaData.storeClass).table.pk);
            String[] linkValueRefNames = JdbcColumn.getColumnNames(
                    valueColumns);
            namegen.generateLinkTableValueRefNames(linkTableNameForNamegen,
                    valuePkNames, fmd.elementType.getName(), linkValueRefNames,
                    false);
            JdbcColumn.setColumnNames(valueColumns, linkValueRefNames);
        } else {
            JdbcColumn c = valueColumns[0];
            if (c.name == null) {
                c.name = namegen.generateLinkTableValueName(
                        linkTableNameForNamegen,
                        fmd.elementType, false);
            }
        }
    }

    /**
     * Complete the key and value column related meta data.
     */
    protected void completeKeyAndValueColumnMetaData(JdbcClass jdbcClass,
            ArrayList cols,
            JdoElement context, JdoExtension[] linkNested,
            JdbcMetaDataBuilder mdb, boolean quiet) {

        // create the value column(s)
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.JDBC_VALUE,
                linkNested);               
        if (fmd.elementTypeMetaData != null) {  // values are OIDs
            JdbcRefMetaDataBuilder rdb = new JdbcRefMetaDataBuilder(
                    fmd.classMetaData, mdb,
                    fmd.elementTypeMetaData, context,
                    JdbcMetaDataBuilder.VALUE_FIELDNAME,
                    ext == null ? null : ext.nested, quiet);
            createValueConstraint = !rdb.isDoNotCreateConstraint();
            valueConstraintName = rdb.getConstraintName();
            valueColumns = rdb.getCols();
            cols.addAll(rdb.getColsList());
        } else {
            if (fmd.elementType == /*CHFC*/Object.class/*RIGHTPAR*/) {           	
                if (fmd.classMetaData.jmd.testing) {
                    fmd.setElementType(/*CHFC*/String.class/*RIGHTPAR*/); // fudge so things work
                } else {
                    throw BindingSupportImpl.getInstance().runtime("You must specify the element-type (or value-type for maps) " +
                            "for collections (and maps)\n" +
                            fmd + "\n" + context.getContext());
                }
            }

            JdbcColumn vc = null;
            if (fmd.category == MDStatics.CATEGORY_COLLECTION) {
                vc = mdb.createColumn(ext == null ? null : ext.nested,
                        JdbcMetaDataBuilder.VALUE_FIELDNAME, fmd.elementType);
            } else if (fmd.category == MDStatics.CATEGORY_ARRAY) {
                vc = mdb.createColumn(ext == null ? null : ext.nested,
                        JdbcMetaDataBuilder.VALUE_FIELDNAME, fmd.elementType);
                if (Debug.DEBUG) {
                    if (fmd.componentType != fmd.elementType) {
                        throw new RuntimeException();
                    }
                }
            } else if (fmd.category == MDStatics.CATEGORY_MAP) {
                vc = mdb.createColumn(ext == null ? null : ext.nested,
                        JdbcMetaDataBuilder.VALUE_FIELDNAME, fmd.elementType);
            } else {
                throw BindingSupportImpl.getInstance().internal("");
            }

            valueColumns = new JdbcColumn[]{vc};
            cols.add(vc);
        }

        //update allowNulls
        boolean nulls = allowNulls();
        for (int i = 0; i < valueColumns.length; i++) {
            JdbcColumn valueColumn = valueColumns[i];
            valueColumn.setNulls(nulls);
        }
    }

    /**
     * Name our linkTable.
     */
    protected void nameLinkTable(JdbcNameGenerator namegen,
            JdbcClass jdbcClass) {
        JdbcClass valueTarget = null;
        if (valuesAreOIDs) valueTarget = (JdbcClass)fmd.elementTypeMetaData.storeClass;
        linkTable.name = namegen.generateLinkTableName(jdbcClass.table.name, fmd.name,
                valueTarget == null ? null : valueTarget.table.name);
    }

    /**
     * Persist pass 2 field for a block of graph entries all with
     * the same class. The same ps'es can be used for all entries in the block.
     */
    public void persistPass2Block(PersistGraph graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batchInserts,
            boolean batchUpdates) throws SQLException {
        if (Debug.DEBUG) {
            if (readOnly) {
                for (int pos = blockStart; pos < blockEnd; pos++) {
                    State ns = graph.getNewState(pos);
                    if (ns.getInternalObjectField(stateFieldNo) != null) {
                        throw BindingSupportImpl.getInstance().internal(
                                "readOnly field in ns");
                    }
                }
            }
        }
        if (readOnly) return;
        if (fmd.ordered) {
            persistPass2BlockOrdered(graph, blockStart, blockEnd, s, con,
                    batchInserts, batchUpdates);
        } else {
            persistPass2BlockUnordered(graph, blockStart, blockEnd, s, con,
                    batchInserts, batchUpdates);
        }
    }

    private void persistPass2BlockOrdered(PersistGraph graph, int blockStart,
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

                OID oid = graph.getOID(pos);
                if (fmd.category == MDStatics.CATEGORY_ARRAY) {
                    if (!oid.isNew()) {
                        //delete the current entries
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
                                        "Field: " + fmd.getTypeQName() + "\n" +
                                        "Instance: " + oid.toSString() + "\n" +
                                        JdbcUtils.getPreparedStatementInfo(
                                                psdelAllSql, psdelAll));
                            }
                        }
                    }

                    Object toInsert = ns.getInternalObjectField(stateFieldNo);
                    if (toInsert != null) {
                        if (psins == null) {
                            psinsSql = getInsertLinkTableRowSql(s);
                            psins = con.prepareStatement(psinsSql);
                        }

                        if (fmd.componentType.isPrimitive()) {
                            //throw not supported
                            throw BindingSupportImpl.getInstance().unsupported();
                        } else {
                            insertOrderedLinkTableRows(oid, null,
                                    toInsert, psins, batchInserts, psinsSql);
                        }
                    }

                } else {
                    OrderedCollectionDiff diff =
                            (OrderedCollectionDiff)ns.getInternalObjectField(
                                    stateFieldNo);
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
                                            "Field: " + fmd.getTypeQName() + "\n" +
                                            "Instance: " + oid.toSString() + "\n" +
                                            JdbcUtils.getPreparedStatementInfo(
                                                    psdelAllSql, psdelAll));
                                }
                            }
                        }
                    } else {
                        int[] deleted = diff.deletedIndexes;
                        if (deleted != null && deleted.length > 0) {
                            if (psdel == null) {
                                psdelSql = getDeleteLinkTableRowSql(s);
                                psdel = con.prepareStatement(psdelSql);
                            }
                            deletedOrderedLinkTableRows(oid, deleted, psdel,
                                    batchUpdates, psdelSql);
                            delCount += deleted.length;
                        }
                    }

                    if (diff != null) {
                        Object[] insertedValues = diff.insertedValues;
                        if (insertedValues != null && insertedValues.length > 0) {
                            if (psins == null) {
                                psinsSql = getInsertLinkTableRowSql(s);
                                psins = con.prepareStatement(psinsSql);
                            }
                            insertOrderedLinkTableRows(oid,
                                    diff.insertedIndexes,
                                    insertedValues, psins, batchInserts,
                                    psinsSql);
                        }
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

    protected void execLinkTableBatchInserts(PreparedStatement psins,
            String psinsSql) {
        try {
            psins.executeBatch();
        } catch (SQLException e) {
			throw mapException(e,
                    "Link table batch insert failed: " +
                    JdbcUtils.toString(e) + "\n" +
                    "Field: " + fmd.getTypeQName() + "\n" +
                    JdbcUtils.getPreparedStatementInfo(psinsSql, psins));
        }
    }

    protected void execLinkTableBatchDeletes(int delCount, PreparedStatement psdel,
            String psdelSql, PreparedStatement psdelAll, String psdelAllSql) {
        if (delCount > 0) {
            int[] a;
            try {
                a = psdel.executeBatch();
            } catch (Exception e) {
				throw mapException(e,
                        "Link table batch delete failed: " +
                        JdbcUtils.toString(e) + "\n" +
                        "Field: " + fmd.getTypeQName() + "\n" +
                        JdbcUtils.getPreparedStatementInfo(psdelSql, psdel));
            }
            for (int i = 0; i < delCount; i++) {
                int c = a[i];
                if (c <= 0) {
                    String psi = JdbcUtils.getPreparedStatementInfo(psdelSql,
                            psdel, i);
                    if (c == 0) {
                        throw BindingSupportImpl.getInstance().concurrentUpdate("Link table row not found on batch delete: " +
                                "Field: " + fmd.getTypeQName() + "\n" + psi, null);
                    }
                    throw BindingSupportImpl.getInstance().datastore("Unexpected update count for link table row batch delete: " +
                            c + "\nField: " + fmd.getTypeQName() + "\n" + psi);
                }
            }
        }
        if (psdelAll != null) {
            try {
                psdelAll.executeBatch();
            } catch (Exception e) {
				throw mapException(e,
                        "Link table batch delete all failed: " +
                        JdbcUtils.toString(e) + "\n" +
                        "Field: " + fmd.getTypeQName() + "\n" +
                        JdbcUtils.getPreparedStatementInfo(psdelAllSql,
                                psdelAll));
            }
        }
    }

    /**
     * Delete rows from an ordered link table.
     */
    private void deletedOrderedLinkTableRows(OID oid, int[] deleted,
            PreparedStatement psdel, boolean batch, String sql)
            throws SQLException {
        JdbcColumn sc = sequenceColumn;
        for (int j = deleted.length - 1; j >= 0; j--) {
            int pp = ((JdbcOID)oid).setParams(psdel, 1);
            sc.set(psdel, pp, deleted[j]);
            if (batch) {
                psdel.addBatch();
            } else {
                int uc;
                try {
                    uc = psdel.executeUpdate();
                } catch (Exception e) {
					throw mapException(e,
                            "Delete link table row failed: " +
                            JdbcUtils.toString(e) + "\n" +
                            "Field: " + fmd.getTypeQName() + "\n" +
                            "Sequence: " + deleted[j] + "\n" +
                            "Instance: " + oid.toSString() + "\n" +
                            JdbcUtils.getPreparedStatementInfo(sql, psdel));
                }
                if (uc == 0) {
                    throw BindingSupportImpl.getInstance().concurrentUpdate("Link table row not found: " +
                            "Field: " + fmd.getTypeQName() + "\n" +
                            "Sequence: " + deleted[j] + "\n" +
                            "Instance: " + oid.toSString() + "\n" +
                            JdbcUtils.getPreparedStatementInfo(sql, psdel), oid);
                }
            }
        }
    }

    /**
     * Insert values into an ordered link table.
     */
    private void insertOrderedLinkTableRows(OID oid, int[] insertedIndexes,
            Object _insertedValues, PreparedStatement psins, boolean batch,
            String sql) throws SQLException {

        Object[] insertedValues = (Object[])_insertedValues;
        int ilen = insertedValues.length;


        if (valuesAreOIDs) {
            for (int j = 0; j < ilen; j++) {
                int pp = ((JdbcOID)oid).setParams(psins, 1);
                sequenceColumn.set(psins, pp++,
                        insertedIndexes == null ? j : insertedIndexes[j]);

                OID _oid = (OID)insertedValues[j];


				
                if (_oid != null) {
                    ((JdbcOID)_oid).setParams(psins, pp);
                } else {
                    JdbcGenericOID.setNullParams(psins, pp, fmd.elementTypeMetaData);
                }
                if (batch) {
                    psins.addBatch();
                } else {
                    try {
                        psins.execute();
                    } catch (Exception e) {
						throw mapException(e,
                                "Insert link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                "Link table value[sequence " + insertedIndexes[j] + "]: " +
                                _oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psins));
                    }
                }
            }
        } else {
            JdbcColumn vc = valueColumns[0];
            for (int j = 0; j < ilen; j++) {
                int pp = ((JdbcOID)oid).setParams(psins, 1);
                sequenceColumn.set(psins, pp++,
                        insertedIndexes == null ? j : insertedIndexes[j]);

                vc.set(psins, pp, insertedValues[j]);


                if (batch) {
                    psins.addBatch();
                } else {
                    try {
                        psins.execute();
                    } catch (Exception e) {
						throw mapException(e,
						        "Insert link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                "Link table value[sequence " + insertedIndexes[j] + "]: " +

                                Utils.toString(insertedValues[j]) + "\n" +


                                JdbcUtils.getPreparedStatementInfo(sql, psins));
                    }
                }
            }
        }
    }

    private void persistPass2BlockUnordered(PersistGraph graph, int blockStart,
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

                UnorderedCollectionDiff diff =
                        (UnorderedCollectionDiff)ns.getInternalObjectField(
                                stateFieldNo);

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
                                        "Field: " + fmd.getTypeQName() + "\n" +
                                        "Instance: " + oid.toSString() + "\n" +
                                        JdbcUtils.getPreparedStatementInfo(
                                                psdelAllSql, psdelAll));
                            }
                        }
                    }
                } else {
                    Object[] deleted = diff.deletedValues;
                    if (deleted != null && deleted.length > 0) {
                        if (psdel == null) {
                            psdelSql = getDeleteLinkTableRowSql(s);
                            psdel = con.prepareStatement(psdelSql);
                        }
                        deleteUnorderedLinkTableRows(oid, deleted, psdel,
                                batchUpdates, psdelSql);
                        delCount += deleted.length;
                    }
                }

                if (diff != null) {
                    Object[] inserted = diff.insertedValues;
                    if (inserted != null && inserted.length > 0) {
                        if (psins == null) {
                            psinsSql = getInsertLinkTableRowSql(s);
                            psins = con.prepareStatement(psinsSql);
                        }
                        insertUnorderedLinkTableRows(oid, inserted,
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
     * Delete values from an unordered link table.
     */
    private void deleteUnorderedLinkTableRows(OID oid, Object[] deleted,
            PreparedStatement psdel, boolean batch, String sql)
            throws SQLException {
        if (valuesAreOIDs) {
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
                                "Delete link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Value: " + ((OID)deleted[j]).toSString() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel));
                    }
                    if (uc == 0) {
                        throw BindingSupportImpl.getInstance().concurrentUpdate("Link table row not found: " +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Value: " + ((OID)deleted[j]).toSString() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel), deleted[j]);
                    }
                }
            }
        } else {
            JdbcColumn vc = valueColumns[0];
            for (int j = deleted.length - 1; j >= 0; j--) {
                int pp = ((JdbcOID)oid).setParams(psdel, 1);
                vc.set(psdel, pp, deleted[j]);
                if (batch) {
                    psdel.addBatch();
                } else {
                    int uc;
                    try {
                        uc = psdel.executeUpdate();
                    } catch (Exception e) {
						throw mapException(e,
                                "Delete link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Value: " + Utils.toString(deleted[j]) + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel));
                    }
                    if (uc == 0) {
                        throw BindingSupportImpl.getInstance().concurrentUpdate("Link table row not found: " +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Value: " + Utils.toString(deleted[j]) + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psdel), oid);
                    }
                }
            }
        }
    }

    /**
     * Insert values into an unordered link table.
     */
    private void insertUnorderedLinkTableRows(OID oid, Object[] inserted,
            PreparedStatement psins, boolean batch, String sql)
            throws SQLException {
        int ilen = inserted.length;
        if (valuesAreOIDs) {
            for (int j = 0; j < ilen; j++) {
                int pp = ((JdbcOID)oid).setParams(psins, 1);
                ((JdbcOID)inserted[j]).setParams(psins, pp);
                if (batch) {
                    psins.addBatch();
                } else {
                    try {
                        psins.execute();
                    } catch (Exception e) {
						throw mapException(e,
                                "Insert link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                "Value: " + ((OID)inserted[j]).toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psins));
                    }
                }
            }
        } else {
            JdbcColumn vc = valueColumns[0];
            for (int j = 0; j < ilen; j++) {
                int pp = ((JdbcOID)oid).setParams(psins, 1);
                vc.set(psins, pp, inserted[j]);
                if (batch) {
                    psins.addBatch();
                } else {
                    try {
                        psins.execute();
                    } catch (Exception e) {
						throw mapException(e,
                                "Insert link table row failed: " +
                                JdbcUtils.toString(e) + "\n" +
                                "Field: " + fmd.getTypeQName() + "\n" +
                                "Instance: " + oid.toSString() + "\n" +
                                "Value: " + Utils.toString(inserted[j]) + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, psins));
                    }
                }
            }
        }
    }

    /**
     * Get SQL to delete a row from our link table.
     */
    protected String getDeleteLinkTableRowSql(CharBuf s) {
        if (deleteRowSql == null) {
            s.clear();
            s.append("delete from ");
            s.append(linkTable.name);
            s.append(" where ");
            linkTable.appendWherePK(s);
            deleteRowSql = s.toString();
        }
        return deleteRowSql;
    }

    /**
     * Get SQL to delete all rows from our link table.
     */
    protected String getDeleteAllLinkTableRowsSql(CharBuf s) {
        if (deleteAllRowsSql == null) {
            s.clear();
            s.append("DELETE FROM ");
            s.append(linkTable.name);
            s.append(" WHERE ");
            SqlDriver driver = linkTable.sqlDriver;
            int nc = ourPkColumns.length;
            JdbcColumn sc = ourPkColumns[0];
            s.append(sc.name);
            s.append(' ');
            s.append('=');
            s.append(' ');
            driver.appendWhereParam(s, sc);
            for (int i = 1; i < nc; i++) {
                s.append(" AND ");
                sc = ourPkColumns[i];
                s.append(sc.name);
                s.append(' ');
                s.append('=');
                s.append(' ');
                driver.appendWhereParam(s, sc);
            }
            deleteAllRowsSql = s.toString();
        }
        return deleteAllRowsSql;
    }

    /**
     * Get SQL to delete all rows from our link table with a 'IN' List. This is only
     * supported if there is a single pk column.
     */
    protected void getDeleteAllLinkTableRowsSqlWithInList(CharBuf s) {
        s.clear();
        s.append("delete from ");
        s.append(linkTable.name);
        s.append(" where ");
        JdbcColumn sc = ourPkColumns[0];
        s.append(sc.name);
        s.append(" IN (");
    }

    /**
     * Get SQL to insert a row into our link table.
     */
    public String getInsertLinkTableRowSql(CharBuf s) {
        if (insertRowSql == null) {
            s.clear();
            s.append("INSERT INTO ");
            s.append(linkTable.name);
            s.append('(');
            linkTable.appendInsertColumnList(s);
            s.append(") VALUES (");
            linkTable.appendInsertValueList(s);
            s.append(')');
            insertRowSql = s.toString();
        }
        return insertRowSql;
    }

    /**
     * if null is allowed by default for this collection.
     */
    private boolean allowNulls() {
        return fmd.ordered || fmd.category == MDStatics.CATEGORY_MAP;
    }

    private boolean updateStateFilter(Struct struct, State state) {
        updateStatistics(struct.size);
        if (state == null) return false;
        if (struct.values == null) struct.values = EMPTY_OBJECT_ARRAY;

        if (Debug.DEBUG) {
            if (!state.containsField(fmd.stateFieldNo)
                    || state.getInternalObjectField(fmd.stateFieldNo) == null) {
                throw BindingSupportImpl.getInstance().internal("");
            }
        }

        if (isFilledWithEmpty(state.getInternalObjectField(fmd.stateFieldNo))) {
            state.setInternalObjectField(fmd.stateFieldNo,
                    struct.asArray(getStructArrayType()));
            return true;
        }
        return false;
    }

    private boolean isFilledWithEmpty(Object val) {
        return (val == emptyArray);
    }

    public Class getStructArrayType() {
        if (valuesAreOIDs) {
            return /*CHFC*/OID.class/*RIGHTPAR*/;
        } else if (fmd.category == MDStatics.CATEGORY_ARRAY) {
            return fmd.componentType;
        } else {
            return /*CHFC*/Object.class/*RIGHTPAR*/;
        }
    }

    private class Struct {

        public int len;

        public Object[] values;


        public int size;

        public void init() {
            len = (int)(avgRowCount * FUDGE_FACTOR);
            if (len < MIN_LEN) len = 0;
            values = (Object[])
                     
                     java.lang.reflect.Array.newInstance(getStructArrayType(), len);

            if (Debug.DEBUG) {
                if (((fetchCount + 1) % 10) == 0) {
                    System.out.println("JdbcLinkCollectionField.fetch" +
                            " avgRowCount = " + avgRowCount + " " +
                            " len = " + len + " " +
                            " expansionCount = " + expansionCount + " " +
                            " fetchCount = " + fetchCount);
                }
            }
            size = 0;
        }

        public void add(Object value) {
            if (!fmd.ordered && value == null) {
                //ignore null for unordered cols.
                return;
            }
            //grow if nec.
            if (size == len) {
                if (len == 0) {
                    values =
                            (Object[])
                            
                            java.lang.reflect.Array.newInstance(getStructArrayType(),
                                    len = MIN_LEN);
                } else {
                    len = len * 3 / 2 + 1;

                    Object[] a = (Object[])java.lang.reflect.Array.newInstance(
                            getStructArrayType(), len);
                    System.arraycopy(values, 0, a, 0, size);


                    values = a;
                    expansionCount++;
                }
            }


            values[size++] = value;


        }

        /**
         * Return the values as an array of componentType.
         */
        public Object asArray(Class componentType) {

			if (values != null && values.length == size) return values;
			Object[] a = (Object[])java.lang.reflect.Array.newInstance(componentType, size);
            System.arraycopy(values, 0, a, 0, size);


            return a;
        }
    }

    public void fillStateWithEmpty(State state) {
        if (!state.containsField(fmd.stateFieldNo)) {
            state.setInternalObjectField(fmd.stateFieldNo, getEmptyArray());
        }
    }

    public Object getEmptyArray() {
        if (emptyArray == null) throw BindingSupportImpl.getInstance().
                internal("The 'emptyArray' field is null");
        return emptyArray;
    }


    public boolean isFilledWithEmpty(State state) {
        return state.getInternalObjectField(fmd.stateFieldNo) == getEmptyArray();
    }

    private void updateStatistics(int size) {
        // Update statistics. This is not thread safe but it is not a
        // problem if the avgRowCount is a bit out sometimes.
        int fc = ++fetchCount;
        if (fc > WINDOW_SIZE) {
            fc = WINDOW_SIZE;
        } else if (fc == 1) {
            avgRowCount = size;
        } else if (fc < 0) {
            fc = fetchCount = WINDOW_SIZE;
        } else {
            avgRowCount = (avgRowCount * (fc - 1) + size) / fc;
        }
    }

    /**
     * Delete a pass 2 field for a block of graph entries all with
     * the same class. The same ps'es can be used for all entries in the block.
     */
    public void deletePass2Block(DeletePacket graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batch)
            throws SQLException {
        if (readOnly) return;
        PreparedStatement ps = null;
        try {
            final int count = blockEnd - blockStart;
            boolean useInList = (ourPkColumns.length == 1) && (count > 1);
            if (!batch && !useInList) {
                //delete one-by-one
                ps = deleteOneByOne(s, con, ps, blockStart, blockEnd, graph);
            } else {
                if (useInList) {
                    ps = deleteWithInList(s, count, con, ps, blockStart, blockEnd, graph);
                } else if (batch) {
                    ps = deleteWithBatch(s, con, ps, blockStart, blockEnd, graph);
                }
            }
        } finally {
            cleanup(ps);
        }
    }

    private PreparedStatement deleteWithBatch(CharBuf s, Connection con,
            PreparedStatement ps, int blockStart, int blockEnd,
            DeletePacket graph) throws SQLException {
        String sql = getDeleteAllLinkTableRowsSql(s);
        ps = con.prepareStatement(sql);
        for (int pos = blockStart; pos < blockEnd; pos++) {
            ((JdbcOID)graph.oids[pos]).setParams(ps, 1);
            ps.addBatch();
        }
        try {
            ps.executeBatch();
        } catch (Exception e) {
            throw mapException(e,
                    "Batch delete link table rows failed: " +
                    JdbcUtils.toString(e) + "\n" +
                    "Field: " + fmd.getTypeQName() + "\n" +
                    JdbcUtils.getPreparedStatementInfo(sql, ps));
        }
        return ps;
    }

    private PreparedStatement deleteWithInList(CharBuf s, int count,
            Connection con, PreparedStatement ps, int blockStart, int blockEnd,
            DeletePacket graph) throws SQLException {
        SqlDriver driver = getSqlDriver();

        if (count > driver.getMaxInOperands()) {
            //must break it up
            final int maxInOps = driver.getMaxInOperands();
            int amountOfFullRuns = count/driver.getMaxInOperands();

            final int amountLeft = count % driver.getMaxInOperands();;
            String sql = null;
            int pos = blockStart;
            final String param = driver.getSqlParamString(ourPkColumns[0].jdbcType);
            getDeleteAllLinkTableRowsSqlWithInList(s);
            if (amountLeft > 0) {
                for (int i = 0; i < amountLeft; i++) {
                    if (i != 0) s.append(",");
                    s.append(param);
                }
                s.append(")");

                sql = s.toString();
                ps = con.prepareStatement(sql);
                for (int curIndex = 0; curIndex < amountLeft; curIndex++, pos++) {
                    ((JdbcOID)graph.oids[pos]).setParams(ps, curIndex + 1);
                }
                try {
                    ps.execute();
                } catch (Exception e) {
                    throw mapException(e,
                            "Batch delete link table rows failed: " +
                            JdbcUtils.toString(e) + "\n" +
                            "Field: " + fmd.getTypeQName() + "\n" +
                            JdbcUtils.getPreparedStatementInfo(sql, ps));
                }
                s.set(s.size() - 1, ',');
            }

            for (int i = 0; i < maxInOps - amountLeft; i++) {
                if (i != 0) s.append(",");
                s.append(param);
            }
            s.append(")");

            sql = s.toString();
            ps = con.prepareStatement(sql);
            for (int i = 0; i < amountOfFullRuns; i ++) {
                for (int curIndex = 0; curIndex < maxInOps; curIndex++, pos++) {
                    ((JdbcOID)graph.oids[pos]).setParams(ps, curIndex + 1);
                }
                try {
                    ps.execute();
                } catch (Exception e) {
                    throw mapException(e,
                            "Batch delete link table rows failed: " +
                            JdbcUtils.toString(e) + "\n" +
                            "Field: " + fmd.getTypeQName() + "\n" +
                            JdbcUtils.getPreparedStatementInfo(sql, ps));
                }
            }
        } else {
            getDeleteAllLinkTableRowsSqlWithInList(s);
            for (int i = 0; i < count; i++) {
                if (i != 0) s.append(",");
                driver.appendWhereParam(s, ourPkColumns[0]);
            }
            s.append(")");
            String sql = s.toString();
            ps = con.prepareStatement(sql);

            int index = 1;
            for (int pos = blockStart; pos < blockEnd; pos++) {
                ((JdbcOID)graph.oids[pos]).setParams(ps, index++);
            }
            try {
                ps.execute();
            } catch (Exception e) {
                throw mapException(e,
                        "Batch delete link table rows failed: " +
                        JdbcUtils.toString(e) + "\n" +
                        "Field: " + fmd.getTypeQName() + "\n" +
                        JdbcUtils.getPreparedStatementInfo(sql, ps));
            }
        }
        return ps;
    }

    private PreparedStatement deleteOneByOne(CharBuf s, Connection con,
            PreparedStatement ps, int blockStart, int blockEnd,
            DeletePacket graph) throws SQLException {
        String sql = getDeleteAllLinkTableRowsSql(s);
        ps = con.prepareStatement(sql);
        for (int pos = blockStart; pos < blockEnd; pos++) {
            ((JdbcOID)graph.oids[pos]).setParams(ps, 1);
            try {
                ps.execute();
            } catch (Exception e) {
                throw mapException(e,
                        "Delete link table rows failed: " +
                        JdbcUtils.toString(e) + "\n" +
                        "Field: " + fmd.getTypeQName() + "\n" +
                        "Instance: " + graph.oids[pos].toSString() + "\n" +
                        JdbcUtils.getPreparedStatementInfo(sql, ps));
            }
        }
        return ps;
    }

    /**
     * Convert this field into an isEmpty expression.
     */
    public SqlExp toIsEmptySqlExp(JdbcJDOQLCompiler comp, SelectExp root) {
        SelectExp se = new SelectExp();
        se.table = linkTable;
        se.jdbcField = this;
        se.subSelectJoinExp = root.createJoinExp(root.table.pk, ourPkColumns,
                se);
        return new UnaryOpExp(new ExistsExp(se, true), UnaryOpExp.OP_NOT);
    }

    /**
     * Convert this field into a contains expression.
     */
    public SqlExp toContainsSqlExp(JdbcJDOQLCompiler comp, SelectExp root,
            Node args) {
        return toContainsSqlExp(valueColumns, fmd.elementTypeMetaData, comp,
                root, args);
    }

    protected SqlExp toContainsSqlExp(JdbcColumn[] cols, ClassMetaData colsCmd,
            JdbcJDOQLCompiler comp, SelectExp root, Node args) {
        SelectExp se = new SelectExp();
        se.table = linkTable;
        se.jdbcField = this;
        se.subSelectJoinExp = root.createJoinExp(root.table.pk, ourPkColumns,
                se);

        if (args instanceof VarNodeIF) {
            VarNode v = ((VarNodeIF)args).getVarNode();
            if (v.getCmd() == null) {
                SelectExp storeExtent = new SelectExp();
                storeExtent.table = linkTable;
                storeExtent.var = v;
                v.setStoreExtent(storeExtent);
                v.setFieldExtent(se);
                v.setFmd(fmd);
                se.var = v;
            } else {
                SelectExp vse = (SelectExp)v.getStoreExtent();
                se.addJoin(cols, vse.table.pk, vse);
                se.var = v;
            }
        } else {
            SqlExp left = JdbcColumn.toSqlExp(cols, se);
            if (colsCmd != null) {
                for (SqlExp e = left; e != null; e = e.getNext()) {
                    ((ColumnExp)e).cmd = colsCmd;
                }
            }
            SqlExp right = comp.getVisitor().toSqlExp(args, root, left, 0, null);
            se.whereExp = SqlExp.createBinaryOpExp(left, BinaryOpExp.EQUAL,
                    right);
        }
        return new ExistsExp(se, true, (args instanceof VarNodeIF ? (VarNodeIF)args : null));
    }

    /**
     * Process meta data for an inverse many-to-many collection.
     */
    private void processInverse(JdbcMetaDataBuilder mdb, JdoExtension e) {
        ClassMetaData ecmd = fmd.elementTypeMetaData;
        if (ecmd.storeClass == null) {
            throw BindingSupportImpl.getInstance().runtime("The inverse extension may only be used for " +
                    "collections of PC instances stored by JDBC\n" +
                    e.getContext());
        }
        String fname = e.getString();
        FieldMetaData f = ecmd.getFieldMetaData(fname);
        if (f.storeField == null) {
            throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' is not persistent\n" +
                    e.getContext());
        }
        if (!(f.storeField instanceof JdbcLinkCollectionField)) {
            throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' is not a collection or array mapped " +
                    "using a link table\n" + e.getContext());
        }
        inverse = (JdbcLinkCollectionField)f.storeField;
        if (f.elementTypeMetaData != fmd.classMetaData) {
            throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' contains " +
                    f.elementTypeMetaData + " and not our class\n" +
                    e.getContext());
        }
        if (inverse == this) {
            throw BindingSupportImpl.getInstance().runtime("Field '" + fname +
                    "' may not be in a many-to-many with itself\n" +
                    e.getContext());
        }
        inverse.inverse = this;
        readOnly = true;
        valuesAreOIDs = true;
        fmd.ordered = false;
        fmd.isManyToMany = true;
        fmd.isReadOnly = true;
        fmd.inverseFieldMetaData = inverse.fmd;
        inverse.fmd.isManyToMany = true;
        inverse.fmd.inverseFieldMetaData = fmd;
        syncWithInverse(mdb);
        emptyArray = mdb.getEmptyArray(getStructArrayType());
    }

    /**
     * Sync our mapping info with our inverse. This will get called twice:
     * once for each side of the many-to-many.
     * @param mdb
     */
    private void syncWithInverse(JdbcMetaDataBuilder mdb) {
        if (inverse == null) return; // not inverse or inverse side not done
        if (readOnly) {
            linkTable = inverse.linkTable;
            if (linkTable != null) { // main side has been done
                valueColumns = inverse.ourPkColumns;
                ourPkColumns = inverse.valueColumns;
                sequenceColumn = inverse.sequenceColumn;
                createInverseIndex(mdb);
            }
            fmd.managed = inverse.fmd.managed;
        } else {
            inverse.syncWithInverse(mdb);
        }
    }

    /**
     * For inverse collections create an index on our primary key fields
     * in the link table of the main collection  unless this has been disabled.
     * @param mdb
     */
    private void createInverseIndex(JdbcMetaDataBuilder mdb) {
        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.INVERSE,
                getExtensions());
        JdoExtension[] nested = ext.nested;

        JdbcIndex idx = null;
        boolean doNotCreateIndex = false;

        int n = nested == null ? 0 : nested.length;
        for (int i = 0; i < n; i++) {
            JdoExtension e = nested[i];
            switch (e.key) {
                case JdoExtensionKeys.JDBC_INDEX:
                    if (idx != null) {
                        throw BindingSupportImpl.getInstance().runtime("Only one jdbc-index extension is allowed here\n" +
                                e.getContext());
                    }
                    if (e.isNoValue()) {
                        doNotCreateIndex = true;
                        break;
                    }
                    idx = new JdbcIndex();
                    idx.name = e.value;
                    break;
                default:
                    if (e.isJdbc()) {
                        MetaDataBuilder.throwUnexpectedExtension(e);
                    }
            }
        }

        if (doNotCreateIndex) return;
        if (idx == null) idx = new JdbcIndex();
        idx.setCols(ourPkColumns);

        // register the name of the index if one was specified otherwise one
        // will be generated later along with user specified indexes
        if (idx.name != null) {
            try {
                mdb.getNameGenerator().addIndexName(linkTable.name,
                        idx.name);
            } catch (IllegalArgumentException x) {
                throw BindingSupportImpl.getInstance().runtime(x.getMessage(),
                        x);
            }
        }

        if (linkTable.indexes != null) {
            throw BindingSupportImpl.getInstance().internal("Link table already has index: "
                    + linkTable + ", " + fmd.getTypeQName());
        }
        linkTable.indexes = new JdbcIndex[]{idx};
    }

    /**
     * Make sure all the indexes on our link tables (if any) have names,
     */
    public void nameLinkTableIndexes(JdbcNameGenerator namegen) {
        if (readOnly) return;
        int n = linkTable == null ? 0 : linkTable.indexes == null ? 0 : linkTable.indexes.length;
        for (int i = 0; i < n; i++) {
            JdbcIndex idx = linkTable.indexes[i];
            if (idx.name == null) {
                JdbcMetaDataBuilder.generateNameForIndex(namegen,
                        linkTable.name, idx);
            }
        }
    }

    /**
     * Return SQL that will fetch all the rows in the link table.
     * This is used when bulk copying one database to another. The OID of
     * the owning table must be first followed by the other columns in the
     * order expected by readRow.
     *
     * @see #readRow
     */
    public String getFetchAllRowsSql(JdbcStorageManager sm) throws SQLException {
        SelectExp root = new SelectExp();
        root.table = linkTable;
        SqlExp e = root.selectList = JdbcColumn.toSqlExp(ourPkColumns, root);
        for (; e.getNext() != null; e = e.getNext()) ;
        if (fmd.ordered) e = e.setNext(sequenceColumn.toSqlExp(root));
        e = e.setNext(JdbcColumn.toSqlExp(valueColumns, root));
        addFetchAllRowsKey(e, root);
        return sm.generateSql(root).toString();
    }

    /**
     * Hook for JdbcMapField to add its key columns to the row. The returned
     * SqlExp must be the last one in the list.
     */
    protected void addFetchAllRowsKey(SqlExp e, SelectExp se) {
    }

    /**
     * Fetch a row of values for this field. This is used when bulk copying
     * one database to another to read all the rows in a given link table.
     * Return the index of the last column read + 1.
     */
    public int readRow(ResultSet rs, LinkRow row) throws SQLException {
        row.owner = (JdbcGenericOID)fmd.classMetaData.createOID(false);
        row.owner.copyKeyFields(rs, 1);
        int pos = ourPkColumns.length + 1;
        if (fmd.ordered) {
            row.seq = ((Integer)sequenceColumn.get(rs, pos++)).intValue();
        }
        if (valuesAreOIDs) {
            OID valueOid = fmd.elementTypeMetaData.createOID(false);
            ((JdbcOID)valueOid).copyKeyFields(rs, pos);
            row.value = valueOid;
            pos += valueColumns.length;
        } else {
            row.value = valueColumns[0].get(rs, pos++);
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
        if (fmd.ordered) sequenceColumn.set(ps, pos++, row.seq);
        if (valuesAreOIDs) {
            JdbcGenericOID v = (JdbcGenericOID)row.value;
            v.setCmd(fmd.classMetaData);
            v.setParams(ps, pos);
        } else {
            valueColumns[0].set(ps, pos, row.value);
        }
    }

    /**
     * A row from our link table. This is used by the bulk database copying
     * operations.
     */
    public static class LinkRow {

        public JdbcGenericOID owner;
        public int seq;
        public Object key;
        public Object value;
    }

}
