
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
package com.versant.core.metadata;

import com.versant.core.common.Debug;
import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is used by MetaDataBuilder to construct fetch groups. Putting all
 * the fetch group stuff in one file should add some much needed sanity.
 */
public class FetchGroupBuilder {

    private final ModelMetaData jmd;
    private final boolean sendCurrentForFGWithSecFields;
    private final boolean readObjectBeforeWrite;
    private Map cmdToUserFgNameSet = new HashMap();

    public FetchGroupBuilder(ModelMetaData jmd,
            boolean sendCurrentForFGWithSecFields, boolean readObjectBeforeWrite) {
        this.jmd = jmd;
        this.sendCurrentForFGWithSecFields = sendCurrentForFGWithSecFields;
        this.readObjectBeforeWrite = readObjectBeforeWrite;
    }

    protected StoreFetchGroup createStoreFetchGroup() {
        return null;
    }

    /**
     * Return the first fetch-group extension in a or null if none.
     */
    public JdoExtension findFetchGroupExt(JdoExtension[] a) {
        if (a == null) return null;
        int n = a.length;
        for (int i = 0; i < n; i++) {
            if (a[i].key == JdoExtensionKeys.FETCH_GROUP) return a[i];
        }
        return null;
    }

    /**
     * Build all the fetch groups. This must be called once all the fields for
     * each class have been created (including fake fields etc.).
     */
    public void buildFetchGroups(boolean quiet) {
        ClassMetaData[] classes = jmd.classes;
        int clen = classes.length;

        // create all the groups with their field arrays
        if (Debug.DEBUG) System.out.println("MDB-FGB: Creating fetch groups");
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                if (cmd.pcSuperMetaData == null) {
                    Set userFgNames = new HashSet();
                    cmdToUserFgNameSet.put(cmd, userFgNames);
                    findUserFgNames(cmd, userFgNames);
                }
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                if (cmd.pcSuperMetaData == null) createFetchGroups(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // resolve next-fetch-group references etc
        if (Debug.DEBUG) {
            System.out.println(
                    "MDB-FGB: Resolving next-fetch-group references");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                processFetchGroups(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                processNextEmbeddedFetchGroup(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // Set the sendFieldsOnFetch on FetchGroup's with secondaryField's
        // if required by the DataStore for the class. Also sets the
        // hasPrimaryFields flag on all FetchGroups.
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            // todo Commenting out these lines appeared to make no difference
//           	if (cmd.pcSuperMetaData != null && cmd.vdsClass == null) {
//           		continue;
//            }
            FetchGroup[] fetchGroups = cmd.fetchGroups;
            if (fetchGroups == null) {
                continue;
            }
            for (int j = fetchGroups.length - 1; j >= 0; j--) {
                FetchGroup fg = fetchGroups[j];
                if (sendCurrentForFGWithSecFields && !fg.hasPrimaryFields(true)
                        && fg.hasSecondaryFields()) {
                    fg.setSendFieldsOnFetch(true);
                }
                if (fg.hasPrimaryFields(false)) {
                    fg.setHasPrimaryFields(true);
                }
            }
        }
    }

    private void findUserFgNames(ClassMetaData cmd, Set userFgNames) {
        // find all the user defined groups
        JdoElement[] elements = cmd.jdoClass.elements;
        int elementsLen = elements.length;
        for (int j = 0; j < elementsLen; j++) {
            JdoElement element = elements[j];
            if (!(element instanceof JdoExtension)) continue;
            JdoExtension e = (JdoExtension)element;
            if (e.key != JdoExtensionKeys.FETCH_GROUP) continue;
            userFgNames.add(e.getString());
        }
        if (cmd.pcSubclasses != null) {
            for (int i = 0; i < cmd.pcSubclasses.length; i++) {
                ClassMetaData pcSubclass = cmd.pcSubclasses[i];
                findUserFgNames(pcSubclass, userFgNames);
            }
        }
    }

    /**
     * Create all groups for cmd with their field arrays and then recursively
     * create groups for its subclasses.
     */
    private void createFetchGroups(ClassMetaData cmd, boolean quiet) {
        if (cmd.fields == null) {
            return; // no fields due to some previous error
        }

        ArrayList groups = cmd.fgTmp = new ArrayList();
        HashMap nameGroupMap = cmd.nameGroupMap = new HashMap();

        FetchGroup dfg = createDefaultFetchGroup(cmd);
        groups.add(dfg);
        nameGroupMap.put(dfg.name, dfg);

        FetchGroup retrieveFG = createRetrieveFetchGroup(cmd);
        groups.add(retrieveFG);
        nameGroupMap.put(retrieveFG.name, retrieveFG);

        FetchGroup allColsFG = createAllColumnsFetchGroup(cmd);
        groups.add(allColsFG);
        nameGroupMap.put(allColsFG.name, allColsFG);

        FetchGroup dfgNoFakes = createFetchGroupDefaultNoFakes(cmd);
        groups.add(dfgNoFakes);
        nameGroupMap.put(dfgNoFakes.name, dfgNoFakes);

        // if you add new groups here add a corresponding line to the block
        // towards the end of this method that caches special groups in
        // ClassMetaData
        addNotNull(createRefFetchGroup(cmd), groups, nameGroupMap);
        addNotNull(createDepFetchGroup(cmd), groups, nameGroupMap);
        addNotNull(createReqFetchGroup(cmd), groups, nameGroupMap);
        addNotNull(createManagedManyToManyFetchGroup(cmd), groups,
                nameGroupMap);
        addNotNull(createRetrieveReferencesHollowFetchGroup(cmd), groups,
                nameGroupMap);
        addNotNull(createHollowFetchGroup(cmd), groups, nameGroupMap);

        // find all the user defined groups
        JdoElement[] elements = cmd.jdoClass.elements;
        int elementsLen = elements.length;
        for (int j = 0; j < elementsLen; j++) {
            JdoElement element = elements[j];
            if (!(element instanceof JdoExtension)) continue;
            JdoExtension e = (JdoExtension)element;
            if (e.key != JdoExtensionKeys.FETCH_GROUP) continue;
            FetchGroup g = new FetchGroup(cmd, e.getString(), createStoreFetchGroup());
            try {
                if (g.name.equals(FetchGroup.DFG_NAME)) {
                    throw BindingSupportImpl.getInstance().runtime("The group name '" + FetchGroup.DFG_NAME +
                            "' is reserved for the " +
                            "default fetch group\n" + e.getContext());
                }
                if (g.name.equals(FetchGroup.REF_NAME)) {
                    throw BindingSupportImpl.getInstance().runtime("The group name '" + FetchGroup.REF_NAME +
                            "' is reserved internal use\n" +
                            e.getContext());
                }
                if (nameGroupMap.containsKey(g.name)) {
                    throw BindingSupportImpl.getInstance().runtime("There is already a group called: '" + g.name + "'\n" +
                            e.getContext());
                }
                nameGroupMap.put(g.name, g);
                g.extension = e;
                processFetchGroupFields(cmd, g, quiet);
                groups.add(g);
            } catch (RuntimeException e1) {
                cmd.addError(e1, quiet);
            }
        }

        /**
         * Create fgs in the top class for all userdefined fgs defined in subclasses
         * that is not created yet.
         */
        if (cmd.pcSuperMetaData == null && cmd.pcSubclasses != null) {
            Set userFgNames = (Set) cmdToUserFgNameSet.get(cmd);
            for (Iterator iterator = userFgNames.iterator(); iterator.hasNext();) {
                String fgName = (String) iterator.next();
                if (nameGroupMap.keySet().contains(fgName)) continue;
                createEmptyUserFg(cmd, fgName);
            }
        }

        // create an empty group for each group from our parent class that
        // has not been extended i.e. group defined here with same name
        if (cmd.pcSuperMetaData != null) {
            List list = cmd.pcSuperMetaData.fgTmp;
            for (int i = 0; i < list.size(); i++) {
                FetchGroup sg = (FetchGroup)list.get(i);
                if (nameGroupMap.containsKey(sg.name)) continue;
                FetchGroup g = new FetchGroup(cmd, sg.name, createStoreFetchGroup());
                g.fields = new FetchGroupField[0];
                groups.add(g);
                nameGroupMap.put(g.name, g);
            }
        }

        // Fill in the fetchGroup for all fields creating new groups for fields
        // without a group that are not in the default fetch group. If the
        // field is a pass1 field then the optimistic locking field (if any)
        // is included in the group.
        FieldMetaData[] fields = cmd.fields;
        int fieldsLen = fields.length;
        for (int i = 0; i < fieldsLen; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.jdoField != null && fmd.jdoField.extensions != null) {
                try {
                    JdoExtension e = findFetchGroupExt(fmd.jdoField.extensions);
                    if (e != null) {
                        String gname = e.getString();
                        fmd.fetchGroup = (FetchGroup)nameGroupMap.get(gname);
                        if (fmd.fetchGroup == null) {
                            throw BindingSupportImpl.getInstance().runtime("No such fetch-group: '" + gname + "'\n" +
                                    e.getContext());
                        }
                    }
                } catch (RuntimeException e1) {
                    fmd.addError(e1, quiet);
                }
            }
            if (fmd.fetchGroup == null) {
                if (fmd.defaultFetchGroup) {
                    fmd.fetchGroup = dfg;
                } else {
                    if (fmd.isEmbeddedRef()) {
                        fmd.fetchGroup = dfg;
                    } else {
                        String name = "_" + fmd.name + cmd.qname;
                        for (int j = 2; nameGroupMap.containsKey(name); j++) {
                            name = "_" + fmd.name + j;
                        }
                        FetchGroup g = new FetchGroup(cmd, name, createStoreFetchGroup());

                        ArrayList a = new ArrayList();
                        FetchGroupField fgf = createFetchGroupFieldWithPrefetch(fmd);
                        a.add(fgf);

                        // classes with storeAllFields set also read all fields
                        // so there is no need to add the optimistic locking field
                        // to fetch groups
                        if (fmd.primaryField && !cmd.storeAllFields
                                && cmd.optimisticLockingField != null) {
                            a.add(new FetchGroupField(cmd.optimisticLockingField));
                        }

                        g.fields = new FetchGroupField[a.size()];
                        a.toArray(g.fields);

                        groups.add(g);
                        nameGroupMap.put(g.name, g);
                        fmd.fetchGroup = g;
                    }
                }
            }
        }

        // dig out some popular fetchgroups for quick access
        cmd.refFetchGroup = (FetchGroup)nameGroupMap.get(FetchGroup.REF_NAME);
        cmd.depFetchGroup = (FetchGroup)nameGroupMap.get(FetchGroup.DEP_NAME);
        cmd.reqFetchGroup = (FetchGroup)nameGroupMap.get(FetchGroup.REQ_NAME);
        cmd.managedManyToManyFetchGroup = (FetchGroup)nameGroupMap.get(
                FetchGroup.MANY_TO_MANY_NAME);
        cmd.retrieveReferencesHollowFetchGroup = (FetchGroup)nameGroupMap.get(FetchGroup.RETRIEVE_REFERENCES_HOLLOW_NAME);
        cmd.hollowFetchGroup = (FetchGroup)nameGroupMap.get(FetchGroup.HOLLOW_NAME);

        // process all of our subclasses
        ClassMetaData[] pcSubclasses = cmd.pcSubclasses;
        if (pcSubclasses != null) {
            for (int i = pcSubclasses.length - 1; i >= 0; i--) {
                createFetchGroups(cmd.pcSubclasses[i], quiet);
            }
        }

        if (cmd.pcSuperMetaData == null) {
            Collections.sort(cmd.fgTmp);
            if (cmd.pcSubclasses != null) {
                doSubFGs(cmd);
            }

        }

        if (cmd.pcSuperMetaData == null) {
            createFGArrays(cmd);
        }
    }

    /**
     * Create an empty fg containing only fields like 'version' etc that should
     * be contained in all fg's.
     */
    private void createEmptyUserFg(ClassMetaData sCmd, String name) {
        List fgs = new ArrayList();
        //add the fields that should be in all the fg's
        FieldMetaData[] fmds = sCmd.fields;
        int n = fmds.length;
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fmds[i];
            if (!fmd.secondaryField && fmd.includeInAllFGs()) {
                fgs.add(new FetchGroupField(fmd));
            }
        }
        FetchGroup g = new FetchGroup(sCmd, name, createStoreFetchGroup());
        g.fields = new FetchGroupField[fgs.size()];
        fgs.toArray(g.fields);
        sCmd.fgTmp.add(g);
        sCmd.nameGroupMap.put(name, g);
    }

    /**
     * Create a FGF for a field with prefetching enabled if that makes sense
     * for the store.
     */
    protected FetchGroupField createFetchGroupFieldWithPrefetch(
            FieldMetaData fmd) {
        return new FetchGroupField(fmd);
    }

    private void addNotNull(FetchGroup g, ArrayList groups,
            HashMap nameGroupMap) {
        if (g != null) {
            groups.add(g);
            nameGroupMap.put(g.name, g);
        }
    }

    private void doSubFGs(ClassMetaData cmd) {
        ClassMetaData[] subCmds = cmd.pcSubclasses;
        if (subCmds == null) return;
        ArrayList list = new ArrayList();
        for (int i = 0; i < subCmds.length; i++) {
            ClassMetaData subCmd = subCmds[i];
            Collections.sort(subCmd.fgTmp);
            list.clear();
            for (int j = 0; j < cmd.fgTmp.size(); j++) {
                FetchGroup fetchGroup = (FetchGroup)cmd.fgTmp.get(j);
                FetchGroup subFG = (FetchGroup)subCmd.nameGroupMap.get(
                        fetchGroup.name);
                if (j == 0) {
                    if (!fetchGroup.name.equals(FetchGroup.DFG_NAME) || !subFG.name.equals(
                            FetchGroup.DFG_NAME)) {
                        throw BindingSupportImpl.getInstance().internal(
                                "DFG broken");
                    }
                }
                list.add(subFG);
            }
            subCmd.fgTmp.removeAll(list);
            list.addAll(subCmd.fgTmp);
            subCmd.fgTmp.clear();
            subCmd.fgTmp.addAll(list);
            doSubFGs(subCmd);
        }
    }

    private void createFGArrays(ClassMetaData cmd) {
        indexFGs(cmd);
        ClassMetaData[] cmds = cmd.pcSubclasses;
        if (cmds == null) return;
        for (int i = 0; i < cmds.length; i++) {
            ClassMetaData aCmd = cmds[i];
            createFGArrays(aCmd);
        }
    }

    private void indexFGs(ClassMetaData cmd) {
        int ng = cmd.fgTmp.size();
        FetchGroup[] fga = cmd.fetchGroups = new FetchGroup[ng];
        FetchGroup[] sfga = cmd.sortedFetchGroups = new FetchGroup[ng];
        cmd.fgTmp.toArray(fga);
        cmd.fgTmp.toArray(sfga);
        Arrays.sort(sfga);
        for (int i = fga.length - 1; i >= 0; i--) {
            if (fga[i].index == -1) {
                fga[i].index = i;
            }
        }
    }

    /**
     * Create the default fetch group for cmd.
     */
    private FetchGroup createDefaultFetchGroup(ClassMetaData cmd) {
        FieldMetaData[] fields = cmd.fields;
        FetchGroup g = new FetchGroup(cmd, FetchGroup.DFG_NAME, createStoreFetchGroup());
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.fetchOIDInDfg) {
                //add it
            } else if (!fmd.defaultFetchGroup || fmd.primaryKey) {
                continue;
            }
            FetchGroupField fgf = new FetchGroupField(fmd);
            fgf.doNotFetchObject = !fmd.defaultFetchGroup;
            a.add(fgf);
        }
        n = a.size();
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

    /**
     * This creates a fetch group that contains only default fetch group fields
     * but no fake fields must be added here.
     */
    private FetchGroup createFetchGroupDefaultNoFakes(ClassMetaData cmd) {
        FetchGroup g = new FetchGroup(cmd, FetchGroup.DFG_NAME_NO_FAKES, createStoreFetchGroup());
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.defaultFetchGroup && !fmd.fake) {
                FetchGroupField fgf = new FetchGroupField(fmd);
                a.add(fgf);
            }
        }
        n = a.size();
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

    /**
     * Create the retrieve fetch group for cmd.
     */
    private FetchGroup createRetrieveFetchGroup(ClassMetaData cmd) {
        FetchGroup g = new FetchGroup(cmd, FetchGroup.RETRIEVE_NAME, createStoreFetchGroup());
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) continue;
            if (fmd.isEmbeddedRef()) continue; // fields of embedded structs should not be retrieved
            FetchGroupField fgf = createFetchGroupFieldWithPrefetch(fmd);
            a.add(fgf);
        }
        n = a.size();
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

    /**
     * Create the all columns fetch group for cmd. This implementation just
     * creates a dummy empty fetch group.
     */
    protected FetchGroup createAllColumnsFetchGroup(ClassMetaData cmd) {
        FetchGroup g = new FetchGroup(cmd, FetchGroup.ALL_COLS_NAME, createStoreFetchGroup());
        g.fields = new FetchGroupField[0];
        return g;
    }

    private FetchGroup createRetrieveReferencesHollowFetchGroup(ClassMetaData cmd) {
        FetchGroup g = createRetrieveFetchGroup(cmd);
        g.name = FetchGroup.RETRIEVE_REFERENCES_HOLLOW_NAME;
        return g;        
    }
    
    private FetchGroup createHollowFetchGroup(ClassMetaData cmd) {
        FetchGroup g = new FetchGroup(cmd, FetchGroup.HOLLOW_NAME, createStoreFetchGroup());
        g.fields = new FetchGroupField[]{};
        return g;
    }
    
    /**
     * Create the ref fetch group for cmd or return null if cmd does not
     * have any fields that reference other objects. Polyref and collection
     * fields are included. This does not fill in the nextFetchGroup and
     * nextKeyFetchGroup for the fields as this can only be done once all
     * the groups have been created.
     */
    private FetchGroup createRefFetchGroup(ClassMetaData cmd) {
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.isDirectRef() && fmd.isFake() && fmd.inverseFieldMetaData != null) {
                continue;
            }
            switch (fmd.category) {
                case MDStatics.CATEGORY_POLYREF:
                    a.add(new FetchGroupField(fmd));
                    break;
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                    if (fmd.elementType == /*CHFC*/Object.class /*RIGHTPAR*/
                            || fmd.keyType == /*CHFC*/Object.class /*RIGHTPAR*/
                            || (fmd.elementType != null && fmd.elementType.isInterface())
                            || (fmd.keyType != null && fmd.keyType.isInterface())) {
                        a.add(new FetchGroupField(fmd));
                        break;
                    }
                default:
                    ClassMetaData refmd = fmd.getRefOrValueClassMetaData();
                    if (refmd != null || fmd.keyTypeMetaData != null) {
                        a.add(new FetchGroupField(fmd));
                    }
            }
        }
        n = a.size();
        if (n == 0) {
            return null;
        }
        FetchGroup g = new FetchGroup(cmd, FetchGroup.REF_NAME, createStoreFetchGroup());
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

    /**
     * Create the dependent fetch group for cmd or return null if cmd does not
     * have any fields that reference dependent objects. This does not fill
     * in the nextFetchGroup and nextKeyFetchGroup for the fields as this can
     * only be done once all the groups have been created.
     */
    private FetchGroup createDepFetchGroup(ClassMetaData cmd) {
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.isEmbeddedRef()) continue;
            if (!fmd.dependentValues && !fmd.dependentKeys) continue;
            if (fmd.category != MDStatics.CATEGORY_POLYREF) {
                ClassMetaData refmd = fmd.getRefOrValueClassMetaData();
                if (refmd == null && fmd.keyTypeMetaData == null) continue;
            }
            a.add(new FetchGroupField(fmd));
        }
        n = a.size();
        if (n == 0) return null;
        FetchGroup g = new FetchGroup(cmd, FetchGroup.DEP_NAME, createStoreFetchGroup());
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

    /**
     * Create the fetch group containing required all the fields that must
     * be filled in the original state when persisting changes to instances.
     * This includes all autoset version fields ans well as autoset timestamp
     * fields used to implement optimistic locking.
     */
    private FetchGroup createReqFetchGroup(ClassMetaData cmd) {
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        if (readObjectBeforeWrite) {
            for (int i = 0; i < n; i++) {
                FieldMetaData fmd = fields[i];
                if (fmd.primaryField) {
                    FetchGroupField fgf = new FetchGroupField(fmd);
                    fgf.doNotFetchObject = true;
                    a.add(fgf);
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                FieldMetaData fmd = fields[i];
                if (isReqField(cmd, fmd)) {
                    a.add(new FetchGroupField(fmd));
                }
            }
        }
        n = a.size();
        if (n == 0) return null;
        FetchGroup g = new FetchGroup(cmd, FetchGroup.REQ_NAME, createStoreFetchGroup());
        g.fields = new FetchGroupField[n];
        a.toArray(g.fields);
        return g;
    }

    /**
     * Is fmd required before changes to an instance can be persisted?
     */
    private boolean isReqField(ClassMetaData cmd, FieldMetaData fmd) {
        if (fmd.autoSet == MDStatics.AUTOSET_NO) {
            return false;
        }
        if (fmd.typeCode == MDStatics.DATE) {
            return fmd == cmd.optimisticLockingField;
        }
        return true;
    }

    /**
     * Create the fetch group containing all the managed many-to-many fields
     * and all the required fields. No group is created if there are no
     * managed many-to-many fields.
     */
    private FetchGroup createManagedManyToManyFetchGroup(ClassMetaData cmd) {
        int manyToManyCount = 0;
        FieldMetaData[] fields = cmd.fields;
        int n = fields.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.isManyToMany && fmd.managed || isReqField(cmd, fmd)) {
                if (fmd.isManyToMany) manyToManyCount++;
                a.add(new FetchGroupField(fmd));
            }
        }
        if (manyToManyCount == 0) return null;
        FetchGroup g = new FetchGroup(cmd, FetchGroup.MANY_TO_MANY_NAME, createStoreFetchGroup());
        g.fields = new FetchGroupField[a.size()];
        a.toArray(g.fields);
        return g;
    }

    /**
     * Process the groups extension to find all of its fields. This does
     * not resolve next-fetch-group extensions as this can only be done
     * when all groups have been created.
     */
    private void processFetchGroupFields(ClassMetaData cmd, FetchGroup g,
            boolean quite) {
        JdoExtension e = g.extension;
        HashSet fieldNameSet = new HashSet(17);
        ArrayList fields = new ArrayList();
        JdoExtension[] nested = e.nested;
        if (nested == null) return;
        int nn = nested.length;
        boolean noFgFields = true;
        for (int k = 0; k < nn; k++) {
            JdoExtension ne = nested[k];
            if (ne.key != JdoExtensionKeys.FIELD_NAME) continue;
            String fname = ne.getString();
            if (fieldNameSet.contains(fname)) {
                throw BindingSupportImpl.getInstance().runtime("Field is already in group: '" + fname + "'\n" +
                        ne.getContext());
            }
            noFgFields = false;
            //don't add pk fields to fg's
            FieldMetaData fmd = cmd.getFieldMetaData(fname);
            if (fmd == null) {
                cmd.addError(BindingSupportImpl.getInstance().runtime("Field does not exist: '" + fname + "'\n" +
                        ne.getContext()), quite);
                continue;
            }
            if (fmd.primaryKey) continue;

            fieldNameSet.add(fname);
            FetchGroupField f = new FetchGroupField(fmd);
            f.extension = ne;
            fields.add(f);
        }


        //add the fields that should be in all the fg's
        FieldMetaData[] fmds = cmd.fields;
        int n = fmds.length;
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fmds[i];
            if (!fmd.secondaryField && fmd.includeInAllFGs() && !fieldNameSet.contains(
                    fmd.name)) {
                fieldNameSet.add(fmd.name);
                FetchGroupField fgf = new FetchGroupField(fmd);
                fields.add(fgf);
            }
        }

        int nf = fields.size();
        if (noFgFields && nf == 0) {
            throw BindingSupportImpl.getInstance().runtime("Fetch group does not contain any fields: '" + g.name + "'\n" +
                    e.getContext());
        }
        g.fields = new FetchGroupField[nf];
        fields.toArray(g.fields);
    }

    /**
     * Process extensions for all fields in all groups. This resolves
     * next-fetch-group and next-key-fetch-group references.
     */
    private void processFetchGroups(ClassMetaData cmd) {
        processRefFetchGroup(cmd);
        FetchGroup[] groups = cmd.fetchGroups;
        if (groups == null) {
            return;
        }
        FetchGroup dep = cmd.refFetchGroup;
        int ng = groups.length;
        for (int i = 0; i < ng; i++) {
            FetchGroup group = groups[i];
            if (group == dep) continue;
            FetchGroupField[] fields = group.fields;
            if (fields == null) continue;
            int nf = fields.length;
            for (int j = 0; j < nf; j++) {
                FetchGroupField f = fields[j];
                ClassMetaData refmd = f.fmd.getRefOrValueClassMetaData();
                ClassMetaData keymd = f.fmd.keyTypeMetaData;
                JdoExtension[] nested = f.extension == null ? null : f.extension.nested;
                if (nested != null) {
                    int nl = nested.length;
                    for (int k = 0; k < nl; k++) {
                        JdoExtension e = nested[k];
                        switch (e.key) {
                            case JdoExtensionKeys.NEXT_FETCH_GROUP:
                                processNextFetchGroup(f, e, refmd);
                                break;
                            case JdoExtensionKeys.NEXT_KEY_FETCH_GROUP:
                                processNextKeyFetchGroup(f, e, keymd);
                                break;
                            default:
                                if (e.isCommon()) {
                                    MetaDataBuilder.throwUnexpectedExtension(e);
                                }
                        }
                    }
                }
                if (f.nextFetchGroup == null && refmd != null && refmd.fetchGroups != null) {
                    if (group == cmd.retrieveReferencesHollowFetchGroup) {
                        f.nextFetchGroup = refmd.hollowFetchGroup;
                    } else {
                        f.nextFetchGroup = refmd.fetchGroups[0];
                    }
                }
                if (f.nextKeyFetchGroup == null && keymd != null && keymd.fetchGroups != null) {
                    if (group == cmd.retrieveReferencesHollowFetchGroup) {
                        f.nextKeyFetchGroup = keymd.hollowFetchGroup;
                    } else {
                        f.nextKeyFetchGroup = keymd.fetchGroups[0];
                    }
                }
            }
        }
    }

    /**
     * Process the embeddedRef fields (ie references to embedded instances) and
     * add follow the next-fg to include the fields in the fg of the root class.
     *
     * This will cause the default fields of the embedded reference and all of its
     * fields to be included in the default fg of the root instance.
     *
     * @param cmd
     */
    private void processNextEmbeddedFetchGroup(ClassMetaData cmd) {
        final FetchGroup[] groups = cmd.fetchGroups;
        if (groups == null) {
            return;
        }
        int ng = groups.length;
        for (int i = 0; i < ng; i++) {
            FetchGroup group = groups[i];
            final FetchGroupField[] fields = group.fields;
            if (fields == null) continue;
            List fgFieldsList = new ArrayList(Arrays.asList(fields));
            for (int j = 0; j < fgFieldsList.size(); j++) {
                final FetchGroupField fetchGroupField = (FetchGroupField) fgFieldsList.get(j);
                if (!fetchGroupField.fmd.isEmbeddedRef()) continue;
                fetchGroupField.embeddedNextFgFields = addEmbeddedFgFields(fetchGroupField.fmd,
                        fetchGroupField.nextFetchGroup, fgFieldsList);
            }
            if (fgFieldsList.size() != fields.length) {
                group.fields = new FetchGroupField[fgFieldsList.size()];
                fgFieldsList.toArray(group.fields);
            }
        }
    }

    private FetchGroupField[] addEmbeddedFgFields(FieldMetaData fmd,
            FetchGroup nextFetchGroup, List list) {
        if (nextFetchGroup == null) return null;
        FieldMetaData[] embeddedFmds = fmd.embeddedFmds;
        //must add the the fields to the owning fg.
        FetchGroupField[] nFgFields = nextFetchGroup.fields;
        List addFgFields = new ArrayList();

        /**
         * Iterate over the fields of the next fetchgroup and find the representative
         * FieldMetadata to create a new fg field.
         */
        for (int i = 0; i < nFgFields.length; i++) {
            FetchGroupField nFgField = nFgFields[i];
            for (int j = 0; j < embeddedFmds.length; j++) {
                if (embeddedFmds[j].origFmd.equals(nFgField.fmd)) {
                    FetchGroupField newFgField = new FetchGroupField(embeddedFmds[j]);
                    if (newFgField.fmd.isEmbeddedRef()) {
                        newFgField.nextFetchGroup = nFgField.nextFetchGroup;
                    }
                    list.add(newFgField);
                    addFgFields.add(newFgField);
                    break;
                }
            }
        }

        if (addFgFields.isEmpty()) return FetchGroup.EMPTY_FETCHGROUP_FIELDS;
        FetchGroupField[] addedFields = new FetchGroupField[addFgFields.size()];
        addFgFields.toArray(addedFields);
        return addedFields;
    }

    private void processNextFetchGroup(FetchGroupField f, JdoExtension e,
            ClassMetaData refmd) {
        if (f.nextFetchGroup != null) {
            throw BindingSupportImpl.getInstance().runtime("Only one next-fetch-group extension is allowed\n" +
                    e.getContext());
        }
        if (refmd == null) {
            throw BindingSupportImpl.getInstance().runtime("Field does not reference a PC class\n" +
                    e.getContext());
        }
        FetchGroup g = refmd.getFetchGroup(e.getString());
        if (g == null) {
            throw BindingSupportImpl.getInstance().runtime("Fetch group '" + e.getString() + "' not found in class " +
                    refmd.qname + "\n" +
                    e.getContext());
        }
        f.nextFetchGroup = g;
    }

    private void processNextKeyFetchGroup(FetchGroupField f, JdoExtension e,
            ClassMetaData keymd) {
        if (f.nextKeyFetchGroup != null) {
            throw BindingSupportImpl.getInstance().runtime("Only one next-key-fetch-group extension is allowed\n" +
                    e.getContext());
        }
        if (keymd == null) {
            throw BindingSupportImpl.getInstance().runtime("Field key does not reference a PC class\n" +
                    e.getContext());
        }
        FetchGroup g = keymd.getFetchGroup(e.getString());
        if (g == null) {
            throw BindingSupportImpl.getInstance().runtime("Fetch group '" + e.getString() + "' not found in class " +
                    keymd.qname + "\n" +
                    e.getContext());
        }
        f.nextKeyFetchGroup = g;
    }

    /**
     * Fill in the nextFetchGroup and nextKeyFetchGroup references for
     * all the fields in the refFetchGroup for cmd (if any). Only dependent
     * object references are followed.
     */
    private void processRefFetchGroup(ClassMetaData cmd) {
        FetchGroup g = cmd.refFetchGroup;
        if (g == null) return;
        FetchGroupField[] fields = g.fields;
        int nf = fields.length;
        for (int j = 0; j < nf; j++) {
            FetchGroupField f = fields[j];
            FieldMetaData fmd = f.fmd;
            // only follow dependent object references for this group
            if (fmd.dependentValues) {
                ClassMetaData refmd = fmd.getRefOrValueClassMetaData();
                if (refmd != null) f.nextFetchGroup = refmd.refFetchGroup;
            }
            if (fmd.dependentKeys) {
                ClassMetaData keymd = fmd.keyTypeMetaData;
                if (keymd != null) f.nextKeyFetchGroup = keymd.refFetchGroup;
            }
        }
    }
}
