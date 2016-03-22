
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

import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.*;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.jdbc.*;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.jdbc.query.JdbcJDOQLCompiler;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.server.PersistGraph;
import com.versant.core.server.StateContainer;
import com.versant.core.util.CharBuf;
import com.versant.core.jdo.query.Node;
import com.versant.core.jdo.query.VarNode;
import com.versant.core.jdo.query.VarNodeIF;
import com.versant.core.common.Debug;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

import com.versant.core.common.BindingSupportImpl;

/**
 * A field that is a Collection or array of a PC class stored using a
 * foreign key in the value class.
 */
public class JdbcFKCollectionField extends JdbcCollectionField {

    /**
     * This is the 'foreign key' field in the value class.
     */
    public JdbcRefField fkField;

    /**
     * This is the JdbcClass for the elements.
     */
    private JdbcClass elementJdbcClass;

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

    public void dump(PrintStream out, String indent) {
        super.dump(out, indent);
        String is = indent + "  ";
        out.println(is + "fkField " + fkField);
    }

    /**
     * Complete the meta data for this collection. This must use info
     * already supplied in the .jdo file and add anything else needed.
     */
    public void processMetaData(JdoElement context, JdbcMetaDataBuilder mdb,
            boolean quiet) {
        ClassMetaData ecmd = fmd.elementTypeMetaData;
        elementJdbcClass = (JdbcClass)ecmd.storeClass;
        if (elementJdbcClass == null) {
            throw BindingSupportImpl.getInstance().runtime("The inverse extension may only be used for " +
                    "collections of PC instances stored by JDBC\n" +
                    context.getContext());
        }
        ClassMetaData cmd = fmd.classMetaData;

        super.processMetaData(context, mdb, quiet);

        useJoin = JdbcField.USE_JOIN_INNER;
        if (fmd.category != MDStatics.CATEGORY_ARRAY) {
            fmd.managed = mdb.getJdbcConfig().managedOneToMany;
        } else {
            fmd.managed = false;
        }

        JdoExtension[] a;
        if (fmd.category == MDStatics.CATEGORY_ARRAY) {
            a = fmd.jdoArray.extensions;
        } else if (fmd.category == MDStatics.CATEGORY_COLLECTION) {
            a = fmd.jdoCollection.extensions;
        } else {
            throw BindingSupportImpl.getInstance().internal(
                    "Category '"
                    + MDStaticUtils.toCategoryString(fmd.category) + "' is not supported for FK Collections");
        }
        int len = a.length;
        for (int i = 0; i < len; i++) {
            JdoExtension e = a[i];
            switch (e.key) {
                case JdoExtensionKeys.MANAGED:
                    if (fmd.category == MDStatics.CATEGORY_ARRAY && e.getBoolean()) {
                        throw BindingSupportImpl.getInstance().invalidOperation(
                                "The managed option is not supported for arrays: " + fmd.name);
                    }
                    fmd.managed = e.getBoolean();
                    break;
                case JdoExtensionKeys.INVERSE:
                case JdoExtensionKeys.JDBC_LINK_FOREIGN_KEY:
                    String fname = e.getString();
                    FieldMetaData f = ecmd.getFieldMetaData(fname);
                    if (f == null) {
                        f = createFakeFKBackRef(cmd, ecmd, mdb, e, quiet);
                    }
                    if (f.isEmbeddedRef()) {
                        throw BindingSupportImpl.getInstance().invalidOperation("an Inverse field may not be Embedded");
                    }
                    if (f.storeField == null) {
                        throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' is not persistent\n" +
                                context.getContext());
                    }
                    if (!(f.storeField instanceof JdbcRefField)) {
                        throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' is not a reference\n" +
                                context.getContext());
                    }
                    fkField = (JdbcRefField)f.storeField;
                    if (!cmd.isAncestorOrSelf(fkField.targetClass)) {
                        throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' references " +
                                fkField.targetClass + " and not our class\n" +
                                context.getContext());
                    }
                    fmd.ordered = false;
                    createIndex(mdb, ecmd, e.nested);
                    break;

                default:
                    if (e.isJdbc()) {
                        throw BindingSupportImpl.getInstance().runtime(
                                "Unexpected extension: " + e + "\n" + e.getContext());
                    }
            }
        }
        if (fkField == null) {
            throw BindingSupportImpl.getInstance().internal("fkField is null");
        }

        fkField.masterCollectionField = this;
        ourPkColumns = fkField.cols;
    }

    private FieldMetaData createFakeFKBackRef(ClassMetaData cmd,
            ClassMetaData ecmd, JdbcMetaDataBuilder mdb,
            JdoExtension e, boolean quiet) {
        fmd.managed = false;

        FieldMetaData f = new FieldMetaData();
        f.fake = true;
        f.typeMetaData = cmd;
        f.name = cmd.getShortName() + "_" + fmd.name;
        f.category = MDStatics.CATEGORY_REF;
        f.ordered = false;
        f.managed = false;
        f.primaryField = true;
        JdbcRefField jdbcRefField = new JdbcRefField();
        jdbcRefField.targetClass = cmd;
        fkField = jdbcRefField;
        f.classMetaData = ecmd;
        jdbcRefField.fmd = f;
        f.storeField = jdbcRefField;
        jdbcRefField.fake = true;
        f.type = cmd.cls;
        f.inverseFieldMetaData = fmd;
        mdb.processRefFieldImpl(elementJdbcClass, jdbcRefField,
                f, e, e.nested, quiet);

//                        JdbcRefMetaDataBuilder rmdb = new JdbcRefMetaDataBuilder(
//                                fmd.classMetaData, mdb, cmd,
//                                cmd.jdbcClass.store, e, fname, e.nested, quiet);
//                        JdbcColumn[] cols = rmdb.getCols();
//                        jdbcRefField.cols = cols;
//                        if (cols != null) {
//                            for (int j = 0; j < cols.length; j++) {
//                                JdbcColumn col = cols[j];
//                                col.nulls = true;
//                                col.comment = "inverse FK for " + cmd.getShortName() + "-" + fmd.name;
//                            }
//                        }
        mdb.getClassInfo(ecmd).elements.add(f.storeField);
        return f;
    }

    /**
     * Create an index on our pkField unless this has been disabled.
     */
    private void createIndex(JdbcMetaDataBuilder mdb, ClassMetaData refCmd,
            JdoExtension[] nested) {
        JdbcClass refJdbcClass = (JdbcClass)refCmd.storeClass;
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
                case JdoExtensionKeys.JDBC_COLUMN:
                case JdoExtensionKeys.JDBC_USE_JOIN:
                case JdoExtensionKeys.JDBC_CONSTRAINT:
                case JdoExtensionKeys.JDBC_REF:
                    // Handled
                    break;
                default:
                    if (e.isJdbc()) {
                        MetaDataBuilder.throwUnexpectedExtension(e);
                    }
            }
        }

        if (doNotCreateIndex) return;
        if (idx == null) idx = new JdbcIndex();
        if (fkField.cols != null) {
            idx.setCols(fkField.cols);
        }

        // register the name of the index if one was specified otherwise one
        // will be generated later along with user specified indexes
        if (idx.name != null) {
            try {
                mdb.getNameGenerator().addIndexName(
                        refJdbcClass.table.name, idx.name);
            } catch (IllegalArgumentException x) {
                throw BindingSupportImpl.getInstance().runtime(x.getMessage(),
                        x);
            }
        }

        mdb.getClassInfo(refCmd).autoIndexes.add(idx);
    }

    public SelectExp addParColJoin(SelectExp joinTo, boolean keyJoin) {
        SelectExp elementExp = new SelectExp();
        elementExp.table = elementJdbcClass.table;
        Join join = joinTo.addJoin(joinTo.table.pk, ourPkColumns, elementExp);

        JdbcColumn clsIdCol = ((JdbcClass)fmd.elementTypeMetaData.storeClass).classIdCol;
        if (clsIdCol != null) {
            SelectExp clsSeExp = elementExp;
            Join baseJoin = join;
            if (clsIdCol.table != elementExp.table) {
                SelectExp se = new SelectExp();
                se.outer = false;
                se.table = clsIdCol.table;
                baseJoin = elementExp.addJoin(elementExp.table.pk, se.table.pk, se);
                clsSeExp = se;
            }
            baseJoin.appendJoinExp(((JdbcClass)fmd.elementTypeMetaData.storeClass).getCheckClassIdExp(
                        clsSeExp));
        }
        return elementExp;
    }

    /**
     * At this stage the first row will be process in its entire and the rest of
     * the rows of the rs must be processed to the read the rest of the collection entries.
     *
     * The first collection entry is already read as part of the normal fetch process.
     * @param rs
     */
    public void fetchParWithSingleRoot3(ResultSet rs, int firstIndex) throws SQLException {

        State state = null;
        //process the current row
        boolean first = true;
        Struct struct = new Struct();
        struct.init();

        while (rs.next()) {
            if (first) {
                first = false;
                OID[] o = (OID[]) state.getInternalObjectField(fmd.stateFieldNo);
                struct.add(o[0]);
            }


        }
    }

    /**
     * Get a SelectExp to select all the rows in this collection using the
     * supplied fetch group field to control joins and so on.
     */
    public SelectExp addOneToManyJoin(FetchSpec spec, FetchOpData src,
                                      SelectExp joinToExp, FetchGroupField fgField, int refLevel,
                                      FetchSpec owningSpec, FetchFieldPath ffPath) {
        SelectExp elementExp = new SelectExp();
        elementExp.outer = true;
        elementExp.table = elementJdbcClass.table;
        Join join = joinToExp.addJoin(joinToExp.table.pk, ourPkColumns, elementExp);

        spec.setFilterExpFactory(owningSpec.getFilterExpFactory());

        //add the oid of the element
        FopGetOID fopGetOid = new FopGetOID(spec,
                    FetchOpDataMainRS.INSTANCE, elementJdbcClass.cmd, elementExp);
        spec.addFetchOp(fopGetOid, true);

        JdbcColumn clsIdCol = ((JdbcClass)fmd.elementTypeMetaData.storeClass).classIdCol;
        if (clsIdCol != null) {
            SelectExp clsSeExp = elementExp;
            Join baseJoin = join;
            if (clsIdCol.table != elementExp.table) {
                SelectExp se = new SelectExp();
                se.outer = elementExp.outer;
                se.table = clsIdCol.table;
                baseJoin = elementExp.addJoin(elementExp.table.pk, se.table.pk, se);
                clsSeExp = se;
            }
            baseJoin.appendJoinExp(((JdbcClass)fmd.elementTypeMetaData.storeClass).getCheckClassIdExp(
                        clsSeExp));
        }

        if (fgField.jdbcUseJoin != JdbcField.USE_JOIN_NO) {
            FopGetState fopGetState = new FopGetState(spec,
                    fopGetOid.getOutputData(), fgField.nextFetchGroup,
                    true, elementExp, refLevel, elementJdbcClass.cmd, ffPath.getCopy().add(this));
            spec.addFetchOp(fopGetState, false);
        }

        // add order by if ordering extension has been used
        if (fmd.ordering != null) {
            elementExp.addOrderBy(fmd.ordering, false);
        }
        if (!spec.isInstanceFetch()) elementExp.prependOrderByForColumns(ourPkColumns);

        joinToExp.appendOrderByExp(elementExp.orderByList);
        elementExp.orderByList = null;
        return elementExp;
    }

    public void fetchParFetch(FetchResult fetchResult,
                              StateContainer stateContainer, Object[] params, FetchSpec fetchSpec, SelectExp owningSe,
                              FetchOpData src, FetchGroupField fgField, int refLevel,
                              ParCollectionFetchResult parColFetchResult, FetchOp fetchOp,
                              FopParCollectionFetch fopParCollFetch, FetchFieldPath ffPath) {
        OID prevOID = null;
        Struct struct = new Struct();
        struct.init();

        boolean toContinue = false;
        FetchSpec subSpec = null;
        FetchResult fResultImp = null;
        if (parColFetchResult == null) {
            subSpec = createCollectionFetchSpec(fetchSpec, owningSe, fgField, refLevel, fopParCollFetch);
            subSpec.finish(1);
            if (Debug.DEBUG) {
                subSpec.printPlan(System.out, "  fk:level " + refLevel + "  ");
            }
            if (subSpec.getParamList() == null) subSpec.setParamList(fetchResult.getParamList());
            fResultImp = subSpec.createFetchResult(
                            fetchResult.getStorageManager(), fetchResult.getConnection(),
                            params, fetchResult.isForUpdate(), fetchResult.isForCount(), 0, 0,
                            0, false, 0);

            parColFetchResult = new ParCollectionFetchResult(this, fResultImp);
            fetchResult.setPass2Data(fetchOp, parColFetchResult);
        }
        fResultImp = parColFetchResult.getFetchResult();

        if (fResultImp.hasNext()) toContinue = true;
        boolean updatedDate = false;

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
            OID owner = (OID) fResultImp.getDiscriminator(stateContainer);

            if (stateContainer.get(owner) == null) {
                //must break/pause here for this batch
                //if we have'nt done any then there is a bug
                if (struct.size == 0) {
                    throw BindingSupportImpl.getInstance().internal("There was no items added to the collection.");
                }
                break;
            }

            Object[] row = (Object[])fResultImp.next(stateContainer);
            if (prevOID == null) {
                prevOID = owner;
            } else if (!prevOID.equals(owner)) {
                //owner changed
                State state = stateContainer.get(prevOID);
                if (state == null) {
                    throw BindingSupportImpl.getInstance().internal("State not found");
                }
                if (updateStateFilter(struct, state)) updatedDate = true;

                prevOID = owner;
                struct.init();
            }
            struct.add((OID) row[0]);
        }
        if (prevOID != null) {
            State state = stateContainer.get(prevOID);
            if (state == null) {
                throw BindingSupportImpl.getInstance().internal("State not found");
            }
            if (updateStateFilter(struct, state)) updatedDate = true;
        }

        //fetch the pass2 fields
        if (updatedDate && toContinue) {
            if (Debug.DEBUG) {
                subSpec.printPlan(System.out, " toContinue ");
            }
            subSpec.fetchPass2(fResultImp, params, stateContainer);
        }

        if (!fResultImp.hasNext()) {
            fResultImp.close();
        }
    }

    public void fetchOneToManyParCollection(FetchSpec subSpec,
                 FetchResult subFetchResult, State state, Object[] params,
                 StateContainer container, FetchResult owningFetchResult) {
        if (subSpec.isInstanceFetch()) {
            fetchOneToManyParCollectionSingle(subSpec, subFetchResult, state, container);
        } else {
            fetchOneToManyParCollectionImp(owningFetchResult, container, subFetchResult, state);
        }
    }

    private void fetchOneToManyParCollectionImp(FetchResult owningFetchResult,
            StateContainer container, FetchResult subFetchResult, State state) {
        Struct struct = new Struct();
        struct.init();

        final OID discr = (OID) subFetchResult.getDiscriminator(container);
        while(subFetchResult.hasNext()) {
            OID nextDiscr = (OID) subFetchResult.getDiscriminator(container);
            if (!discr.equals(nextDiscr)) {
                break;
            }

            //The first row must be the current row of the owning resultSet
            JdbcOID oid = (JdbcOID) ((Object[])subFetchResult.next(container))[0];
            //only add the oid if it is a valid oid ie the columns value was not null
            if (oid != null) struct.add(oid);
        }
        if (!subFetchResult.hasNext()) subFetchResult.close();
        updateState(struct, state);
    }

    private void fetchOneToManyParCollectionSingle(FetchSpec subSpec,
                 FetchResult subFetchResult, State state,
                 StateContainer container) {
        Struct struct = new Struct();
        struct.init();
        while(subFetchResult.hasNext()) {
            //The first row must be the current row of the owning resultSet
            JdbcOID oid = (JdbcOID) ((Object[])subFetchResult.next(container))[0];
            //only add the oid if it is a valid oid ie the columns value was not null
            if (oid != null) struct.add(oid);
        }
        subFetchResult.close();
        updateState(struct, state);
    }

    private FetchSpec createCollectionFetchSpec(FetchSpec spec, SelectExp owningSe,
            FetchGroupField fgField, int refLevel, FopParCollectionFetch fopParCollectionFetch) {

        SelectExp root = spec.createQueryFilter();
        //follow the join path back to the root exp
        SelectExp joinToSExp = fopParCollectionFetch.createJoinPath(root);

        SelectExp elementExp = new SelectExp();
        elementExp.table = elementJdbcClass.table;
        elementExp.appendOrderByForColumns(ourPkColumns);
        joinToSExp.addJoin(joinToSExp.table.pk, ourPkColumns, elementExp);

        FetchSpec fSpec = new FetchSpec(root,
                ((JdbcClass)fmd.classMetaData.storeClass).sqlDriver);
        fSpec.setParentFetchSpec(spec);
        fSpec.getOptions().setUseOneToManyJoin(spec.getOptions().isUseParallelQueries());

        //add the owning oid of this collection.
        FopGetOID stateOIDFop = new FopGetOID(fSpec,
                FetchOpDataMainRS.INSTANCE, fmd.classMetaData,
                elementExp, ourPkColumns);
        fSpec.addDiscriminator(new FetchOpDiscriminator(fSpec, stateOIDFop), false);

        //add the oid of the element
        FopGetOID fopGetOid = new FopGetOID(fSpec,
                    FetchOpDataMainRS.INSTANCE, elementJdbcClass.cmd,
                    elementExp);
        fSpec.addFetchOp(fopGetOid, true);

        if (fgField.jdbcUseJoin != JdbcField.USE_JOIN_NO) {
            FopGetState fopGetState = new FopGetState(fSpec,
                    fopGetOid.getOutputData(), fgField.nextFetchGroup,
                    true, elementExp, refLevel, elementJdbcClass.cmd,
                    fopParCollectionFetch.getFetchFieldPathCopy().add(this));
            fSpec.addFetchOp(fopGetState, false);
        }

        // add order by if ordering extension has been used
        if (fmd.ordering != null) {
            elementExp.addOrderBy(fmd.ordering, true);
        }

        if (Debug.DEBUG) {
            System.out.println("%%% JdbcFKCollectionField.getSelectExp: " +
                    fmd.getQName());
            elementExp.dump("  ");
            System.out.println("%%%");
        }
        root.appendOrderByExp(elementExp.orderByList);
//        root.orderByList = elementExp.orderByList;

        fSpec.finish(1);
        return fSpec;
    }

    protected void prepareParFetch(FetchSpec spec, FetchOptions options, SelectExp owningSe,
                                   int refLevel, FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {
        //if the query only contains pass2 fields then we should not do a oneToMany join
        //as this would require an unnecc. join to the owner.
        if (options.isUseOneToManyJoin() && refLevel == 1) {
            options.setUseOneToManyJoin(false);
            FopOneToManyParCollectionFetch fgc3 = new FopOneToManyParCollectionFetch(spec, src, this,
                    owningSe, refLevel, fgField, ffPath);
            //this fop must be last
            spec.addFetchOpLast(fgc3);
        } else {
            FopParCollectionFetch fgc2 = new FopParCollectionFetch(spec, src, this,
                    owningSe, refLevel, fgField, ffPath);
            spec.addFetchOp(fgc2, false);
        }
    }

    public FetchSpec createSingleFetch(FetchSpec spec, SelectExp owningSe,
            int refLevel, FetchOpData src, FetchGroupField field) {

        SelectExp root = new SelectExp();
        root.table = elementJdbcClass.table;
        root.selectList = JdbcColumn.toSqlExp(root.table.pk, root);
        root.whereExp = JdbcColumn.createEqualsParamExp(ourPkColumns, root);

        FetchSpec subSpec = new FetchSpec(root, spec.getSqlDriver(), true);
        FopGetOID fetchOid = new FopGetOID(subSpec, FetchOpDataMainRS.INSTANCE,
                elementJdbcClass.cmd, root);

        subSpec.addFetchOp(fetchOid, true);

        if (field.jdbcUseJoin != JdbcField.USE_JOIN_NO) {
            FopGetState fetchState = new FopGetState(subSpec,
                    fetchOid.getOutputData(), field.nextFetchGroup, true, root,
                    refLevel, elementJdbcClass.cmd, null);
            subSpec.addFetchOp(fetchState, false);
        }

        // add a check to only consider classes of this typeS
        if (((JdbcClass)fmd.elementTypeMetaData.storeClass).classIdCol != null) {
            root.whereExp.setNext(((JdbcClass)fmd.elementTypeMetaData.storeClass).getCheckClassIdExp(
                    root));
            AndExp andExp = new AndExp(root.whereExp);
            root.whereExp = andExp;
        }

        // add order by if ordering extension has been used
        if (fmd.ordering != null) {
            root.addOrderBy(fmd.ordering, false);
        }
        subSpec.finish(1);
        return subSpec;
    }

    protected void prepareSingleFetch(FetchSpec spec, FetchOptions options,
            SelectExp owningSe, int refLevel, FetchOpData src, FetchGroupField field) {
        FopGetCollection fetchCollection = new FopGetCollection(spec, src, this, owningSe, refLevel, field);
        spec.addFetchOp(fetchCollection, false);
    }

    public void fetchSingleFetch(FetchResult fetchResult,
                                 StateContainer stateContainer, Object[] params,
                                 State state, SelectExp owningSe, int refLevel, FetchOpData src, FetchGroupField fgField) {
        FetchSpec subSpec = createSingleFetch(fetchResult.getFetchSpec(), owningSe, refLevel, src, fgField);

        FetchResult subResult = subSpec.createFetchResult(
                fetchResult.getStorageManager(), fetchResult.getConnection(),
                params, fetchResult.isForUpdate(), fetchResult.isForCount(), 0,
                0, 0, false, 0);

        try {
            if (subResult.hasNext()) {
                Struct struct = new Struct();
                struct.init();
                while (subResult.hasNext()) {
                    OID oid = (OID) (((Object[]) subResult.next(stateContainer))[0]);
                    struct.add(oid);
                }
                subResult.close();
                updateStateFilter(struct, state);
                subResult.fetchPass2(stateContainer);
            }
        } finally {
            subResult.close();
        }
    }

    /**
     * Persist pass 2 field for a block of graph entries all with
     * the same class. The same ps'es can be used for all entries in the block.
     */
    public void persistPass2Block(PersistGraph graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batchInserts,
            boolean batchUpdates) throws SQLException {
        // nothing to do
    }

    private void updateStats(int size) {
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

    public void fillStateWithEmpty(State state) {
        if (!state.containsField(fmd.stateFieldNo)) {
            state.setInternalObjectField(fmd.stateFieldNo, EMPTY_OID_ARRAY);
        }
    }

    public boolean isFilledWithEmpty(State state) {
        return state.getInternalObjectField(fmd.stateFieldNo) == EMPTY_OID_ARRAY;
    }

    private boolean updateState(Struct s, State state) {
        if (state == null) return false;
        if (s.values == null) s.values = EMPTY_OID_ARRAY;
        s.trim();
        state.setInternalObjectField(fmd.stateFieldNo, s.values);
        return true;
    }

    private boolean updateStateFilter(Struct s, State state) {
        if (state == null) return false;
        if (s.values == null) s.values = EMPTY_OID_ARRAY;
        if (isFilledWithEmpty(state)) {
            s.trim();
            state.setInternalObjectField(fmd.stateFieldNo, s.values);
            return true;
        }
        return false;
    }

    public void appendOrderExpForFilterExp(SelectExp se, SelectExp root) {
        if (fmd.elementTypeMetaData != null) {
            root.appendOrderByForColumns(
                    ((JdbcClass)fmd.elementTypeMetaData.storeClass).table.pk, se);
        }
    }

    private class Struct {

        public int len;
        public OID[] values;
        public int size;

        public OID prevRootOID;
        public OID currentRootOID;

        public OID prevStateOID;
        public OID currentStateOID;

        public void init() {
            len = (int)(avgRowCount * FUDGE_FACTOR);
            if (len < MIN_LEN) len = 0;
            values = len == 0 ? null : new OID[len];
            if (Debug.DEBUG) {
                if (((fetchCount + 1) % 10) == 0) {
                    System.out.println("JdbcFkCollectionField.fetch" +
                            " avgRowCount = " + avgRowCount + " " +
                            " len = " + len + " " +
                            " expansionCount = " + expansionCount + " " +
                            " fetchCount = " + fetchCount);
                }
            }
            size = 0;
        }

        private void add(OID value) {
            //grow if nec.
            if (size == len) {
                if (len == 0) {
                    values = new OID[len = MIN_LEN];
                } else {
                    len = len * 3 / 2 + 1;
                    OID[] a = new OID[len];
                    System.arraycopy(values, 0, a, 0, size);
                    values = a;
                    expansionCount++;
                }
            }
            values[size++] = value;
        }

        /**
         * Trim values down to size elements.
         */
        public void trim() {
            if (values.length == size) return;
            OID[] a = new OID[size];
            System.arraycopy(values, 0, a, 0, size);
            values = a;
        }
    }

    /**
     * Update statistics. This is not thread safe but it is not a
     * problem if the avgRowCount is a bit out sometimes.
     */
    private void updateStatistics(int size) {
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

    public SelectExp getSelectFilterJoinExp(boolean value, SelectExp lhSe,
            SelectExp rootSe, boolean addRootJoin) {
        SelectExp root = new SelectExp();
        root.table = elementJdbcClass.table;
        lhSe.addJoin(lhSe.table.pk, ourPkColumns, root);
        return root;
    }

    /**
     * Convert this field into an isEmpty expression.
     */
    public SqlExp toIsEmptySqlExp(JdbcJDOQLCompiler comp, SelectExp root) {
        SelectExp se = new SelectExp();
        se.table = elementJdbcClass.table;
        se.jdbcField = this;
        se.subSelectJoinExp = root.createJoinExp(root.table.pk, ourPkColumns,
                se);
        // @todo what about empty/null checking?
        return new UnaryOpExp(new ExistsExp(se, true), UnaryOpExp.OP_NOT);
    }

    /**
     * Convert this field into a contains expression.
     */
    public SqlExp toContainsSqlExp(JdbcJDOQLCompiler comp, SelectExp root,
            Node args) {
        if (args instanceof VarNodeIF) {
            VarNode v = ((VarNodeIF)args).getVarNode();
            SelectExp vse = (SelectExp)v.getStoreExtent();

            //same table
            if (vse.table == ourPkColumns[0].table) {
                vse.subSelectJoinExp = root.createJoinExp(root.table.pk,
                        ourPkColumns, vse);
            } else {
                SelectExp se = new SelectExp();
                se.table = ourPkColumns[0].table;
                se.outer = vse.outer;
                vse.addJoin(vse.table.pk, ourPkColumns[0].table.pk, se);
            }

            if (v.getCmd() != fmd.elementTypeMetaData) {
                //should be a subclass
                vse.whereExp = SelectExp.appendWithAnd(vse.whereExp,
                        ((JdbcClass)fmd.elementTypeMetaData.storeClass).getCheckClassIdExp(
                                vse));
            }

            return new ExistsExp(vse, true, v);
        } else {
            SelectExp se = new SelectExp();
            se.table = elementJdbcClass.table;
            se.jdbcField = this;
            se.subSelectJoinExp = root.createJoinExp(root.table.pk,
                    ourPkColumns, se);
            SqlExp left = JdbcColumn.toSqlExp(se.table.pkSimpleCols, se);
            for (SqlExp e = left; e != null; e = e.getNext()) {
                ((ColumnExp)e).cmd = fmd.elementTypeMetaData;
            }
            SqlExp right = comp.getVisitor().toSqlExp(args, root, left, 0, null);
            if (left.getNext() == null && right.getNext() == null) {
                BinaryOpExp ans = new BinaryOpExp(left, BinaryOpExp.EQUAL,
                        right);
                if (right instanceof ParamExp) {
                    ParamExp p = (ParamExp)right;
                    p.usage.expList = ans;
                    p.usage.expCount = 1;
                }
                se.whereExp = ans;
            } else {
                throw BindingSupportImpl.getInstance().internal(
                        "not implemented");
            }
            return new ExistsExp(se, false);
        }
    }

}
