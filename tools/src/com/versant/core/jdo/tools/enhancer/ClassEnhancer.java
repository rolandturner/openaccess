
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
package com.versant.core.jdo.tools.enhancer;

import com.versant.lib.bcel.Constants;
import com.versant.lib.bcel.classfile.*;
import com.versant.lib.bcel.generic.*;
import com.versant.core.common.Debug;
import com.versant.core.jdo.tools.enhancer.info.ClassInfo;
import com.versant.core.jdo.tools.enhancer.info.FieldInfo;
import com.versant.core.jdo.tools.enhancer.utils.SerialUIDHelper;
import com.versant.core.jdo.tools.enhancer.utils.SwapFieldHelper;
import com.versant.core.jdo.tools.enhancer.utils.TableSwitchHelper;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.AppIdUtils;

import javax.jdo.JDOUserException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.identity.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.net.URL;

/**
 * This class does the enhancement on class files
 *
 */
public class ClassEnhancer {

    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;
    private InstructionFactory instructionFactory;
    private ClassInfo classInfo;
    private Set fieldSet;
    private File outputDir;
    private ClassLoader loader;
    private static int javaVersion;

    private boolean isEmpty = false;
    private boolean didWeAddADefaultConstructor = false;
    private boolean didWeAddACloneMethod = false;

    public static final int JAVA_1_0 = 10;
    public static final int JAVA_1_1 = 11;
    public static final int JAVA_1_2 = 12;
    public static final int JAVA_1_3 = 13;
    public static final int JAVA_1_4 = 14;
    public static final int JAVA_1_5 = 15;

    private static final String getField = com.versant.lib.bcel.generic.GETFIELD.class.getName();
    private static final String putField = com.versant.lib.bcel.generic.PUTFIELD.class.getName();
    private static final String invokeSpecial = com.versant.lib.bcel.generic.INVOKESPECIAL.class.getName();
    private static final String aload = com.versant.lib.bcel.generic.ALOAD.class.getName();
    private static final String dup = com.versant.lib.bcel.generic.DUP.class.getName();


    private HashMap getAndSettersMap;
    private HashMap typeToReturnType;
    private HashMap typeToSetField;
    private HashMap typeToFieldProvider;
    private HashMap typeToProvidedField;
    private HashMap typeToReplacingField;
    private HashMap typeToGetField;
    private HashMap typeToLoadType;
    private HashMap typeToFieldReplacer;

    private HashMap primativeTypesToWrapper;

    private String fileSeparator;
	private char charfileSeparator;         //
    private static final String VERSANT_STATE_MANAGER = com.versant.core.jdo.VersantStateManager.class.getName();

    private final static String PERSISTENCE_CAPABLE = javax.jdo.spi.PersistenceCapable.class.getName();
    private final static ObjectType PC_OBJECT_TYPE = new ObjectType(PERSISTENCE_CAPABLE);
    private final static String STATE_MANAGER = javax.jdo.spi.StateManager.class.getName();
    private final static ObjectType SM_OBJECT_TYPE = new ObjectType(STATE_MANAGER);
    private int CHECK_WRITE = javax.jdo.spi.PersistenceCapable.CHECK_WRITE;
    private int CHECK_READ_WRITE = javax.jdo.spi.PersistenceCapable.CHECK_READ + javax.jdo.spi.PersistenceCapable.CHECK_WRITE;
    private int MEDIATE_READ_WRITE = javax.jdo.spi.PersistenceCapable.MEDIATE_READ + javax.jdo.spi.PersistenceCapable.MEDIATE_WRITE;
    private int synthetic;
	private String vendorName = "jdoVersant";

    private long currentSerialVersionUID;
    private static final String DIRTY_FIELD_NAME = "jdoVersantDirty";
    private static final String LOADED_FIELD_NAME = "jdoVersantLoaded";
    private static final String OID_FIELD_NAME = "jdoVersantOID";
    private static final String VERSION_FIELD_NAME = "jdoVersantVersion";
    private static final String DETACHABLE_INTERFASE = com.versant.core.jdo.VersantDetachable.class.getName();
    private static final String DETACHED_STATE_MANAGER = com.versant.core.jdo.VersantDetachedStateManager.class.getName();

    public static final String STRING_IDENTITY = StringIdentity.class.getName();
    public static final String LONG_IDENTITY = LongIdentity.class.getName();
    public static final String INT_IDENTITY = IntIdentity.class.getName();
    public static final String SHORT_IDENTITY = ShortIdentity.class.getName();
    public static final String BYTE_IDENTITY = ByteIdentity.class.getName();
    public static final String CHAR_IDENTITY = CharIdentity.class.getName();

    private static final ObjectType LONG_IDENTITY_TYPE = new ObjectType(LONG_IDENTITY);
    private static final ObjectType STRING_IDENTITY_TYPE = new ObjectType(STRING_IDENTITY);
    private static final ObjectType SHORT_IDENTITY_TYPE = new ObjectType(SHORT_IDENTITY);
    private static final ObjectType INT_IDENTITY_TYPE = new ObjectType(INT_IDENTITY);
    private static final ObjectType CHAR_IDENTITY_TYPE = new ObjectType(CHAR_IDENTITY);
    private static final ObjectType BYTE_IDENTITY_TYPE = new ObjectType(BYTE_IDENTITY);

    private int totlalManagedFields = 0;
    private boolean detach;
    private File currentOutputFile;

    public static final ObjectType INTEGER_TYPE = new ObjectType(
            "java.lang.Integer");
    public static final ObjectType BYTE_TYPE = new ObjectType("java.lang.Byte");
    public static final ObjectType CHARACTER_TYPE = new ObjectType(
            "java.lang.Character");
    public static final ObjectType SHORT_TYPE = new ObjectType(
            "java.lang.Short");
    public static final ObjectType FLOAT_TYPE = new ObjectType(
            "java.lang.Float");
    public static final ObjectType DOUBLE_TYPE = new ObjectType(
            "java.lang.Double");
    public static final ObjectType LONG_TYPE = new ObjectType("java.lang.Long");
    public static final ObjectType BOOLEAN_TYPE = new ObjectType(
            "java.lang.Boolean");
    public static final ObjectType BITSET_TYPE = new ObjectType(
            "java.util.BitSet");

//	private static final String MAKE_HOLLOW_INTERFASE = com.versant.core.jdo.test.model.versantMakeHollow.class.getName();

    static {

        // Determine the Java version by looking at available classes
        // java.lang.StrictMath was introduced in JDK 1.3
        // java.lang.ThreadLocal was introduced in JDK 1.2
        // java.lang.Void was introduced in JDK 1.1
        // Count up version until a NoClassDefFoundError ends the try

        try {
            javaVersion = JAVA_1_0;
            Class.forName("java.lang.Void");
            javaVersion = JAVA_1_1;
            Class.forName("java.lang.ThreadLocal");
            javaVersion = JAVA_1_2;
            Class.forName("java.lang.StrictMath");
            javaVersion = JAVA_1_3;
            Class.forName("java.lang.CharSequence");
            javaVersion = JAVA_1_4;
//            Class.forName("java.lang.StringBuilder");
//            javaVersion = JAVA_1_5;
        } catch (ClassNotFoundException cnfe) {
            // swallow as we've hit the max class version that
            // we have
        }
    }

    public ClassEnhancer(File outputDir, ClassLoader loader) {
        this.outputDir = outputDir;
        this.loader = loader;
        fileSeparator = System.getProperty("file.separator");
		charfileSeparator = fileSeparator.charAt(0);

        typeToSetField = new HashMap();
        typeToSetField.put(Type.INT,    "setIntField");
        typeToSetField.put(Type.BYTE,   "setByteField");
        typeToSetField.put(Type.LONG,   "setLongField");
        typeToSetField.put(Type.CHAR,   "setCharField");
        typeToSetField.put(Type.SHORT,  "setShortField");
        typeToSetField.put(Type.FLOAT,  "setFloatField");
        typeToSetField.put(Type.DOUBLE, "setDoubleField");
        typeToSetField.put(Type.STRING, "setStringField");
        typeToSetField.put(Type.BOOLEAN,"setBooleanField");

        typeToFieldProvider = new HashMap();
        typeToFieldProvider.put(Type.INT,    "fetchIntField");
        typeToFieldProvider.put(Type.BYTE,   "fetchByteField");
        typeToFieldProvider.put(Type.CHAR,   "fetchCharField");
        typeToFieldProvider.put(Type.SHORT,  "fetchShortField");
        typeToFieldProvider.put(Type.FLOAT,  "fetchFloatField");
        typeToFieldProvider.put(Type.DOUBLE, "fetchDoubleField");
        typeToFieldProvider.put(Type.LONG,   "fetchLongField");
        typeToFieldProvider.put(Type.BOOLEAN,"fetchBooleanField");
        typeToFieldProvider.put(Type.STRING, "fetchStringField");

        typeToProvidedField = new HashMap();
        typeToProvidedField.put(Type.INT,   "providedIntField");
        typeToProvidedField.put(Type.BYTE,  "providedByteField");
        typeToProvidedField.put(Type.CHAR,  "providedCharField");
        typeToProvidedField.put(Type.SHORT, "providedShortField");
        typeToProvidedField.put(Type.FLOAT, "providedFloatField");
        typeToProvidedField.put(Type.DOUBLE,"providedDoubleField");
        typeToProvidedField.put(Type.LONG,  "providedLongField");
        typeToProvidedField.put(Type.BOOLEAN,"providedBooleanField");
        typeToProvidedField.put(Type.STRING,"providedStringField");

        typeToReplacingField = new HashMap();
        typeToReplacingField.put(Type.INT,      "replacingIntField");
        typeToReplacingField.put(Type.BYTE,     "replacingByteField");
        typeToReplacingField.put(Type.CHAR,     "replacingCharField");
        typeToReplacingField.put(Type.SHORT,    "replacingShortField");
        typeToReplacingField.put(Type.FLOAT,    "replacingFloatField");
        typeToReplacingField.put(Type.DOUBLE,   "replacingDoubleField");
        typeToReplacingField.put(Type.LONG,     "replacingLongField");
        typeToReplacingField.put(Type.BOOLEAN,  "replacingBooleanField");
        typeToReplacingField.put(Type.STRING,   "replacingStringField");

        typeToFieldReplacer = new HashMap();
        typeToFieldReplacer.put(Type.INT,      "storeIntField");
        typeToFieldReplacer.put(Type.BYTE,     "storeByteField");
        typeToFieldReplacer.put(Type.CHAR,     "storeCharField");
        typeToFieldReplacer.put(Type.SHORT,    "storeShortField");
        typeToFieldReplacer.put(Type.FLOAT,    "storeFloatField");
        typeToFieldReplacer.put(Type.DOUBLE,   "storeDoubleField");
        typeToFieldReplacer.put(Type.LONG,     "storeLongField");
        typeToFieldReplacer.put(Type.BOOLEAN,  "storeBooleanField");
        typeToFieldReplacer.put(Type.STRING,   "storeStringField");

        typeToGetField = new HashMap();
        typeToGetField.put(Type.INT,    "getIntField");
        typeToGetField.put(Type.BYTE,   "getByteField");
        typeToGetField.put(Type.CHAR,   "getCharField");
        typeToGetField.put(Type.SHORT,  "getShortField");
        typeToGetField.put(Type.FLOAT,  "getFloatField");
        typeToGetField.put(Type.DOUBLE, "getDoubleField");
        typeToGetField.put(Type.LONG,   "getLongField");
        typeToGetField.put(Type.BOOLEAN,"getBooleanField");
        typeToGetField.put(Type.STRING, "getStringField");

        typeToReturnType = new HashMap();
        typeToReturnType.put(Type.INT,      new IRETURN());
        typeToReturnType.put(Type.BYTE,     new IRETURN());
        typeToReturnType.put(Type.CHAR,     new IRETURN());
        typeToReturnType.put(Type.SHORT,    new IRETURN());
        typeToReturnType.put(Type.FLOAT,    new FRETURN());
        typeToReturnType.put(Type.DOUBLE,   new DRETURN());
        typeToReturnType.put(Type.LONG,     new LRETURN());
        typeToReturnType.put(Type.BOOLEAN,  new IRETURN());
        typeToReturnType.put(Type.STRING,   new ARETURN());

        typeToLoadType = new HashMap();
        typeToLoadType.put(Type.INT,      new ILOAD(1));
        typeToLoadType.put(Type.BYTE,     new ILOAD(1));
        typeToLoadType.put(Type.CHAR,     new ILOAD(1));
        typeToLoadType.put(Type.SHORT,    new ILOAD(1));
        typeToLoadType.put(Type.FLOAT,    new FLOAD(1));
        typeToLoadType.put(Type.DOUBLE,   new DLOAD(1));
        typeToLoadType.put(Type.LONG,     new LLOAD(1));
        typeToLoadType.put(Type.BOOLEAN,  new ILOAD(1));
        typeToLoadType.put(Type.STRING,   new ALOAD(1));


        primativeTypesToWrapper = new HashMap(8);
        primativeTypesToWrapper.put(Type.INT, INTEGER_TYPE);
        primativeTypesToWrapper.put(Type.BYTE, BYTE_TYPE);
        primativeTypesToWrapper.put(Type.CHAR, CHARACTER_TYPE);
        primativeTypesToWrapper.put(Type.SHORT, SHORT_TYPE);
        primativeTypesToWrapper.put(Type.FLOAT, FLOAT_TYPE);
        primativeTypesToWrapper.put(Type.DOUBLE, DOUBLE_TYPE);
        primativeTypesToWrapper.put(Type.LONG, LONG_TYPE);
        primativeTypesToWrapper.put(Type.BOOLEAN, BOOLEAN_TYPE);


    }



    public void setGetAndSettersMap(HashMap map){
        getAndSettersMap = map;
    }

    private JavaClass getJavaClass(String className)throws IOException{
        String classFileName = className.replace('.','/')+".class";
        InputStream inputStream = loader.getResourceAsStream(classFileName);
        if (inputStream == null){
	        inputStream = loader.getResourceAsStream("/" + classFileName);
	        if (inputStream == null){
		        throw new javax.jdo.JDOFatalUserException(
                    "Class not found: " + className);
	        }
        }
        ClassParser parser = new ClassParser(inputStream, classFileName);
        return parser.parse();
    }

    private JavaClass getOrigJavaClass(String className) throws IOException {
        String classFileName = className.replace('.', '/') + ".class";
        InputStream inputStream = loader.getResourceAsStream(classFileName);
        URL currentFileURL = loader.getResource(classFileName);
        if (currentFileURL.toString().startsWith("jar:") && outputDir == null){
            throw new javax.jdo.JDOFatalUserException("Can not write class "+ className +" into a jar. Please specify a output directory.");
        }
        currentOutputFile = new File(currentFileURL.getFile());
        if (inputStream == null) {
            inputStream = loader.getResourceAsStream("/" + classFileName);
            currentFileURL = loader.getResource("/" + classFileName);
            if (currentFileURL.toString().startsWith("jar:") && outputDir == null) {
                throw new javax.jdo.JDOFatalUserException("Can not write class " + className + " into a jar. Please specify a output directory.");
            }
            currentOutputFile = new File(currentFileURL.getFile());
            if (inputStream == null) {
                throw new javax.jdo.JDOFatalUserException("Class not found: " + className);
            }
        }
        ClassParser parser = new ClassParser(inputStream, classFileName);
        return parser.parse();
    }

    public boolean enhance(ClassInfo classInfo,ClassMetaData cmd, boolean makeFieldsPrivate, boolean detached){
        try{
            this.detach = detached;
            this.classInfo = classInfo;
            fieldSet = classInfo.getFieldList();
            if (fieldSet.isEmpty()){
                isEmpty = true;
            } else {
                isEmpty = false;
            }
            JavaClass javaClass = getOrigJavaClass(classInfo.getClassName());
            classGen = new ClassGen(javaClass);
            // ConstantPoolGen is used to represent the constant pool of a Classfile            ll
            constantPoolGen = classGen.getConstantPool();
            // used to create objects representing VM instructions
            instructionFactory = new InstructionFactory(constantPoolGen);

             if (implementsPC()){ //if the class already implements PC, don't enhance
                return false;
            }
            currentSerialVersionUID = SerialUIDHelper.computeSerialVersionUID(javaClass);
            synthetic = classGen.getConstantPool().addUtf8("Synthetic");


            boolean topClass = classInfo.getTopPCSuperClass() == null;
            boolean appIdentity = classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION;
            didWeAddADefaultConstructor = false;
            didWeAddACloneMethod = false;


            //Tasks
            rewriteStaticConstructor();
            setDefaultConstructor();
            setClass$();
            addSerialVersionUID();
            addJdoFieldNames();
            addJdoFieldFlags();
//            if (detach) addDetatchFields();
            if (topClass) addJdoStateManager();            //super
            if (topClass) addJdoFlags();                   //super
            addJdoInheritedFieldCount();
            addJdoPersistenceCapableSuperclass();
            addJdoFieldTypes();
            addJdoGetManagedFieldCount();
            if (topClass) addInterrogatives();             //super
            if (topClass) addJdoGetVersion();
            addFieldGetters();
            addFieldSetters();
            addJdoReplaceField();
            addJdoReplaceFields();               //super??????????????/
            addJdoProvideField();
            addJdoProvideFields();               //super??????????????/
            addJdoCopyFields();
            addJdoCopyField();
            if (topClass) addJdoPreSerialize();                //super
            addWriteObject();
            addReadObject();
            addRegisterClass();
            addJdoNewInstance1();
            addJdoNewInstance2();
            if (topClass) {
                addJdoNewObjectIdInstance1();
                addJdoNewObjectIdInstance2();
                addJdoNewObjectIdInstance3();
            }
            if (topClass) addJdoGetObjectId();                    //super
            if (topClass) addJdoGetTransactionalObjectId();       //super
            if (topClass) addJdoReplaceStateManager();            //super
            if (topClass) addJdoCopyKeyFieldsToObjectId1();
            if (topClass) addJdoCopyKeyFieldsToObjectId2();
            if (topClass) addJdoCopyKeyFieldsFromObjectId1();
            if (topClass) addJdoCopyKeyFieldsFromObjectId2();
            if (topClass) addJdoReplaceFlags();                   //super


            addJdoInterface();
            if (makeFieldsPrivate){
                setEnhancedFieldsPrivate();
            }
            swapGetterAndSetter();
            swapClone();

//	        addVersantMakeHollow(); // put it in later
            totlalManagedFields = 0;
            if (detach){
                if (topClass){
                    List hier = cmd.getHeirarchyList();
                    for (Iterator iterator = hier.iterator(); iterator.hasNext();) {
                        ClassMetaData data = (ClassMetaData) iterator.next();
                        totlalManagedFields += data.managedFields.length;
                    }
                    addFields();
                    addSetLoadedInt("versantSetLoaded");  //public void versantSetLoaded(int i);
                    addIsLoadedInt("versantIsLoaded"); //boolean versantIsLoaded(int i);
                    addIsDirty("versantIsDirty");   //boolean versantIsDirty();
                    addMakeDirtyInt("versantMakeDirty");
                    addIsDirtyInt("versantIsDirty");  //public boolean versantIsDirty(int fieldNo);
                    addSetOid("versantSetOID"); //public void versantSetOID(Object id);
                    addGetOid("versantGetOID"); //public Object versantGetOID();
                    addGetVersion("versantGetVersion"); //public Object versantGetVersion();
                    addSetVersion("versantSetVersion"); //public void versantSetVersion(Object version);
                    addGetStateManager("versantGetDetachedStateManager"); //StateManager versantGetDetachedStateManager();
                    addDetachInterfase();
                }
                addMakeDirtyString("versantMakeDirty"); //void versantMakeDirty(String s);// if the names change, change this method.
            }

            if (didWeAddADefaultConstructor) {
                isOurConstructorValid();
            }
            dumpClass();

            System.out.println("Persistence Capable = " + classInfo.getClassName());

        } catch (Exception e){
	        e.printStackTrace();
            if (Debug.DEBUG) {
                Debug.ERR.println("Error in Enhancer");
                e.printStackTrace(Debug.ERR);
            }
        }
        return true;
    }

    private void isOurConstructorValid() {
        //this gets done only for the least-derived persistence capable class.
        if (classInfo.getPersistenceCapableSuperclass() != null) {
            return;
        }
        String superName = classGen.getSuperclassName();
        try {
            JavaClass javaClass = getJavaClass(superName);
            ClassGen classGen = new ClassGen(javaClass);
            Method[] methods = classGen.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                // native and abstract methods don't have any code
                // so continue with next method
                if (m.isNative() || m.isAbstract()) {
                    continue;
                }
                if (m.getName().equals("<init>")) {              //is constructor
                    if (m.getSignature().equals("()V")) {        //is no args constructor
                        if (!m.isPrivate()) {
                            return;
                        }
                    }
                }
            }
            throw new JDOUserException("Could not create a valid default constructor for class "+ this.classGen.getClassName());
        } catch (IOException e) {
            // hide exception, this class is not on our classpath
        }
    }

    private void addDefaultConstructorToNonPersistantSuperClasses(String superName) {
        //this gets done only for the least-derived persistence capable class.
        if (superName == null){
            if (classInfo.getPersistenceCapableSuperclass() != null) {
                return;
            }
            superName = classGen.getSuperclassName();
        }

        try {
            JavaClass javaClass = getJavaClass(superName);
            ClassGen classGen = new ClassGen(javaClass);
            // ConstantPoolGen is used to represent the constant pool of a Classfile            ll
            ConstantPoolGen constantPoolGen = classGen.getConstantPool();

            Method[] methods = classGen.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                // native and abstract methods don't have any code
                // so continue with next method
                if (m.isNative() || m.isAbstract()) {
                    continue;
                }

                if (m.getName().equals("<init>")) {              //is constructor
                    if (m.getSignature().equals("()V")) {        //is no args constructor
                        if (m.isPublic()) {
                            return;
                        } else {                                 //there is a default constructor but access is wrong
                            m.isPublic(true);
                            m.isProtected(false);                //change access to protected
                            m.isPrivate(false);                 //take away private access
                            String fileName = classGen.getClassName().replace('.', charfileSeparator) + ".class";
                            File dumpFile = new File(outputDir, fileName);
                            try {
                                classGen.getJavaClass().dump(dumpFile);
                            } catch (IOException e) {
                                //hide, we could not write out the class
                            }
                            return;
                        }
                    }
                }
            }

            InstructionList il = new InstructionList();
            il.append(InstructionConstants.THIS); // Push `this'
            il.append(new INVOKESPECIAL(constantPoolGen.addMethodref(classGen.getSuperclassName(),
                    "<init>",
                    "()V")));
            il.append(InstructionConstants.RETURN);

            MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC,
                    // todo this is not to spec it should be protected
                    Type.VOID,
                    Type.NO_ARGS,
                    null,
                    "<init>",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();

            classGen.addMethod(methodGen.getMethod());

            String fileName = classGen.getClassName().replace('.', charfileSeparator) + ".class";
            File dumpFile = new File(outputDir, fileName);
            boolean error = false;
            try {
                classGen.getJavaClass().dump(dumpFile);
            } catch (IOException e) {
                //hide, we could not write out the class
                error = true;

            }

            if (!error){
                // set our next class
                // Print warning
                if (this.classGen.getSuperclassName().equals(classGen.getClassName())){
                    System.out.println("WARNING: persistence capable class '" +
                            this.classGen.getClassName() +
                            "' has a non persistence super class \n'"
                            + classGen.getClassName() +
                            "', that does not have a default constructor, will add one.");
                }

                addDefaultConstructorToNonPersistantSuperClasses(classGen.getSuperclassName());
            }

        } catch (IOException e) {
            // hide exception, this class is not on our classpath
        }
    }


    private void swapClone() {
        // representation of methods in the class
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];

            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()){
                continue;
            }
            //we do not want to enhance our enhanced methods
            if (m.getName().startsWith("<cl")){
                continue;
            }

            boolean changed = false;

            MethodGen mg = new MethodGen(m,classGen.getClassName(),constantPoolGen);

            boolean messedUpClone = false;
            // get the code in form of an InstructionList object
            InstructionList il = mg.getInstructionList();

            // get the first instruction
            InstructionHandle ih = il.getStart();

            while (ih != null) {
                Instruction ins = ih.getInstruction();
                if (ins.getClass().getName().equals(invokeSpecial)){
                    INVOKESPECIAL is = (INVOKESPECIAL)ins;
                    if ((is.getClassName(constantPoolGen).equals("java.lang.Object") &&
                            is.getMethodName(constantPoolGen).equals("clone") &&
                            is.getSignature(constantPoolGen).equals("()Ljava/lang/Object;")) ||
                            ((classInfo.getTopPCSuperClass() == null) &&
                                    ("clone".equals(is.getMethodName(constantPoolGen))) &&
                                    ("()Ljava/lang/Object;".equals(is.getSignature(constantPoolGen))))) {
                        messedUpClone = isBcelMessingUpLocalVars(mg);
                        if (messedUpClone){
                            add15Clone();
                            ih.setInstruction(instructionFactory.createInvoke(
                                    classGen.getClassName(),
                                    "jdoClone",
                                    Type.OBJECT,
                                    new Type[]{},
                                    Constants.INVOKESPECIAL));

                        } else {
                            il.append(is,getCloneIL());
                        }
                        il.setPositions();
                        il.update();
                        changed = true;

                    }
                }
                // next instruction
                ih = ih.getNext();
            }
            // don't forget to write the code
            if (changed){
                if (!messedUpClone){
                    il.setPositions();
                    il.update();
                    mg.setMaxLocals();
                    mg.setMaxStack();
                    classGen.replaceMethod(m, mg.getMethod());
                } else {
                    il.setPositions();
                    il.update();
                    mg.setMaxLocals();
                    mg.setMaxStack();
                    Method method = fixJdk15LocalVars(mg, m);
                    classGen.replaceMethod(m, method);
                }
            }
        }
    }

    private void add15Clone() {
        if (!didWeAddACloneMethod) {
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(Constants.ACC_PROTECTED,
                    Type.OBJECT,
                    new Type[]{},
                    new String[]{},
                    "jdoClone",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            il.append(new ALOAD(0));
            il.append(instructionFactory.createInvoke("java.lang.Object",
                    "clone",
                    Type.OBJECT,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
            il.append(instructionFactory.createCheckCast(new ObjectType(classGen.getClassName())));
            il.append(new ASTORE(1));
            InstructionHandle copy_From = il.append(new ALOAD(1));
            il.append(new ICONST(0));
            if (javaVersion >= JAVA_1_4) {
                il.append(instructionFactory.createPutField(classGen.getClassName(),
                        "jdoFlags",
                        Type.BYTE));
            } else {
                il.append(instructionFactory.createPutField(getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));
            }

            il.append(new ALOAD(1));
            il.append(new ACONST_NULL());
            if (javaVersion >= JAVA_1_4) {
                il.append(instructionFactory.createPutField(classGen.getClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
            } else {
                il.append(instructionFactory.createPutField(getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
            }

            il.append(new ALOAD(1));
            il.append(new ARETURN());

            methodGen.addLocalVariable("copy", new ObjectType(classGen.getClassName()), 1, copy_From, il.getEnd());


            methodGen.addException("java.lang.CloneNotSupportedException");
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
            didWeAddACloneMethod = true;
        }

    }

    private InstructionList getCloneIL(){
        InstructionList il = new InstructionList();
        il.append(new DUP());
        il.append(instructionFactory.createCheckCast(new ObjectType(classGen.getClassName())));
        il.append(new ACONST_NULL());
        if (javaVersion >= JAVA_1_4) {
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
        } else {
            il.append(instructionFactory.createPutField(getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
        }
        il.append(new DUP());
        il.append(instructionFactory.createCheckCast(new ObjectType(classGen.getClassName())));
        il.append(new ICONST(0));
        if (javaVersion >= JAVA_1_4) {
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    "jdoFlags",
                    Type.BYTE));
        } else {
            il.append(instructionFactory.createPutField(getTopPCSuperOrCurrentClassName(),
                    "jdoFlags",
                    Type.BYTE));
        }
        return il;


    }


//	private void addJdogenieMakeHollow() {
//
//        InstructionList il = new InstructionList();
//        MethodGen methodGen = new MethodGen(
//                Constants.ACC_PUBLIC ,
//                Type.VOID,
//                new Type[]{},
//                new String[]{},
//                vendorName+"MakeHollow",
//                classGen.getClassName(),
//                il,
//                constantPoolGen);
//
//		for (Iterator iterator = fieldSet.iterator();iterator.hasNext();) {
//			FieldInfo fieldInfo = (FieldInfo) iterator.next();
//			if (fieldInfo.isPrimative() && !fieldInfo.isArray()){
//				continue;
//			}
//			il.append(new ALOAD(0));
//			il.append(new ACONST_NULL());
//			il.append(instructionFactory.createPutField(
//			        classGen.getClassName(),
//			        fieldInfo.getFieldName(),
//			        fieldInfo.getType()));
//
//		}
//		if (classInfo.getPersistenceCapableSuperclass() != null){
//			il.append(new ALOAD(0));
//			il.append(instructionFactory.createInvoke(
//			        classInfo.getPersistenceCapableSuperclass() ,
//			        vendorName+"MakeHollow" ,
//			        Type.VOID ,
//			        new Type[]{} ,
//			        Constants.INVOKESPECIAL));
//			il.append(new RETURN());
//		} else {
//            il.append(new RETURN());
//		}
//
//        methodGen.setMaxLocals();
//        methodGen.setMaxStack();
//        classGen.addMethod(methodGen.getMethod());
//        il.dispose();
////		classGen.addInterface(MAKE_HOLLOW_INTERFASE);
//    }

	private void dumpClass()throws VerifyException{
        String fileName = classGen.getClassName().replace('.',charfileSeparator)+".class";
        File dumpFile;
        if (outputDir != null){
            dumpFile = new File(outputDir,fileName);
        } else {
            dumpFile = currentOutputFile;
        }
        try {
            classGen.getJavaClass().dump(dumpFile);
        } catch (IOException e) {
            throw new VerifyException(e);
        }

    }



    /** Copy fields to an outside source from the key fields in the ObjectId.
     * This method is generated in the PersistenceCapable class to generate
     * a call to the field manager for each key field in the ObjectId.  For
     * example, an ObjectId class that has three key fields (int id,
     * String name, and Float salary) would have the method generated:
     * <P>void copyKeyFieldsFromObjectId
     * <P>        (PersistenceCapable oid, ObjectIdFieldManager fm) {
     * <P>     fm.storeIntField (0, oid.id);
     * <P>     fm.storeStringField (1, oid.name);
     * <P>     fm.storeObjectField (2, oid.salary);
     * <P>}
     * <P>The implementation is responsible for implementing the
     * ObjectIdFieldManager to store the values for the key fields.
     * private void jdoCopyKeyFieldsFromObjectId(PersistenceCapable.ObjectIdFieldReplacer fm, Object oid){
     */
    private void addJdoCopyKeyFieldsFromObjectId1() {
        if (classInfo.getTopPCSuperClass() == null ||
                (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION && classInfo.getObjectidClass() != null)){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.VOID,
                    new Type[]{
                        new ObjectType("javax.jdo.spi.PersistenceCapable$ObjectIdFieldConsumer"),
                        Type.OBJECT
                    },
                    new String[]{"fc","oid"},
                    "jdoCopyKeyFieldsFromObjectId",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                    && classInfo.getObjectidClass() != null) {
                boolean isSingleField = AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass());
                String oidClassName = classInfo.getObjectidClass();
                Iterator iter = classInfo.getFieldList().iterator();
                int count = 0;
                boolean isObject = false;
                while (iter.hasNext()){
                    FieldInfo info = (FieldInfo)iter.next();
                    if (info.primaryKey()){
                        String fieldReplacerMethod = null;
                        if (typeToFieldReplacer.containsKey(info.getType())){
                            fieldReplacerMethod = (String)typeToFieldReplacer.get(info.getType());
                            isObject = false;
                        } else {
                            isObject = true;
                        }
                        il.append(new ALOAD(1));
                        il.append(instructionFactory.createGetStatic(
                                classGen.getClassName(),
                                "jdoInheritedFieldCount",
                                Type.INT));
                        il.append(new PUSH(constantPoolGen, count));
                        il.append(new IADD());

                        String wrapperClassName = null;
                        Type primType = null;
                        if (isSingleField) {
                            if (oidClassName.equals(STRING_IDENTITY)) {
                                primType = Type.STRING;
                            } else if (oidClassName.equals(LONG_IDENTITY)) {
                                wrapperClassName = Long.class.getName();
                                primType = Type.LONG;
                            } else if (oidClassName.equals(INT_IDENTITY)) {
                                wrapperClassName = Integer.class.getName();
                                primType = Type.INT;
                            } else if (oidClassName.equals(SHORT_IDENTITY)) {
                                wrapperClassName = Short.class.getName();
                                primType = Type.SHORT;
                            } else if (oidClassName.equals(BYTE_IDENTITY)) {
                                wrapperClassName = Byte.class.getName();
                                primType = Type.BYTE;
                            } else {
                                wrapperClassName = Character.class.getName();
                                primType = Type.CHAR;
                            }

                            if (isObject) {
                                il.append(instructionFactory.createNew(wrapperClassName));
                                il.append(InstructionConstants.DUP);
                            }
                        }
                        il.append(new ALOAD(2));
                        il.append(instructionFactory.createCheckCast(new ObjectType(classInfo.getObjectidClass())));
                        if (isSingleField) {
                            il.append(instructionFactory.createInvoke(
                                    classInfo.getObjectidClass(),
                                    "getKey",
                                    primType,
                                    Type.NO_ARGS,
                                    Constants.INVOKEVIRTUAL));
                        } else {
                            il.append(instructionFactory.createGetField(
                                    classInfo.getObjectidClass(),
                                    info.getFieldName(),
                                    info.getType()));
                        }
                        if (isObject){
                            if (isSingleField) {
                                il.append(instructionFactory.createInvoke(
                                        wrapperClassName,
                                        "<init>",
                                        Type.VOID,
                                        new Type[]{primType},
                                        Constants.INVOKESPECIAL));
                            }
                            il.append(instructionFactory.createInvoke(
                                    "javax.jdo.spi.PersistenceCapable$ObjectIdFieldConsumer",
                                    "storeObjectField",
                                    Type.VOID,
                                    new Type[]{
                                        Type.INT,
                                        Type.OBJECT},
                                    Constants.INVOKEINTERFACE));
                        } else {
                            il.append(instructionFactory.createInvoke(
                                    "javax.jdo.spi.PersistenceCapable$ObjectIdFieldConsumer",
                                    fieldReplacerMethod,
                                    Type.VOID,
                                    new Type[]{
                                        Type.INT,
                                        info.getType()},
                                    Constants.INVOKEINTERFACE));
                        }
                    }
                    count ++;
                }
            }
            il.append(new RETURN());
//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }

    private void addJdoCopyKeyFieldsFromObjectId2(){
        if (classInfo.getTopPCSuperClass() == null ||
                (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                && classInfo.getObjectidClass() != null)) {

            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PROTECTED,
                    Type.VOID,
                    new Type[]{
                        Type.OBJECT
                    },
                    new String[]{"oid"},
                    "jdoCopyKeyFieldsFromObjectId",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                    && classInfo.getObjectidClass() != null) {

                il.append(new ALOAD(1));
                il.append(instructionFactory.createCheckCast(new ObjectType(classInfo.getObjectidClass())));
                il.append(new ASTORE(2));
                InstructionHandle pckStartHandle = null;
                boolean first = true;
                ClassInfo currentClass = getTopPCSuperOrCurrentClass();
                Iterator iter = currentClass.getFieldList().iterator();
                while (iter.hasNext()){
                    FieldInfo info = (FieldInfo)iter.next();
                    if (info.primaryKey()){
                        if (first){
                            pckStartHandle = il.append(new ALOAD(0));
                            first = false;
                        } else {
                            il.append(new ALOAD(0));
                        }
                        if (AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass())) {
                            String objectIdClass = classInfo.getObjectidClass();

                            if (!info.isPrimative() && !info.getReturnType().equals("java.lang.String")) {
                                Type primitiveType = null;
                                if (objectIdClass.equals(LongIdentity.class.getName())) {
                                    primitiveType = Type.LONG;
                                } else if (objectIdClass.equals(CharIdentity.class.getName())) {
                                    primitiveType = Type.CHAR;
                                } else if (objectIdClass.equals(IntIdentity.class.getName())) {
                                    primitiveType = Type.INT;
                                } else if (objectIdClass.equals(ShortIdentity.class.getName())) {
                                    primitiveType = Type.SHORT;
                                } else if (objectIdClass.equals(ByteIdentity.class.getName())) {
                                    primitiveType = Type.BYTE;
                                }
                                il.append(instructionFactory.createNew(info.getReturnType()));
                                il.append(InstructionConstants.DUP);
                                il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
                                il.append(instructionFactory.createInvoke(
                                        objectIdClass,
                                        "getKey",
                                        primitiveType,
                                        Type.NO_ARGS,
                                        Constants.INVOKEVIRTUAL));
                                il.append(instructionFactory.createInvoke(
                                        info.getReturnType(),
                                        "<init>",
                                        Type.VOID,
                                        new Type[]{primitiveType},
                                        Constants.INVOKESPECIAL));
                                il.append(instructionFactory.createFieldAccess(
                                        classInfo.getClassName(),
                                        info.getFieldName(),
                                        info.getType(),
                                        Constants.PUTFIELD));
                            } else {  // Primitive type
                            il.append(new ALOAD(2));
                            il.append(instructionFactory.createInvoke(
                                    classInfo.getObjectidClass(),
                                    "getKey",
                                    info.getType(),
                                    Type.NO_ARGS,
                                    Constants.INVOKEVIRTUAL));
                            il.append(instructionFactory.createPutField(
                                    classInfo.getClassName(),
                                    info.getFieldName(),
                                    info.getType()));
                            }

                        } else {
                        il.append(new ALOAD(2));
                        il.append(instructionFactory.createGetField(
                                classInfo.getObjectidClass(),
                                info.getFieldName(),
                                info.getType()));
                        il.append(instructionFactory.createPutField(
                                classInfo.getClassName(),
                                info.getFieldName(),
                                info.getType()));
                        }
                    }
                }
                il.append(new RETURN());
                methodGen.addLocalVariable(
                        "pck",
                        new ObjectType(classInfo.getObjectidClass()),
                        2,
                        pckStartHandle,
                        il.getEnd());
            } else {
                il.append(new RETURN());
            }
//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }





    private String getTopPCSuperOrCurrentClassName(){
        if (classInfo.getTopPCSuperClass() == null){
            return classInfo.getClassName();
        } else {
            return classInfo.getTopPCSuperClass().getClassName();
        }
    }

    private ClassInfo getTopPCSuperOrCurrentClass(){
        if (classInfo.getTopPCSuperClass() == null){
            return classInfo;
        } else {
            return classInfo.getTopPCSuperClass();
        }
    }

    private void addJdoReplaceFlags(){
        if (classInfo.getTopPCSuperClass() == null){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC ,
                    Type.VOID,
                    new Type[]{},
                    new String[]{},
                    "jdoReplaceFlags",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            IFNULL ifnull = new IFNULL(null);
            il.append(ifnull);
            il.append(new ALOAD(0));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createInvoke(
                    STATE_MANAGER,
                    "replacingFlags",
                    Type.BYTE,
                    new Type[] {PC_OBJECT_TYPE},
                    Constants.INVOKEINTERFACE));
            il.append(instructionFactory.createPutField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoFlags",
                    Type.BYTE));
            InstructionHandle handle = il.append(new RETURN());
            ifnull.setTarget(handle);
//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();

            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }

    private boolean mustEnhance(Method m){
        String name = m.getName();
	    if (name.startsWith(vendorName+"MakeHollow")){
		    return false;
	    }
        if (name.startsWith(vendorName)){
	        return true;
        }else if (name.startsWith("jdo")){
            if (classInfo.isInstanceCallbacks() &&
                    (name.equals("jdoPreStore") || name.equals("jdoPreDelete"))
                    && m.getSignature().equals("()V")){
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void swapGetterAndSetter(){

        // representation of methods in the class
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];

            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()){
                continue;
            }
            //we do not want to enhance our enhanced methods
            if (m.getName().startsWith("<cl")){
                continue;
            }

            if (!mustEnhance(m)){
                continue;
            }

            boolean changed = false;
            boolean messedUp = false;

            MethodGen mg = new MethodGen(m,classGen.getClassName(),constantPoolGen);

            // get the code in form of an InstructionList object
            InstructionList il = mg.getInstructionList();

            // get the first instruction
            InstructionHandle ih = il.getStart();
            while (ih != null) {
                Instruction ins = ih.getInstruction();
                if (ins.getClass().getName().equals(getField)){//if (ins instanceof GETFIELD)
                    GETFIELD is = (GETFIELD)ins;
                    String key = is.getClassName(constantPoolGen) +"|"+ is.getFieldName(constantPoolGen);
                    if (getAndSettersMap.containsKey(key)) {
                        SwapFieldHelper helper = (SwapFieldHelper)getAndSettersMap.get(key);
                        messedUp = isBcelMessingUpLocalVars(mg);
                        // replace it with our static replacement method
                        ih.setInstruction(instructionFactory.createInvoke(
                                helper.className,
                                helper.jdoGetName,
                                helper.type,
                                new Type[] {
                                    new ObjectType(helper.className)
                                },
                                Constants.INVOKESTATIC));
                        il.setPositions();
                        il.update();

                        changed = true;
                        InstructionHandle prevIhDUP = ih.getPrev();
                        Instruction iffyDup = prevIhDUP.getInstruction();
                        if (iffyDup.getClass().getName().equals(dup)){  // The previos ist was a DUP
                            ih = ih.getPrev();
                            InstructionHandle prevIhALOAD = ih.getPrev();
                            Instruction iffyAload = prevIhALOAD.getInstruction();
                            if (iffyAload.getClass().getName().equals(aload)){ // The ist before that was a ALOAD
                                ALOAD aLoad = (ALOAD)iffyAload;
                                ih.setInstruction(aLoad); // Swap ref out
                                il.setPositions();
                                il.update();
                            }else {
                                ih = ih.getNext();
                            }
                        }
                    }
                } else if (ins.getClass().getName().equals(putField)){
                    PUTFIELD is = (PUTFIELD)ins;
                    String key = is.getClassName(constantPoolGen) +"|"+ is.getFieldName(constantPoolGen);
                    if (getAndSettersMap.containsKey(key)) {
                        SwapFieldHelper helper = (SwapFieldHelper)getAndSettersMap.get(key);
                        messedUp = isBcelMessingUpLocalVars(mg);
                        // replace it with our static replacement method
                        ih.setInstruction(instructionFactory.createInvoke(
                                helper.className,
                                helper.jdoSetName,
                                Type.VOID,
                                new Type[] {
                                    new ObjectType(helper.className),
                                    helper.type
                                },
                                Constants.INVOKESTATIC));
                        il.setPositions();
                        il.update();
                        changed = true;
                    }
                }
                // next instruction
                ih = ih.getNext();
            }
            if (changed){
                if (!messedUp) {
                    il.setPositions();
                    il.update();
                    mg.setMaxLocals();
                    mg.setMaxStack();
                    Method method = mg.getMethod();
                    classGen.replaceMethod(m, method);
                } else {
                    il.setPositions();
                    il.update();
                    mg.setMaxLocals();
                    mg.setMaxStack();
                    Method method = fixJdk15LocalVars(mg, m);
                    classGen.replaceMethod(m, method);
                }
            }
        }
    }

    private boolean isBcelMessingUpLocalVars(MethodGen mg){
        if (classGen.getMajor() >= 49) {
            Attribute[] attributes = mg.getCodeAttributes();
            for (int j = 0; j < attributes.length; j++) {
                return true;
            }
        }
        return false;
    }

    /**
     * BCEL messes up the local variable stuff, this method fixes it
     */
    private Method fixJdk15LocalVars(MethodGen mg, Method oldMethod) {
        Method method = mg.getMethod();
        LocalVariableTable oldVariableTable = oldMethod.getLocalVariableTable();
        if (oldVariableTable != null) {
            LocalVariable[] oldVariables = oldVariableTable.getLocalVariableTable();
            LocalVariableTable newVariableTable = method.getLocalVariableTable();
            LocalVariable[] newVariables = newVariableTable.getLocalVariableTable();
            for (int j = 0; j < oldVariables.length; j++) {
                LocalVariable oldVariable = oldVariables[j];
                int index = oldVariable.getIndex();
                for (int k = 0; k < newVariables.length; k++) {
                    LocalVariable newVariable = newVariables[k];
                    if (index == newVariable.getIndex() &&
                            (newVariable.getName().equals(oldVariable.getName()))) {
                        if (oldVariable.getStartPC() == newVariable.getStartPC() &&
                                oldVariable.getLength() != newVariable.getLength()) {
                            newVariable.setLength(oldVariable.getLength());
                        }

                    }
                }
            }
        }
        return method;
    }


    private void setEnhancedFieldsPrivate(){
        Field [] fields = classGen.getFields();
        Iterator iter = fieldSet.iterator();
        while (iter.hasNext()){
            FieldInfo info = (FieldInfo)iter.next();
            String fieldname = info.getFieldName();
            for (int i = 0;i < fields.length; i++) {
                Field f = fields[i];
                if (f.getName().equals(fieldname)){
                    if (!f.isPrivate()){
                        f.isProtected(false);
                        f.isPublic(false);
                        f.isPrivate(true);
                    }
                }
            }
        }
    }


    private void addJdoInterface(){
        classGen.addInterface(PERSISTENCE_CAPABLE);
    }


    private void addJdoCopyKeyFieldsToObjectId2(){
        if (classInfo.getTopPCSuperClass() == null ||
                (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION && classInfo.getObjectidClass() != null)) {
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.VOID,
                    new Type[]{
                        new ObjectType("javax.jdo.spi.PersistenceCapable$ObjectIdFieldSupplier"),
                        Type.OBJECT
                    },
                    new String[]{"fs","oid"},
                    "jdoCopyKeyFieldsToObjectId",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION && classInfo.getObjectidClass() != null) {
                if (AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass())) {
                    il.append(instructionFactory.createNew(JDOFatalInternalException.class.getName()));
                    il.append(InstructionConstants.DUP);
                    il.append(new LDC(classGen.getConstantPool().addString(
                            "It's illegal to call jdoCopyKeyFieldsToObjectId on a class with Single Field Identity.")));
                    il.append(instructionFactory.createInvoke(
                            JDOFatalInternalException.class.getName(),
                            Constants.CONSTRUCTOR_NAME,
                            Type.VOID,
                            new Type[]{Type.STRING},
                            Constants.INVOKESPECIAL));
                    il.append(InstructionConstants.ATHROW);
                } else {
                    Iterator iter = classInfo.getFieldList().iterator();
                    int count = 0;
                    boolean isObject = false;
                    while (iter.hasNext()){
                        FieldInfo info = (FieldInfo)iter.next();
                        if (info.primaryKey()){
                            String fieldProviderMethod = null;
                            if (typeToFieldProvider.containsKey(info.getType())){
                                fieldProviderMethod = (String)typeToFieldProvider.get(info.getType());
                                isObject = false;
                            } else {
                                isObject = true;
                            }
                            il.append(new ALOAD(2));
                            il.append(instructionFactory.createCheckCast(new ObjectType(classInfo.getObjectidClass())));
                            il.append(new ALOAD(1));
                            il.append(instructionFactory.createGetStatic(
                                    classGen.getClassName(),
                                    "jdoInheritedFieldCount",
                                    Type.INT));
                            il.append(new PUSH(constantPoolGen, count));
                            il.append(new IADD());
                            if (isObject){
                                il.append(instructionFactory.createInvoke(
                                        "javax.jdo.spi.PersistenceCapable$ObjectIdFieldSupplier",
                                        "fetchObjectField",
                                        Type.OBJECT,
                                        new Type[]{Type.INT},
                                        Constants.INVOKEINTERFACE));
                                il.append(instructionFactory.createCheckCast((ReferenceType)info.getType()));
                            } else {
                                il.append(instructionFactory.createInvoke(
                                        "javax.jdo.spi.PersistenceCapable$ObjectIdFieldSupplier",
                                        fieldProviderMethod,
                                        info.getType(),
                                        new Type[]{Type.INT},
                                        Constants.INVOKEINTERFACE));

                            }
                            il.append(instructionFactory.createPutField(classInfo.getObjectidClass(),
                                    info.getFieldName(),
                                    info.getType()));

                        }
                        count ++;
                    }
                }
            }
            il.append(new RETURN());

//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    // not cool, maybe ???????? test
    private void addJdoCopyKeyFieldsToObjectId1(){
        if (classInfo.getTopPCSuperClass() == null
                || (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                && classInfo.getObjectidClass() != null)) {
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.VOID,
                    new Type[]{
                        Type.OBJECT
                    },
                    new String[]{"oid"},
                    "jdoCopyKeyFieldsToObjectId",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                    && classInfo.getObjectidClass() != null) {
                if (AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass())) {
                    il.append(instructionFactory.createNew(JDOFatalInternalException.class.getName()));
                    il.append(InstructionConstants.DUP);
                    il.append(new LDC(classGen.getConstantPool().addString(
                            "It's illegal to call jdoCopyKeyFieldsToObjectId on a class with Single Field Identity.")));
                    il.append(instructionFactory.createInvoke(
                            JDOFatalInternalException.class.getName(),
                                    Constants.CONSTRUCTOR_NAME,
                                    Type.VOID,
                                    new Type[]{Type.STRING},
                                    Constants.INVOKESPECIAL));
                    il.append(InstructionConstants.ATHROW);
                } else {
                    Iterator iter = classInfo.getFieldList().iterator();
                    while (iter.hasNext()){
                        FieldInfo info = (FieldInfo)iter.next();
                        if (info.primaryKey()){
                            il.append(new ALOAD(1));
                            il.append(instructionFactory.createCheckCast(
                                    new ObjectType(classInfo.getObjectidClass())));
                            il.append(new ALOAD(0));
                            il.append(instructionFactory.createGetField(
                                    classInfo.getClassName(),
                                    info.getFieldName(),
                                    info.getType()));
                            il.append(instructionFactory.createPutField(
                                    classInfo.getObjectidClass(),
                                    info.getFieldName(),
                                    info.getType()));
                        }
                    }
                }
            }
            il.append(new RETURN());
//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    private void addJdoNewObjectIdInstance1(){
        if (classInfo.getTopPCSuperClass() == null ||
                (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                && classInfo.getObjectidClass() != null)) {
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.OBJECT,
                    null,
                    null,
                    "jdoNewObjectIdInstance",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                    && classInfo.getObjectidClass() != null) {
                if (AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass())){
                    String oidClassName = classInfo.getObjectidClass();
                    ObjectType oidType;

                    if (oidClassName.equals(STRING_IDENTITY)) {
                        oidType = STRING_IDENTITY_TYPE;
                    } else if (oidClassName.equals(LONG_IDENTITY)) {
                        oidType = LONG_IDENTITY_TYPE;
                    } else if (oidClassName.equals(INT_IDENTITY)) {
                        oidType = INT_IDENTITY_TYPE;
                    } else if (oidClassName.equals(SHORT_IDENTITY)) {
                        oidType = SHORT_IDENTITY_TYPE;
                    } else if (oidClassName.equals(BYTE_IDENTITY)) {
                        oidType = BYTE_IDENTITY_TYPE;
                    } else {
                        oidType = CHAR_IDENTITY_TYPE;
                    }
                    for (Iterator iter = classInfo.getFieldList().iterator(); iter.hasNext();) {
                        FieldInfo f = (FieldInfo) iter.next();
                        if (f.primaryKey()) {

                            il.append(instructionFactory.createNew(oidType));
                            il.append(InstructionConstants.DUP);
                            il.append(InstructionConstants.ALOAD_0);
                            il.append(instructionFactory.createInvoke(
                                    getTopPCSuperOrCurrentClass().getClassName(),
                                    "getClass",
                                    new ObjectType("java.lang.Class"),
                                    Type.NO_ARGS,
                                    Constants.INVOKEVIRTUAL));
                            il.append(InstructionConstants.ALOAD_0);
                            il.append(instructionFactory.createGetField(
                                    getTopPCSuperOrCurrentClass().getClassName(),
                                    f.getFieldName(),
                                    f.getType()));
                            il.append(instructionFactory.createInvoke(
                                    oidType.getClassName(),
                                    Constants.CONSTRUCTOR_NAME,
                                    Type.VOID,
                                    new Type[]{new ObjectType("java.lang.Class"),
                                            f.getType()},
                                    Constants.INVOKESPECIAL));
                        }
                    }
                } else {
                    il.append(instructionFactory.createNew(classInfo.getObjectidClass()));
                    il.append(new DUP());
                    il.append(instructionFactory.createInvoke(
                            classInfo.getObjectidClass(),
                            Constants.CONSTRUCTOR_NAME,
                            Type.VOID,
                            new Type[]{},
                            Constants.INVOKESPECIAL));
                }
            } else {
                il.append(new ACONST_NULL());
            }
            il.append(new ARETURN());

            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    private void addJdoNewObjectIdInstance2(){
        if (classInfo.getTopPCSuperClass() == null ||
                (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION && classInfo.getObjectidClass() != null)){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.OBJECT,
                    new Type[]{Type.STRING},
                    new String[]{"str"},
                    "jdoNewObjectIdInstance",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                    && classInfo.getObjectidClass() != null) {
                if (AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass())) {
                    String oidClassName = classInfo.getObjectidClass();
                    ObjectType oidType;


                    if (oidClassName.equals(STRING_IDENTITY)) {
                        oidType = STRING_IDENTITY_TYPE;
                    } else if (oidClassName.equals(LONG_IDENTITY)) {
                        oidType = LONG_IDENTITY_TYPE;
                    } else if (oidClassName.equals(INT_IDENTITY)) {
                        oidType = INT_IDENTITY_TYPE;
                    } else if (oidClassName.equals(SHORT_IDENTITY)) {
                        oidType = SHORT_IDENTITY_TYPE;
                    } else if (oidClassName.equals(BYTE_IDENTITY)) {
                        oidType = BYTE_IDENTITY_TYPE;
                    } else {
                        oidType = CHAR_IDENTITY_TYPE;
                    }


                    il.append(instructionFactory.createNew(oidType));
                    il.append(InstructionConstants.DUP);
                    il.append(new ALOAD(0));
                    il.append(instructionFactory.createInvoke(
                            "java.lang.Object",
                            "getClass",
                            new ObjectType("java.lang.Class"),
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                    il.append(new ALOAD(1));
                    il.append(instructionFactory.createInvoke(
                            oidType.getClassName(),
                            Constants.CONSTRUCTOR_NAME,
                            Type.VOID,
                            new Type[]{new ObjectType("java.lang.Class"),
                                    Type.STRING},
                            Constants.INVOKESPECIAL));
                    il.append(new ARETURN());
                } else {
                    il.append(instructionFactory.createNew(classInfo.getObjectidClass()));
                    il.append(new DUP());
                    il.append(new ALOAD(1));
                    il.append(instructionFactory.createInvoke(
                            classInfo.getObjectidClass(),
                            "<init>",
                            Type.VOID,
                            new Type[]{Type.STRING},
                            Constants.INVOKESPECIAL));
                    il.append(new ARETURN());
                }
            } else {
                il.append(new ACONST_NULL());
                il.append(new ARETURN());
            }


            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }

    private void addJdoNewObjectIdInstance3() throws IOException {
        if (classInfo.getTopPCSuperClass() == null ||
                (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION && classInfo.getObjectidClass() != null)) {
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC,
                    Type.OBJECT,
                    new Type[]{Type.OBJECT},
                    new String[]{"key"},
                    "jdoNewObjectIdInstance",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION
                    && classInfo.getObjectidClass() != null) {
                if (AppIdUtils.isSingleFieldIdentityClass(classInfo.getObjectidClass())) {
                    String oidClassName = classInfo.getObjectidClass();
                    ObjectType oidType;
                    ObjectType oidKeyType;

                    if (oidClassName.equals(STRING_IDENTITY)) {
                        oidType = STRING_IDENTITY_TYPE;
                        oidKeyType = Type.STRING;
                    } else if (oidClassName.equals(LONG_IDENTITY)) {
                        oidType = LONG_IDENTITY_TYPE;
                        oidKeyType = new ObjectType(Long.class.getName());
                    } else if (oidClassName.equals(INT_IDENTITY)) {
                        oidType = INT_IDENTITY_TYPE;
                        oidKeyType = new ObjectType(Integer.class.getName());
                    } else if (oidClassName.equals(SHORT_IDENTITY)) {
                        oidType = SHORT_IDENTITY_TYPE;
                        oidKeyType = new ObjectType(Short.class.getName());
                    } else if (oidClassName.equals(BYTE_IDENTITY)) {
                        oidType = BYTE_IDENTITY_TYPE;
                        oidKeyType = new ObjectType(Byte.class.getName());
                    } else {
                        oidType = CHAR_IDENTITY_TYPE;
                        oidKeyType = new ObjectType(Character.class.getName());
                    }


                    il.append(new ALOAD(1));
                    BranchInstruction checkKeyIsNull = new IFNONNULL(null);
                    il.append(checkKeyIsNull);
                    il.append(instructionFactory.createNew(IllegalArgumentException.class.getName()));
                    il.append(InstructionConstants.DUP);
                    il.append(new LDC(classGen.getConstantPool().addString("key is null")));
                    il.append(
                            instructionFactory.createInvoke(
                                    IllegalArgumentException.class.getName(),
                                    Constants.CONSTRUCTOR_NAME,
                                    Type.VOID,
                                    new Type[]{Type.STRING},
                                    Constants.INVOKESPECIAL));
                    il.append(InstructionConstants.ATHROW);
                    checkKeyIsNull.setTarget(il.append(InstructionConstants.ALOAD_1));
                    il.append(instructionFactory.createInstanceOf(Type.STRING));
                    il.append(InstructionConstants.ICONST_1);
                    BranchInstruction isInstanceof = new IF_ICMPEQ(null);
                    il.append(isInstanceof);

                    // new oidType(getClass(), (oidKeyType)key);
                    il.append(instructionFactory.createNew(oidType));
                    il.append(InstructionConstants.DUP);
                    il.append(InstructionConstants.ALOAD_0);
                    il.append(instructionFactory.createInvoke(
                            "java.lang.Object",
                            "getClass",
                            new ObjectType("java.lang.Class"),
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                    il.append(InstructionConstants.ALOAD_1);
                    il.append(instructionFactory.createCheckCast(oidKeyType));
                    il.append(instructionFactory.createInvoke(
                            oidType.getClassName(),
                            Constants.CONSTRUCTOR_NAME,
                            Type.VOID,
                            new Type[]{new ObjectType("java.lang.Class"),
                                    oidKeyType},
                            Constants.INVOKESPECIAL));

                    // "return"
                    il.append(InstructionFactory.createReturn(Type.OBJECT));

                    // "new oidType(getClass(), (String)key);"
                    isInstanceof.setTarget(il.append(instructionFactory.createNew(oidType)));
                    il.append(InstructionConstants.DUP);
                    il.append(InstructionConstants.ALOAD_0);
                    il.append(instructionFactory.createInvoke(
                            "java.lang.Object",
                            "getClass",
                            new ObjectType("java.lang.Class"),
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                    il.append(InstructionConstants.ALOAD_1);
                    il.append(instructionFactory.createCheckCast(Type.STRING));
                    il.append(instructionFactory.createInvoke(
                            oidType.getClassName(),
                            Constants.CONSTRUCTOR_NAME,
                            Type.VOID,
                            new Type[]{new ObjectType("java.lang.Class"),
                                    Type.STRING},
                            Constants.INVOKESPECIAL));
                    il.append(InstructionFactory.createReturn(Type.OBJECT));
                } else {

                    Collection types =  getConstructorArgumentTypes(classInfo.getObjectidClass());
                    for (Iterator iter = types.iterator(); iter.hasNext();) {
                        Type type = (Type) iter.next();
                        il.append(new ALOAD(1));
                        if (type instanceof ObjectType) {
                            il.append(new INSTANCEOF(constantPoolGen.addClass((ObjectType) type)));
                        } else if (type instanceof ArrayType){
                            il.append(new INSTANCEOF(constantPoolGen.addArrayClass((ArrayType) type)));
                        }
                        IFEQ ifeq = new IFEQ(null);
                        il.append(ifeq);
                        il.append(instructionFactory.createNew(classInfo.getObjectidClass()));
                        il.append(new DUP());
                        il.append(new ALOAD(1));
                        if (!type.equals(Type.OBJECT)){
                            il.append(instructionFactory.createCheckCast((ReferenceType) type));
                        }
                        il.append(instructionFactory.createInvoke(classInfo.getObjectidClass(),
                                "<init>",
                                Type.VOID,
                                new Type[]{type},
                                Constants.INVOKESPECIAL));
                        il.append(new ARETURN());
                        InstructionHandle aload1Handle = il.append(new NOP());
                        ifeq.setTarget(aload1Handle);
                    }
                    il.append(new ACONST_NULL());
                    il.append(new ARETURN());
                }
            } else {
                il.append(new ACONST_NULL());
                il.append(new ARETURN());
            }

            methodGen.removeNOPs();
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }

    private void addJdoGetVersion(){
        //todo jdo2, implement this method
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC,
                Type.OBJECT,
                new Type[]{},
                new String[]{},
                "jdoGetVersion",
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ACONST_NULL());
        il.append(new ARETURN());
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addJdoIsDetached() {
        //todo jdo2, implement this method
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC,
                Type.BOOLEAN,
                new Type[]{},
                new String[]{},
                "jdoIsDetached",
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ICONST(0));
        il.append(new IRETURN());
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }


    private Collection getConstructorArgumentTypes(String className)
            throws IOException {
        JavaClass javaClass = getOrigJavaClass(className);
        ClassGen classGen = new ClassGen(javaClass);
        ArrayList argumentTypes = new ArrayList();
        Method[] methods = classGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()) {
                continue;
            }
            if (m.getName().equals("<init>")) {//is constructor
                Type[] types = m.getArgumentTypes();
                if (types.length == 1 &&
                        (types[0] instanceof ObjectType ||
                        types[0] instanceof ArrayType)){
                    argumentTypes.add(types[0]);
                }
            }
        }
        return argumentTypes;
    }


    private void addJdoReplaceStateManager(){
        if (classInfo.getTopPCSuperClass() == null){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC  | Constants.ACC_SYNCHRONIZED,
                    Type.VOID,
                    new Type[]{SM_OBJECT_TYPE},
                    new String[]{"sm"},
                    "jdoReplaceStateManager",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);


            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            IFNULL ifnullInst1 = new IFNULL(null);
            il.append(ifnullInst1);
            il.append(new ALOAD(0));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ALOAD(0));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke(
                    STATE_MANAGER,
                    "replacingStateManager",
                    SM_OBJECT_TYPE,
                    new Type[]{
                        PC_OBJECT_TYPE,
                        SM_OBJECT_TYPE},
                    Constants.INVOKEINTERFACE));
            il.append(instructionFactory.createPutField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            GOTO gotoInst = new GOTO(null);
            il.append(gotoInst);
            InstructionHandle secManHandle = il.append(instructionFactory.createInvoke(
                    "java.lang.System",
                    "getSecurityManager",
                    new ObjectType("java.lang.SecurityManager"),
                    new Type[]{},
                    Constants.INVOKESTATIC));
            ifnullInst1.setTarget(secManHandle);
            il.append(new ASTORE(2));
            InstructionHandle startSecHandle = il.append(new ALOAD(2));
            IFNULL ifnullInst2 = new IFNULL(null);
            il.append(ifnullInst2);
            il.append(new ALOAD(2));
            il.append(instructionFactory.createGetStatic(
                    "javax.jdo.spi.JDOPermission",
                    "SET_STATE_MANAGER",
                    new ObjectType("javax.jdo.spi.JDOPermission")));
            il.append(instructionFactory.createInvoke(
                    "java.lang.SecurityManager",
                    "checkPermission",
                    Type.VOID,
                    new Type[]{new ObjectType("java.security.Permission")},
                    Constants.INVOKEVIRTUAL));
            InstructionHandle ifnullHandle = il.append(new ALOAD(0));
            ifnullInst2.setTarget(ifnullHandle);
            il.append(new ALOAD(1));
            il.append(instructionFactory.createPutField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            InstructionHandle gotoHandle = il.append(new RETURN());
            gotoInst.setTarget(gotoHandle);
            methodGen.addLocalVariable(
                    "sec",
                    new ObjectType("java.lang.SecurityManager"),
                    2,
                    startSecHandle,
                    il.getEnd());


            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }

    }


    private void addJdoGetTransactionalObjectId(){
        if (classInfo.getTopPCSuperClass() == null){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.OBJECT,
                    new Type[]{},
                    new String[]{},
                    "jdoGetTransactionalObjectId",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            IFNONNULL ifnonnullInst = new IFNONNULL(null);
            il.append(ifnonnullInst);
            il.append(new ACONST_NULL());
            GOTO gotoInst = new GOTO(null);
            il.append(gotoInst);
            InstructionHandle ifnonnullHandle = il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createInvoke(
                    STATE_MANAGER,
                    "getTransactionalObjectId",
                    Type.OBJECT,
                    new Type[]{PC_OBJECT_TYPE},
                    Constants.INVOKEINTERFACE));
            InstructionHandle gotoHandle = il.append(new ARETURN());
            gotoInst.setTarget(gotoHandle);
            ifnonnullInst.setTarget(ifnonnullHandle);

//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    private void addJdoGetObjectId(){
        if (classInfo.getTopPCSuperClass() == null){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC,
                    Type.OBJECT,
                    new Type[]{},
                    new String[]{},
                    "jdoGetObjectId",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);


            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            IFNONNULL ifnonnullInst = new IFNONNULL(null);
            il.append(ifnonnullInst);
            il.append(new ACONST_NULL());
            GOTO gotoInst = new GOTO(null);
            il.append(gotoInst);
            InstructionHandle ifnonnullHandle = il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createInvoke(
                    STATE_MANAGER,
                    "getObjectId",
                    Type.OBJECT,
                    new Type[]{PC_OBJECT_TYPE},
                    Constants.INVOKEINTERFACE));
            InstructionHandle gotoHandle = il.append(new ARETURN());
            gotoInst.setTarget(gotoHandle);
            ifnonnullInst.setTarget(ifnonnullHandle);

//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    private void addJdoNewInstance2(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC,
                PC_OBJECT_TYPE,
                new Type[]{SM_OBJECT_TYPE,Type.OBJECT},
                new String[]{"sm","oid"},
                "jdoNewInstance",
                classGen.getClassName(),
                il,
                constantPoolGen);
        if (classGen.isAbstract()){
            il.append(instructionFactory.createNew("javax.jdo.JDOFatalInternalException"));
            il.append(new DUP());
            il.append(instructionFactory.createInvoke(
                    "javax.jdo.JDOFatalInternalException",
                    "<init>",
                    Type.VOID,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
            il.append(new ATHROW());

        } else {
            if (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_DATASTORE){   // datastore
                il.append(instructionFactory.createNew(classGen.getClassName()));
                il.append(new DUP());
                il.append(instructionFactory.createInvoke(
                        classGen.getClassName(),
                        "<init>",
                        Type.VOID ,
                        new Type[]{},
                        Constants.INVOKESPECIAL));
                il.append(new ASTORE(3));
                InstructionHandle pcStartHandle = il.append(new ALOAD(3));
                il.append(new ALOAD(1));
                il.append(instructionFactory.createPutField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.append(new ALOAD(3));
                il.append(new ICONST(1));
                il.append(instructionFactory.createPutField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));
                il.append(new ALOAD(3));
                InstructionHandle returnHandle = il.append(new ARETURN());
                methodGen.addLocalVariable("pc", new ObjectType(classGen.getClassName()), 3, pcStartHandle, returnHandle);
            } else {// this class has application Identity
                il.append(instructionFactory.createNew(classGen.getClassName()));
                il.append(new DUP());
                il.append(instructionFactory.createInvoke(
                        classGen.getClassName(),
                        "<init>",
                        Type.VOID ,
                        new Type[]{},
                        Constants.INVOKESPECIAL));
                il.append(new ASTORE(3));
                InstructionHandle pcStartHandle = il.append(new ALOAD(3));
                il.append(new ALOAD(1));
                il.append(instructionFactory.createPutField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.append(new ALOAD(3));
                il.append(new ICONST(1));
                il.append(instructionFactory.createPutField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));
                il.append(new ALOAD(0));
                il.append(new ALOAD(2));
                il.append(instructionFactory.createInvoke(
                        getTopPCSuperOrCurrentClass().getClassName(),
                        "jdoCopyKeyFieldsFromObjectId",
                        Type.VOID ,
                        new Type[]{Type.OBJECT},
                        Constants.INVOKEVIRTUAL));

                il.append(new ALOAD(3));
                il.append(new ARETURN());
                methodGen.addLocalVariable("pc", new ObjectType(classGen.getClassName()), 3, pcStartHandle, il.getEnd());
            }
        }

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }


    private void addJdoNewInstance1(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC,
                PC_OBJECT_TYPE,
                new Type[]{SM_OBJECT_TYPE},
                new String[]{"sm"},
                "jdoNewInstance",
                classGen.getClassName(),
                il,
                constantPoolGen);
        if (classGen.isAbstract()){
            il.append(instructionFactory.createNew("javax.jdo.JDOFatalInternalException"));
            il.append(new DUP());
            il.append(instructionFactory.createInvoke(
                    "javax.jdo.JDOFatalInternalException",
                    "<init>",
                    Type.VOID,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
            il.append(new ATHROW());
        } else {
            il.append(instructionFactory.createNew(classGen.getClassName()));
            il.append(new DUP());
            il.append(instructionFactory.createInvoke(
                    classGen.getClassName(),
                    "<init>",
                    Type.VOID ,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
            il.append(new ASTORE(2));
            InstructionHandle pcStartHandle = il.append(new ALOAD(2));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createPutField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ALOAD(2));
            il.append(new ICONST(1));
            il.append(instructionFactory.createPutField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoFlags",
                    Type.BYTE));
            il.append(new ALOAD(2));
            InstructionHandle returnHandle = il.append(new ARETURN());
            methodGen.addLocalVariable(
                    "pc",
                    new ObjectType(classGen.getClassName()),
                    2,
                    pcStartHandle,
                    returnHandle);
        }

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }


    private void addRegisterClass(){
        Method m = getStaticConstructor();
        MethodGen methodGen = new MethodGen(
                m,
                classGen.getClassName(),
                constantPoolGen);
        InstructionList il = methodGen.getInstructionList();
        InstructionHandle returnHandle = il.getEnd();//The last instruction of <clinit> will always be the return

        String className = getSetClass$Field(classGen.getClassName());
        InstructionHandle nopTarget = il.append(new NOP());
        il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                className,
                new ObjectType("java.lang.Class")));
        IFNONNULL ifnonnull = new IFNONNULL(null);
        il.append(ifnonnull);
        il.append(new PUSH(constantPoolGen ,classGen.getClassName()));
        il.append(instructionFactory.createInvoke(
                classGen.getClassName(),
                "class$",
                new ObjectType("java.lang.Class") ,
                new Type[]{Type.STRING},
                Constants.INVOKESTATIC));
        il.append(new DUP());
        il.append(instructionFactory.createPutStatic(
                classGen.getClassName(),
                className,
                new ObjectType("java.lang.Class")));
        GOTO gotoIns = new GOTO(null);
        il.append(gotoIns);
        InstructionHandle ifnonnullHandle = il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                className,
                new ObjectType("java.lang.Class")));
        ifnonnull.setTarget(ifnonnullHandle);
        InstructionHandle gotoHandle = il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                "jdoFieldNames",
                new ArrayType("java.lang.String",1)));
        gotoIns.setTarget(gotoHandle);
        il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                "jdoFieldTypes",
                new ArrayType("java.lang.Class",1)));
        il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                "jdoFieldFlags",
                new ArrayType(Type.BYTE,1)));
        il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                "jdoPersistenceCapableSuperclass",
                new ObjectType("java.lang.Class")));
        if (classGen.isAbstract()){
            il.append(new ACONST_NULL());
        } else {
            il.append(instructionFactory.createNew(classGen.getClassName()));
            il.append(new DUP());
            il.append(instructionFactory.createInvoke(
                    classGen.getClassName(),
                    "<init>",
                    Type.VOID ,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
        }
        il.append(instructionFactory.createInvoke(
                "javax.jdo.spi.JDOImplHelper",
                "registerClass",
                Type.VOID ,
                new Type[]{
                    new ObjectType("java.lang.Class"),
                    new ArrayType(Type.STRING,1),
                    new ArrayType("java.lang.Class",1),
                    new ArrayType(Type.BYTE,1),
                    new ObjectType("java.lang.Class"),
                    PC_OBJECT_TYPE
                },
                Constants.INVOKESTATIC));
        il.append(new RETURN());
        try{
            il.delete(returnHandle);
        } catch (TargetLostException e){
            InstructionHandle[] targets = e.getTargets();
            for (int i = 0; i < targets.length ; i++){
                InstructionTargeter[] targeters = targets[i].getTargeters();
                for (int j = 0; j < targeters.length ; j++){
                    targeters[j].updateTarget(targets[i], nopTarget);
                }
            }
        }
        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(m, methodGen.getMethod());
        il.dispose();
    }


    private boolean writeObjectExist(){
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equals("writeObject") &&
                    m.getSignature().equals("(Ljava/io/ObjectOutputStream;)V")){
                return true;
            }
        }
        return false;

    }

    private Method getWriteObject() {
        Method[] methods = classGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equals("writeObject") &&
                    m.getSignature().equals("(Ljava/io/ObjectOutputStream;)V")) {
                return m;
            }
        }
        return null;

    }

    private boolean readObjectExist() {
        Method[] methods = classGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equals("readObject") &&
                    m.getSignature().equals("(Ljava/io/ObjectInputStream;)V")) {
                return true;
            }
        }
        return false;

    }

    private Method getReadObject() {
        Method[] methods = classGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equals("readObject") &&
                    m.getSignature().equals("(Ljava/io/ObjectInputStream;)V")) {
                return m;
            }
        }
        return null;

    }


    private void addWriteObject(){
        boolean rename = false;
        if (writeObjectExist()
                && classInfo.getTopPCSuperClass() == null) {
            // we must rename this method
            Method m = getWriteObject();
            MethodGen mg = new MethodGen(m, classGen.getClassName(), constantPoolGen);
            mg.setName("versantWriteObject");
            classGen.replaceMethod(m,mg.getMethod());
            rename = true;
        } else if (writeObjectExist() && classInfo.getTopPCSuperClass() != null){
            return;
        }
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PRIVATE,
                Type.VOID,
                new Type[]{new ObjectType("java.io.ObjectOutputStream")},
                new String[]{"out"},
                "writeObject",
                classGen.getClassName(),
                il,
                constantPoolGen);
        if (classInfo.getTopPCSuperClass() == null){
            il.append(new ALOAD(0));
            il.append(instructionFactory.createInvoke(
                    getTopPCSuperOrCurrentClass().getClassName(),
                    "jdoPreSerialize",
                    Type.VOID ,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
        }
        if (rename){
            il.append(new ALOAD(0));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke(
                    classGen.getClassName(),
                    "versantWriteObject",
                    Type.VOID,
                    new Type[]{new ObjectType("java.io.ObjectOutputStream")},
                    Constants.INVOKESPECIAL));
        } else {
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke(
                    "java.io.ObjectOutputStream",
                    "defaultWriteObject",
                    Type.VOID ,
                    new Type[]{},
                    Constants.INVOKEVIRTUAL));
        }

        if (classInfo.getTopPCSuperClass() == null && detach){
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    "jdoStateManager",
                    new ObjectType(STATE_MANAGER)));
            il.append(new INSTANCEOF(constantPoolGen.addClass(DETACHED_STATE_MANAGER)));
            IFEQ ifeq = new IFEQ(null);
            il.append(ifeq);
            il.append(new ALOAD(1));
            il.append(new ICONST(1));
            il.append(instructionFactory.createInvoke("java.io.ObjectOutputStream",
                    "writeBoolean",
                    Type.VOID,
                    new Type[]{Type.BOOLEAN},
                    Constants.INVOKEVIRTUAL));
            il.append(new ALOAD(1));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    "jdoStateManager",
                    new ObjectType(STATE_MANAGER)));
            il.append(instructionFactory.createInvoke("java.io.ObjectOutputStream",
                    "writeObject",
                    Type.VOID,
                    new Type[]{Type.OBJECT},
                    Constants.INVOKEVIRTUAL));
            il.append(new ALOAD(1));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    "jdoFlags",
                    Type.BYTE));
            il.append(instructionFactory.createInvoke("java.io.ObjectOutputStream",
                    "writeByte",
                    Type.VOID,
                    new Type[]{Type.INT},
                    Constants.INVOKEVIRTUAL));
            GOTO aGoto = new GOTO(null);
            il.append(aGoto);
            InstructionHandle aload1Handle = il.append(new ALOAD(1));
            ifeq.setTarget(aload1Handle);
            il.append(new ICONST(0));
            il.append(instructionFactory.createInvoke("java.io.ObjectOutputStream",
                    "writeBoolean",
                    Type.VOID,
                    new Type[]{Type.BOOLEAN},
                    Constants.INVOKEVIRTUAL));
            InstructionHandle returnHandle = il.append(new RETURN());
            aGoto.setTarget(returnHandle);
        } else {
            il.append(new RETURN());
        }

        methodGen.addException("java.io.IOException");
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();

    }

    private void addReadObject() {
        if (classInfo.getTopPCSuperClass() == null && detach) {
            boolean rename = false;
            if (readObjectExist()) {
                // we must rename this method
                Method m = getReadObject();
                MethodGen mg = new MethodGen(m, classGen.getClassName(), constantPoolGen);
                mg.setName("versantReadObject");
                classGen.replaceMethod(m, mg.getMethod());
                rename = true;
            }

            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(Constants.ACC_PRIVATE,
                    Type.VOID,
                    new Type[]{new ObjectType("java.io.ObjectInputStream")},
                    new String[]{"in"},
                    "readObject",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            if (rename){
                il.append(new ALOAD(0));
                il.append(new ALOAD(1));  il.append(instructionFactory.createInvoke(
                        classGen.getClassName(),
                        "versantReadObject",
                        Type.VOID,
                        new Type[]{new ObjectType("java.io.ObjectInputStream")},
                        Constants.INVOKESPECIAL));


            } else {
                il.append(new ALOAD(1));
                il.append(instructionFactory.createInvoke("java.io.ObjectInputStream",
                        "defaultReadObject",
                        Type.VOID,
                        new Type[]{},
                        Constants.INVOKEVIRTUAL));
            }
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke("java.io.ObjectInputStream",
                    "readBoolean",
                    Type.BOOLEAN,
                    new Type[]{},
                    Constants.INVOKEVIRTUAL));
            IFEQ ifeq = new IFEQ(null);
            il.append(ifeq);//22
            il.append(new ALOAD(0));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke("java.io.ObjectInputStream",
                    "readObject",
                    Type.OBJECT,
                    new Type[]{},
                    Constants.INVOKEVIRTUAL));
            il.append(instructionFactory.createCheckCast(new ObjectType(DETACHED_STATE_MANAGER)));
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    "jdoStateManager",
                    new ObjectType(STATE_MANAGER)));
            il.append(new ALOAD(0));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke("java.io.ObjectInputStream",
                    "readByte",
                    Type.BYTE,
                    new Type[]{},
                    Constants.INVOKEVIRTUAL));
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    "jdoFlags",
                    Type.BYTE));


            InstructionHandle returnHandle = il.append(new RETURN());
            ifeq.setTarget(returnHandle);

            methodGen.addException("java.io.IOException");
            methodGen.addException("java.lang.ClassNotFoundException");
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    private void addJdoPreSerialize(){
        if (classInfo.getTopPCSuperClass() == null){
            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PRIVATE,
                    Type.VOID,
                    new Type[]{},
                    new String[]{},
                    "jdoPreSerialize",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            InstructionHandle returnHandle = il.insert(new RETURN());
            il.insert(instructionFactory.createInvoke(
                    STATE_MANAGER,
                    "preSerialize",
                    Type.VOID ,
                    new Type[]{PC_OBJECT_TYPE},
                    Constants.INVOKEINTERFACE));
            il.insert(new ALOAD(0));
            il.insert(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.insert(new ALOAD(0));
            il.insert(new IFNULL(returnHandle));
            il.insert(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.insert(new ALOAD(0));

//            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
        }
    }


    private void addJdoCopyFields(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC,
                Type.VOID,
                new Type[]{Type.OBJECT, new ArrayType(Type.INT,1)},
                new String[]{"pc","fieldNumbers"},
                "jdoCopyFields",
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ALOAD(1));
        il.append(instructionFactory.createCheckCast(new ObjectType(classGen.getClassName())));
        il.append(new ASTORE(3));
        InstructionHandle otherStartHandle = il.append(new ALOAD(3));
        il.append(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        il.append(new ALOAD(0));
        il.append(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        IF_ACMPEQ if_acmpeq = new IF_ACMPEQ(null);
        il.append(if_acmpeq);
        il.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
        il.append(new DUP());
        il.append(new PUSH(constantPoolGen,"this.jdoStateManager != other.jdoStateManager"));
        il.append(instructionFactory.createInvoke(
                "java.lang.IllegalArgumentException",
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING},
                Constants.INVOKESPECIAL));
        il.append(new ATHROW());
        InstructionHandle if_acmpeqHandle = il.append(new ALOAD(0));
        if_acmpeq.setTarget(if_acmpeqHandle);
        il.append(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        IFNONNULL ifnonnull = new IFNONNULL(null);
        il.append(ifnonnull);
        il.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
        il.append(new DUP());
        il.append(new PUSH(constantPoolGen,"this.jdoStateManager == null"));
        il.append(instructionFactory.createInvoke(
                "java.lang.IllegalArgumentException",
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING},
                Constants.INVOKESPECIAL));
        il.append(new ATHROW());
        InstructionHandle ifnonnullHandle = il.append(new ICONST(0));
        ifnonnull.setTarget(ifnonnullHandle);
        il.append(new ISTORE(4));
        GOTO aGoto = new GOTO(null);
        InstructionHandle iStartHandle = il.append(aGoto);
        InstructionHandle if_icmpltHandle = il.append(new ALOAD(0));
        il.append(new ALOAD(3));
        il.append(new ALOAD(2));
        il.append(new ILOAD(4));
        il.append(new IALOAD());
        il.append(instructionFactory.createInvoke(
                classGen.getClassName(),
                "jdoCopyField",
                Type.VOID,
                new Type[]{new ObjectType(classGen.getClassName()),Type.INT},
                Constants.INVOKEVIRTUAL));
        il.append(new IINC(4,1));
        InstructionHandle aGotoHandle = il.append(new ILOAD(4));
        aGoto.setTarget(aGotoHandle);
        il.append(new ALOAD(2));
        il.append(new ARRAYLENGTH());
        il.append(new IF_ICMPLT(if_icmpltHandle));
        il.append(new RETURN());

        methodGen.addLocalVariable("other", new ObjectType(classGen.getClassName()), 3, otherStartHandle, il.getEnd());
        methodGen.addLocalVariable("i", Type.INT, 4, iStartHandle, il.getEnd());
//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }


    private void addJdoCopyField(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC,
                Type.VOID,
                new Type[]{new ObjectType(classGen.getClassName()), Type.INT},
                new String[]{"other","fieldNumber"},
                "jdoCopyField",
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ILOAD(2));
        il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                "jdoInheritedFieldCount",
                Type.INT));
        il.append(new ISUB());
        il.append(new ISTORE(3));
        InstructionHandle relativeFieldStartHandle = il.append(new ILOAD(3));

        int switchCount = fieldSet.size();
        int[] match = new int[switchCount];
        InstructionHandle[] targets = new InstructionHandle[switchCount];
        ArrayList tempInsLists = new ArrayList(switchCount+1);
        int i = 0;
        ArrayList tempList = new ArrayList(fieldSet);
        for (Iterator fieldIter = tempList.iterator(); fieldIter.hasNext();i++) {
            FieldInfo fieldInfo = (FieldInfo)fieldIter.next();
            InstructionList tempIL = new InstructionList();
            match[i] = fieldInfo.getFieldNo();
            targets[i] = tempIL.append(new ALOAD(0));
            tempIL.append(new ALOAD(1));
            tempIL.append(instructionFactory.createGetField(
                    classGen.getClassName(),
                    fieldInfo.getFieldName(),
                    fieldInfo.getType()));
            tempIL.append(instructionFactory.createPutField(
                    classGen.getClassName(),
                    fieldInfo.getFieldName(),
                    fieldInfo.getType()));
            tempIL.append(new RETURN());
            tempInsLists.add(tempIL);
        }
        // Do default
        InstructionList tempIL = new InstructionList();
        InstructionHandle defaultHandle = null;
        if (classInfo.getTopPCSuperClass() == null){
            defaultHandle = tempIL.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
            tempIL.append(new DUP());
            tempIL.append(new PUSH(constantPoolGen,"fieldNumber"));
            tempIL.append(instructionFactory.createInvoke(
                    "java.lang.IllegalArgumentException",
                    "<init>",
                    Type.VOID ,
                    new Type[]{Type.STRING},
                    Constants.INVOKESPECIAL));
            tempIL.append(new ATHROW());
        } else {
            defaultHandle = tempIL.append(new ALOAD(0));
            tempIL.append(new ALOAD(1));
            tempIL.append(new ILOAD(2));
            tempIL.append(instructionFactory.createInvoke(
                    classInfo.getTopPCSuperClass().getClassName(),
                    "jdoCopyField",
                    Type.VOID,
                    new Type[]{new ObjectType(classInfo.getTopPCSuperClass().getClassName()),
                               Type.INT},
                    Constants.INVOKESPECIAL));
        }
        tempInsLists.add(tempIL);
        // start the lookupSwitch
        il.append(new LOOKUPSWITCH(match,targets,defaultHandle));
        for (Iterator tempIlIter = tempInsLists.iterator(); tempIlIter.hasNext();) {   // add all instructions
            InstructionList list = (InstructionList) tempIlIter.next();
            il.append(list);
        }
        il.append(new RETURN());

        methodGen.addLocalVariable("relativeField", Type.INT, 3, relativeFieldStartHandle, il.getEnd());

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }


    private void addJdoProvideFields(){

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC ,
                Type.VOID,
                new Type[]{new ArrayType(Type.INT,1)},
                new String[]{"fieldNumbers"},
                "jdoProvideFields",
                classGen.getClassName(),
                il,
                constantPoolGen);
        il.append(new ICONST(0));
        il.append(new ISTORE(2));
        InstructionHandle iloadHandle = il.append(new ILOAD(2));
        il.append(new ALOAD(1));
        il.append(new ARRAYLENGTH());
        IF_ICMPGE if_icmpge = new IF_ICMPGE(null);
        il.append(if_icmpge);
        il.append(new ALOAD(1));
        il.append(new ILOAD(2));
        il.append(new IALOAD());
        il.append(new ISTORE(3));
        InstructionHandle aloadHandle = il.append(new ALOAD(0));
        il.append(new ILOAD(3));
        il.append(instructionFactory.createInvoke(
                classGen.getClassName(),
                "jdoProvideField",
                Type.VOID ,
                new Type[]{Type.INT},
                Constants.INVOKEVIRTUAL));
        InstructionHandle iincHandle = il.append(new IINC(2,1));
        il.append(new GOTO(iloadHandle));
        InstructionHandle returnHandle = il.append(new RETURN());
        if_icmpge.setTarget(returnHandle);
        methodGen.addLocalVariable("i",Type.INT,2,iloadHandle,returnHandle);
        methodGen.addLocalVariable("fieldNumber",Type.INT,3,aloadHandle,iincHandle);
//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

	private void addJdoProvideField(){

		ArrayList myList = new ArrayList(fieldSet);
		ListIterator fieldIter = myList.listIterator();

		while (fieldIter.hasNext()){fieldIter.next();}

		InstructionList il = new InstructionList();

		MethodGen methodGen = new MethodGen(
		        Constants.ACC_PUBLIC,
		        Type.VOID,
		        new Type[]{Type.INT},
		        new String[]{"fieldNumber"},
		        "jdoProvideField",
		        classGen.getClassName(),
		        il,
		        constantPoolGen);

		if (!isEmpty){
			int size = fieldSet.size();
			int[] match = new int[size];
			int fieldNum = size;
			InstructionHandle[] targets = new InstructionHandle[size];
			InstructionHandle defaultHandle = null;
			InstructionHandle returnHandel = null;
			Set switchList = new TreeSet();
			if (classInfo.getPersistenceCapableSuperclass() == null){
				defaultHandle = il.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
				il.append(new DUP());

                il.append(instructionFactory.createNew("java.lang.StringBuffer"));
                il.append(new DUP());
                il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "<init>",
                        Type.VOID,
                        new Type[]{},
                        Constants.INVOKESPECIAL));
                il.append(new PUSH(constantPoolGen, "Class " + classGen.getClassName() +
                        " called with invalid fieldNumber = "));
                il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "append",
                        Type.STRINGBUFFER,
                        new Type[]{Type.STRING},
                        Constants.INVOKEVIRTUAL));
                il.append(new ILOAD(1));
                il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "append",
                        Type.STRINGBUFFER,
                        new Type[]{Type.INT},
                        Constants.INVOKEVIRTUAL));
                il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "toString",
                        Type.STRING,
                        new Type[]{},
                        Constants.INVOKEVIRTUAL));

				il.append(instructionFactory.createInvoke(
				        "java.lang.IllegalArgumentException",
				        "<init>",
				        Type.VOID ,
				        new Type[]{Type.STRING},
				        Constants.INVOKESPECIAL));
				il.append(new ATHROW());
				returnHandel = il.append(new RETURN());
			} else {
				returnHandel = il.insert(new RETURN());
				il.insert(new ATHROW());
				il.insert(instructionFactory.createInvoke(
				        "java.lang.IllegalArgumentException",
				        "<init>",
				        Type.VOID ,
				        new Type[]{Type.STRING},
				        Constants.INVOKESPECIAL));
//				il.insert(new PUSH(constantPoolGen,"fieldNumber"));

                il.insert(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "toString",
                        Type.STRING,
                        new Type[]{},
                        Constants.INVOKEVIRTUAL));
                il.insert(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "append",
                        Type.STRINGBUFFER,
                        new Type[]{Type.INT},
                        Constants.INVOKEVIRTUAL));
                il.insert(new ILOAD(1));
                il.insert(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "append",
                        Type.STRINGBUFFER,
                        new Type[]{Type.STRING},
                        Constants.INVOKEVIRTUAL));
                il.insert(new PUSH(constantPoolGen, "Class " + classGen.getClassName() +
                        " called with invalid fieldNumber = "));
                il.insert(instructionFactory.createInvoke("java.lang.StringBuffer",
                        "<init>",
                        Type.VOID,
                        new Type[]{},
                        Constants.INVOKESPECIAL));
                il.insert(new DUP());
                il.insert(instructionFactory.createNew("java.lang.StringBuffer"));


				il.insert(new DUP());
				InstructionHandle newHandel = il.insert(instructionFactory.createNew("java.lang.IllegalArgumentException"));
				il.insert(new GOTO(returnHandel));
				il.insert(instructionFactory.createInvoke(
				        classInfo.getPersistenceCapableSuperclass(),
				        "jdoProvideField",
				        Type.VOID ,
				        new Type[]{Type.INT},
				        Constants.INVOKESPECIAL));
				il.insert(new ILOAD(1));
				il.insert(new ALOAD(0));
				il.insert(new IFGE(newHandel));
				defaultHandle = il.insert(new ILOAD(2));
			}

			while (fieldIter.hasPrevious()){
				FieldInfo fieldInfo = (FieldInfo)fieldIter.previous();
				fieldNum --;
				Type fieldType = fieldInfo.getType();
				String stateManagerProvidedField = null;
				boolean isObject = false;
				if (typeToProvidedField.containsKey(fieldInfo.getType())){
					stateManagerProvidedField = (String)typeToProvidedField.get(fieldInfo.getType());
					isObject = false;
				} else {
					isObject = true;
				}

				il.insert(new GOTO(returnHandel));

				if (isObject){
					il.insert(instructionFactory.createInvoke(
					        STATE_MANAGER,
					        "providedObjectField",
					        Type.VOID ,
					        new Type[]{PC_OBJECT_TYPE,Type.INT,Type.OBJECT},
					        Constants.INVOKEINTERFACE));
				} else {
					il.insert(instructionFactory.createInvoke(
					        STATE_MANAGER,
					        stateManagerProvidedField,
					        Type.VOID,
					        new Type[]{PC_OBJECT_TYPE,Type.INT,fieldType},
					        Constants.INVOKEINTERFACE));
				}
				il.insert(instructionFactory.createGetField(
				        classGen.getClassName(),
				        fieldInfo.getFieldName(),
				        fieldType));

				il.insert(new ALOAD(0));
				il.insert(new ILOAD(1));
				il.insert(new ALOAD(0));
				il.insert(instructionFactory.createGetField(
				        getTopPCSuperOrCurrentClassName(),
				        "jdoStateManager",
				        SM_OBJECT_TYPE));
				InstructionHandle switchHandel = il.insert(new ALOAD(0));
				TableSwitchHelper tsh = new TableSwitchHelper();
				tsh.match = fieldNum;
				tsh.target = switchHandel;
				switchList.add(tsh);

			}
			Iterator sIter = switchList.iterator();
			int count = 0;
			while (sIter.hasNext()){
				TableSwitchHelper tsh = (TableSwitchHelper)sIter.next();
				match[count] = tsh.match;
				targets[count] = tsh.target;
				count ++;
			}

			il.insert(new TABLESWITCH(match,targets,defaultHandle));
			InstructionHandle relativeFieldFromHandle = il.insert(new ILOAD(2));
			il.insert(new ISTORE(2));
			il.insert(new ISUB());
			il.insert(instructionFactory.createGetStatic(
			        classGen.getClassName(),
			        "jdoInheritedFieldCount",
			        Type.INT));
			il.insert(new ILOAD(1));
			methodGen.addLocalVariable("relativeField",Type.INT,2,relativeFieldFromHandle,returnHandel);
		} else if (classInfo.getPersistenceCapableSuperclass() != null){


            il.append(new ILOAD(1));
			il.append(instructionFactory.createGetStatic(
						        classGen.getClassName(),
						        "jdoInheritedFieldCount",
						        Type.INT));
			il.append(new ISUB());
            il.append(new ISTORE(2));
			InstructionHandle relativeField_Start = il.append(new ILOAD(2));
			IFGE ifge = new IFGE(null);
			il.append(ifge);
			il.append(new ALOAD(0));
			il.append(new ILOAD(1));
			il.append(instructionFactory.createInvoke(
				        classInfo.getPersistenceCapableSuperclass(),
				        "jdoProvideField",
				        Type.VOID ,
				        new Type[]{Type.INT},
				        Constants.INVOKESPECIAL));
			GOTO aGoto = new GOTO(null);
			il.append(aGoto);
			InstructionHandle newHandel = il.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
			ifge.setTarget(newHandel);
			il.append(new DUP());
//			il.append(new PUSH(constantPoolGen,"fieldNumber"));
            il.append(instructionFactory.createNew("java.lang.StringBuffer"));
            il.append(new DUP());
            il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                    "<init>",
                    Type.VOID,
                    new Type[]{},
                    Constants.INVOKESPECIAL));
            il.append(new PUSH(constantPoolGen, "Class "+classGen.getClassName() +
                    " called with invalid fieldNumber = "));
            il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                    "append",
                    Type.STRINGBUFFER,
                    new Type[]{Type.STRING},
                    Constants.INVOKEVIRTUAL));
            il.append(new ILOAD(1));
            il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                    "append",
                    Type.STRINGBUFFER,
                    new Type[]{Type.INT},
                    Constants.INVOKEVIRTUAL));
            il.append(instructionFactory.createInvoke("java.lang.StringBuffer",
                    "toString",
                    Type.STRING,
                    new Type[]{},
                    Constants.INVOKEVIRTUAL));
            
			il.append(instructionFactory.createInvoke(
				        "java.lang.IllegalArgumentException",
				        "<init>",
				        Type.VOID ,
				        new Type[]{Type.STRING},
				        Constants.INVOKESPECIAL));
			il.append(new ATHROW());
			il.append(new RETURN());
			aGoto.setTarget(il.getEnd());

			methodGen.addLocalVariable("relativeField",Type.INT,2,relativeField_Start,il.getEnd());

		} else {
			il.append(new RETURN());
		}
//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
		methodGen.setMaxStack();
		classGen.addMethod(methodGen.getMethod());
		il.dispose();
	}

    private void addJdoReplaceFields(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC ,
                Type.VOID,
                new Type[]{new ArrayType(Type.INT,1)},
                new String[]{"fieldNumbers"},
                "jdoReplaceFields",
                classGen.getClassName(),
                il,
                constantPoolGen);
        il.append(new ICONST(0));
        il.append(new ISTORE(2));
        InstructionHandle iloadHandle = il.append(new ILOAD(2));
        il.append(new ALOAD(1));
        il.append(new ARRAYLENGTH());
        IF_ICMPGE if_icmpge = new IF_ICMPGE(null);
        il.append(if_icmpge);
        il.append(new ALOAD(1));
        il.append(new ILOAD(2));
        il.append(new IALOAD());
        il.append(new ISTORE(3));
        InstructionHandle aloadHandle = il.append(new ALOAD(0));
        il.append(new ILOAD(3));
        il.append(instructionFactory.createInvoke(
                classGen.getClassName(),
                "jdoReplaceField",
                Type.VOID ,
                new Type[]{Type.INT},
                Constants.INVOKEVIRTUAL));
        InstructionHandle iincHandle = il.append(new IINC(2,1));
        il.append(new GOTO(iloadHandle));
        InstructionHandle returnHandle = il.append(new RETURN());
        if_icmpge.setTarget(returnHandle);
        methodGen.addLocalVariable("i",Type.INT,2,iloadHandle,returnHandle);
        methodGen.addLocalVariable("fieldNumber",Type.INT,3,aloadHandle,iincHandle);
//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addJdoReplaceField(){

        int size = fieldSet.size();
        int[] match = new int[size];
        InstructionHandle[] targets = new InstructionHandle[size];
        InstructionHandle defaultHandle = null;
        InstructionHandle returnHandel = null;
        Set switchList = new TreeSet();
        ArrayList myList = new ArrayList(fieldSet);
        ListIterator fieldIter = myList.listIterator();
        int fieldNum = size;
        while (fieldIter.hasNext()){fieldIter.next();}
        InstructionList il = new InstructionList();

        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC,
                Type.VOID,
                new Type[]{Type.INT},
                new String[]{"fieldNumber"},
                "jdoReplaceField",
                classGen.getClassName(),
                il,
                constantPoolGen);
        if (!isEmpty){
            if (classInfo.getPersistenceCapableSuperclass() == null){
                defaultHandle = il.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
                il.append(new DUP());
                il.append(new PUSH(constantPoolGen,"fieldNumber"));
                il.append(instructionFactory.createInvoke(
                        "java.lang.IllegalArgumentException",
                        "<init>",
                        Type.VOID ,
                        new Type[]{Type.STRING},
                        Constants.INVOKESPECIAL));
                il.append(new ATHROW());
                returnHandel = il.append(new RETURN());
            } else {
                returnHandel = il.insert(new RETURN());
                il.insert(new ATHROW());
                il.insert(instructionFactory.createInvoke(
                        "java.lang.IllegalArgumentException",
                        "<init>",
                        Type.VOID ,
                        new Type[]{Type.STRING},
                        Constants.INVOKESPECIAL));
                il.insert(new PUSH(constantPoolGen,"fieldNumber"));
                il.insert(new DUP());
                InstructionHandle newHandel = il.insert(instructionFactory.createNew("java.lang.IllegalArgumentException"));
                il.insert(new GOTO(returnHandel));
                il.insert(instructionFactory.createInvoke(
                        classInfo.getPersistenceCapableSuperclass(),
                        "jdoReplaceField",
                        Type.VOID ,
                        new Type[]{Type.INT},
                        Constants.INVOKESPECIAL));
                il.insert(new ILOAD(1));
                il.insert(new ALOAD(0));
                il.insert(new IFGE(newHandel));
                defaultHandle = il.insert(new ILOAD(2));
            }

            while (fieldIter.hasPrevious()){
                FieldInfo fieldInfo = (FieldInfo)fieldIter.previous();
                fieldNum --;
                Type fieldType = fieldInfo.getType();
                String stateManagerReplaceField = null;
                boolean isObject = false;
                if (typeToReplacingField.containsKey(fieldInfo.getType())){
                    stateManagerReplaceField = (String)typeToReplacingField.get(fieldInfo.getType());
                    isObject = false;
                } else {
                    isObject = true;
                }

                il.insert(new GOTO(returnHandel));
                il.insert(instructionFactory.createPutField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        fieldType));
                if (isObject){
                    if (fieldInfo.isArray()){
                        il.insert(instructionFactory.createCheckCast(new ObjectType(fieldInfo.getSignature())));
                    }else{
                        il.insert(instructionFactory.createCheckCast(new ObjectType(fieldInfo.getReturnType())));
                    }
                    il.insert(instructionFactory.createInvoke(
                            STATE_MANAGER,
                            "replacingObjectField",
                            Type.OBJECT ,
                            new Type[]{PC_OBJECT_TYPE,Type.INT},
                            Constants.INVOKEINTERFACE));
                } else {
                    il.insert(instructionFactory.createInvoke(
                            STATE_MANAGER,
                            stateManagerReplaceField,
                            fieldType ,
                            new Type[]{PC_OBJECT_TYPE,Type.INT},
                            Constants.INVOKEINTERFACE));
                }
                il.insert(new ILOAD(1));
                il.insert(new ALOAD(0));
                il.insert(instructionFactory.createGetField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.insert(new ALOAD(0));
                InstructionHandle switchHandel = il.insert(new ALOAD(0));
                TableSwitchHelper tsh = new TableSwitchHelper();
                tsh.match = fieldNum;
                tsh.target = switchHandel;
                switchList.add(tsh);
            }
            Iterator sIter = switchList.iterator();
            int count = 0;
            while (sIter.hasNext()){
                TableSwitchHelper tsh = (TableSwitchHelper)sIter.next();
                match[count] = tsh.match;
                targets[count] = tsh.target;
                count ++;
            }

            il.insert(new TABLESWITCH(match,targets,defaultHandle));
            InstructionHandle relativeFieldFromHandle = il.insert(new ILOAD(2));
            il.insert(new ISTORE(2));
            il.insert(new ISUB());
            il.insert(instructionFactory.createGetStatic(
                    classGen.getClassName(),
                    "jdoInheritedFieldCount",
                    Type.INT));
            il.insert(new ILOAD(1));
            methodGen.addLocalVariable("relativeField",Type.INT,2,relativeFieldFromHandle,returnHandel);
        } else if (classInfo.getPersistenceCapableSuperclass() != null){
	        il.append(new ILOAD(1));
	        il.append(instructionFactory.createGetStatic(
                    classGen.getClassName(),
                    "jdoInheritedFieldCount",
                    Type.INT));
	        il.append(new ISUB());
	        il.append(new ISTORE(2));
	        InstructionHandle relativeField_Start = il.append(new ILOAD(2));
			IFGE ifge = new IFGE(null);
			il.append(ifge);
			il.append(new ALOAD(0));
			il.append(new ILOAD(1));

			il.append(instructionFactory.createInvoke(
				        classInfo.getPersistenceCapableSuperclass(),
				        "jdoReplaceField",
				        Type.VOID ,
				        new Type[]{Type.INT},
				        Constants.INVOKESPECIAL));
			GOTO aGoto = new GOTO(null);
			il.append(aGoto);
			InstructionHandle newHandel = il.append(instructionFactory.createNew("java.lang.IllegalArgumentException"));
			ifge.setTarget(newHandel);
			il.append(new DUP());
			il.append(new PUSH(constantPoolGen,"fieldNumber"));
			il.append(instructionFactory.createInvoke(
				        "java.lang.IllegalArgumentException",
				        "<init>",
				        Type.VOID ,
				        new Type[]{Type.STRING},
				        Constants.INVOKESPECIAL));
			il.append(new ATHROW());
			il.append(new RETURN());
			aGoto.setTarget(il.getEnd());

			methodGen.addLocalVariable("relativeField",Type.INT,2,relativeField_Start,il.getEnd());
        } else {
	        il.append(new RETURN());
        }

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }





    private void addFieldGetters(){
        Iterator fieldIter = fieldSet.iterator();
        int fieldNum = 0;

        while (fieldIter.hasNext()){
            FieldInfo fieldInfo = (FieldInfo)fieldIter.next();
            int acc = Constants.ACC_STATIC  |
                    (fieldInfo.isPrivate() ? Constants.ACC_PRIVATE : (short)0) |
                    (fieldInfo.isProtected() ? Constants.ACC_PROTECTED : (short)0) |
                    (fieldInfo.isPublic() ? Constants.ACC_PUBLIC : (short)0);
            Type returnType = fieldInfo.getType();
            ReturnInstruction returnInstruction;
            String stateManagerGetField = null;
            boolean isObject = false;
            if (typeToGetField.containsKey(returnType)){
                stateManagerGetField = (String)typeToGetField.get(returnType);
                returnInstruction = (ReturnInstruction)typeToReturnType.get(returnType);
                isObject = false;
            } else {
                stateManagerGetField = "getObjectField";
                returnInstruction = new ARETURN();
                isObject = true;
            }

            InstructionList il = new InstructionList();

            MethodGen methodGen = new MethodGen(
                    acc,
                    returnType,
                    new Type[]{new ObjectType(classGen.getClassName())},
                    new String[]{"x"},
                    fieldInfo.getJdoGetName(),
                    classGen.getClassName(),
                    il,
                    constantPoolGen);


            if (fieldInfo.getFlag() == CHECK_READ_WRITE ){// this field is in the default fetch group
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));
                IFGT ifgt = new IFGT(null);
                il.append(ifgt);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                InstructionHandle ifgtHandel = il.append(new ALOAD(0));
                ifgt.setTarget(ifgtHandel);
                il.append(instructionFactory.createGetField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.append(new ASTORE(1));
                InstructionHandle smHandle = il.append(new ALOAD(1));
                IFNULL ifnull = new IFNULL(null);                        // null to aload_0 58
                il.append(ifnull);
                il.append(new ALOAD(1));
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(
                        classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(instructionFactory.createInvoke(
                        STATE_MANAGER,
                        "isLoaded",
                        Type.BOOLEAN ,
                        new Type[]{PC_OBJECT_TYPE,Type.INT},
                        Constants.INVOKEINTERFACE));
                IFEQ ifeq = new IFEQ(null);
                il.append(ifeq);                                          // null to aload_1 41
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                InstructionHandle ifeqHandel = il.append(new ALOAD(1));
                ifeq.setTarget(ifeqHandel);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(
                        classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                if (isObject){//if there is a object cast it
                    il.append(instructionFactory.createInvoke(
                            STATE_MANAGER,
                            stateManagerGetField,
                            Type.OBJECT ,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                Type.OBJECT},
                            Constants.INVOKEINTERFACE));
                    il.append(instructionFactory.createCheckCast((ReferenceType)fieldInfo.getType()));
                } else {
                    il.append(instructionFactory.createInvoke(
                            STATE_MANAGER,
                            stateManagerGetField,
                            returnType ,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                returnType},
                            Constants.INVOKEINTERFACE));
                }
                il.append(returnInstruction);
                InstructionHandle ifNullHandle = il.append(new ALOAD(0));
                ifnull.setTarget(ifNullHandle);
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                methodGen.addLocalVariable("sm",SM_OBJECT_TYPE ,1,smHandle,il.getEnd());

            } else if (fieldInfo.getFlag() == MEDIATE_READ_WRITE){

                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.append(new ASTORE(1));
                InstructionHandle smHandle = il.append(new ALOAD(1));
                IFNULL ifnull = new IFNULL(null);                        // null to aload_0 58
                il.append(ifnull);
                il.append(new ALOAD(1));
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(
                        classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(instructionFactory.createInvoke(
                        STATE_MANAGER,
                        "isLoaded",
                        Type.BOOLEAN ,
                        new Type[]{PC_OBJECT_TYPE,Type.INT},
                        Constants.INVOKEINTERFACE));
                IFEQ ifeq = new IFEQ(null);
                il.append(ifeq);                                          // null to aload_1 41
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                InstructionHandle ifeqHandel = il.append(new ALOAD(1));
                ifeq.setTarget(ifeqHandel);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(
                        classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                if (isObject){//if there is a object cast it
                    il.append(instructionFactory.createInvoke(
                            STATE_MANAGER,
                            stateManagerGetField,
                            Type.OBJECT ,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                Type.OBJECT},
                            Constants.INVOKEINTERFACE));
                    il.append(instructionFactory.createCheckCast((ReferenceType)fieldInfo.getType()));
                } else {
                    il.append(instructionFactory.createInvoke(
                            STATE_MANAGER,
                            stateManagerGetField,
                            returnType ,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                returnType},
                            Constants.INVOKEINTERFACE));
                }
                il.append(returnInstruction);
                InstructionHandle ifNullHandle = il.append(new ALOAD(0));
                ifnull.setTarget(ifNullHandle);
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                methodGen.addLocalVariable("sm",SM_OBJECT_TYPE,1,smHandle,il.getEnd());


            } else {//no mediation
                if (fieldInfo.primaryKey() &&
                        (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION) &&
                        classInfo.isKeyGen()){

                    il.append(new ALOAD(0));
                    il.append(instructionFactory.createGetField(classGen.getClassName(),
                            "jdoStateManager",
                            new ObjectType("javax.jdo.spi.StateManager")));
                    il.append(new INSTANCEOF(constantPoolGen.addClass(VERSANT_STATE_MANAGER)));
                    IFEQ ifeq = new IFEQ(null);
                    il.append(ifeq);          //25
                    il.append(new ALOAD(0));
                    il.append(instructionFactory.createGetField(classGen.getClassName(),
                            "jdoStateManager",
                            new ObjectType("javax.jdo.spi.StateManager")));
                    il.append(instructionFactory.createCheckCast(new ObjectType(VERSANT_STATE_MANAGER)));
                    il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                            "jdoInheritedFieldCount",
                            Type.INT));
                    il.append(new PUSH(constantPoolGen, fieldInfo.getFieldNo()));
                    il.append(new IADD());
                    il.append(instructionFactory.createInvoke(VERSANT_STATE_MANAGER,
                            "fillNewAppPKField",
                            Type.VOID,
                            new Type[]{Type.INT},
                            Constants.INVOKEINTERFACE));
                    InstructionHandle nopHandle = il.append(new NOP());
                    ifeq.setTarget(nopHandle);
                }
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
            }
            methodGen.removeNOPs();
            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
            fieldNum ++;

        }

    }

    private void makeSynthetic(FieldGenOrMethodGen gen) {
        gen.addAttribute(new Synthetic(synthetic, 0, null, gen.getConstantPool().getConstantPool()));
    }


    private void addFieldSetters(){
        Iterator fieldIter = fieldSet.iterator();
        int fieldNum = 0;

        while (fieldIter.hasNext()){
            FieldInfo fieldInfo = (FieldInfo)fieldIter.next();

            int acc = Constants.ACC_STATIC  |
                    (fieldInfo.isPrivate() ? Constants.ACC_PRIVATE : (short)0) |
                    (fieldInfo.isProtected() ? Constants.ACC_PROTECTED : (short)0) |
                    (fieldInfo.isPublic() ? Constants.ACC_PUBLIC : (short)0);

            Type fieldType = fieldInfo.getType();
            boolean isObject = false;
            String stateManagerSetField = null;
            if (typeToSetField.containsKey(fieldType)){
                stateManagerSetField = (String)typeToSetField.get(fieldType);
                isObject = false;
            } else {
                stateManagerSetField = "setObjectField";
                isObject = true;
            }

            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(
                    acc,
                    Type.VOID,
                    new Type[]{new ObjectType(classGen.getClassName()),
                               fieldType},
                    new String[]{"x",
                                 "newValue"},
                    fieldInfo.getJdoSetName(),
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            int flags = fieldInfo.getFlag();
            boolean isLorD = false;
            if (fieldType.equals(Type.LONG) || fieldType.equals(Type.DOUBLE)) isLorD = true;
            IFNE ifne = new IFNE(null);
            if (flags == CHECK_READ_WRITE || flags == CHECK_WRITE ){// this field is in the default fetch group or is transient transactional
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));

                il.append(ifne);         // 13
                il.append(new ALOAD(0));
                if (isObject){
                    il.append(new ALOAD(1));
                } else {
                    il.append((LoadInstruction)typeToLoadType.get(fieldType));
                }
                il.append(instructionFactory.createPutField(
                        classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        fieldType));
                il.append(new RETURN());
            }
            InstructionHandle ifneHandel = il.append(new ALOAD(0));
            ifne.setTarget(ifneHandel);
            il.append(instructionFactory.createGetField(
                    getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ASTORE((isLorD ? 3 : 2)));
            InstructionHandle smStartHandle = il.append(new ALOAD((isLorD ? 3 : 2)));
            IFNULL ifnull = new IFNULL(null);
            il.append(ifnull);
            il.append(new ALOAD((isLorD ? 3 : 2)));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetStatic(
                    classGen.getClassName(),
                    "jdoInheritedFieldCount",
                    Type.INT));
            il.append(new PUSH(constantPoolGen, fieldNum));
            il.append(new IADD());
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(
                    classGen.getClassName(),
                    fieldInfo.getFieldName(),
                    fieldType));
            if (isObject){//if there is a object call it with Object's
                il.append(new ALOAD(1));
                il.append(instructionFactory.createInvoke(
                        STATE_MANAGER,
                        stateManagerSetField,
                        Type.VOID ,
                        new Type[]{PC_OBJECT_TYPE,
                                   Type.INT,
                                   Type.OBJECT,
                                   Type.OBJECT},
                        Constants.INVOKEINTERFACE));
            } else {
                il.append((LoadInstruction)typeToLoadType.get(fieldType));
                il.append(instructionFactory.createInvoke(
                        STATE_MANAGER,
                        stateManagerSetField,
                        Type.VOID,
                        new Type[]{PC_OBJECT_TYPE,
                                   Type.INT,
                                   fieldType,
                                   fieldType},
                        Constants.INVOKEINTERFACE));
            }
            GOTO aGoto = new GOTO(null);
            il.append(aGoto);
            InstructionHandle ifnullHandle = il.append(new ALOAD(0));
            ifnull.setTarget(ifnullHandle);
            if (isObject){//if there is a object call it with Object's
                il.append(new ALOAD(1));
            } else {
                il.append((LoadInstruction)typeToLoadType.get(fieldType));
            }
            il.append(instructionFactory.createPutField(
                    classGen.getClassName(),
                    fieldInfo.getFieldName(),
                    fieldType));
            InstructionHandle lastHandle = il.append(new RETURN());
            aGoto.setTarget(lastHandle);


            methodGen.addLocalVariable(
                    "sm",
                    SM_OBJECT_TYPE,
                    (isLorD ? 3 : 2),
                    smStartHandle,
                    il.getEnd());

            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
            fieldNum ++;

        }

    }

    private void addJdo2FieldGetters() {
        Iterator fieldIter = fieldSet.iterator();
        int fieldNum = 0;

        while (fieldIter.hasNext()) {
            FieldInfo fieldInfo = (FieldInfo) fieldIter.next();
            int acc = Constants.ACC_STATIC |
                    (fieldInfo.isPrivate() ? Constants.ACC_PRIVATE : (short) 0) |
                    (fieldInfo.isProtected() ? Constants.ACC_PROTECTED : (short) 0) |
                    (fieldInfo.isPublic() ? Constants.ACC_PUBLIC : (short) 0);
            Type returnType = fieldInfo.getType();
            ReturnInstruction returnInstruction;
            String stateManagerGetField = null;
            boolean isObject = false;
            if (typeToGetField.containsKey(returnType)) {
                stateManagerGetField = (String) typeToGetField.get(returnType);
                returnInstruction = (ReturnInstruction) typeToReturnType.get(returnType);
                isObject = false;
            } else {
                stateManagerGetField = "getObjectField";
                returnInstruction = new ARETURN();
                isObject = true;
            }

            InstructionList il = new InstructionList();

            MethodGen methodGen = new MethodGen(acc,
                    returnType,
                    new Type[]{new ObjectType(classGen.getClassName())},
                    new String[]{"pc"},
                    fieldInfo.getJdoGetName(),
                    classGen.getClassName(),
                    il,
                    constantPoolGen);


            if (fieldInfo.getFlag() == CHECK_READ_WRITE) {// this field is in the default fetch group
                
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));
                IFGT ifgt = new IFGT(null);
                il.append(ifgt);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                InstructionHandle ifgtHandel = il.append(new ALOAD(0));
                ifgt.setTarget(ifgtHandel);
                il.append(instructionFactory.createGetField(getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.append(new ASTORE(1));
                InstructionHandle smHandle = il.append(new ALOAD(1));
                IFNULL ifnull = new IFNULL(null);                        // null to aload_0 58
                il.append(ifnull);
                il.append(new ALOAD(1));
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(instructionFactory.createInvoke(STATE_MANAGER,
                        "isLoaded",
                        Type.BOOLEAN,
                        new Type[]{PC_OBJECT_TYPE, Type.INT},
                        Constants.INVOKEINTERFACE));
                IFEQ ifeq = new IFEQ(null);
                il.append(ifeq);                                          // null to aload_1 41
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                InstructionHandle ifeqHandel = il.append(new ALOAD(1));
                ifeq.setTarget(ifeqHandel);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                if (isObject) {//if there is a object cast it
                    il.append(instructionFactory.createInvoke(STATE_MANAGER,
                            stateManagerGetField,
                            Type.OBJECT,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                Type.OBJECT},
                            Constants.INVOKEINTERFACE));
                    il.append(instructionFactory.createCheckCast((ReferenceType) fieldInfo.getType()));
                } else {
                    il.append(instructionFactory.createInvoke(STATE_MANAGER,
                            stateManagerGetField,
                            returnType,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                returnType},
                            Constants.INVOKEINTERFACE));
                }
                il.append(returnInstruction);
                InstructionHandle ifNullHandle = il.append(new ALOAD(0));
                ifnull.setTarget(ifNullHandle);
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                methodGen.addLocalVariable("sm", SM_OBJECT_TYPE, 1, smHandle, il.getEnd());

            } else if (fieldInfo.getFlag() == MEDIATE_READ_WRITE) {

                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(getTopPCSuperOrCurrentClassName(),
                        "jdoStateManager",
                        SM_OBJECT_TYPE));
                il.append(new ASTORE(1));
                InstructionHandle smHandle = il.append(new ALOAD(1));
                IFNULL ifnull = new IFNULL(null);                        // null to aload_0 58
                il.append(ifnull);
                il.append(new ALOAD(1));
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(instructionFactory.createInvoke(STATE_MANAGER,
                        "isLoaded",
                        Type.BOOLEAN,
                        new Type[]{PC_OBJECT_TYPE, Type.INT},
                        Constants.INVOKEINTERFACE));
                IFEQ ifeq = new IFEQ(null);
                il.append(ifeq);                                          // null to aload_1 41
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                InstructionHandle ifeqHandel = il.append(new ALOAD(1));
                ifeq.setTarget(ifeqHandel);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                if (isObject) {//if there is a object cast it
                    il.append(instructionFactory.createInvoke(STATE_MANAGER,
                            stateManagerGetField,
                            Type.OBJECT,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                Type.OBJECT},
                            Constants.INVOKEINTERFACE));
                    il.append(instructionFactory.createCheckCast((ReferenceType) fieldInfo.getType()));
                } else {
                    il.append(instructionFactory.createInvoke(STATE_MANAGER,
                            stateManagerGetField,
                            returnType,
                            new Type[]{
                                PC_OBJECT_TYPE,
                                Type.INT,
                                returnType},
                            Constants.INVOKEINTERFACE));
                }
                il.append(returnInstruction);
                InstructionHandle ifNullHandle = il.append(new ALOAD(0));
                ifnull.setTarget(ifNullHandle);
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
                methodGen.addLocalVariable("sm", SM_OBJECT_TYPE, 1, smHandle, il.getEnd());


            } else {//no mediation
                if (fieldInfo.primaryKey() &&
                        (classInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION) &&
                        classInfo.isKeyGen()) {

                    il.append(new ALOAD(0));
                    il.append(instructionFactory.createGetField(classGen.getClassName(),
                            "jdoStateManager",
                            new ObjectType("javax.jdo.spi.StateManager")));
                    il.append(new INSTANCEOF(constantPoolGen.addClass(VERSANT_STATE_MANAGER)));
                    IFEQ ifeq = new IFEQ(null);
                    il.append(ifeq);          //25
                    il.append(new ALOAD(0));
                    il.append(instructionFactory.createGetField(classGen.getClassName(),
                            "jdoStateManager",
                            new ObjectType("javax.jdo.spi.StateManager")));
                    il.append(instructionFactory.createCheckCast(new ObjectType(VERSANT_STATE_MANAGER)));
                    il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                            "jdoInheritedFieldCount",
                            Type.INT));
                    il.append(new PUSH(constantPoolGen, fieldInfo.getFieldNo()));
                    il.append(new IADD());
                    il.append(instructionFactory.createInvoke(VERSANT_STATE_MANAGER,
                            "fillNewAppPKField",
                            Type.VOID,
                            new Type[]{Type.INT},
                            Constants.INVOKEINTERFACE));
                    InstructionHandle nopHandle = il.append(new NOP());
                    ifeq.setTarget(nopHandle);
                }
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        fieldInfo.getFieldName(),
                        returnType));
                il.append(returnInstruction);
            }
            methodGen.removeNOPs();
            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
            fieldNum++;

        }

    }

    private void addJdo2FieldSetters() {
        Iterator fieldIter = fieldSet.iterator();
        int fieldNum = 0;

        while (fieldIter.hasNext()) {
            FieldInfo fieldInfo = (FieldInfo) fieldIter.next();

            int acc = Constants.ACC_STATIC |
                    (fieldInfo.isPrivate() ? Constants.ACC_PRIVATE : (short) 0) |
                    (fieldInfo.isProtected() ? Constants.ACC_PROTECTED : (short) 0) |
                    (fieldInfo.isPublic() ? Constants.ACC_PUBLIC : (short) 0);

            Type fieldType = fieldInfo.getType();
            boolean isObject = false;
            String stateManagerSetField = null;
            if (typeToSetField.containsKey(fieldType)) {
                stateManagerSetField = (String) typeToSetField.get(fieldType);
                isObject = false;
            } else {
                stateManagerSetField = "setObjectField";
                isObject = true;
            }

            InstructionList il = new InstructionList();
            MethodGen methodGen = new MethodGen(acc,
                    Type.VOID,
                    new Type[]{new ObjectType(classGen.getClassName()),
                               fieldType},
                    new String[]{"pc",
                                 "new_"+ fieldInfo.getFieldName()},
                    fieldInfo.getJdoSetName(),
                    classGen.getClassName(),
                    il,
                    constantPoolGen);

            int flags = fieldInfo.getFlag();
            IFEQ ifeq = null;
            IFNULL ifnull = new IFNULL(null);
            if (flags == CHECK_READ_WRITE || flags == CHECK_WRITE) {// this field
                // is in the default fetch group or is transient transactional
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(getTopPCSuperOrCurrentClassName(),
                        "jdoFlags",
                        Type.BYTE));
                ifeq = new IFEQ(null);
                il.append(ifeq);         // 13
            }
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(ifnull);
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(getTopPCSuperOrCurrentClassName(),
                    "jdoStateManager",
                    SM_OBJECT_TYPE));
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                    "jdoInheritedFieldCount",
                    Type.INT));
            il.append(new PUSH(constantPoolGen, fieldNum));
            il.append(new IADD());
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    fieldInfo.getFieldName(),
                    fieldType));
            if (isObject) {//if there is a object call it with Object's
                il.append(new ALOAD(1));
                il.append(instructionFactory.createInvoke(STATE_MANAGER,
                        stateManagerSetField,
                        Type.VOID,
                        new Type[]{PC_OBJECT_TYPE,
                                   Type.INT,
                                   Type.OBJECT,
                                   Type.OBJECT},
                        Constants.INVOKEINTERFACE));
            } else {
                il.append((LoadInstruction) typeToLoadType.get(fieldType));
                il.append(instructionFactory.createInvoke(STATE_MANAGER,
                        stateManagerSetField,
                        Type.VOID,
                        new Type[]{PC_OBJECT_TYPE,
                                   Type.INT,
                                   fieldType,
                                   fieldType},
                        Constants.INVOKEINTERFACE));
            }
            il.append(new RETURN());
            InstructionHandle ifeqHandle1 = il.append(new ALOAD(0));
            if (ifeq != null){
                ifeq.setTarget(ifeqHandle1);
            }
            ifnull.setTarget(ifeqHandle1);
            if(isObject) {//if there is a object call it with Object's
                il.append(new ALOAD(1));
            } else {
                il.append((LoadInstruction) typeToLoadType.get(fieldType));
            }
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    fieldInfo.getFieldName(),
                    fieldType));
            IFEQ ifeqDetach = null;
            if (detach){
                il.append(new ALOAD(0));
                il.append(instructionFactory.createInvoke(
                        getTopPCSuperOrCurrentClassName(),
                        "jdoIsDetached",
                        Type.BOOLEAN,
                        new Type[]{},
                        Constants.INVOKEVIRTUAL));
                ifeqDetach = new IFEQ(null);
                il.append(ifeqDetach);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        "jdoModifiedFields",
                        BITSET_TYPE));
                il.append(instructionFactory.createGetStatic(classGen.getClassName(),
                        "jdoInheritedFieldCount",
                        Type.INT));
                il.append(new PUSH(constantPoolGen, fieldNum));
                il.append(new IADD());
                il.append(instructionFactory.createInvoke("java.util.BitSet",
                        "set",
                        Type.VOID,
                        new Type[]{Type.INT},
                        Constants.INVOKEVIRTUAL));
            }
            InstructionHandle lastHandle = il.append(new RETURN());
            if (ifeqDetach != null) {
                ifeqDetach.setTarget(lastHandle);
            }
            makeSynthetic(methodGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
            il.dispose();
            fieldNum++;

        }

    }


    private void addJdoGetManagedFieldCount(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_STATIC | Constants.ACC_PROTECTED,
                Type.INT,
                null,
                null,
                "jdoGetManagedFieldCount",
                classGen.getClassName(),
                il,
                constantPoolGen);
        il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                "jdoInheritedFieldCount",
                Type.INT));
        il.append(new PUSH(constantPoolGen, fieldSet.size()));
        il.append(new IADD());
        il.append(new IRETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }



    private void addJdoInheritedFieldCount(){
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_STATIC ,//| NEW Constants.ACC_FINAL,
                Type.INT,
                "jdoInheritedFieldCount",
                constantPoolGen);
        makeSynthetic(fieldGen);
        classGen.addField(fieldGen.getField());
        Method m = getStaticConstructor();
        MethodGen methodGen = new MethodGen(
                m,
                classGen.getClassName(),
                constantPoolGen);
        InstructionList il = methodGen.getInstructionList();
        InstructionHandle initialReturnHandle = il.getEnd();
        InstructionHandle nopTarget = il.append(new NOP());
        if (classInfo.getPersistenceCapableSuperclass() == null){
            il.append(new ICONST(0));
        } else {
	        il.append(instructionFactory.createInvoke(
                    classInfo.getPersistenceCapableSuperclass(),
                    "jdoGetManagedFieldCount",
                    Type.INT,
                    new Type[]{},
                    Constants.INVOKESTATIC));
        }
        il.append(instructionFactory.createPutStatic(
                classGen.getClassName(),
                "jdoInheritedFieldCount",
                Type.INT));
        il.append(new RETURN());
        try{
//            System.out.println("initialReturnHandle that was deleted = " + initialReturnHandle);
            il.delete(initialReturnHandle);
        } catch (TargetLostException e){
            InstructionHandle[] targets = e.getTargets();
            for (int i = 0; i < targets.length ; i++){
                InstructionTargeter[] targeters = targets[i].getTargeters();
                for (int j = 0; j < targeters.length ; j++){
                    targeters[j].updateTarget(targets[i], nopTarget);
                }
            }
        }

        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(m,methodGen.getMethod());
        il.dispose();
    }

    private void addJdoFieldTypes(){
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_STATIC ,//| NEW Constants.ACC_FINAL,
                new ArrayType("java.lang.Class",1),
                "jdoFieldTypes",
                constantPoolGen);
        makeSynthetic(fieldGen);
        classGen.addField(fieldGen.getField());
        Method m = getStaticConstructor();
        MethodGen methodGen = new MethodGen(
                m,
                classGen.getClassName(),
                constantPoolGen);
        InstructionList il = methodGen.getInstructionList();

        InstructionHandle initialReturnHandle = il.getEnd();
        InstructionHandle nopTarget = il.append(new NOP());
        il.append(new PUSH(constantPoolGen, fieldSet.size()));
        il.append(new ANEWARRAY(constantPoolGen.addClass(new ObjectType("java.lang.Class"))));
        il.append(new DUP());
        Iterator iter = fieldSet.iterator();
        int push = 0;
        while (iter.hasNext()){
            FieldInfo field = (FieldInfo)iter.next();
            if(field.isPrimative()){
                il.append(new PUSH(constantPoolGen, push));
                il.append(instructionFactory.createGetStatic(
                        field.getPrimativeTypeObject(),
                        "TYPE",
                        new ObjectType("java.lang.Class")));
                il.append(new AASTORE());
                il.append(new DUP());
            } else {
                InstructionList tempIl = new InstructionList();
                String returnType = field.getReturnType();
                String fieldName = getSetClass$Field(field);
                tempIl.insert(new DUP());
                InstructionHandle gotoAASTORE = tempIl.insert(new AASTORE());
                InstructionHandle gotoGetStatic = tempIl.insert(instructionFactory.createGetStatic(
                        classGen.getClassName(),
                        fieldName,
                        new ObjectType("java.lang.Class")));
                tempIl.insert(new GOTO(gotoAASTORE));
                tempIl.insert(instructionFactory.createPutStatic(
                        classGen.getClassName(),
                        fieldName,
                        new ObjectType("java.lang.Class")));
                tempIl.insert(new DUP());
                tempIl.insert(instructionFactory.createInvoke(
                        classGen.getClassName(),
                        "class$",
                        new ObjectType("java.lang.Class"),
                        new Type[]{new ObjectType("java.lang.String")},
                        Constants.INVOKESTATIC));
                if (field.isArray()){
                    tempIl.insert(new PUSH(constantPoolGen,field.getSignature().replace('/','.')));
                } else {
                    tempIl.insert(new PUSH(constantPoolGen,returnType));
                }

                tempIl.insert(new IFNONNULL(gotoGetStatic));
                tempIl.insert(instructionFactory.createGetStatic(
                        classGen.getClassName(),
                        fieldName,
                        new ObjectType("java.lang.Class")));
                tempIl.insert(new PUSH(constantPoolGen, push));

                il.append(tempIl);
            }
            push++;
        }
        try{
            il.delete(il.getEnd());
        } catch (TargetLostException e){//there should never be target for this instruction (DUP)
            e.printStackTrace();
        }

        il.append(instructionFactory.createPutStatic(
                classGen.getClassName(),
                "jdoFieldTypes",
                new ArrayType(new ObjectType("java.lang.Class"),1)));
        il.append(new RETURN());
        try{
            il.delete(initialReturnHandle);
        } catch (TargetLostException e){
            InstructionHandle[] targets = e.getTargets();
            for (int i = 0; i < targets.length ; i++){
                InstructionTargeter[] targeters = targets[i].getTargeters();
                for (int j = 0; j < targeters.length ; j++){
                    targeters[j].updateTarget(targets[i], nopTarget);
                }
            }
        }
        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(m,methodGen.getMethod());
        il.dispose();
    }


    private void addJdoFieldFlags(){
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_STATIC ,//| NEW Constants.ACC_FINAL,
                new ArrayType(Type.BYTE,1),
                "jdoFieldFlags",
                constantPoolGen);

        makeSynthetic(fieldGen);
        classGen.addField(fieldGen.getField());
        Method m = getStaticConstructor();
        MethodGen methodGen = new MethodGen(
                m,
                classGen.getClassName(),
                constantPoolGen);
        InstructionList il = methodGen.getInstructionList();

        InstructionHandle initialReturnHandle = il.getEnd();
        InstructionHandle nopTarget = il.append(new NOP());
        il.append(new PUSH(constantPoolGen, fieldSet.size()));
        il.append(new NEWARRAY((Type.BYTE).getType()));
        Iterator iter = fieldSet.iterator();
        int push = 0;
        while (iter.hasNext()){
            FieldInfo field = (FieldInfo)iter.next();
            il.append(new DUP());
            il.append(new PUSH(constantPoolGen, push));
            il.append(new PUSH(constantPoolGen, field.getFlag()));
            il.append(new BASTORE());
            push++;
        }
        il.append(instructionFactory.createPutStatic(
                classGen.getClassName(),
                "jdoFieldFlags",
                new ArrayType(Type.BYTE,1)));
        il.append(new RETURN());
        try{
            il.delete(initialReturnHandle);
        } catch (TargetLostException e){
            InstructionHandle[] targets = e.getTargets();
            for (int i = 0; i < targets.length ; i++){
                InstructionTargeter[] targeters = targets[i].getTargeters();
                for (int j = 0; j < targeters.length ; j++){
                    targeters[j].updateTarget(targets[i], nopTarget);
                }
            }
        }
        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(m,methodGen.getMethod());
        il.dispose();

    }




    private void addInterrogatives(){
        if (classInfo.getTopPCSuperClass() == null){
            setInterrogative("jdoIsPersistent","isPersistent");
            setInterrogative("jdoIsTransactional","isTransactional");
            setInterrogative("jdoIsNew","isNew");
            setInterrogative("jdoIsDirty","isDirty");
            setInterrogative("jdoIsDeleted","isDeleted");
            setJdoGetPersistanceManager();
            setJdoMakeDirty();
            addJdoIsDetached();
        }
    }

    /**
     * Add method
     * public PersistenceManager jdoGetPersistenceManager(){
     *     return jdoStateManager == null ? null : jdoStateManager.getPersistenceManager(this);
     * }
     */
    private void setJdoGetPersistanceManager(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC ,
                new ObjectType("javax.jdo.PersistenceManager"),
                null,
                null,
                "jdoGetPersistenceManager",
                classGen.getClassName(),
                il,
                constantPoolGen);

        InstructionHandle ireturnHandle = il.insert(new ARETURN());
        il.insert(instructionFactory.createInvoke(
                STATE_MANAGER,
                "getPersistenceManager",
                new ObjectType("javax.jdo.PersistenceManager"),
                new Type[]{PC_OBJECT_TYPE},
                Constants.INVOKEINTERFACE));
        il.insert(new ALOAD(0));
        il.insert(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        InstructionHandle ifNonNullHandle = il.insert(new ALOAD(0));
        il.insert(new GOTO(ireturnHandle));
        il.insert(new ACONST_NULL());
        il.insert(new IFNONNULL(ifNonNullHandle));
        il.insert(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        il.insert(new ALOAD(0));

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();

    }
    /**
     * Add method
     *
     *  public void jdoMakeDirty(String fieldName){
     *      if(jdoStateManager==null) return;
     *      jdoStateManager.makeDirty(this, fieldName);
     *  }
     *
     */
    private void setJdoMakeDirty(){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC ,
                Type.VOID,
                new Type[]{Type.STRING},
                new String[]{"fieldName"},
                "jdoMakeDirty",
                classGen.getClassName(),
                il,
                constantPoolGen);
        il.insert(new RETURN());
        il.insert(instructionFactory.createInvoke(
                STATE_MANAGER,
                "makeDirty",
                Type.VOID,
                new Type[]{PC_OBJECT_TYPE, Type.STRING},
                Constants.INVOKEINTERFACE));
        il.insert(new ALOAD(1));
        il.insert(new ALOAD(0));
        il.insert(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        InstructionHandle ifNonNullHandle = il.insert(new ALOAD(0));
        il.insert(new RETURN());
        il.insert(new IFNONNULL(ifNonNullHandle));
        il.insert(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        il.insert(new ALOAD(0));

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addJdoFieldNames(){
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_STATIC ,// | NEW Constants.ACC_FINAL,
                new ArrayType(Type.STRING,1),
                "jdoFieldNames",
                constantPoolGen);
        makeSynthetic(fieldGen);
        classGen.addField(fieldGen.getField());
        Method m = getStaticConstructor();
        MethodGen methodGen = new MethodGen(
                m,
                classGen.getClassName(),
                constantPoolGen);
        InstructionList il = methodGen.getInstructionList();

        InstructionHandle initialReturnHandle = il.getEnd();

        InstructionHandle nopTarget = il.append(new NOP());
        il.append(new PUSH(constantPoolGen, fieldSet.size()));
        il.append(new ANEWARRAY(constantPoolGen.addClass(Type.STRING)));

        Iterator iter = fieldSet.iterator();
        int push = 0;
        while (iter.hasNext()){
            FieldInfo field = (FieldInfo)iter.next();
            il.append(new DUP());
            il.append(new PUSH(constantPoolGen, push));
            il.append(new PUSH(constantPoolGen,field.getFieldName()));
            il.append(new AASTORE());
            push++;
        }
        il.append(instructionFactory.createPutStatic(
                classGen.getClassName(),
                "jdoFieldNames",
                new ArrayType(Type.STRING,1)));
        il.append(new RETURN());
        try{
//            System.out.println("initialReturnHandle that was deleted = " + initialReturnHandle);
            il.delete(initialReturnHandle);
        } catch (TargetLostException e){
            InstructionHandle[] targets = e.getTargets();
            for (int i = 0; i < targets.length ; i++){
                InstructionTargeter[] targeters = targets[i].getTargeters();
                for (int j = 0; j < targeters.length ; j++){
                    targeters[j].updateTarget(targets[i], nopTarget);
                }
            }
        }

        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(m,methodGen.getMethod());
        il.dispose();
    }


    private void setInterrogative(String methodName,String callingMethodName){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_PUBLIC ,
                Type.BOOLEAN,
                null,
                null,
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);
        InstructionHandle ireturnHandle = il.insert(new IRETURN());
        il.insert(instructionFactory.createInvoke(
                STATE_MANAGER,
                callingMethodName,
                Type.BOOLEAN,
                new Type[]{PC_OBJECT_TYPE},
                Constants.INVOKEINTERFACE));
        il.insert(new ALOAD(0));
        il.insert(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        InstructionHandle ifNonNullHandle = il.insert(new ALOAD(0));
        il.insert(new GOTO(ireturnHandle));
        il.insert(new ICONST(0));
        il.insert(new IFNONNULL(ifNonNullHandle));
        il.insert(instructionFactory.createGetField(
                getTopPCSuperOrCurrentClassName(),
                "jdoStateManager",
                SM_OBJECT_TYPE));
        il.insert(new ALOAD(0));

//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();

    }

    private void addSerialVersionUID(){
        if (!hasSerialVersionUID()){
            FieldGen fieldGen = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_STATIC  | Constants.ACC_FINAL,
                    Type.LONG,
                    "serialVersionUID",
                    constantPoolGen);
            fieldGen.setInitValue(currentSerialVersionUID);
            makeSynthetic(fieldGen);
            classGen.addField(fieldGen.getField());

            if (javaVersion >= JAVA_1_4){
                /**
                 * todo add serialVersionUID static constructor to for jdk 1.4
                 */
            }
        }
    }
    /**
     * Check if current class has a serialVersionUID.
     *
     * @return true if it does have a serialVersionUID, else false.
     */
    private boolean hasSerialVersionUID(){
        Field [] fields = classGen.getFields();
        for(int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.getName().equals("serialVersionUID")){
                return true;
            }
        }
        return false;
    }

    /**
     * Addes a field jdoPersistenceCapableSuperclass and initializes it to null if there
     * is no persistence capable superclass else it initializes it.
     *
     */
    private void addJdoPersistenceCapableSuperclass(){
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_STATIC ,//| NEW Constants.ACC_FINAL,
                new ObjectType("java.lang.Class"),
                "jdoPersistenceCapableSuperclass",
                constantPoolGen);
        makeSynthetic(fieldGen);
        classGen.addField(fieldGen.getField());
        Method m = getStaticConstructor();
        MethodGen methodGen = new MethodGen(
                m,
                classGen.getClassName(),
                constantPoolGen);
        InstructionList il = methodGen.getInstructionList();
        InstructionHandle initialReturnHandle = il.getEnd();
        InstructionHandle nopTarget = il.append(new NOP());
        if (classInfo.getPersistenceCapableSuperclass() == null){
            il.append(InstructionConstants.ACONST_NULL);
            il.append(instructionFactory.createPutStatic(
                    classGen.getClassName(),
                    "jdoPersistenceCapableSuperclass",
                    new ObjectType("java.lang.Class")));
        } else {

            String className = classInfo.getPersistenceCapableSuperclass();
            String fieldName = getSetClass$Field(className);

            il.append(instructionFactory.createGetStatic(
                    classGen.getClassName(),
                    fieldName,
                    new ObjectType("java.lang.Class")));
            IFNONNULL ifNonNull = new IFNONNULL(null);
            il.append(ifNonNull);
            il.append(new PUSH(constantPoolGen,className));
            il.append(instructionFactory.createInvoke(
                    classGen.getClassName(),
                    "class$",
                    new ObjectType("java.lang.Class"),
                    new Type[]{new ObjectType("java.lang.String")},
                    Constants.INVOKESTATIC));
            il.append(new DUP());
            il.append(instructionFactory.createPutStatic(
                    classGen.getClassName(),
                    fieldName,
                    new ObjectType("java.lang.Class")));
            GOTO gotoInst = new GOTO(null);
            il.append(gotoInst);
            InstructionHandle ifNonNullHandle = il.append(instructionFactory.createGetStatic(
                    classGen.getClassName(),
                    fieldName,
                    new ObjectType("java.lang.Class")));
            ifNonNull.setTarget(ifNonNullHandle);
            InstructionHandle gotoHandle = il.append(instructionFactory.createPutStatic(
                    classGen.getClassName(),
                    "jdoPersistenceCapableSuperclass",
                    new ObjectType("java.lang.Class")));
            gotoInst.setTarget(gotoHandle);
        }
        il.append(new RETURN());
        try{
            il.delete(initialReturnHandle);
        } catch (TargetLostException e){
            InstructionHandle[] targets = e.getTargets();
            for (int i = 0; i < targets.length ; i++){
                InstructionTargeter[] targeters = targets[i].getTargeters();
                for (int j = 0; j < targeters.length ; j++){
                    targeters[j].updateTarget(targets[i], nopTarget);
                }
            }
        }
        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(m,methodGen.getMethod());
        il.dispose();
    }
    /**
     * All class variables i.e. String.class has a static variable called
     * class$java$lang$String of type java.lang.Class, this method creates it if
     * it does not exist.
     *
     * @param  className
     * @return String fieldName i.e. class$java$lang$String
     */
    private String getSetClass$Field(String className){
        String fullClassName = "class."+className;
        String fieldName = fullClassName.replace('.','$');
        Field [] fields = classGen.getFields();
        for(int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.getName().equals(fieldName)){
                return fieldName;
            }
        }
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_STATIC,
                new ObjectType("java.lang.Class"),
                fieldName,
                constantPoolGen);
        classGen.addField(fieldGen.getField());
        return fieldName;

    }
    /**
     * All class variables i.e. String.class has a static variable called
     * class$java$lang$String of type java.lang.Class, this method creates it if
     * it does not exist.
     *
     * @param field
     * @return String fieldName i.e. class$java$lang$String
     */
    private String getSetClass$Field(FieldInfo field){
        String fullClassName = null;
        if (field.isArray()){
            fullClassName = ("array" + field.getSignature()).replace('/', '$');
            fullClassName = fullClassName.replace('[', '$');
            fullClassName = (fullClassName.replace(';', ' ')).trim();
        }else{
            fullClassName = "class."+field.getReturnType();
        }
        String fieldName = fullClassName.replace('.','$');
        Field [] fields = classGen.getFields();
        for(int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.getName().equals(fieldName)){
                return fieldName;
            }
        }
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_STATIC,
                new ObjectType("java.lang.Class"),
                fieldName,
                constantPoolGen);
        makeSynthetic(fieldGen);
        classGen.addField(fieldGen.getField());
        return fieldName;

    }
    /**
     * Check if current class implements PersistenceCapable.
     *
     * @return true if it does implement PersistenceCapable else false.
     */
    private boolean implementsPC(){
        String[] interfaceNames = classGen.getInterfaceNames();
        for(int i = 0; i < interfaceNames.length; i++) {
            if (interfaceNames[i].equals(PERSISTENCE_CAPABLE)){
                return true;
            }
        }
        return false;
    }
    /**
     * All class variables i.e. String.class has a static class$ method that check if the class
     * exists by doing a class.forname() on the class and throws a NoClassDefFoundError error,
     * if the class does not exists
     *
     */
    private void setClass$(){
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];

            if (m.getName().equals("class$") &&
                    "(Ljava/lang/String;)Ljava/lang/Class;".equals(m.getSignature())){
                return;
            }
        }
        // there is no class$ method so make one
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_STATIC,
                new ObjectType("java.lang.Class"),
                new Type[]{new ObjectType("java.lang.String")},
                new String[]{"x0"},
                "class$",
                classGen.getClassName(),
                il,
                constantPoolGen);

        InstructionHandle tryStart = il.append(new ALOAD(0));
        il.append(instructionFactory.createInvoke(
                "java.lang.Class",
                "forName",
                new ObjectType("java.lang.Class"),
                new Type[]{new ObjectType("java.lang.String")},
                Constants.INVOKESTATIC));
        InstructionHandle tryEnd = il.append(new ARETURN());
        InstructionHandle handler = il.append(new ASTORE(1));
        InstructionHandle varStart = il.append(instructionFactory.createNew("java.lang.NoClassDefFoundError"));
        il.append(new DUP());
        il.append(new ALOAD(1));
        il.append(instructionFactory.createInvoke(
                "java.lang.Throwable",
                "getMessage",
                new ObjectType("java.lang.String"),
                new Type[]{},
                Constants.INVOKEVIRTUAL));
        il.append(instructionFactory.createInvoke(
                "java.lang.NoClassDefFoundError",
                "<init>",
                Type.VOID,
                new Type[]{new ObjectType("java.lang.String")},
                Constants.INVOKESPECIAL));
        InstructionHandle varEnd = il.append(new ATHROW());
        methodGen.addLocalVariable("x1",new ObjectType("java.lang.ClassNotFoundException"),varStart,varEnd);
        methodGen.addExceptionHandler(tryStart,tryEnd,handler,new ObjectType("java.lang.ClassNotFoundException"));
        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        Method class$Method = methodGen.getMethod();
        classGen.addMethod(class$Method);
        il.dispose();
        return ;

    }

    private void rewriteStaticConstructor(){
        Method[] methods = classGen.getMethods();
        for (int k = 0; k < methods.length; k++) {
            Method m = methods[k];
            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()) {
                continue;
            }
            if (m.getName().equals("<clinit>")) {//is static constructor
                boolean hasChanges = false;
                MethodGen mg = new MethodGen(m, classGen.getClassName(), constantPoolGen);

                // get the code in form of an InstructionList object
                InstructionList il = mg.getInstructionList();
                Instruction ins;
                InstructionHandle ih = il.getStart();
                while (ih != null) {
                    ins = ih.getInstruction();
                    if (ins instanceof LDC) {
                        LDC ldc = (LDC) ins;
                        Constant c = constantPoolGen.getConstantPool().getConstant(ldc.getIndex());
                        if (c.getTag() == Constants.CONSTANT_Class) {
                            hasChanges = true;
                            String name = constantPoolGen.getConstantPool().getConstantString(ldc.getIndex(), Constants.CONSTANT_Class);

                            if (!name.startsWith("[")) {
                                name = "L" + name + ";";
                            }

                            Type type = Type.getType(name);
                            InstructionList tempIl = pushDotClass(type);
                            InstructionHandle startTarget = tempIl.getStart();
                            InstructionHandle newTarget = tempIl.getEnd();
                            il.append(ih, tempIl);
                            try {
                                il.delete(ih);
                            } catch (TargetLostException e) {
                                InstructionHandle[] targets = e.getTargets();
                                for (int i = 0; i < targets.length; i++) {
                                    InstructionTargeter[] targeters = targets[i].getTargeters();
                                    for (int j = 0; j < targeters.length; j++) {
                                        targeters[j].updateTarget(targets[i], newTarget);
                                    }
                                }
                            }
                            il.setPositions();
                            il.update();
                            // Fix line numbers
                            LineNumberGen[] numbersGen = mg.getLineNumbers();
                            if (numbersGen != null){
                                for (int i = 0; i < numbersGen.length; i++) {
                                    LineNumberGen lineNumberGen = numbersGen[i];
                                    if (lineNumberGen.containsTarget(newTarget)){
                                        lineNumberGen.setInstruction(startTarget);
                                    }
                                }
                            }

                            ih = il.getStart();

                        }
                    }
                    ih = ih.getNext();
                }
                if (hasChanges){
                    il.setPositions();
                    il.update();
                    mg.removeNOPs();
                    mg.setMaxLocals();
                    mg.setMaxStack();
                    classGen.replaceMethod(m, mg.getMethod());
                }
            }
        }
    }
    /**
     * Returns the static constructor if it exists, else it creates it and then
     * returns it.
     *
     */
    private Method getStaticConstructor(){
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()){
                continue;
            }
            if (m.getName().equals("<clinit>")){//is static constructor
                return m;
            }
        }
        // there is no static constructor so make one
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(
                Constants.ACC_STATIC,
                Type.VOID,
                null,
                null,
                "<clinit>",
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new RETURN());
//        makeSynthetic(methodGen);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        Method clMethod = methodGen.getMethod();
        classGen.addMethod(clMethod);
        il.dispose();
        return clMethod;

    }

    /**
     * Adds field jdoStateManager it does not need to be initializes.
     *
     */
    private void addJdoStateManager(){
        //this field only gets added to the least-derived persistence capable class.
        if (classInfo.getPersistenceCapableSuperclass() == null){
            FieldGen fieldGen = new FieldGen(
                    Constants.ACC_PROTECTED | Constants.ACC_TRANSIENT,
                    SM_OBJECT_TYPE,
                    "jdoStateManager",
                    constantPoolGen);
            makeSynthetic(fieldGen);
            classGen.addField(fieldGen.getField());
        }
    }

    /**
     * Adds detatch fields it does not need to be initializes.
     */
    private void addDetatchFields() {
        //this field only gets added to the least-derived persistence capable class.
        if (classInfo.getPersistenceCapableSuperclass() == null) {
            FieldGen fieldGen = new FieldGen(Constants.ACC_PROTECTED,
                    BITSET_TYPE,
                    "jdoLoadedFields",
                    constantPoolGen);
            makeSynthetic(fieldGen);
            classGen.addField(fieldGen.getField());

            FieldGen fieldGen2 = new FieldGen(Constants.ACC_PROTECTED,
                    BITSET_TYPE,
                    "jdoModifiedFields",
                    constantPoolGen);
            makeSynthetic(fieldGen2);
            classGen.addField(fieldGen2.getField());
        }
    }

    /**
     * Adds field jdoFlags in the top most persistence capable super class and initializes
     * it to javax.jdo.spi.PersistenceCapable.READ_WRITE_OK.
     * The initialization needs to occur in all the constructors as it is with all class fields.
     *
     */
    private void addJdoFlags() {
        //this field only gets added to the least-derived persistence capable class.
        if (classInfo.getPersistenceCapableSuperclass() == null){
            FieldGen fieldGen = new FieldGen(
                    Constants.ACC_PROTECTED | Constants.ACC_TRANSIENT,
                    Type.BYTE,
                    "jdoFlags",
                    constantPoolGen);
            makeSynthetic(fieldGen);
            classGen.addField(fieldGen.getField());
            List constructors = getInitilizationConstructors();//very important to only get these constructors
            Iterator iter = constructors.iterator();
            while (iter.hasNext()){
                Method constructor = (Method)iter.next();
                MethodGen mg = new MethodGen(
                        constructor,
                        classInfo.getClassName(),
                        constantPoolGen);

                InstructionList il = mg.getInstructionList();
                // get the first instruction
                InstructionHandle ih = il.getStart();
                while (ih != null) {
                    Instruction ins = ih.getInstruction();

                    if (ins instanceof INVOKESPECIAL) {// found the INVOKESPECIAL call
                        InstructionList tmpIl = new InstructionList();
                        tmpIl.append(new ALOAD(0));
                        tmpIl.append(new ICONST(0));//javax.jdo.spi.PersistenceCapable.READ_WRITE_OK
                        tmpIl.append(instructionFactory.createPutField(classInfo.getClassName(),
                                "jdoFlags",
                                Type.BYTE));
                        il.append(ih, tmpIl);
                        il.setPositions();
                        il.update();

                        mg.setInstructionList(il);
                        mg.setMaxLocals();
                        mg.setMaxStack();
                        classGen.replaceMethod(constructor, mg.getMethod());
                        il.dispose();
                        break;
                    }
                    // next instruction
                    ih = ih.getNext();
                }
            }
        }
    }

    /**
     * Gets a list of all the constructors methods
     */
    private List getConstructors(){
        ArrayList constuctors = new ArrayList();
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()){
                continue;
            }
            if (m.getName().equals("<init>")){//is constructor
                constuctors.add(m);
            }
        }
        return constuctors;
    }
    /**
     * Sets a default constructor if it needs one or changes the default
     * constructor's scope if needed.
     */
    private void setDefaultConstructor(){
//        if (classGen.isAbstract())return;
        boolean setDefautConstructor = true;
        Method[] methods = classGen.getMethods();
        for(int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()){
                continue;
            }

            if (m.getName().equals("<init>")){              //is constructor
                if (m.getSignature().equals("()V")){        //is no args constructor
//                  if (m.isPublic() || m.isProtected()){   //is public or protected
                    if (m.isPublic() ){
                        setDefautConstructor = false;       //there is a default constructor with the right access
                    }else {                                 //there is a default constructor but access is wrong
                        m.isPublic(true);
	                    m.isProtected(false);                //change access to protected
                        m.isPrivate(false);                 //take away private access
                        setDefautConstructor = false;       //now there is a default constructor with the right access
                    }
                }
            }
        }
        if (setDefautConstructor){
            InstructionList il = new InstructionList();
            il.append(InstructionConstants.THIS); // Push `this'
            il.append(new INVOKESPECIAL(constantPoolGen.addMethodref(
                    classGen.getSuperclassName(),
                    "<init>",
                    "()V")));
            il.append(InstructionConstants.RETURN);

            MethodGen methodGen = new MethodGen(
                    Constants.ACC_PUBLIC, // todo this is not to spec it should be protected
                    Type.VOID,
                    Type.NO_ARGS,
                    null,
                    "<init>",
                    classGen.getClassName(),
                    il,
                    constantPoolGen);
            methodGen.setMaxLocals();
            methodGen.setMaxStack();

            classGen.addMethod(methodGen.getMethod());
            didWeAddADefaultConstructor = true;
//            classGen.addEmptyConstructor(Constants.ACC_PUBLIC);// todo this is not to spec it should be protected
            // If initialization of user-declared field are required use this method
            //createDefaultInitializationConstructor();
        }
    }

    /**
     * Gets a list of all the initilization constructors methods
     */
    private List getInitilizationConstructors(){
        ArrayList initConst = new ArrayList();
        List list = getConstructors();
        Iterator iter = list.iterator();
        while (iter.hasNext()){
            Method m = (Method)iter.next();

            MethodGen mg = new MethodGen(m, classGen.getClassName(), constantPoolGen);

            // get the code in form of an InstructionList object
            InstructionList il = mg.getInstructionList();

            // get the first instruction
            InstructionHandle ih = il.getStart();
            while (ih != null) {
                Instruction ins = ih.getInstruction();
                if (ins.getClass().getName().equals(invokeSpecial)) {
                    INVOKESPECIAL is = (INVOKESPECIAL) ins;
                    if (is.getClassName(constantPoolGen).equals(classGen.getSuperclassName()) &&
                            is.getMethodName(constantPoolGen).equals("<init>")) {
                        initConst.add(m);
                    }
                }
                // next instruction
                ih = ih.getNext();
            }
        }
        return initConst;
    }


    /**
     * this method is not needed yet.
     */
//    private void createDefaultInitializationConstructor(){
//        List constructors = getInitilizationConstructors();
//        Iterator iter = constructors.iterator();
//        boolean wasGen = false;
//        while (iter.hasNext()){
//            Method m = (Method)iter.next();
//            MethodGen mg = new MethodGen(m, classInfo.getClassName() ,constantPoolGen);
//            LineNumberGen[] lineNumberGen = mg.getLineNumbers();
//            int firstLine = 0;
//            int nextLine = 10000;
//            int nextOffSet = 0;
//            InstructionHandle fromIH = null;
//            for (int i = 0; i < lineNumberGen.length ; i++){
//                int sourceLine = lineNumberGen[i].getSourceLine();
//                if (lineNumberGen[i].getLineNumber().getStartPC() == 0){
//                    firstLine = lineNumberGen[i].getSourceLine();
//                }
//
//                if (sourceLine > firstLine && sourceLine < nextLine){
//                    if (lineNumberGen[i].getInstruction().getInstruction() instanceof RETURN) {
//                    } else {
//                        fromIH = lineNumberGen[i].getInstruction();
//                        nextLine = sourceLine;
//                        nextOffSet = lineNumberGen[i].getLineNumber().getStartPC();
//                    }
//                }
//            }
//
//            if (nextOffSet > 5){
//                InstructionList il = mg.getInstructionList();
//                InstructionHandle toIH = il.getEnd().getPrev();
//                try{
//                    if (!fromIH.equals(il.getEnd().getPrev())){
//                        il.delete(fromIH,il.getEnd().getPrev());
//                    }
//                } catch (TargetLostException e){
//                    InstructionHandle[] targets = e.getTargets();
//                    for (int i = 0; i < targets.length ; i++){
//                        InstructionTargeter[] targeters = targets[i].getTargeters();
//                        for (int j = 0; j < targeters.length ; j++){
//                            targeters[j].updateTarget(targets[i], il.getEnd());
//                        }
//                    }
//                }
//
//                MethodGen newCons = new MethodGen(  Constants.ACC_PROTECTED,
//                        Type.VOID,
//                        new Type[]{},
//                        null,
//                        "<init>",
//                        classInfo.getClassName(),
//                        il,
//                        constantPoolGen);
//                newCons.getMaxLocals();
//                newCons.getMaxStack();
//                classGen.addMethod(newCons.getMethod());
//                wasGen = true;
//                break;
//            }
//        }
//        if (!wasGen){
//            classGen.addEmptyConstructor(Constants.ACC_PROTECTED);
//        }
//    }




    /**
     * All class variables i.e. String.class has a static variable called
     * class$java$lang$String of type java.lang.Class, this method creates it if
     * it does not exist.
     *
     * @param type
     * @return String fieldName i.e. class$java$lang$String
     */
    private InstructionList pushDotClass(Type type) {
        InstructionList il = new InstructionList();
        String signature = type.getSignature();
        String fieldName = null;
        String tempFieldName = null;
        String loadName = null;

        if (signature.startsWith("[")) {
            tempFieldName = ("array" + signature).replace('/', '$');
            tempFieldName = tempFieldName.replace('[', '$');
            fieldName = (tempFieldName.replace(';', ' ')).trim();
            loadName = signature.replace('/', '.');
        } else {
            tempFieldName = ("class/" + signature).replace('/', '$');
            fieldName = (tempFieldName.replace(';', ' ')).trim();
            loadName = type.toString();
        }

        il.append(instructionFactory.createGetStatic(classGen.getClassName(), fieldName, new ObjectType("java.lang.Class")));
        IFNONNULL ifnonnull = new IFNONNULL(null);
        il.append(ifnonnull);
        il.append(new PUSH(constantPoolGen, loadName));
        il.append(instructionFactory.createInvoke(
                classGen.getClassName(),
                "class$",
                new ObjectType("java.lang.Class"),
                new Type[]{Type.STRING},
                Constants.INVOKESTATIC));
        il.append(new DUP());
        il.append(instructionFactory.createPutStatic(
                classGen.getClassName(),
                fieldName,
                new ObjectType("java.lang.Class")));
        GOTO aGoto = new GOTO(null);
        il.append(aGoto);
        InstructionHandle getStaticHandle = il.append(instructionFactory.createGetStatic(
                classGen.getClassName(),
                fieldName,
                new ObjectType("java.lang.Class")));
        ifnonnull.setTarget(getStaticHandle);
        InstructionHandle lastHandle = il.append(new NOP());
        aGoto.setTarget(lastHandle);

        setClass$();
        Field[] fields = classGen.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.getName().equals(fieldName)) {
                return il;
            }
        }
        FieldGen fieldGen = new FieldGen(
                Constants.ACC_STATIC | Constants.ACC_PRIVATE,
                new ObjectType("java.lang.Class"),
                fieldName,
                constantPoolGen);
        classGen.addField(fieldGen.getField());
        return il;

    }



    /**
     * Compute the JVM signature for the class.
     */
    static String getSignature(Class clazz) {
        String type = null;
        if (clazz.isArray()) {
            Class cl = clazz;
            int dimensions = 0;
            while (cl.isArray()) {
                dimensions++;
                cl = cl.getComponentType();
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dimensions; i++) {
                sb.append("[");
            }
            sb.append(getSignature(cl));
            type = sb.toString();
        } else if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                type = "I";
            } else if (clazz == Byte.TYPE) {
                type = "B";
            } else if (clazz == Long.TYPE) {
                type = "J";
            } else if (clazz == Float.TYPE) {
                type = "F";
            } else if (clazz == Double.TYPE) {
                type = "D";
            } else if (clazz == Short.TYPE) {
                type = "S";
            } else if (clazz == Character.TYPE) {
                type = "C";
            } else if (clazz == Boolean.TYPE) {
                type = "Z";
            } else if (clazz == Void.TYPE) {
                type = "V";
            }
        } else {
            type = "L" + clazz.getName().replace('.', '/') + ";";
        }
        return type;
    }

    // all the detach suff goes here


    private void addIsDirtyInt(String methodName) {

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.BOOLEAN,
                new Type[]{Type.INT},
                new String[]{"fieldNo"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        int num = getNumOfControlFields();
        if (num == 1) {
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    getDirtyFieldName(0),
                    Type.INT));
            il.append(new ICONST(1));
            il.append(new ILOAD(1));
            il.append(new ISHL());
            il.append(new IAND());
            IFNE ifne = new IFNE(null);
            il.append(ifne);
            il.append(new ICONST(0));
            il.append(new IRETURN());
            InstructionHandle handle = il.append(new ICONST(1));
            ifne.setTarget(handle);
            il.append(new IRETURN());

        } else {
            il.append(new ILOAD(1));
            for (int i = 0; i < num; i++) {
                il.append(new PUSH(constantPoolGen, ((32 * i) + 32)));
                IF_ICMPGE if_icmpge = new IF_ICMPGE(null);
                il.append(if_icmpge);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        getDirtyFieldName(i),
                        Type.INT));
                il.append(new ICONST(1));
                il.append(new ILOAD(1));
                il.append(new ISHL());
                il.append(new IAND());
                IFEQ ifeq = new IFEQ(null);
                il.append(ifeq);
                il.append(new ICONST(1));
                GOTO aGoto = new GOTO(null);
                il.append(aGoto);
                InstructionHandle iconst0Handle = il.append(new ICONST(0));
                ifeq.setTarget(iconst0Handle);
                InstructionHandle gotoHandle = il.append(new IRETURN());
                aGoto.setTarget(gotoHandle);
                InstructionHandle iload1Handle = il.append(new ILOAD(1));
                if_icmpge.setTarget(iload1Handle);
            }
            // take last one and replace with ICONST_0
            InstructionHandle lastHandle = il.getEnd();
            InstructionHandle replaceHandle = il.append(new ICONST(0));
            try {
                il.delete(lastHandle);
            } catch (TargetLostException e) {
                InstructionHandle[] targets = e.getTargets();
                for (int i = 0; i < targets.length; i++) {
                    InstructionTargeter[] targeters = targets[i].getTargeters();
                    for (int j = 0; j < targeters.length; j++) {
                        targeters[j].updateTarget(targets[i], replaceHandle);
                    }
                }
            }
            il.append(new IRETURN());
        }

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addMakeDirtyInt(String methodName) {

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.VOID,
                new Type[]{Type.INT},
                new String[]{"fieldNo"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        int num = getNumOfControlFields();
        if (num == 1) {
            il.append(new ALOAD(0));
            il.append(new DUP());
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    getDirtyFieldName(0),
                    Type.INT));

            il.append(new ICONST(1));
            il.append(new ILOAD(1));
            il.append(new ISHL());
            il.append(new IOR());
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    getDirtyFieldName(0),
                    Type.INT));
            il.append(new RETURN());
        } else {
            il.append(new ILOAD(1));
            for (int i = 0; i < num; i++) {
                il.append(new PUSH(constantPoolGen, ((32 * i) + 32)));
                IF_ICMPGE if_icmpge = new IF_ICMPGE(null);
                il.append(if_icmpge);

                il.append(new ALOAD(0));
                il.append(new DUP());
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        getDirtyFieldName(i),
                        Type.INT));
                il.append(new ICONST(1));
                il.append(new ILOAD(1));
                il.append(new ISHL());
                il.append(new IOR());
                il.append(instructionFactory.createPutField(classGen.getClassName(),
                        getDirtyFieldName(i),
                        Type.INT));
                if (i != (num - 1)) {
                    il.append(new RETURN());
                    InstructionHandle iloadHandle = il.append(new ILOAD(1));
                    if_icmpge.setTarget(iloadHandle);
                } else {
                    InstructionHandle returnHandle = il.append(new RETURN());   // last return
                    if_icmpge.setTarget(returnHandle);
                }
            }

        }
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addMakeDirtyString(String methodName) {

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC,
                Type.VOID,
                new Type[]{Type.STRING},
                new String[]{"fieldName"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        ArrayList list = new ArrayList(fieldSet);
        Collections.sort(list);
        Iterator iter = list.iterator();
        ArrayList gotos = new ArrayList();

        while (iter.hasNext()) {
            FieldInfo info = (FieldInfo) iter.next();
            String fieldname = info.getFieldName();
            int fieldNo = info.getFieldNo();

            il.append(new PUSH(constantPoolGen, fieldname));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke(
                    "java.lang.String",
                    "equals",
                    Type.BOOLEAN,
                    new Type[]{Type.OBJECT},
                    Constants.INVOKEVIRTUAL));
            IFEQ ifeq = new IFEQ(null);        //17
            il.append(ifeq);
            il.append(new ALOAD(0));
            il.append(new PUSH(constantPoolGen, fieldNo));
            il.append(instructionFactory.createInvoke(
                    classGen.getClassName(),
                    "versantMakeDirty",
                    Type.VOID,
                    new Type[]{Type.INT},
                    Constants.INVOKEVIRTUAL));
            GOTO aGoto = new GOTO(null);
            gotos.add(aGoto);
            il.append(aGoto);
            InstructionHandle nopHandle = il.append(new NOP());
            ifeq.setTarget(nopHandle);
        }
        // now we do the else part
        if (classInfo.getPersistenceCapableSuperclass() != null){
            il.append(new ALOAD(0));
            il.append(new ALOAD(1));
            il.append(instructionFactory.createInvoke(
                    classInfo.getPersistenceCapableSuperclass(),
                    "versantMakeDirty",
                    Type.VOID,
                    new Type[]{Type.STRING},
                    Constants.INVOKESPECIAL));
        } else {
           // todo throw exception or something
        }



        InstructionHandle lastHandle = il.append(new RETURN());
        for (Iterator iterator = gotos.iterator(); iterator.hasNext();) {
            GOTO aGoto = (GOTO) iterator.next();
            aGoto.setTarget(lastHandle);
        }
        methodGen.removeNOPs();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }


    private void addIsDirty(String methodName) {

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC , //| NEW Constants.ACC_FINAL,
                Type.BOOLEAN,
                new Type[]{},
                new String[]{},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        ArrayList list = new ArrayList();
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    getDirtyFieldName(i),
                    Type.INT));
            IFNE ifne = new IFNE(null);
            list.add(ifne);
            il.append(ifne);
        }
        il.append(new ICONST(0));
        il.append(new IRETURN());
        InstructionHandle ifne_handle = il.append(new ICONST(1));
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            IFNE ifne = (IFNE) iter.next();
            ifne.setTarget(ifne_handle);
        }
        il.append(new IRETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addIsLoadedInt(String methodName) {

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.BOOLEAN,
                new Type[]{Type.INT},
                new String[]{"fieldNo"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        int num = getNumOfControlFields();
        if (num == 1) {
            il.append(new ALOAD(0));
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    getLoadedFieldName(0),
                    Type.INT));
            il.append(new ICONST(1));
            il.append(new ILOAD(1));
            il.append(new ISHL());
            il.append(new IAND());
            IFNE ifne = new IFNE(null);
            il.append(ifne);
            il.append(new ICONST(0));
            il.append(new IRETURN());
            InstructionHandle handle = il.append(new ICONST(1));
            ifne.setTarget(handle);
            il.append(new IRETURN());

        } else {
            il.append(new ILOAD(1));
            for (int i = 0; i < num; i++) {
                il.append(new PUSH(constantPoolGen, ((32 * i) + 32)));
                IF_ICMPGE if_icmpge = new IF_ICMPGE(null);
                il.append(if_icmpge);
                il.append(new ALOAD(0));
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        getLoadedFieldName(i),
                        Type.INT));
                il.append(new ICONST(1));
                il.append(new ILOAD(1));
                il.append(new ISHL());
                il.append(new IAND());
                IFEQ ifeq = new IFEQ(null);
                il.append(ifeq);
                il.append(new ICONST(1));
                GOTO aGoto = new GOTO(null);
                il.append(aGoto);
                InstructionHandle iconst0Handle = il.append(new ICONST(0));
                ifeq.setTarget(iconst0Handle);
                InstructionHandle gotoHandle = il.append(new IRETURN());
                aGoto.setTarget(gotoHandle);
                InstructionHandle iload1Handle = il.append(new ILOAD(1));
                if_icmpge.setTarget(iload1Handle);
            }
            // take last one and replace with ICONST_0
            InstructionHandle lastHandle = il.getEnd();
            InstructionHandle replaceHandle = il.append(new ICONST(0));
            try {
                il.delete(lastHandle);
            } catch (TargetLostException e) {
                InstructionHandle[] targets = e.getTargets();
                for (int i = 0; i < targets.length; i++) {
                    InstructionTargeter[] targeters = targets[i].getTargeters();
                    for (int j = 0; j < targeters.length; j++) {
                        targeters[j].updateTarget(targets[i], replaceHandle);
                    }
                }
            }
            il.append(new IRETURN());
        }

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addSetLoadedInt(String methodName) {

        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.VOID,
                new Type[]{Type.INT},
                new String[]{"fieldNo"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        int num = getNumOfControlFields();
        if (num == 1) {
            il.append(new ALOAD(0));
            il.append(new DUP());
            il.append(instructionFactory.createGetField(classGen.getClassName(),
                    getLoadedFieldName(0),
                    Type.INT));

            il.append(new ICONST(1));
            il.append(new ILOAD(1));
            il.append(new ISHL());
            il.append(new IOR());
            il.append(instructionFactory.createPutField(classGen.getClassName(),
                    getLoadedFieldName(0),
                    Type.INT));
            il.append(new RETURN());
        } else {
            il.append(new ILOAD(1));
            for (int i = 0; i < num; i++) {
                il.append(new PUSH(constantPoolGen, ((32 * i) + 32)));
                IF_ICMPGE if_icmpge = new IF_ICMPGE(null);
                il.append(if_icmpge);

                il.append(new ALOAD(0));
                il.append(new DUP());
                il.append(instructionFactory.createGetField(classGen.getClassName(),
                        getLoadedFieldName(i),
                        Type.INT));
                il.append(new ICONST(1));
                il.append(new ILOAD(1));
                il.append(new ISHL());
                il.append(new IOR());
                il.append(instructionFactory.createPutField(classGen.getClassName(),
                        getLoadedFieldName(i),
                        Type.INT));
                if (i != (num - 1)) {
                    il.append(new RETURN());
                    InstructionHandle iloadHandle = il.append(new ILOAD(1));
                    if_icmpge.setTarget(iloadHandle);
                } else {
                    InstructionHandle returnHandle = il.append(new RETURN());   // last return
                    if_icmpge.setTarget(returnHandle);
                }
            }

        }
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }



    private final int getNumOfControlFields() {
        return (totlalManagedFields / 32) + 1;
    }

    private final String getDirtyFieldName(int index) {
        return DIRTY_FIELD_NAME + index;
    }

    private final String getLoadedFieldName(int index) {
        return LOADED_FIELD_NAME + index;
    }

    private void addFields() {
        int num = getNumOfControlFields();
        for (int i = 0; i < num; i++) {
            FieldGen fieldGenFilled = new FieldGen(// Added our int LOADED_FIELD_NAME , to see what has been loaded in
                    Constants.ACC_PRIVATE ,
                    Type.INT,
                    LOADED_FIELD_NAME + i,
                    constantPoolGen);
            makeSynthetic(fieldGenFilled);
            classGen.addField(fieldGenFilled.getField());

            FieldGen fieldGenDirtyFields = new FieldGen(// Added our int DIRTY_FIELD_NAME , to see what is dirty
                    Constants.ACC_PRIVATE ,
                    Type.INT,
                    DIRTY_FIELD_NAME + i,
                    constantPoolGen);
            makeSynthetic(fieldGenDirtyFields);
            classGen.addField(fieldGenDirtyFields.getField());

        }

        FieldGen fieldGenVersionField = new FieldGen(// Added our Version field
                Constants.ACC_PRIVATE ,
                Type.OBJECT,
                VERSION_FIELD_NAME,
                constantPoolGen);
        makeSynthetic(fieldGenVersionField);
        classGen.addField(fieldGenVersionField.getField());

        FieldGen fieldGenOIDField = new FieldGen(// Added our OID field
                Constants.ACC_PRIVATE ,
                Type.OBJECT,
                OID_FIELD_NAME ,
                constantPoolGen);
        makeSynthetic(fieldGenOIDField);
        classGen.addField(fieldGenOIDField.getField());
    }

    private void addGetOid(String methodName){
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,// | NEW Constants.ACC_FINAL,
                Type.OBJECT,
                new Type[]{},
                new String[]{},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ALOAD(0));
        il.append(instructionFactory.createGetField(
                classGen.getClassName(),
                OID_FIELD_NAME,
                Type.OBJECT));
        il.append(new ARETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();

    }

    private void addSetOid(String methodName) {
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.VOID,
                new Type[]{Type.OBJECT},
                new String[]{"oid"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ALOAD(0));
        il.append(new ALOAD(1));
        il.append(instructionFactory.createPutField(
                classGen.getClassName(),
                OID_FIELD_NAME,
                Type.OBJECT));
        il.append(new RETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addGetVersion(String methodName) {
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.OBJECT,
                new Type[]{},
                new String[]{},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ALOAD(0));
        il.append(instructionFactory.createGetField(
                classGen.getClassName(),
                VERSION_FIELD_NAME,
                Type.OBJECT));
        il.append(new ARETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();

    }

    private void addGetStateManager(String methodName) {
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                new ObjectType(DETACHED_STATE_MANAGER),
                new Type[]{},
                new String[]{},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ALOAD(0));
        il.append(instructionFactory.createGetField(classGen.getClassName(),
                "jdoStateManager",
                new ObjectType(STATE_MANAGER)));
        il.append(new INSTANCEOF(constantPoolGen.addClass(DETACHED_STATE_MANAGER)));
        IFEQ ifeq = new IFEQ(null);
        il.append(ifeq);
        il.append(new ALOAD(0));
        il.append(instructionFactory.createGetField(classGen.getClassName(),
                "jdoStateManager",
                new ObjectType(STATE_MANAGER)));
        il.append(instructionFactory.createCheckCast(new ObjectType(DETACHED_STATE_MANAGER)));
        il.append(new ARETURN());
        InstructionHandle nullHandle = il.append(new ACONST_NULL());
        ifeq.setTarget(nullHandle);
        il.append(new ARETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();

    }

    private void addSetVersion(String methodName) {
        InstructionList il = new InstructionList();
        MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC ,//| NEW Constants.ACC_FINAL,
                Type.VOID,
                new Type[]{Type.OBJECT},
                new String[]{"version"},
                methodName,
                classGen.getClassName(),
                il,
                constantPoolGen);

        il.append(new ALOAD(0));
        il.append(new ALOAD(1));
        il.append(instructionFactory.createPutField(
                classGen.getClassName(),
                VERSION_FIELD_NAME,
                Type.OBJECT));
        il.append(new RETURN());

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        il.dispose();
    }

    private void addDetachInterfase() {
        classGen.addInterface(DETACHABLE_INTERFASE);
    }
}
