
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
package com.versant.core.jdbc;

import com.versant.core.jdo.query.*;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.util.IntArray;

/**
 * This is a class that knows how to decode a result or projection query.
 */
public class ProjectionQueryDecoder {
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    public static final int TYPE_THIS = 1;
    public static final int TYPE_AGGRETATE = 2;
    public static final int TYPE_FIELD = 3;
    public static final int TYPE_EXP = 4;
    public static final int TYPE_VAR = 5;

    private int[] typeMap;
    private Object[] fmdMap;
    private int[] typeCodeMap;
    private int[] refIndexes;
    private int firstThisColumn = -1;
    private boolean containsOnlyThis;
    private boolean containsThis;
    private boolean aggregateOnly = true;
    private boolean containsAggregate;
    private SqlDriver driver;


    public ProjectionQueryDecoder(ResultNode resultNode, SqlDriver driver) {
        int size = resultNode.getResultSize();
        typeMap = new int[size];
        fmdMap = new Object[size];
        typeCodeMap = new int[size];
        refIndexes = new int[size];
        this.driver = driver;
        calculateTypes(size, resultNode);
    }


    private void calculateTypes(int size, ResultNode resultNode) {
        int counter = 0;
        IntArray refIndexArray = new IntArray();
        for (Node n = resultNode.childList; n != null; n = n.next) {
            if (n instanceof ReservedFieldNode) {
                typeCodeMap[counter] = MDStatics.OID;
                typeMap[counter] = TYPE_THIS;
                containsThis = true;
                aggregateOnly = false;
                if (firstThisColumn == -1) {
                    firstThisColumn = counter;
                }
            } else if (n instanceof AggregateNode) {
                typeMap[counter] = TYPE_AGGRETATE;
                containsAggregate = true;
                typeCodeMap[counter] = getTypeCode((AggregateNode)n, driver);
            } else if (n instanceof AddNode || n instanceof MultiplyNode) {
                typeMap[counter] = TYPE_EXP;
                typeCodeMap[counter] = getTypeCode(n.childList);
            } else {
                typeMap[counter] = TYPE_FIELD;
                if (n instanceof FieldNode) {
                    fmdMap[counter] = ((FieldNode)n).fmd;
                    typeCodeMap[counter] = ((FieldMetaData)fmdMap[counter]).typeCode;
                } else if (n instanceof FieldNavNode) {
                    fmdMap[counter] = ((FieldNavNode)n).getResultFmd();
                    typeCodeMap[counter] = ((FieldMetaData)fmdMap[counter]).typeCode;
                } else if (n instanceof VarNodeIF) {
                    typeMap[counter] = TYPE_VAR;
                    typeCodeMap[counter] = MDStatics.OID;
                    fmdMap[counter] = ((VarNodeIF)n).getVarNode().getCmd();
                } else {
                    throw BindingSupportImpl.getInstance().internal("");
                }

                //calculate if ref field.
                if (typeMap[counter] == TYPE_FIELD
                        && (((FieldMetaData)fmdMap[counter]).category == MDStatics.CATEGORY_REF
                        || ((FieldMetaData)fmdMap[counter]).category == MDStatics.CATEGORY_POLYREF)) {
                    refIndexArray.add(counter);
                    typeCodeMap[counter] = MDStatics.OID;
                }
                aggregateOnly = false;
            }
            counter++;
        }

        if (size == 1 && containsThis == true) {
            containsOnlyThis = true;
        }

        if (refIndexArray.size() == 0) {
            refIndexes = EMPTY_INT_ARRAY;
        } else {
            refIndexes = refIndexArray.toArray();
        }
    }

    private int getTypeCode(AggregateNode node, SqlDriver driver) {
        int type = node.getType();
        if (type == AggregateNode.TYPE_COUNT
                || node instanceof AggregateCountStarNode) {

			return MDStatics.LONGW;


		}
        return driver.getAggregateTypeCode(type, getTypeCode(node.childList));
    }

    public boolean containsThis() {
        return containsThis;
    }

    public boolean containsAggregate() {
        return containsAggregate;
    }

    public boolean aggregateOnly() {
        return aggregateOnly;
    }

    public int[] getResultTypeArray() {
        return typeMap;
    }

    public Object[] getFmdArray() {
        return fmdMap;
    }

    public int[] getTypeCodes() {
        return typeCodeMap;
    }

    /**
     * The index of the first occur. of 'this' in the projection.
     */
    public int getFirstThisIndex() {
        return firstThisColumn;
    }

    /**
     * Array containing the index pos of ref fields.
     * return null if no ref fields.
     */
    public int[] getRefIndexArray() {
        return refIndexes;
    }

    /**
     * If this is a result that only contains 'this' and no other
     * fields in the projection.
     */
    public boolean isContainsThisOnly() {
        return containsOnlyThis;
    }

    /**
     * The wrapper typecode of the exp. If the field eval to a primitive then the
     * wrapper for that type is returned.
     */
    public static int getTypeCode(Node fn) {
        if (fn instanceof AddNode || fn instanceof MultiplyNode) {
            return getTypeCode(fn.childList);
        }


        int typeCode = MDStatics.BYTEW;


        for (Node n = fn; n != null; n = n.next) {

            int tmpCode = MDStaticUtils.primToNumberMapping[getTypeCodeImp(n)];


            if (tmpCode > typeCode) {
                typeCode = tmpCode;
            }
        }
        return typeCode;
    }

    public static int getTypeCodeImp(Node n) {
        if (n instanceof FieldNode) {
            return ((FieldNode)n).fmd.typeCode;
        } else if (n instanceof FieldNavNode) {
            return getTypeCodeImp(n.childList);
        } else {
            throw BindingSupportImpl.getInstance().internal("Expecting either a fieldNode or a FieldNavNode.");
        }
    }
}
