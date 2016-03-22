
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

import com.versant.core.common.*;
import com.versant.core.metadata.*;
import com.versant.core.metadata.parser.JdoRoot;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.util.BeanUtils;
import com.versant.core.vds.metadata.*;
import com.versant.odbms.model.*;
import com.versant.odbms.model.transcriber.Transcriber;
import com.versant.odbms.model.transcriber.TranscriberAdapterFactory;

import java.util.ArrayList;

/**
 * VdsMetaDataBuilder defines a user schema class given JDO metadata definitions.
 * A user schema class is considered to be resolved when all its application
 * field indices are set.
 * This receiver uses a naming policy to map JDO namespace elements i.e.
 * Java classes and fields to schema namespace.
 */
public class VdsMetaDataBuilder extends MetaDataBuilder {

    public static final String TIMESTAMP_FIELD_NAME = "o_ts_timestamp";

    private final VdsConfig vdsConfig;
    private NamingPolicy _namingPolicy;
    private VdsSchemaClassBinder _binder;

    /**
     * This is attached to the UserSchemaClass as the UserObject.
     */
    private static class ClassInfo {

        ClassMetaData cmd;
        ArrayList/*<FieldMetaData>*/ fakeFields = new ArrayList();
    }

    /**
     * The jdbcDriver parameter may be null if this is not available.
     */
    public VdsMetaDataBuilder(ConfigInfo config, VdsConfig vdsConfig,
            ClassLoader loader, boolean quiet) {
        super(config, loader, quiet);
        this.vdsConfig = vdsConfig;
    }

    public ModelMetaData buildMetaData(JdoRoot[] roots) {
        //  create and set the naming policy
        if (vdsConfig.namingPolicy != null) {
            String cname = vdsConfig.namingPolicy;
            if (BriefNamingPolicy.ALIAS.equalsIgnoreCase(cname)) {
                cname = BriefNamingPolicy.class.getName();
            } else if (IdenticalNamingPolicy.ALIAS.equalsIgnoreCase(cname)) {
                cname = IdenticalNamingPolicy.class.getName();
            }
            _namingPolicy = (NamingPolicy)BeanUtils.newInstance(
                    cname, loader, NamingPolicy.class);
        } else {
            _namingPolicy = new BriefNamingPolicy();
        }
        BeanUtils.setProperties(_namingPolicy, vdsConfig.namingPolicyProps);

        _binder = new VdsSchemaClassBinder();

        jmd.vdsModel = new UserSchemaModel();
        jmd.sendStateOnDelete = true;
        jmd.setUntypedOIDFactory(new VdsUntypedOID.Factory());

        return super.buildMetaData(roots);
    }

    public int getCdCacheStrategy() {
        return MDStatics.CACHE_STRATEGY_YES;
    }

    public boolean isCdRefsInDefaultFetchGroup() {
        return true;
    }

    public boolean isSendCurrentForFGWithSecFields() {
        return true;
    }

    public boolean isReadObjectBeforeWrite() {
        return true;
    }

    protected void preBuildFetchGroupsHook() {
        ClassMetaData[] cmds = jmd.classes;
        for (int i = 0; i < cmds.length; i++) {
            ClassMetaData cmd = cmds[i];
            cmd.storeAllFields = true;
            cmd.datastoreIdentityTypeCode = MDStatics.LONG;
            cmd.datastoreIdentityType = Long.TYPE;
        }
        getUserSchemaModel((UserSchemaModel)jmd.vdsModel, jmd, quiet);
    }

    protected void postAllFieldsCreatedHook() {
    }

    public VdsSchemaClassBinder getBinder(){
        return _binder;
    }

    /**
     * Gets the naming policy used by this receiver. Never null.
     */
    public NamingPolicy getVdsNamingPolicy() {
        return _namingPolicy;
    }

    /**
     * Gets the class this receiver expects as the input metadata.
     */
    public Class getSupportedMetaClassType() {
        return ClassMetaData.class;
    }

    public Class getSupportedMetaFieldType() {
        return FieldMetaData.class;
    }

    /**
     * This important method populates the given <code>UserSchemaModel</code>
     * with the classes contained in the given <code>inputModel</code>.
     *
     * @param model
     * @param inputModel must be an instance of <code>JDOMetaDataRepository</code>.
     * @return the same <code>UserSchemaModel</code> that is passed as input
     *         argument. But now populated with resolved schema classes.
     */
    public synchronized UserSchemaModel getUserSchemaModel(
            final UserSchemaModel model, final Object inputModel, boolean quiet) {
//        assert inputModel instanceof JDOMetaData;
        if (Debug.DEBUG) {
            Debug.assertInternal(inputModel instanceof ModelMetaData,
                    "inputModel is not a instanceof JDOMetaData");
        }

        ClassMetaData[] classes = ((ModelMetaData)inputModel).classes;
        for (int i = 0; i < classes.length; i++) {
//            String schemaClassName = _namingPolicy.mapClassName(classes[i]);
//            UserSchemaClass result = model.getSchemaClass(schemaClassName);
//        	if (result == null) {
//        		getUserSchemaClass(model, classes[i]);
//        	}
//        	else {
//                RuntimeException e = BindingSupportImpl.getInstance().runtime("There " +
//                        "is already a class defined for class'" + 
//                		classes[i].qname + "' in database. Use naming " + 
//						"policy to avoid class name conflict");
//                classes[i].addError(e, quiet);
//        	}
        	getUserSchemaClass(model, classes[i]);
        }
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperClass != null) {
                cmd.optimisticLockingField = cmd.top.optimisticLockingField;
            }
//            assert cmd.optimisticLockingField != null  : cmd;
            if (Debug.DEBUG) {
                Debug.assertInternal(cmd.optimisticLockingField != null,
                        "optimisticLockingField is null for "+cmd.toString());
            }
        }
        return model;
    }

    /**
     * Generates a schema class definition using specified JDO Class MetaData.
     * How different Java field types are mapped to datastore type system is
     * controlled by this receiver. The generated schema class is added to the
     * specified container.
     * <p/>
     * If the container already contains the schema class and the schema class
     * is resolved then the schema class is returned.
     * <p/>
     * If the container already contains the schema class and but the schema class
     * is not resolved then the schema class is resolved before it is returned.
     *
     * @return a resolved schema class with all its application properties
     *         set
     */
    public synchronized UserSchemaClass getUserSchemaClass(
            final UserSchemaModel model, final Object metadata) {
//        assert metadata != null : "Can not get schema class for null metadata";
//        assert metadata instanceof ClassMetaData : metadata.getClass() + " is not of type ClassMetaData";
//        assert model != null : "Can not define class with null ApplicationView";
        if (Debug.DEBUG) {
            Debug.assertInternal(metadata != null,
                    "Can not get schema class for null metadata");
            Debug.assertInternal(metadata instanceof ClassMetaData,
                    metadata.getClass() + " is not of type ClassMetaData");
            Debug.assertInternal(model != null,
                    "Can not define class with null ApplicationView");
        }

        ClassMetaData cmd = (ClassMetaData)metadata;
        UserSchemaClass result = model.getAssociatedSchemaClass(cmd);
        if (result == null) {
            String schemaClassName = _namingPolicy.mapClassName(cmd);
            result = model.getSchemaClass(schemaClassName);
            if (result != null) {
                model.associateSchemaClass(cmd,result);
            }
        }
        if (result != null) {
            if (!result.isResolved()) {
                resolve(model, result);
            }
        } else {
            result = createNewClass(model, cmd);
            resolve(model, result);
        }
//        assert result.isResolved();
        if (Debug.DEBUG) {
            Debug.assertInternal(result.isResolved(),
                    "result is not resolved");
        }

        return result;
    }

    /**
     * Gets or generates a schema class definition that may or may not have
     * all its application properties set.
     */
    public UserSchemaClass getUserSchemaClassInternal(
            final UserSchemaModel aView,
            final ClassMetaData aJDOClass) {
        if (aJDOClass == null) throw new RuntimeException();
        UserSchemaClass result = aView.getAssociatedSchemaClass(aJDOClass);
        if (result != null) {
            return result;
        }

        String schemaClassName = _namingPolicy.mapClassName(aJDOClass);
        result = aView.getSchemaClass(schemaClassName);
        if (result != null) {
            aView.associateSchemaClass(aJDOClass, result);
            return result;
        }
        result = createNewClass(aView, aJDOClass);
        return result;
    }

    /**
     * Creates a new class definition and registers it to the model so that
     * others can refer to it. However, the class may not be resolved as
     * all related classes may not be resolved.
     * <p>New schema class definition requires the superclass definition being
     * available. Hence all superclasses are defined before this class.
     * <p/>
     * All the declared fields are created but not the inherited fields.
     */
    private UserSchemaClass createNewClass(final UserSchemaModel model,
            final ClassMetaData cmd) {
//        assert model.getAssociatedSchemaClass(cmd) == null;
        if (Debug.DEBUG) {
            Debug.assertInternal(model.getAssociatedSchemaClass(cmd) == null,
                    "associated SchemaClass is null");
        }
        cmd.notifyDataStoreOnDirtyOrDelete = true;
        ClassMetaData superCmd = cmd.pcSuperMetaData;
        UserSchemaClass[] superSchemaClass = null;
        if (superCmd != null) {
            superSchemaClass = new UserSchemaClass[1];
            superSchemaClass[0] = getUserSchemaClassInternal(model, superCmd);
        }
        String schemaClassName = _namingPolicy.mapClassName(cmd);
        String applicationName = cmd.qname;
        UserSchemaClass result = model.getAssociatedSchemaClass(cmd);
        // superclasses may have defined me
        if (result != null) return result;
        result = new UserSchemaClass(schemaClassName, superSchemaClass,
                model).setAuxiliaryInfo(applicationName);
        result.addListener(getBinder());
        cmd.storeClass = result;
        ClassInfo info = new ClassInfo();
        info.cmd = cmd;
        result.setUserObject(info);
        model.associateSchemaClass(cmd, result);
        FieldMetaData[] fields = cmd.fields;
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) continue;
            createNewField(model, fmd);
        }

        // add a fake field for the version if we are the topmost class
        if (superCmd == null) {
            FieldMetaData fmd = new FieldMetaData();
            fmd.fake = true;
            fmd.category = MDStatics.CATEGORY_SIMPLE;
            fmd.primaryField = true;
            fmd.classMetaData = cmd;
            fmd.defaultFetchGroup = true;
            fmd.name = TIMESTAMP_FIELD_NAME;
            fmd.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
            fmd.setType(Integer.TYPE);
            fmd.setAutoSet(MDStatics.AUTOSET_CREATED);
			//String qname = result.getName() + "::" + TIMESTAMP_FIELD_NAME;
            fmd.storeField = new VdsTimeStampField(fmd);
            addFakeField(result, fmd);
            cmd.optimisticLockingField = fmd;
        }

        // add fake fields onto the end of fmd.fields
        int n = info.fakeFields.size();
        if (n > 0) {
            FieldMetaData[] a = new FieldMetaData[fields.length + n];
            System.arraycopy(fields, 0, a, 0, fields.length);
            for (int i = 0; i < n; i++) {
                FieldMetaData fmd = (FieldMetaData)info.fakeFields.get(i);
                a[fmd.fieldNo = fields.length + i] = fmd;
            }
            cmd.fields = a;
        }

        // replace the ClassInfo with ClassMetaData as we no longer need it
        // and ClassMetaData is more convenient later
        result.setUserObject(cmd);

        return result;
    }

    /**
     * Add a fake field to the class.
     */
    public void addFakeField(UserSchemaClass schemaClass, FieldMetaData fmd) {
//        assert fmd.fake;
        if (Debug.DEBUG) {
            Debug.assertInternal(fmd.fake,
                    "FieldMetaData is not fake");
        }
        ClassInfo info = (ClassInfo)schemaClass.getUserObject();
        info.fakeFields.add(fmd);
    }

    /**
     * Get the ClassMetaData for the class.
     */
    private ClassMetaData getCMD(UserSchemaClass schemaClass) {
        try {
            ClassInfo info = (ClassInfo)schemaClass.getUserObject();
            return info == null ? null : info.cmd;
        } catch (ClassCastException ex){
            return (ClassMetaData)schemaClass.getUserObject();
        }
    }

    /**
     * Creates the field of the specified <code>aOwnerClass</code> from the given
     * <code>fmd</code>.
     */
    private VdsField createNewField(final UserSchemaModel model,
            final FieldMetaData fmd) {
//        assert fmd.persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
        if (Debug.DEBUG) {
            Debug.assertInternal(fmd.persistenceModifier ==
                    MDStatics.PERSISTENCE_MODIFIER_PERSISTENT,
                    "fmd.persistenceModifier is not " +
                    "MDStatics.PERSISTENCE_MODIFIER_PERSISTENT");
        }
        String schemaFieldName = _namingPolicy.mapFieldName(fmd);

        // The <code>fmd</code> is checked for all natively represented
        // classes. The factory of the schema field knows how to define a schema
        // field given a known Java class
        if (TranscriberAdapterFactory.getSupportedFieldTypes().contains(fmd.type) && fmd.type.getComponentType() == null) {
            return new VdsSimpleField(fmd, schemaFieldName);
        }

        // Otherwise use the category to decide what to do
        switch (fmd.category) {

            case MDStatics.CATEGORY_POLYREF:
                return new VdsRefField(fmd, schemaFieldName,
                        SystemSchemaClass.NULL_DOMAIN);

            case MDStatics.CATEGORY_REF:
                return new VdsRefField(fmd, schemaFieldName,
                        getUserSchemaClassInternal(model, fmd.typeMetaData));

            case MDStatics.CATEGORY_ARRAY:
                return new VdsArrayField(fmd, schemaFieldName, this, model);

            case MDStatics.CATEGORY_COLLECTION:
                return new VdsCollectionField(fmd, schemaFieldName, this,
                        model);

            case MDStatics.CATEGORY_MAP:
                return new VdsMapField(fmd, schemaFieldName, this,
                        model);

            case MDStatics.CATEGORY_EXTERNALIZED:
            	return new VdsExternalizedField(fmd, schemaFieldName, this,
                        model);
        }
//        assert false : fmd + " has no schema definition policy";
        if (Debug.DEBUG) {
            Debug.assertInternal(false,
                    fmd + " has no schema definition policy");
        }
        return null; // keep compiler happy
    }

    /**
     * Schema field that represents Array of primitives, primitive wrappers,
     * known classes (String,Locale etc), first-class objects, persistent but
     * unknown objects are created by this method. The field is added to the
     * specified <code>aOwnerClass</code>.
     *
     * @param model                 container of the user schema classes defined by this receiver
     * @param aOwnerClass           schema class that will own the field created by this method
     * @param aFieldName            name of the field to be created
     * @param aApplicationFieldName application name of the field
     * @param aFieldJavaClass       Java class of the value contained in the created field.
     *                              must be an array type.
     * @param elementTypeMetaData   meta data of the element type. May be null.
     * @param nullity               nullity flags to be added to the created field
     * @return
     */
    public UserSchemaField createNewArrayField(UserSchemaModel model,
            UserSchemaClass aOwnerClass,
            String aFieldName,
            String aApplicationFieldName,
            Class aFieldJavaClass,
            ClassMetaData elementTypeMetaData,
            int nullity) {
//        assert model != null;
//        assert aOwnerClass != null;
//        assert aFieldName != null;
//        assert aFieldJavaClass != null;
//        assert aFieldJavaClass.isArray();
//        assert !aFieldJavaClass.getComponentType().isArray();

        if (Debug.DEBUG) {
            Debug.assertInternal(model != null,
                    "model is null");
            Debug.assertInternal(aOwnerClass != null,
                    "aOwnerClass is null");
            Debug.assertInternal(aFieldName != null,
                    "aFieldName is null");
            Debug.assertInternal(aFieldJavaClass != null,
                    "aFieldJavaClass is null");
            Debug.assertInternal(aFieldJavaClass.isArray(),
                    "aFieldJavaClass is not a Array");
            Debug.assertInternal(!aFieldJavaClass.getComponentType().isArray(),
                    "aFieldJavaClass's componentType is a array");
        }

        if (TranscriberAdapterFactory.getSupportedFieldTypes().contains(aFieldJavaClass)) {
            return aOwnerClass.newField(aFieldName, 
                                        aFieldJavaClass,
                                        nullity);
        } else {
            SchemaClass domain;
            if (elementTypeMetaData == null) {
                domain = SystemSchemaClass.NULL_DOMAIN;
            } else {
                domain = getUserSchemaClassInternal(model, elementTypeMetaData);
            }
            UserSchemaField result = aOwnerClass.newDynamicField(
                                        aFieldName,
                                        domain,
                                        nullity);
            boolean hasNullSyntheticField = result.getSyntheticField(SchemaField.NULL_SYNTHETIC_FIELD) != null;
            Transcriber transcriber = new OIDArrayTranscriber(
                    					elementTypeMetaData, 
                    					hasNullSyntheticField);
            result.setTranscriber(transcriber);
            
            return result;
        }
    }

    /**
     * Get the schema name for a class.
     */
    public String getSchemaClassNameForJavaClass(ClassMetaData cm,
            Class aJavaClass) {
        if (cm != null) {
            return _namingPolicy.mapClassName(cm);
        } else if (isImplicitlyPersistenceCapable(aJavaClass)) {
            return "Object";
        }
        return dropPackageName(aJavaClass.getName());
    }

    private static String dropPackageName(String s) {
        int index = s.lastIndexOf('.');
        if (index < 0) {
            return s;
        }
        return s.substring(index + 1);
    }

    /**
     * Resolves the closure of a class object model by synchronizing or defining
     * it to the datastore.
     */
    private void resolve(final UserSchemaModel model,
            final UserSchemaClass aSchemaClass) {
        if (aSchemaClass.isResolved()) return;
        UserSchemaClass[] superSchemaClasses = aSchemaClass.getSuperClasses();
        for (int i = 0; i < superSchemaClasses.length; i++) {
            ClassMetaData superJDOClass = getCMD(superSchemaClasses[i]);
//            assert superJDOClass != null;
            if (Debug.DEBUG) {
                Debug.assertInternal(superJDOClass != null,
                        "superJDOClass is null");
            }
            resolve(model, superSchemaClasses[i]);
        }
        aSchemaClass.setResolved(true);
        UserSchemaField[] schemaFields = aSchemaClass.getDeclaredFields();
        for (int i = 0; i < schemaFields.length; i++) {
            UserSchemaField schemaField = schemaFields[i];
            SchemaClass domain = schemaField.getDomain();
            if (domain.isPrimitive()) {
                continue;
            }
            if (domain instanceof SystemSchemaClass) {
                continue;
            }
//            assert domain instanceof UserSchemaClass;
            if (Debug.DEBUG) {
                Debug.assertInternal(domain instanceof UserSchemaClass,
                        "domain is not a instanceof UserSchemaClass");
            }
            resolve(model, (UserSchemaClass)domain);
        }
//        ClassMetaData cmd = getCMD(aSchemaClass);
//        if (cmd == null) return;
//        FieldMetaData[] stateFields = cmd.stateFields;
//        aSchemaClass.setApplicationFieldCount(stateFields.length);
//        for (int i = 0; i < stateFields.length; i++) {
//            FieldMetaData fmd = stateFields[i];
//            if (fmd.fake || fmd.persistenceModifier != MDStatics.PERSISTENCE_MODIFIER_PERSISTENT)
//                continue;
//            UserSchemaField schemaField = null;
//            String applicationFieldName = fmd.name;
//            schemaField = aSchemaClass.getApplicationField(applicationFieldName);
//            if (schemaField == null)
//                throw new RuntimeException(applicationFieldName + " not found in " + aSchemaClass + " while resolving");
//            aSchemaClass.setApplicationFieldIndex(fmd.stateFieldNo, schemaField);
//        }
//        assert aSchemaClass.isResolved();
        if (Debug.DEBUG) {
            Debug.assertInternal(aSchemaClass.isResolved(),
                    "aSchemaClass is not resolved");
        }
    }

    /**
     * Answers in affirmative if the given class is implictly persistence capable.
     * A class is implictly persistence capable iff
     * <OL>
     * <LI>is an interface other than <code>java.io.Serializable
     * </OL>
     *
     * @param c is the given class whose persistence capability is to be determined
     * @return
     */
    public boolean isImplicitlyPersistenceCapable(Class c) {
        return c != null && c.isInterface() && c != java.io.Serializable.class;
    }

    protected void resolveOrdering(ClassMetaData cmd, boolean quiet) {
        // VDS does not support ordering yet so ignore these extensions
    }

}
