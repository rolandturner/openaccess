
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
package com.versant.core.compiler;

/**
 * Utility methods for getting info from a class file.
 */
public class ClassFileUtils {

    private static final int CONSTANT_Class = 7;
    private static final int CONSTANT_Fieldref = 9;
    private static final int CONSTANT_Methodref = 10;
    private static final int CONSTANT_InterfaceMethodref = 11;
    private static final int CONSTANT_String = 8;
    private static final int CONSTANT_Integer = 3;
    private static final int CONSTANT_Float = 4;
    private static final int CONSTANT_Long = 5;
    private static final int CONSTANT_Double = 6;
    private static final int CONSTANT_NameAndType = 12;
    private static final int CONSTANT_Utf8 = 1;

    /**
     * Get the name of the class represented by bytecode.
     */
    public static String getClassName(byte[] bytecode) {
        int magic = getU4(bytecode, 0);
        if (magic != 0xCAFEBABE) {
            throw new IllegalArgumentException(
                    "Not a class file: Magic 0xCAFEBABE bad: " +
                    Integer.toHexString(magic));
        }
        int constantPoolCount = getU2(bytecode, 8);
        int[] cpOffset = new int[constantPoolCount - 1];
        int offset = 10;
        for (int i = 0; i < constantPoolCount - 1; i++) {
            cpOffset[i] = offset;
            int tag = bytecode[offset] & 0xFF;
            if (tag == CONSTANT_Long || tag == CONSTANT_Double) {
                ++i;
            }
            offset += getConstantPoolEntryLength(bytecode, offset);
        }
        offset += 2;
        int thisClass = getU2(bytecode, offset);
        int nameCpIndex = getU2(bytecode, cpOffset[thisClass - 1] + 1);
        return getConstantPoolString(bytecode, cpOffset, nameCpIndex);
    }

    /**
     * Get the constant pool entry number i which is must be a UTF8
     * string.
     */
    private static String getConstantPoolString(byte[] bytecode,
            int[] cpOffset, int i) {
        int offset = cpOffset[i - 1];
        if (bytecode[offset] != CONSTANT_Utf8) {
            throw new IllegalArgumentException("Not a UTF8 pool entry " + i +
                    " at offset " + offset);
        }
        int len = getU2(bytecode, offset + 1);
        return new String(bytecode, offset + 3, len);
    }

    private static int getU4(byte[] bytecode, int offset) {
        int b0 = (bytecode[offset] & 0xFF);
        int b1 = (bytecode[offset + 1] & 0xFF);
        int b2 = (bytecode[offset + 2] & 0xFF);
        int b3 = (bytecode[offset + 3] & 0xFF);
        return (b0 << 24) + (b1 << 16) + (b2  << 8) + b3;
    }

    private static int getU2(byte[] bytecode, int offset) {
        int b0 = (bytecode[offset] & 0xFF);
        int b1 = (bytecode[offset + 1] & 0xFF);
        return (b0 << 8) + b1;
    }

    /**
     * Get the length in bytes of an entry in the constant pool.
     */
    private static int getConstantPoolEntryLength(byte[] bytecode, int offset) {
        int tag = bytecode[offset] & 0xFF;
        switch (tag) {
            case CONSTANT_Class:
            case CONSTANT_String:
                return 3;
            case CONSTANT_Fieldref:
            case CONSTANT_Methodref:
            case CONSTANT_InterfaceMethodref:
            case CONSTANT_Integer:
            case CONSTANT_Float:
            case CONSTANT_NameAndType:
                return 5;
            case CONSTANT_Long:
            case CONSTANT_Double:
                return 9;
            case CONSTANT_Utf8:
                return 3 + getU2(bytecode, offset + 1);
            case 0:
                return 0;
        }
        throw new IllegalArgumentException("Unknown constant pool entry type " +
                tag + " at " + Integer.toHexString(offset));
    }

}
