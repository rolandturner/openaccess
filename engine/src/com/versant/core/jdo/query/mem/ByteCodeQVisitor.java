
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
package com.versant.core.jdo.query.mem;

import com.versant.core.jdo.query.*;
import com.versant.core.jdo.query.MemVisitor;
import com.versant.core.common.NotImplementedException;
import com.versant.core.common.OID;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MDStaticUtils;

import java.util.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.lang.Integer;

import com.versant.lib.bcel.generic.*;
import com.versant.lib.bcel.Constants;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.QueryStateWrapper;

/**
 * Walks Node tree generating bytecode for the filter.
 */
public class ByteCodeQVisitor implements MemVisitor {

    private ClassMetaData candidateClass;
    private ClassGen cg;
    private ConstantPoolGen cp;
    private InstructionList il;
    private MethodGen mg;
    private InstructionFactory factory;
    private Map paramMap;

    private int valueLVCount;

    private final Map varToIndexMap = new HashMap();

    private boolean first = true;

    private static final int COMP_EQ = 1;
    private static final int COMP_GE = 2;
    private static final int COMP_LT = 3;
    private static final int COMP_GT = 4;
    private static final int COMP_LE = 5;
    private static final int COMP_NE = 6;

    private CompiledMemQuery compiledMemQuery;
    private static final String NAME_Q_STATE_WRAPPER = QueryStateWrapper.class.getName();
    private static final String M_NAME_GETSTATE = "getState";
    private static final String NAME_COLLECTION = Collection.class.getName();
    private static final String NAME_ITERATOR = Iterator.class.getName();


    private static final String NAME_COMPARABLE = Comparable.class.getName();
    private static final String NAME_OBJECT = Object.class.getName();
    private static final String NAME_NUMBER = Number.class.getName();

    private static final ObjectType OBJECT_TYPE_COLLECTION = new ObjectType(NAME_COLLECTION);
    private static final ObjectType OBJECT_TYPE_ITERATOR = new ObjectType(NAME_ITERATOR);
    private static final ObjectType OBJECT_TYPE_NUMBER = new ObjectType(NAME_NUMBER);
    private static final Type[] ARG_TYPES_INT = new Type[] {Type.INT};
    private static final Type[] ARG_TYPES_OBJECT = new Type[] {Type.OBJECT};
    private static final Type[] ARG_TYPES_STRING = new Type[] {Type.STRING};
    private static final String NAME_STRING = String.class.getName();
    private static final ObjectType OBJECT_TYPE_STRING = new ObjectType(NAME_STRING);
    private static final ObjectType OBJECT_TYPE_OID = new ObjectType(OID.class.getName());
    private static final String NAME_STRING_BUFFER = StringBuffer.class.getName();
    private static final ObjectType OBJECT_TYPE_STRING_BUFFER = new ObjectType(NAME_STRING_BUFFER);
    private static final String NAME_BIG_INT = BigInteger.class.getName();

    private static final ObjectType RET_TYPE_BIG_INT = new ObjectType(NAME_BIG_INT);
    private static final Type[] ARG_TYPES_BIG_INT = new Type[] {RET_TYPE_BIG_INT};
    private static final String NAME_BIG_DEC = BigDecimal.class.getName();
    private static final ObjectType RET_TYPE_BIG_DEC = new ObjectType(NAME_BIG_DEC);
    private static final Type[] ARG_TYPES_BIG_DEC = new Type[] {RET_TYPE_BIG_DEC};

    public ByteCodeQVisitor(ClassGen classGen, InstructionFactory instructionFactory, String name, ClassMetaData candidateClass, CompiledMemQuery compiledMemQuery) {
        this.compiledMemQuery = compiledMemQuery;
        this.candidateClass = candidateClass;

        cg = classGen;
        factory = instructionFactory;
        cp = cg.getConstantPool();
        this.il = new InstructionList();
        mg = new MethodGen(Constants.ACC_PUBLIC, // access flags
                                        Type.BOOLEAN,               // return type
                                        new Type[] {new ObjectType(NAME_Q_STATE_WRAPPER), new ArrayType(Type.OBJECT, 1)},
                                        new String[] {"state", "params"}, // arg names
                                        "exec", name,    // method, class
                                        il, cp);
    }

    public void setParamMap(Map paramMap) {
        this.paramMap = paramMap;
    }

    public void finish() {
        il.append(InstructionConstants.IRETURN);
        mg.removeNOPs();
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    public Field visitNode(Node node, Object obj) {
        throw new NotImplementedException();
    }


    /**
     * Find the variable with name or null if none.
     */
    private VarNode findVar(String name) {
        VarNode[] vars = compiledMemQuery.vars;
        if (vars == null) return null;
        for (int i = vars.length - 1; i >= 0; i--) {
            VarNode v = vars[i];
            if (v.getIdentifier().equals(name)) return v;
        }
        return null;
    }

    /**
     * Must refactor
     * @param node
     * @param obj
     * @return
     */
    public Field visitLiteralNode(LiteralNode node, Object obj) {
        BCField f = new BCField();
        switch(node.type){
            case LiteralNode.TYPE_STRING:
                f.bcType = MDStatics.STRING;
                f.classType = String.class;
                f.ih = il.append(new PUSH(cp, node.value));
                break;
            case LiteralNode.TYPE_BOOLEAN:
                f.bcType = MDStatics.BOOLEAN;
                f.classType = Boolean.TYPE;
                f.ih = il.append(new PUSH(cp, Boolean.valueOf(node.value)));
                break;
            case LiteralNode.TYPE_CHAR:
                f.bcType = MDStatics.CHAR;
                f.classType = Character.TYPE;
                f.ih = il.append(new PUSH(cp, node.value.toCharArray()[0]));
                break;
            case LiteralNode.TYPE_LONG:
                f.bcType = MDStatics.LONG;
                f.classType = Long.TYPE;
                f.ih = il.append(new PUSH(cp, Long.parseLong(node.value)));
                break;
            case LiteralNode.TYPE_DOUBLE:
                f.bcType = MDStatics.DOUBLE;
                f.classType = Double.TYPE;
                f.ih = il.append(new PUSH(cp, Double.parseDouble(node.value)));
                break;
            case LiteralNode.TYPE_NULL:
            case LiteralNode.TYPE_OTHER:
                f.bcType = MDStatics.NULL;
                f.classType = Object.class;
//                f.ih = il.append(InstructionConstants.ACONST_NULL);
//                /**
//                 * This will probally be nulls
//                 */
//                throw new NotImplementedException();
                break;
            default:
                throw BindingSupportImpl.getInstance().runtime("Unkown literal type  "+node.type);
        }
        return f;
    }

    /**
     * This may be an state field on a state eg person.person
     * @param node
     * @param obj
     * @return
     */
    public Field visitFieldNavNode(FieldNavNode node, Object obj) {
        return visitStateFieldNavNodeRoot(node, candidateClass);
    }

    private Field visitStateFieldNavNodeRoot(FieldNavNode node, ClassMetaData currentClass) {
        Field result = null;
        FieldMetaData f = null;

        VarNode varNode = findVar(node.lexeme);
        if (varNode != null) {
            first = false;
            Object o = varToIndexMap.get(varNode.getIdentifier());
            il.append(new ALOAD(((Integer)o).intValue()));
            if (node.childList instanceof FieldNavNode) {
                result = visitStateFieldNavNode((FieldNavNode) node.childList, varNode.getCmd(), null);
            } else {
                result = visitFieldNode((FieldNode) node.childList, varNode.getCmd());
            }
        } else {
            f = currentClass.getFieldMetaData(node.lexeme);
            if(f == null){
                throw BindingSupportImpl.getInstance().runtime("Class "+currentClass+
                        " does not have a field "+ node.lexeme);
            }
            switch(f.category){
                case FieldMetaData.CATEGORY_SIMPLE:
                    break;
                case FieldMetaData.CATEGORY_REF:
                    InstructionHandle ih = null;
                    il.append(new ALOAD(1));//load the state
                    il.append(new PUSH(cp, f.stateFieldNo));
                    il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, M_NAME_GETSTATE,
                            new ObjectType(NAME_Q_STATE_WRAPPER), ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
                    first = false;
                    if (node.childList instanceof FieldNavNode) {
                        result = visitStateFieldNavNode((FieldNavNode) node.childList, f.typeMetaData, ih);
                    } else {
                        result = visitFieldNode((FieldNode) node.childList, f.typeMetaData);
                    }
                case FieldMetaData.CATEGORY_MAP:
                    break;
                case FieldMetaData.CATEGORY_COLLECTION:
                    break;
                case FieldMetaData.CATEGORY_TRANSACTIONAL:
                    break;
                case FieldMetaData.CATEGORY_ARRAY:
                    break;
            }
        }
        return result;
    }

    private Field visitStateFieldNavNode(FieldNavNode node, ClassMetaData currentClass, InstructionHandle ih) {
        Field result = null;
        FieldMetaData f = currentClass.getFieldMetaData(node.lexeme);
        if(f == null){
            throw BindingSupportImpl.getInstance().runtime("Class "+currentClass+
                    " does not have a field "+ node.lexeme);
        }
        switch(f.category){
            case FieldMetaData.CATEGORY_SIMPLE:
                break;
            case FieldMetaData.CATEGORY_REF:
                il.append(new PUSH(cp, f.stateFieldNo));
                il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, M_NAME_GETSTATE,
                        new ObjectType(NAME_Q_STATE_WRAPPER), ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
                if (node.childList instanceof FieldNavNode) {
                    result = visitStateFieldNavNode((FieldNavNode) node.childList, f.typeMetaData, ih);
                } else {
                    result = visitFieldNode((FieldNode) node.childList, f.typeMetaData);
                }
            case FieldMetaData.CATEGORY_MAP:
                break;
            case FieldMetaData.CATEGORY_COLLECTION:
                break;
            case FieldMetaData.CATEGORY_TRANSACTIONAL:
                break;
            case FieldMetaData.CATEGORY_ARRAY:
                break;
        }
        return result;
    }

    /**
     * This is used from a methodNode to deduce the name of a var node.
     * @param mNode
     * @return
     */
    private String getLVName(MethodNode mNode) {
        return ((VarNode)mNode.childList.next).getIdentifier();
    }

    public Field visitMethodNode(MethodNode node, Object obj) {
        Field result = null;
        switch (node.getMethod()) {
            case MethodNode.CONTAINS:
                if (node.childList.next instanceof VarNode) {
                    doContainsMethod(node);
                } else {
                    doContainMethodParam(node);
                }
                break;
            case MethodNode.ENDS_WITH:
            case MethodNode.STARTS_WITH:
                doStringWithMethod(node);
                break;
            case MethodNode.IS_EMPTY:
                doIsEmpty(node);
                break;
            case MethodNode.CONTAINS_KEY:
                doContainsKey(node);
                break;
            case MethodNode.TO_LOWER_CASE:
                result = toLowerCase(node);
                break;
            default:
                throw new NotImplementedException();
        }
        return result;
    }

    private BCField toLowerCase(MethodNode node) {
        first = true;
        BCField f = (BCField) node.childList.visit(this, null);
        il.append(factory.createInvoke(NAME_STRING, "toLowerCase", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        return f;
    }

    private boolean isVarContainsMethodNode(MethodNode node) {
        if ((node.getMethod() == MethodNode.CONTAINS)
                && (node.childList.next instanceof VarNode)) {
                return true;
        }
        return false;
    }

    private void doStringWithMethod(MethodNode node) {
        String name = node.getName();
        BCField fl = (BCField) node.childList.visit(this, null);
        if (fl.bcType != MDStatics.STRING) {
            throw BindingSupportImpl.getInstance().invalidOperation("The " + name + " may only be used on a String field");
        }
        node.childList.next.visit(this, null);
        il.append(factory.createCheckCast(OBJECT_TYPE_STRING));
        il.append(factory.createInvoke(NAME_STRING, name, Type.BOOLEAN, ARG_TYPES_STRING, Constants.INVOKEVIRTUAL));
    }

    private void doIsEmpty(MethodNode node) {
        first = true;
        node.childList.visit(this, null);
        il.append(factory.createCheckCast(OBJECT_TYPE_COLLECTION));
        il.append(factory.createInvoke(NAME_COLLECTION, "isEmpty", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    }

    private void doContainsKey(MethodNode node) {
        first = true;
        node.childList.visit(this, null);
        try {
            il.delete(il.getEnd());
        } catch (TargetLostException e) {
            throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
        }
        node.childList.next.visit(this, null);
        il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, "containsKey", Type.BOOLEAN, new Type[] {Type.INT, Type.OBJECT}, Constants.INVOKEVIRTUAL));
    }

    private void doContainMethodParam(MethodNode node) {
        first = true;
        BCField bcField = (BCField) node.childList.visit(this, null);
        if (Map.class.isAssignableFrom(bcField.classType)) {
            if (bcField.getFMD().isElementTypePC()) {
                try {
                    il.delete(il.getEnd());
                } catch (TargetLostException e) {
                    throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
                }
                il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, "getInternalOIDValueCollectionForMap", OBJECT_TYPE_COLLECTION,
                    ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
            } else {
                try {
                    il.delete(il.getEnd());
                } catch (TargetLostException e) {
                    throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
                }
                il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, "getInternalValueCollectionForMap", OBJECT_TYPE_COLLECTION,
                    ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
            }
            bcField = (BCField) node.childList.next.visit(this, null);
            if (bcField.bcType == MDStatics.NULL) {
                il.append(InstructionConstants.ACONST_NULL);
            }
            il.append(factory.createInvoke(NAME_COLLECTION, "contains", Type.BOOLEAN, ARG_TYPES_OBJECT, Constants.INVOKEINTERFACE));
        } else {
            if (bcField.getFMD().isElementTypePC()) {
                try {
                    il.delete(il.getEnd());
                } catch (TargetLostException e) {
                    throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
                }
                il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, "getInternalOIDCollection", OBJECT_TYPE_COLLECTION,
                    ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
            } else {
                il.append(factory.createCheckCast(OBJECT_TYPE_COLLECTION));
            }
            bcField = (BCField) node.childList.next.visit(this, null);
            if (bcField.bcType == MDStatics.NULL) {
                il.append(InstructionConstants.ACONST_NULL);
            }
            il.append(factory.createInvoke(NAME_COLLECTION, "contains", Type.BOOLEAN, ARG_TYPES_OBJECT, Constants.INVOKEINTERFACE));
        }
    }

    private void doContainsMethod(MethodNode node) {
        GOTO gotoEnd = new GOTO(null);
        String lv = getLVName(node);

        //create localVar for declared var
        il.append(InstructionConstants.ACONST_NULL);
        LocalVariableGen var1 = mg.addLocalVariable(lv,
                new ObjectType(NAME_Q_STATE_WRAPPER), null, null);
        int var1Index = var1.getIndex();
        varToIndexMap.put(lv, new Integer(var1Index));
        il.append(new ASTORE(var1Index));
        var1.setStart(il.append(new NOP()));

        first = true;
        node.childList.visit(this, null);
        //cast to collection
        il.append(factory.createCheckCast(OBJECT_TYPE_COLLECTION));

        //create a localvar for the collection by name col
        LocalVariableGen colLV = mg.addLocalVariable(lv + "Col", OBJECT_TYPE_COLLECTION, null, null);
        int colLVIndex = colLV.getIndex();
        colLV.setStart(il.append(new ASTORE(colLVIndex)));

        il.append(new ALOAD(colLVIndex));
        il.append(factory.createInvoke(NAME_COLLECTION, "isEmpty", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));

        InstructionHandle end = null;

        if (node.next == null) {
            il.append(InstructionConstants.ICONST_1);
            il.append(InstructionConstants.IXOR);
        } else {
            il.append(InstructionConstants.DUP);
            IFEQ ifNotEmpty = new IFEQ(null);
            il.append(ifNotEmpty);
            // else jump to end
            il.append(InstructionConstants.POP);
            il.append(InstructionConstants.ICONST_0);
            il.append(gotoEnd);

            //create the iterator
            InstructionHandle createIter = il.append(new ALOAD(colLVIndex));
            il.append(factory.createInvoke(NAME_COLLECTION, "iterator", OBJECT_TYPE_ITERATOR, Type.NO_ARGS, Constants.INVOKEINTERFACE));
            LocalVariableGen itLV = mg.addLocalVariable(lv + "Iter", OBJECT_TYPE_ITERATOR, null, null);
            int itLVIndex = itLV.getIndex();
            itLV.setStart(il.append(new ASTORE(itLVIndex)));

            GOTO gotoHasNext = new GOTO(null);
            il.append(gotoHasNext);

            InstructionHandle loadIterForNext = il.append(new ALOAD(itLVIndex));
            il.append(factory.createInvoke(NAME_ITERATOR, "next",
                    Type.OBJECT, Type.NO_ARGS, Constants.INVOKEINTERFACE));
            il.append(factory.createCheckCast(new ObjectType(NAME_Q_STATE_WRAPPER)));
            il.append(new ASTORE(var1Index));

            //visit the expr
            //This must leave a 1 or 0 on the stack
            il.append(InstructionConstants.POP);
            node.next.visit(this, null);
            il.append(InstructionConstants.DUP);
            IFEQ ifNonTrue = new IFEQ(null);
            il.append(ifNonTrue);
            GOTO gotoEnd2 = new GOTO(null);
            il.append(gotoEnd2);

            //createthe hasNext of the iter
            InstructionHandle loadIterForHasNext = il.append(new ALOAD(itLVIndex));
            il.append(factory.createInvoke(NAME_ITERATOR, "hasNext",
                    Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));
            IFNE ifMore = new IFNE(loadIterForNext);
            il.append(ifMore);

            gotoHasNext.setTarget(loadIterForHasNext);
            ifNonTrue.setTarget(loadIterForHasNext);
            ifNotEmpty.setTarget(createIter);

            end = il.append(new NOP());
            gotoEnd2.setTarget(end);
            gotoEnd.setTarget(end);
        }

    }

    public Field visitPrimaryExprNode(PrimaryExprNode node, Object obj) {
        return null;
    }

    public Field visitFieldNode(FieldNode node, ClassMetaData cmd) {
        if (cmd == null) {
            return visitFieldNodeImp(candidateClass.getFieldMetaData(node.lexeme), node);
        } else {
            return visitFieldNodeImp(cmd.getFieldMetaData(node.lexeme), node);
        }
    }

    private Field visitFieldNodeImp(FieldMetaData fmd, FieldNode node) {
        BCField field;
        if (fmd == null) {
            throw BindingSupportImpl.getInstance().runtime("Class " + candidateClass.qname + " does not have a field: " + node.lexeme);
        }
        field = new BCStateField(fmd);
        if (first) {
            field.ih = il.append(new ALOAD(1));//load state on stack
            first = false;
            il.append(new PUSH(cp, fmd.stateFieldNo));
        } else {
            field.ih = il.append(new PUSH(cp, fmd.stateFieldNo));
        }
        if (fmd.typeMetaData == null) {
            if (!fmd.isElementTypePC()) {
                il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, fmd.stateGetMethodName, getBCellStateFieldType(fmd),
                        ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
            } else {
                il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, "getInternalStateCollection", OBJECT_TYPE_COLLECTION,
                        ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
            }
        } else {
            il.append(factory.createInvoke(NAME_Q_STATE_WRAPPER, "getOID", OBJECT_TYPE_OID,
                    ARG_TYPES_INT, Constants.INVOKEVIRTUAL));
        }
        return field;
    }

    public Field visitFieldNode(FieldNode node, Object obj) {
        return visitFieldNodeImp(candidateClass.getFieldMetaData(node.lexeme), node);
    }

    private static final Type getBCellStateFieldType(FieldMetaData fmd) {
        switch (fmd.category) {
            case MDStatics.CATEGORY_SIMPLE:
                return getTypeFromTypeCode(fmd.typeCode);
            default:
                return Type.OBJECT;
        }
    }

    private static Type getTypeFromTypeCode(int typeCode) {
        switch (typeCode) {
            case MDStatics.INT:
                return Type.INT;
            case MDStatics.LONG:
                return Type.LONG;
            case MDStatics.SHORT:
                return Type.SHORT;
            case MDStatics.STRING:
                return Type.STRING;
            case MDStatics.BOOLEAN:
                return Type.BOOLEAN;
            case MDStatics.BYTE:
                return Type.BYTE;
            case MDStatics.CHAR:
                return Type.CHAR;
            case MDStatics.DOUBLE:
                return Type.DOUBLE;
            case MDStatics.FLOAT:
                return Type.FLOAT;
            default:
                return Type.OBJECT;
        }
    }

    private Field doCompare(BinaryNode node, int type) {
        Object obj = null;
        first = true;
        BCField fl = (BCField) node.getLeft().visit(this, obj);
        first = true;
        BCField fr = (BCField) node.getRight().visit(this, obj);
        InstructionHandle nopHandle = il.append(InstructionConstants.NOP);
        int widestType = getWidestType(fl.bcType, fr.bcType);

        if (fl.isPrimitive() && fr.isPrimitive()) {
            promotePrimToPrim(widestType, fl, fr.ih);
            promotePrimToPrim(widestType, fr, nopHandle);
            switch (widestType) {
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.CHAR:
                case MDStatics.INT:
                    writeIIEq(type);
                    break;
                case MDStatics.LONG:
                    writeLLEq(type);
                    break;
                case MDStatics.FLOAT:
                    writeFFEq(type);
                    break;
                case MDStatics.DOUBLE:
                    writeDDEq(type);
                    break;
                case MDStatics.BOOLEAN:
                    writeIIEq(type);
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("Comparing field type'" + MDStaticUtils.toSimpleName(fl.bcType) + "' to"
                            + " field type '" + MDStaticUtils.toSimpleName(fl.bcType) + "' is not implemented correctly.");
            }
        } else if (!fl.isPrimitive() && !fr.isPrimitive()) {
            promoteToObjects(widestType, fl, fr.ih);
            promoteToObjects(widestType, fr, nopHandle);
            if (fl.bcType == MDStatics.NULL || fr.bcType == MDStatics.NULL) {
                if (type == COMP_EQ) {
                    IFNONNULL ifnull = new IFNONNULL(null);
                    il.append(ifnull);
                    il.append(InstructionConstants.ICONST_1);
                    GOTO gotoEnd = new GOTO(null);
                    il.append(gotoEnd);
                    InstructionHandle falseHandle = il.append(InstructionConstants.ICONST_0);
                    InstructionHandle endHandle = il.append(InstructionConstants.NOP);
                    ifnull.setTarget(falseHandle);
                    gotoEnd.setTarget(endHandle);
                } else if (type == COMP_NE) {
                    IFNULL ifnull = new IFNULL(null);
                    il.append(ifnull);
                    il.append(InstructionConstants.ICONST_1);
                    GOTO gotoEnd = new GOTO(null);
                    il.append(gotoEnd);
                    InstructionHandle falseHandle = il.append(InstructionConstants.ICONST_0);
                    InstructionHandle endHandle = il.append(InstructionConstants.NOP);
                    ifnull.setTarget(falseHandle);
                    gotoEnd.setTarget(endHandle);
                } else {
                    throw BindingSupportImpl.getInstance().invalidOperation("Only '==' and '!=' operators is implemented for comparing to a null value");
                }
            } else if (Comparable.class.isAssignableFrom(fl.classType)) {
                writeComparable(type);
            } else {
               il.append(factory.createInvoke(NAME_OBJECT, "equals", Type.BOOLEAN,
                       ARG_TYPES_OBJECT, Constants.INVOKEVIRTUAL));
            }
        } else {
            if (widestType == MDStatics.BIGDECIMAL || widestType == MDStatics.BIGINTEGER) {
                if (fl.isPrimitive()) {
                    promotePrimToBig(widestType, fl, fr.ih);
                } else {
                    promotePrimToBig(widestType, fr, nopHandle);
                }
                writeComparable(type);
            } else {
                promoteAllToPrim(widestType, fl, fr.ih);
                promoteAllToPrim(widestType, fr, nopHandle);
                switch (widestType) {
                    case MDStatics.BOOLEAN:
                    case MDStatics.BOOLEANW:
                    case MDStatics.BYTE:
                    case MDStatics.BYTEW:
                    case MDStatics.SHORT:
                    case MDStatics.SHORTW:
                    case MDStatics.CHAR:
                    case MDStatics.CHARW:
                    case MDStatics.INT:
                    case MDStatics.INTW:
                        writeIIEq(type);
                        break;
                    case MDStatics.LONG:
                    case MDStatics.LONGW:
                        writeLLEq(type);
                        break;
                    case MDStatics.FLOAT:
                    case MDStatics.FLOATW:
                        writeFFEq(type);
                        break;
                    case MDStatics.DOUBLE:
                    case MDStatics.DOUBLEW:
                        writeDDEq(type);
                        break;
                    default:
                        throw BindingSupportImpl.getInstance().internal("Comparing field type'" + MDStaticUtils.toSimpleName(fl.bcType) + "' to"
                            + " field type '" + MDStaticUtils.toSimpleName(fl.bcType) + "' is not implemented correctly.");
                }
            }
        }
        return null;
    }



    private void writeComparable(int type) {
        il.append(factory.createInvoke(NAME_COMPARABLE, "compareTo", Type.INT,
                ARG_TYPES_OBJECT, Constants.INVOKEINTERFACE));
        BranchInstruction bInstr = null;
        switch (type) {
            case COMP_EQ:
                bInstr = new IFNE(null);
                break;
            case COMP_GE:
                bInstr = new IFLT(null);
                break;
            case COMP_LT:
                bInstr = new IFGE(null);
                break;
            case COMP_GT:
                bInstr = new IFLE(null);
                break;
            case COMP_LE:
                bInstr = new IFGT(null);
                break;
            case COMP_NE:
                bInstr = new IFEQ(null);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("No operator of type '" + type + "'");
        }
        il.append(bInstr);
        il.append(InstructionConstants.ICONST_1);
        GOTO gotoEnd = new GOTO(null);
        il.append(gotoEnd);
        //goto end
        InstructionHandle loadFalse = il.append(InstructionConstants.ICONST_0);
        InstructionHandle endHandle = il.append(InstructionConstants.NOP);

        bInstr.setTarget(loadFalse);
        gotoEnd.setTarget(endHandle);
    }

    public Field visitEqualNode(EqualNode node, Object obj) {
        return doCompare(node, COMP_EQ);
    }

    public Field visitNotEqualNode(NotEqualNode node, Object obj) {
        return doCompare(node, COMP_NE);
    }

    public Field visitLikeNode(LikeNode node, Object obj) {
        return null;
    }

    private void writeIIEq(int compType) {
        BranchInstruction bInstr = null;
        switch (compType) {
            case COMP_EQ:
                bInstr = new IF_ICMPNE(null);
                break;
            case COMP_GE:
                bInstr = new IF_ICMPLT(null);
                break;
            case COMP_LT:
                bInstr = new IF_ICMPGE(null);
                break;
            case COMP_GT:
                bInstr = new IF_ICMPLE(null);
                break;
            case COMP_LE:
                bInstr = new IF_ICMPGT(null);
                break;
            case COMP_NE:
                bInstr = new IF_ICMPEQ(null);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("No operator of type '" + compType + "'");
        }
        il.append(bInstr);
        il.append(InstructionConstants.ICONST_1);
        GOTO gotoEnd = new GOTO(null);
        il.append(gotoEnd);
        //goto end
        InstructionHandle loadFalse = il.append(InstructionConstants.ICONST_0);
        InstructionHandle endHandle = il.append(InstructionConstants.NOP);

        bInstr.setTarget(loadFalse);
        gotoEnd.setTarget(endHandle);
    }

    private void writeDDEq(int compType) {
        BranchInstruction bInstr = null;
        il.append(new DCMPL());
        switch (compType) {
            case COMP_EQ:
                bInstr = new IFNE(null);
                break;
            case COMP_GE:
                bInstr = new IFLT(null);
                break;
            case COMP_LT:
                bInstr = new IFGE(null);
                break;
            case COMP_GT:
                bInstr = new IFLE(null);
                break;
            case COMP_LE:
                bInstr = new IFGT(null);
                break;
            case COMP_NE:
                bInstr = new IFEQ(null);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("No operator of type '" + compType + "'");
        }
        il.append(bInstr);
        il.append(InstructionConstants.ICONST_1);
        GOTO gotoEnd = new GOTO(null);
        il.append(gotoEnd);
        //goto end
        InstructionHandle loadFalse = il.append(InstructionConstants.ICONST_0);
        InstructionHandle endHandle = il.append(InstructionConstants.NOP);

        bInstr.setTarget(loadFalse);
        gotoEnd.setTarget(endHandle);
    }

    private void writeFFEq(int compType) {
        BranchInstruction bInstr = null;
        il.append(new FCMPL());
        switch (compType) {
            case COMP_EQ:
                bInstr = new IFNE(null);
                break;
            case COMP_GE:
                bInstr = new IFLT(null);
                break;
            case COMP_LT:
                bInstr = new IFGE(null);
                break;
            case COMP_GT:
                bInstr = new IFLE(null);
                break;
            case COMP_LE:
                bInstr = new IFGT(null);
                break;
            case COMP_NE:
                bInstr = new IFEQ(null);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("No operator of type '" + compType + "'");
        }
        il.append(bInstr);
        il.append(InstructionConstants.ICONST_1);
        GOTO gotoEnd = new GOTO(null);
        il.append(gotoEnd);
        //goto end
        InstructionHandle loadFalse = il.append(InstructionConstants.ICONST_0);
        InstructionHandle endHandle = il.append(InstructionConstants.NOP);

        bInstr.setTarget(loadFalse);
        gotoEnd.setTarget(endHandle);
    }

    private void writeLLEq(int type) {
        BranchInstruction bInstr = null;
        switch (type) {
            case COMP_EQ:
                il.append(new LCMP());
                bInstr = new IFNE(null);
                break;
            case COMP_GE:
                il.append(new LCMP());
                bInstr = new IFLT(null);
                break;
            case COMP_GT:
                il.append(new LCMP());
                bInstr = new IFLE(null);
                break;
            case COMP_LE:
                il.append(new LCMP());
                bInstr = new IFGT(null);
                break;
            case COMP_LT:
                il.append(new LCMP());
                bInstr = new IFGE(null);
                break;
            case COMP_NE:
                il.append(new LCMP());
                bInstr = new IFEQ(null);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("No operator of type '" + type + "'");
        }
        il.append(bInstr);
        il.append(InstructionConstants.ICONST_1);
        GOTO gotoEnd = new GOTO(null);
        il.append(gotoEnd);
        //goto end
        InstructionHandle loadFalse = il.append(InstructionConstants.ICONST_0);
        InstructionHandle endHandle = il.append(InstructionConstants.NOP);

        bInstr.setTarget(loadFalse);
        gotoEnd.setTarget(endHandle);
    }

    public Field visitAndNode(AndNode node, Object obj) {
        List ifFalseList = new ArrayList();
        first = true;
        node.childList.visit(this, obj);

        //leave if the child is an methodNode
        if (node.childList instanceof MethodNode) {
            MethodNode mNode = (MethodNode)node.childList;
            if (isVarContainsMethodNode(mNode)) return null;
        }

        il.append(InstructionConstants.DUP);
        IFEQ ifFalse = new IFEQ(null);
        il.append(ifFalse);

        IFEQ ifFalseVar = null;
        Node n = node.childList.next;
        while (n != null) {
            first = true;
            //pop the uncesc value
            il.append(InstructionConstants.POP);
            n.visit(this, obj);
            il.append(InstructionConstants.DUP);
            ifFalseVar = new IFEQ(null);
            ifFalseList.add(ifFalseVar);
            il.append(ifFalseVar);
            n = n.next;
        }

        InstructionHandle endHandle = il.append(new NOP());
        ifFalse.setTarget(endHandle);

        for (int i = 0; i < ifFalseList.size(); i++) {
            ((IFEQ) ifFalseList.get(i)).setTarget(endHandle);
        }
        return null;
    }

    public Field visitOrNode(OrNode node, Object obj) {
        List ifFalseList = new ArrayList();
        node.childList.visit(this, obj);

        il.append(InstructionConstants.DUP);
        IFNE ifTrue = new IFNE(null);
        il.append(ifTrue);
//
        IFNE ifTrueVar = null;
        Node n = node.childList.next;

        while (n != null) {
            first = true;
            //pop the uncesc value
            il.append(InstructionConstants.POP);
            n.visit(this, obj);
            il.append(InstructionConstants.DUP);
            ifTrueVar = new IFNE(null);
            ifFalseList.add(ifTrueVar);
            il.append(ifTrueVar);
            n = n.next;
        }

        InstructionHandle endHandle = il.append(new NOP());
        ifTrue.setTarget(endHandle);

        for (int i = 0; i < ifFalseList.size(); i++) {
            ((BranchInstruction) ifFalseList.get(i)).setTarget(endHandle);
        }
        return null;
    }

    public Field visitMultiplyNode(MultiplyNode node, Object obj) {
        BCField result = null;
        BCField f = (BCField)node.childList.visit(this,obj);
        BCField fnNext = null;
        int widestType = 0;

        List fieldsToPromote = new ArrayList();
        fieldsToPromote.add(f);
        for(Node n = node.childList.next ; n != null; n = n.next) {
            first = true;
            fnNext = (BCField)n.visit(this, null);
            fieldsToPromote.add(fnNext);
            widestType = getWidestType(f.bcType, fnNext.bcType);
            f = fnNext;
        }
        InstructionHandle nopHandle = il.append(InstructionConstants.NOP);
        int n = fieldsToPromote.size();
        int lastIndex = n - 1;
        for (int i = 0; i < n; i++) {
            BCField bcField1 = (BCField) fieldsToPromote.get(i);
            BCField bcField2 = (i == lastIndex) ? null : (BCField) fieldsToPromote.get(i + 1);
            if (bcField2 == null) {
                promoteTo(widestType, bcField1, nopHandle);
            } else {
                promoteTo(widestType, bcField1, bcField2.ih);
            }
        }

        if (fieldsToPromote.size() == 2) {
            insertMultiply(widestType, nopHandle, node.ops[0]);
        } else {
            for (int i = 1; i < n; i++) {
                if (i == lastIndex) {
                    insertMultiply(widestType, nopHandle, node.ops[i -1]);
                } else {
                    BCField bcField2 = (BCField) fieldsToPromote.get(i + 1);
                    insertMultiply(widestType, bcField2.ih, node.ops[i -1]);
                }
            }
        }
        result = new BCField();
        result.bcType = widestType;
        result.classType = MDStaticUtils.toSimpleClass(MDStaticUtils.toSimpleName(widestType));
        return result;
    }

    public Field visitAddNode(AddNode node, Object obj) {
        BCField result = null;
        first = true;
        BCField f = (BCField)node.childList.visit(this,obj);
        BCField fnNext = null;
        int widestType = 0;
        if (f.bcType == MDStatics.STRING) {
            il.append(factory.createNew(NAME_STRING_BUFFER));
            il.append(InstructionConstants.DUP);
            il.append(factory.createInvoke(NAME_STRING_BUFFER, "<init>",
                                 Type.VOID, Type.NO_ARGS,
                                 Constants.INVOKESPECIAL));
            LocalVariableGen lg = mg.addLocalVariable("buffer",
                    OBJECT_TYPE_STRING_BUFFER, null, null);
            int bufferIndex = lg.getIndex();
            lg.setStart(il.append(new ASTORE(bufferIndex)));
            il.append(new ALOAD(bufferIndex));
            il.append(InstructionConstants.SWAP);
            il.append(factory.createInvoke(NAME_STRING_BUFFER, "append", OBJECT_TYPE_STRING_BUFFER,
                    ARG_TYPES_STRING, Constants.INVOKEVIRTUAL));

            for(Node n = node.childList.next ; n != null; n = n.next) {
                first = true;
                n.visit(this, null);
                il.append(factory.createInvoke(NAME_STRING_BUFFER, "append", OBJECT_TYPE_STRING_BUFFER,
                    ARG_TYPES_STRING, Constants.INVOKEVIRTUAL));
            }
            il.append(factory.createInvoke(NAME_STRING_BUFFER, "toString", Type.STRING,
                        Type.NO_ARGS, Constants.INVOKEVIRTUAL));
            result = new BCField();
            result.setBcType(MDStatics.STRING);
            result.classType = String.class;
        } else {
            List fieldsToPromote = new ArrayList();
            fieldsToPromote.add(f);
            for(Node n = node.childList.next ; n != null; n = n.next) {
                first = true;
                fnNext = (BCField)n.visit(this, null);
                fieldsToPromote.add(fnNext);
                widestType = getWidestType(f.bcType, fnNext.bcType);
                f = fnNext;
            }
            InstructionHandle nopHandle = il.append(InstructionConstants.NOP);
            int n = fieldsToPromote.size();
            int lastIndex = n - 1;
            for (int i = 0; i < n; i++) {
                BCField bcField1 = (BCField) fieldsToPromote.get(i);
                BCField bcField2 = (i == lastIndex) ? null : (BCField) fieldsToPromote.get(i + 1);
                if (bcField2 == null) {
                    promoteTo(widestType, bcField1, nopHandle);
                } else {
                    promoteTo(widestType, bcField1, bcField2.ih);
                }
            }

            if (fieldsToPromote.size() == 2) {
                insertAddOp(widestType, nopHandle, node.ops[0]);
            } else {
                for (int i = 1; i < n; i++) {
                    if (i == lastIndex) {
                        insertAddOp(widestType, nopHandle, node.ops[i -1]);
                    } else {
                        BCField bcField2 = (BCField) fieldsToPromote.get(i + 1);
                        insertAddOp(widestType, bcField2.ih, node.ops[i -1]);
                    }
                }
            }
            result = new BCField();
            result.bcType = widestType;
            result.classType = MDStaticUtils.toSimpleClass(MDStaticUtils.toSimpleName(widestType));
        }
        return result;
    }

    private void insertMultiply(int widestType, InstructionHandle insertHandle, int type) {
        if (type == MultiplyNode.OP_TIMES) {
            switch (widestType) {
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.CHAR:
                case MDStatics.INT:
                    il.insert(insertHandle, InstructionConstants.IMUL);
                    break;
                case MDStatics.LONG:
                    il.insert(insertHandle, InstructionConstants.LMUL);
                    break;
                case MDStatics.FLOAT:
                    il.insert(insertHandle, InstructionConstants.FMUL);
                    break;
                case MDStatics.DOUBLE:
                    il.insert(insertHandle, InstructionConstants.DMUL);
                    break;
                case MDStatics.BIGINTEGER:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_INT, "multiply",
                            RET_TYPE_BIG_INT, ARG_TYPES_BIG_INT, Constants.INVOKEVIRTUAL));
                    break;
                case MDStatics.BIGDECIMAL:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_DEC, "multiply",
                            RET_TYPE_BIG_DEC, ARG_TYPES_BIG_DEC, Constants.INVOKEVIRTUAL));
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("The '*' operator is not implemented correctly for type '"
                            + MDStaticUtils.toSimpleName(widestType) + "'");
            }
        } else {
            switch (widestType) {
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.CHAR:
                case MDStatics.INT:
                    il.insert(insertHandle, InstructionConstants.IDIV);
                    break;
                case MDStatics.LONG:
                    il.insert(insertHandle, InstructionConstants.LDIV);
                    break;
                case MDStatics.FLOAT:
                    il.insert(insertHandle, InstructionConstants.FDIV);
                    break;
                case MDStatics.DOUBLE:
                    il.insert(insertHandle, InstructionConstants.DDIV);
                    break;
                case MDStatics.BIGINTEGER:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_INT, "divide",
                            RET_TYPE_BIG_INT, ARG_TYPES_BIG_INT, Constants.INVOKEVIRTUAL));
                    break;
                case MDStatics.BIGDECIMAL:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_DEC, "divide",
                            RET_TYPE_BIG_DEC, ARG_TYPES_BIG_DEC, Constants.INVOKEVIRTUAL));
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("The '/' operator is not implemented correctly for type '"
                            + MDStaticUtils.toSimpleName(widestType) + "'");
            }
        }
    }

    private void insertAddOp(int widestType, InstructionHandle insertHandle, int type) {
        if (type == AddNode.OP_PLUS) {
            switch (widestType) {
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.CHAR:
                case MDStatics.INT:
                    il.insert(insertHandle, InstructionConstants.IADD);
                    break;
                case MDStatics.LONG:
                    il.insert(insertHandle, InstructionConstants.LADD);
                    break;
                case MDStatics.FLOAT:
                    il.insert(insertHandle, InstructionConstants.FADD);
                    break;
                case MDStatics.DOUBLE:
                    il.insert(insertHandle, InstructionConstants.DADD);
                    break;
                case MDStatics.BIGINTEGER:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_INT, "add",
                            RET_TYPE_BIG_INT, ARG_TYPES_BIG_INT, Constants.INVOKEVIRTUAL));
                    break;
                case MDStatics.BIGDECIMAL:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_DEC, "add",
                            RET_TYPE_BIG_DEC, ARG_TYPES_BIG_DEC, Constants.INVOKEVIRTUAL));
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("The '+' operator is not implemented correctly for type '"
                            + MDStaticUtils.toSimpleName(widestType) + "'");
            }
        } else {
            switch (widestType) {
                case MDStatics.BYTE:
                case MDStatics.SHORT:
                case MDStatics.CHAR:
                case MDStatics.INT:
                    il.insert(insertHandle, InstructionConstants.ISUB);
                    break;
                case MDStatics.LONG:
                    il.insert(insertHandle, InstructionConstants.LSUB);
                    break;
                case MDStatics.FLOAT:
                    il.insert(insertHandle, InstructionConstants.FSUB);
                    break;
                case MDStatics.DOUBLE:
                    il.insert(insertHandle, InstructionConstants.DSUB);
                    break;
                case MDStatics.BIGINTEGER:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_INT, "subtract",
                            RET_TYPE_BIG_INT, ARG_TYPES_BIG_INT, Constants.INVOKEVIRTUAL));
                    break;
                case MDStatics.BIGDECIMAL:
                    il.insert(insertHandle, factory.createInvoke(NAME_BIG_DEC, "subtract",
                            RET_TYPE_BIG_DEC, ARG_TYPES_BIG_DEC, Constants.INVOKEVIRTUAL));
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal("The '-' operator is not implemented correctly for type '"
                            + MDStaticUtils.toSimpleName(widestType) + "'");
            }
        }
    }

    private void promoteTo(int typePromoteTo, BCField otherType, InstructionHandle insertHandle) {
        if (otherType.bcType == typePromoteTo) {
            if (otherType.bcType == MDStatics.BIGINTEGER) {
                //add a cast
                il.insert(insertHandle, factory.createCheckCast(RET_TYPE_BIG_INT));
            } else if (otherType.bcType == MDStatics.BIGDECIMAL) {
                //add a cast
                il.insert(insertHandle, factory.createCheckCast(RET_TYPE_BIG_DEC));
            }
            return;
        }
        switch (typePromoteTo) {
            case MDStatics.BYTE:
            case MDStatics.SHORT:
            case MDStatics.CHAR:
            case MDStatics.INT:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "intValue",
                            Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                }
                break;
            case MDStatics.LONG:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "longValue",
                            Type.LONG, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                } else {
                    il.insert(insertHandle, InstructionConstants.I2L);
                }
                break;
            case MDStatics.FLOAT:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "floatValue",
                            Type.FLOAT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                } else {
                    if (otherType.bcType == MDStatics.LONG) {
                        il.insert(insertHandle, InstructionConstants.L2F);
                    } else {
                        il.insert(insertHandle, InstructionConstants.I2F);
                    }
                }
                break;
            case MDStatics.DOUBLE:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "doubleValue",
                            Type.DOUBLE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                } else {
                    switch (otherType.bcType) {
                        case MDStatics.BYTE:
                        case MDStatics.SHORT:
                        case MDStatics.CHAR:
                        case MDStatics.INT:
                            il.insert(insertHandle, InstructionConstants.I2D);
                            break;
                        case MDStatics.LONG:
                            il.insert(insertHandle, InstructionConstants.L2D);
                            break;
                        case MDStatics.FLOAT:
                            il.insert(insertHandle, InstructionConstants.F2D);
                            break;
                    }
                }
                break;
            case MDStatics.BIGINTEGER:
                insertBigTypeFromPrim(insertHandle, otherType, NAME_BIG_INT);
                break;
            case MDStatics.BIGDECIMAL:
                insertBigTypeFromPrim(insertHandle, otherType, NAME_BIG_DEC);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Promoting to type '" + MDStaticUtils.toSimpleName(typePromoteTo) + "' is not supported");
        }

    }

    private void insertBigTypeFromObject(InstructionHandle insertHandle, String typeName) {
        LocalVariableGen valueOfLV = null;
        int colLVIndex;

        il.insert(insertHandle, factory.createInvoke(NAME_OBJECT, "toString",
                        OBJECT_TYPE_STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));

        //create a localvar for the string
        valueOfLV = mg.addLocalVariable("valueOfString" + valueLVCount++, Type.STRING, null, null);
        colLVIndex = valueOfLV.getIndex();
        valueOfLV.setStart(il.insert(insertHandle, new ASTORE(colLVIndex)));

        il.insert(insertHandle, factory.createNew(typeName));
        il.insert(insertHandle, InstructionConstants.DUP);
        il.insert(insertHandle, new ALOAD(colLVIndex));
        il.insert(insertHandle, factory.createInvoke(typeName, "<init>",
                Type.VOID, ARG_TYPES_STRING, Constants.INVOKESPECIAL));
    }

    private void insertBigTypeFromPrim(InstructionHandle insertHandle, BCField otherType, String typeName) {
        LocalVariableGen valueOfLV;
        int colLVIndex;

        il.insert(insertHandle, factory.createInvoke(NAME_STRING, "valueOf" ,
                OBJECT_TYPE_STRING, new Type[] {getTypeFromTypeCode(otherType.bcType)}, Constants.INVOKESTATIC));

        //create a localvar for the string
        valueOfLV = mg.addLocalVariable("valueOfString" + valueLVCount++, Type.STRING, null, null);
        colLVIndex = valueOfLV.getIndex();
        valueOfLV.setStart(il.insert(insertHandle, new ASTORE(colLVIndex)));

        il.insert(insertHandle, factory.createNew(typeName));
        il.insert(insertHandle, InstructionConstants.DUP);
        il.insert(insertHandle, new ALOAD(colLVIndex));
        il.insert(insertHandle, factory.createInvoke(typeName, "<init>",
                Type.VOID, ARG_TYPES_STRING, Constants.INVOKESPECIAL));
    }

    /**
     * This will check if the type to promote to is either a BigInteger or a BigDecimal. If either is true
     * the the other type must be converted to a the corresponding type.
     *
     * @param typePromoteTo
     * @param otherType
     * @param insertHandle
     */
    private void promoteToObjects(int typePromoteTo, BCField otherType, InstructionHandle insertHandle) {
        if (typePromoteTo == otherType.bcType) {
            return;
        }
        switch (typePromoteTo) {
            case MDStatics.BIGINTEGER:
                insertBigTypeFromObject(insertHandle, NAME_BIG_INT);
                break;
            case MDStatics.BIGDECIMAL:
                insertBigTypeFromObject(insertHandle, NAME_BIG_DEC);
                break;
            default:
                //do nothing. This is a no-op
        }

    }

    /**
     * This must promote a prim to an big type.
     *
     * @param typePromoteTo
     * @param otherType
     * @param insertHandle
     */
    private void promotePrimToBig(int typePromoteTo, BCField otherType, InstructionHandle insertHandle) {
        if (typePromoteTo == otherType.bcType) {
            return;
        }
        switch (typePromoteTo) {
            case MDStatics.BIGINTEGER:
                insertBigTypeFromPrim(insertHandle, otherType, NAME_BIG_INT);
                break;
            case MDStatics.BIGDECIMAL:
                insertBigTypeFromPrim(insertHandle, otherType, NAME_BIG_DEC);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Only 'BigInteger' and 'BigDecimal' is supported");
        }

    }

    /**
     * This will promote a primitive to a bigger primitive.
     *
     * @param typePromoteTo
     * @param otherType
     * @param insertHandle
     */
    private void promotePrimToPrim(int typePromoteTo, BCField otherType, InstructionHandle insertHandle) {
        if (otherType.bcType == typePromoteTo) {
            return;
        }
        switch (typePromoteTo) {
            case MDStatics.BYTE:
            case MDStatics.SHORT:
            case MDStatics.CHAR:
            case MDStatics.INT:
                break;
            case MDStatics.LONG:
                il.insert(insertHandle, InstructionConstants.I2L);
                break;
            case MDStatics.FLOAT:
                if (otherType.bcType == MDStatics.LONG) {
                    il.insert(insertHandle, InstructionConstants.L2F);
                } else {
                    il.insert(insertHandle, InstructionConstants.I2F);
                }
                break;
            case MDStatics.DOUBLE:
                switch (otherType.bcType) {
                    case MDStatics.BYTE:
                    case MDStatics.SHORT:
                    case MDStatics.CHAR:
                    case MDStatics.INT:
                        il.insert(insertHandle, InstructionConstants.I2D);
                        break;
                    case MDStatics.LONG:
                        il.insert(insertHandle, InstructionConstants.L2D);
                        break;
                    case MDStatics.FLOAT:
                        il.insert(insertHandle, InstructionConstants.F2D);
                        break;
                }
                break;
            case MDStatics.BOOLEAN:
                throw BindingSupportImpl.getInstance().invalidOperation("Promoting between primitives and boolean's is not allowed");
            default:
                throw BindingSupportImpl.getInstance().internal("Promoting from type '"
                        + MDStaticUtils.toSimpleName(otherType.bcType) + "'"
                        + " to type '" + MDStaticUtils.toSimpleName(typePromoteTo) + "' is not implemented correctly");
        }

    }

    /**
     * Promote all to primitives
     * @param typePromoteTo
     * @param otherType
     * @param insertHandle
     */
    private void promoteAllToPrim(int typePromoteTo, BCField otherType, InstructionHandle insertHandle) {
        if (otherType.bcType == typePromoteTo && otherType.isPrimitive()) {
            return;
        }
        switch (typePromoteTo) {
            case MDStatics.BYTE:
            case MDStatics.SHORT:
            case MDStatics.CHAR:
            case MDStatics.INT:
            case MDStatics.INTW:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "intValue",
                            Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                }
                break;
            case MDStatics.LONG:
            case MDStatics.LONGW:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "longValue",
                            Type.LONG, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                } else {
                    il.insert(insertHandle, InstructionConstants.I2L);
                }
                break;
            case MDStatics.FLOAT:
            case MDStatics.FLOATW:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "floatValue",
                            Type.FLOAT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                } else {
                    if (otherType.bcType == MDStatics.LONG) {
                        il.insert(insertHandle, InstructionConstants.L2F);
                    } else {
                        il.insert(insertHandle, InstructionConstants.I2F);
                    }
                }
                break;
            case MDStatics.DOUBLE:
            case MDStatics.DOUBLEW:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(OBJECT_TYPE_NUMBER));
                    il.insert(insertHandle, factory.createInvoke(NAME_NUMBER, "doubleValue",
                            Type.DOUBLE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                } else {
                    switch (otherType.bcType) {
                        case MDStatics.BYTE:
                        case MDStatics.SHORT:
                        case MDStatics.CHAR:
                        case MDStatics.INT:
                            il.insert(insertHandle, InstructionConstants.I2D);
                            break;
                        case MDStatics.LONG:
                            il.insert(insertHandle, InstructionConstants.L2D);
                            break;
                        case MDStatics.FLOAT:
                            il.insert(insertHandle, InstructionConstants.F2D);
                            break;
                    }
                }
                break;
            case MDStatics.BOOLEAN:
            case MDStatics.BOOLEANW:
                if (!otherType.isPrimitive()) {
                    il.insert(insertHandle, factory.createCheckCast(new ObjectType(Boolean.class.getName())));
                    il.insert(insertHandle, factory.createInvoke(Boolean.class.getName(), "booleanValue",
                            Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                }
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Promoting from type '"
                        + MDStaticUtils.toSimpleName(otherType.bcType) + "'"
                        + " to type '" + MDStaticUtils.toSimpleName(typePromoteTo) + "' is not implemented correctly");
        }

    }

    /**
     * This must check the types and determine the wider of the 2.
     * @param typeL
     * @param typeR
     * @return
     */
    private int getWidestType(int typeL, int typeR) {
        if (typeL == typeR) {
            return typeL;
        }
        int lValue = getWidth(typeL);
        int rValue = getWidth(typeR);
        if (lValue < rValue) {
            return typeR;
        } else {
            return typeL;
        }
    }

    private int getWidth(int type) {
        switch (type) {
            case MDStatics.BOOLEAN:
            case MDStatics.BOOLEANW:
            case MDStatics.BYTE:
            case MDStatics.BYTEW:
                return 1;
            case MDStatics.SHORT:
            case MDStatics.SHORTW:
                return 1;
            case MDStatics.CHAR:
            case MDStatics.CHARW:
                return 1;
            case MDStatics.INT:
            case MDStatics.INTW:
                return 1;
            case MDStatics.LONG:
            case MDStatics.LONGW:
                return 2;
            case MDStatics.FLOAT:
            case MDStatics.FLOATW:
                return 3;
            case MDStatics.DOUBLE:
            case MDStatics.DOUBLEW:
                return 4;
            case MDStatics.BIGINTEGER:
                return 8;
            case MDStatics.BIGDECIMAL:
                return 16;
            case MDStatics.NULL:
                return -1;
            default:
                return -1;
        }
    }

    public Field visitUnaryOpNode(UnaryOpNode node, Object obj) {
        BCField result = null;
        switch (node.op) {
            case UnaryOpNode.OP_BANG:
                node.childList.visit(this, null);
                IFNE ifTrue = new IFNE(null);
                il.append(ifTrue);
                il.append(InstructionConstants.ICONST_1);
                GOTO gotoEnd = new GOTO(null);
                il.append(gotoEnd);
                InstructionHandle toFalseHandle = il.append(InstructionConstants.ICONST_0);
                ifTrue.setTarget(toFalseHandle);
                gotoEnd.setTarget(il.append(InstructionConstants.NOP));
                break;
            case UnaryOpNode.OP_MINUS:
                result = (BCField) node.childList.visit(this, null);
                switch (result.bcType) {
                    case MDStatics.BYTE:
                    case MDStatics.CHAR:
                    case MDStatics.SHORT:
                    case MDStatics.INT:
                        il.append(InstructionConstants.INEG);
                        break;
                    case MDStatics.BYTEW:
                    case MDStatics.CHARW:
                    case MDStatics.SHORTW:
                    case MDStatics.INTW:
                        il.append(factory.createCheckCast(OBJECT_TYPE_NUMBER));
                        il.append(factory.createInvoke(NAME_NUMBER, "intValue",
                            Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                        il.append(InstructionConstants.INEG);
                        result = new BCField();
                        result.classType = Integer.TYPE;
                        result.bcType = MDStatics.INT;
                        break;
                    case MDStatics.LONGW:
                        il.append(factory.createCheckCast(OBJECT_TYPE_NUMBER));
                        il.append(factory.createInvoke(NAME_NUMBER, "longValue",
                            Type.LONG, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                        result = new BCField();
                        result.classType = Long.TYPE;
                        result.bcType = MDStatics.LONG;
                    case MDStatics.LONG:
                        il.append(InstructionConstants.LNEG);
                        break;
                    case MDStatics.FLOATW:
                        il.append(factory.createCheckCast(OBJECT_TYPE_NUMBER));
                        il.append(factory.createInvoke(NAME_NUMBER, "floatValue",
                            Type.FLOAT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                        result = new BCField();
                        result.classType = Float.TYPE;
                        result.bcType = MDStatics.FLOAT;
                    case MDStatics.FLOAT:
                        il.append(InstructionConstants.FNEG);
                        break;
                    case MDStatics.DOUBLEW:
                        il.append(factory.createCheckCast(OBJECT_TYPE_NUMBER));
                        il.append(factory.createInvoke(NAME_NUMBER, "doubleValue",
                            Type.DOUBLE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                        result = new BCField();
                        result.classType = Double.TYPE;
                        result.bcType = MDStatics.DOUBLE;
                    case MDStatics.DOUBLE:
                        il.append(InstructionConstants.DNEG);
                        break;
                    default:
                        throw new NotImplementedException();
                }
                break;
            case UnaryOpNode.OP_PLUS:
                result = (BCField) node.childList.visit(this, null);
                break;
            case UnaryOpNode.OP_TILDE:
                result = (BCField) node.childList.visit(this, null);
                InstructionHandle nopHandle = il.append(InstructionConstants.NOP);
                promoteAllToPrim(result.bcType, result, nopHandle);
                switch (result.bcType) {
                    case MDStatics.BYTE:
                    case MDStatics.CHAR:
                    case MDStatics.SHORT:
                    case MDStatics.INT:
                    case MDStatics.BYTEW:
                    case MDStatics.CHARW:
                    case MDStatics.SHORTW:
                    case MDStatics.INTW:
                        if (result.bcType != MDStatics.INT) {
                            result = new BCField();
                            result.bcType = MDStatics.INT;
                            result.classType = Integer.TYPE;
                        }
                        il.append(InstructionConstants.ICONST_M1);
                        il.append(InstructionConstants.IXOR);
                        break;
                    case MDStatics.LONG:
                    case MDStatics.LONGW:
                        if (result.bcType != MDStatics.LONG) {
                            result = new BCField();
                            result.bcType = MDStatics.LONG;
                            result.classType = Long.TYPE;
                        }
                        il.append(new PUSH(cp, -1l));
                        il.append(InstructionConstants.LXOR);
                        break;
                    default:
                        throw new NotImplementedException();
                }
                break;
            default:
                throw new NotImplementedException("UnaryNode type '" + node.op + "' is not implemented correctly");
        }
        return result;
    }

    public Field visitCompareOpNode(CompareOpNode node, Object obj) {
        return doCompare(node, getCompType(node));
    }

    private final int getCompType(CompareOpNode node) {
        switch (node.op) {
            case CompareOpNode.GT:
                return COMP_GT;
            case CompareOpNode.LT:
                return COMP_LT;
            case CompareOpNode.GE:
                return COMP_GE;
            case CompareOpNode.LE:
                return COMP_LE;
            default:
                 throw BindingSupportImpl.getInstance().internal("No operator of type '" + node.op + "'");
        }
    }

    public Field visitUnaryNode(UnaryNode node, Object obj) {
        node.childList.visit(this,obj);
        return null;
    }

    public Field visitBinaryNode(BinaryNode node, Object obj) {
        return null;
    }

    public Field visitMultiNode(Node node, Object obj) {
        return null;
    }

    public Field visitCastNode(CastNode node, Object obj) {
        return null;
    }

    public Field visitParamNode(ParamNode node, Object obj) {
        BCField field = null;
        Object paramValue = paramMap.get(node.getIdentifier());
        if (paramValue == null) {
            field = new BCField();
            field.bcType = MDStatics.NULL;
            field.classType = Object.class;
        } else {
            Class clazz = paramValue.getClass();
            if(OID.class.isAssignableFrom(clazz)) {
                field = new BCField();
                field.classType = clazz;
                field.bcType = MDStaticUtils.toTypeCode(clazz);
                field.ih = il.append(new ALOAD(2));
                il.append(new PUSH(cp, node.getIndex()));
                il.append(new AALOAD());
            } else {
                field = new BCField();
                field.classType = clazz;
                field.bcType = MDStaticUtils.toTypeCode(clazz);
                field.ih = il.append(new ALOAD(2));
                il.append(new PUSH(cp, node.getIndex()));
                il.append(new AALOAD());
            }
        }
        return field;
    }

    public Field visitParamNodeProxy(ParamNodeProxy node, Object obj) {
        return node.getParamNode().visit(this, obj);
    }

    public Field visitArgNode(ArgNode node, Object obj) {
        return null;
    }

    public Field visitArrayNode(ArrayNode node, Object obj) {
        return null;
    }

    public Field visitImportNode(ImportNode node, Object obj) {
        return null;
    }

    public Field visitLeafNode(LeafNode node, Object obj) {
        return null;
    }

    public Field visitOrderNode(OrderNode node, Object obj) {
        return null;
    }

    public Field visitVarNode(VarNode node, Object obj) {
        return null;
    }

    public Field visitVarNodeProxy(VarNodeProxy node, Object obj) {
        return node.getVarNode().visit(this, obj);
    }

    public Field visitReservedFieldNode(ReservedFieldNode node, Object obj) {
        return null;
    }


    private class BCStateField extends BCField {
        private InstructionHandle ih;
        private FieldMetaData fmd;

        public BCStateField(FieldMetaData fmd) {
            this.fmd = fmd;
            initFromFMD(fmd);
        }

        private void initFromFMD(FieldMetaData fmd) {
            this.classType = fmd.type;
            bcType = fmd.typeCode;

        }

        public BCStateField(InstructionHandle ih) {
            this.ih = ih;
        }

        public InstructionHandle getIh() {
            return ih;
        }

        public void setIh(InstructionHandle ih) {
            this.ih = ih;
        }

        public FieldMetaData getFMD() {
            return fmd;
        }
    }
}
