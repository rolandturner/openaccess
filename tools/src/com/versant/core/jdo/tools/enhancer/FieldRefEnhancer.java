
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

import com.versant.core.jdo.tools.enhancer.utils.*;

import java.util.*;
import java.io.*;
import java.net.URL;

import com.versant.lib.bcel.classfile.*;
import com.versant.lib.bcel.generic.*;
import com.versant.lib.bcel.Constants;
import com.versant.lib.bcel.*;
/**
 * The FieldRefEnhancer catches all the Persistent aware classes
 */
public class FieldRefEnhancer {
    private ClassLoader loader;
    private HashMap fieldToEnhanceMap;
    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;
    private InstructionFactory instructionFactory;
    private File outputDir;
    private ArrayList pcList = new ArrayList();
    private boolean hasChanges;
    private static final String GET_FIELD = com.versant.lib.bcel.generic.GETFIELD.class.getName();
    private static final String PUT_FIELD = com.versant.lib.bcel.generic.PUTFIELD.class.getName();
    private static final String A_LOAD = com.versant.lib.bcel.generic.ALOAD.class.getName();
    private static final String D_U_P = com.versant.lib.bcel.generic.DUP.class.getName();
    private int aware;
    private String fileSeparator;
	private char charfileSeparator;
    private File currentOutputFile;


    public FieldRefEnhancer(File outputDir,ClassLoader loader) {
        this.outputDir = outputDir;
        this.loader = loader;
        fileSeparator = System.getProperty("file.separator");
        charfileSeparator = fileSeparator.charAt(0);
    }

    public void setFieldsToEnhanceMap(HashMap map){
        fieldToEnhanceMap = map;
    }

    public int getAwareNum(){
        return aware;
    }
    public void setPersistentCapable(String pcClassName){
        pcList.add(pcClassName);
    }

    public void enhance(Set scopeFiles){
        if (fieldToEnhanceMap.isEmpty())return;
        Iterator iter = scopeFiles.iterator();
        while (iter.hasNext()){
            String fileName = (String)iter.next();
            try {
                hasChanges = false;
                JavaClass javaClass = getJavaClass(fileName);
                classGen = new ClassGen(javaClass);
                if (!pcList.contains(classGen.getClassName())) {
                    // ConstantPoolGen is used to represent the constant pool of a Classfile
                    constantPoolGen = classGen.getConstantPool();
                    // used to create objects representing VM instructions
                    instructionFactory = new InstructionFactory(constantPoolGen);
                    swapGetterAndSetter();
                    if (hasChanges){
                        dumpClass();
                        aware++;
                        System.out.println("Persistent Aware = " + classGen.getClassName());
                    }
                }
            } catch (Exception e){
                e.printStackTrace(); // TODO fix this
            }
        }
    }


    private JavaClass getJavaClass(String className) throws IOException {
        InputStream inputStream = loader.getResourceAsStream(className);
        if (inputStream == null) {
            if (inputStream == null) {
                throw new javax.jdo.JDOFatalUserException("Class not found: " + className);
            }
        }
        ClassParser parser = new ClassParser(inputStream, className);
        return parser.parse();
    }

    private void dumpClass() throws VerifyException {
        String fileName = classGen.getClassName().replace('.', charfileSeparator) + ".class";
        File dumpFile;
        if (outputDir != null) {
            dumpFile = new File(outputDir, fileName);
        } else {
            URL currentFileURL = loader.getResource(fileName);
            if (currentFileURL.toString().startsWith("jar:")) {
                throw new javax.jdo.JDOFatalUserException("Can not write file " + fileName + " into a jar. Please specify a output directory.");
            }
            currentOutputFile = new File(currentFileURL.getFile());
            dumpFile = currentOutputFile;
        }
        try {
            classGen.getJavaClass().dump(dumpFile);
        } catch (IOException e) {
            throw new VerifyException(e);
        }

    }


    private void swapGetterAndSetter() {

        // representation of methods in the class
        Method[] methods = classGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];

            // native and abstract methods don't have any code
            // so continue with next method
            if (m.isNative() || m.isAbstract()) {
                continue;
            }
            //we do not want to enhance our enhanced methods
            if (m.getName().startsWith("jdo") || m.getName().startsWith("<c")) {
                continue;
            }

            boolean changed = false;
            boolean messedUp = false;

            MethodGen mg = new MethodGen(m, classGen.getClassName(), constantPoolGen);

            // get the code in form of an InstructionList object
            InstructionList il = mg.getInstructionList();

            // get the first instruction
            InstructionHandle ih = il.getStart();
            while (ih != null) {
                Instruction ins = ih.getInstruction();
                if (ins.getClass().getName().equals(GET_FIELD)) {//if (ins instanceof GETFIELD)
                    GETFIELD is = (GETFIELD) ins;
                    if (!is.getFieldName(constantPoolGen).startsWith("jdo")) {
                        String key = is.getClassName(constantPoolGen) + "|" + is.getFieldName(constantPoolGen);
                        if (fieldToEnhanceMap.containsKey(key)) {
                            SwapFieldHelper helper = (SwapFieldHelper) fieldToEnhanceMap.get(key);
                            messedUp = isBcelMessingUpLocalVars(mg);
                            // replace it with our static replacement method
                            ih.setInstruction(instructionFactory.createInvoke(
                                    helper.className,
                                    helper.jdoGetName,
                                    helper.type,
                                    new Type[]{
                                        new ObjectType(helper.className)
                                    },
                                    Constants.INVOKESTATIC));
                            il.setPositions();
                            il.update();

                            changed = true;
                            InstructionHandle prevIhDUP = ih.getPrev();
                            Instruction iffyDup = prevIhDUP.getInstruction();
                            if (iffyDup.getClass().getName().equals(D_U_P)) {  // The previos ist was a DUP
                                ih = ih.getPrev();
                                InstructionHandle prevIhALOAD = ih.getPrev();
                                Instruction iffyAload = prevIhALOAD.getInstruction();
                                if (iffyAload.getClass().getName().equals(A_LOAD)) { // The inst before that was a ALOAD
                                    ALOAD aLoad = (ALOAD) iffyAload;
                                    ih.setInstruction(aLoad); // Swap ref out
                                    il.setPositions();
                                    il.update();
                                } else {
                                    ih = ih.getNext();
                                }
                            }
                        }
                    }
                } else if (ins.getClass().getName().equals(PUT_FIELD)) {
                    PUTFIELD is = (PUTFIELD) ins;
                    if (!is.getFieldName(constantPoolGen).startsWith("jdo")) {
                        String key = is.getClassName(constantPoolGen) + "|" + is.getFieldName(constantPoolGen);
                        if (fieldToEnhanceMap.containsKey(key)) {
                            SwapFieldHelper helper = (SwapFieldHelper) fieldToEnhanceMap.get(key);
                            messedUp = isBcelMessingUpLocalVars(mg);
                            // replace it with our static replacement method
                            ih.setInstruction(instructionFactory.createInvoke(
                                    helper.className,
                                    helper.jdoSetName,
                                    Type.VOID,
                                    new Type[]{
                                        new ObjectType(helper.className),
                                        helper.type
                                    },
                                    Constants.INVOKESTATIC));
                            il.setPositions();
                            il.update();
                            changed = true;
                        }
                    }
                }
                // next instruction
                ih = ih.getNext();
            }
            if (changed) {
                hasChanges = true;
                if (!messedUp){
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

    private boolean isBcelMessingUpLocalVars(MethodGen mg) {
        if (classGen.getMajor() >= 49) {
            Attribute[] attributes = mg.getCodeAttributes();
            for (int j = 0; j < attributes.length; j++) {
                return true;
            }
        }
        return false;
    }

}
