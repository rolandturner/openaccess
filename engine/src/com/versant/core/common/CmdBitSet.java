
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
package com.versant.core.common;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.MDStatics;

/**
 * This is a set type Container for ClassMetaData.
 * It is based on the index of the ClassMetaData.
 */
public final class CmdBitSet {
    private static final ClassMetaData[] EMPTY_CMDS = new ClassMetaData[0];
    private final int[] bitSet;
    private final ModelMetaData jmd;
    private int size;
    private boolean cacheble = true;

    public CmdBitSet(ModelMetaData jmd) {
        bitSet = new int[(jmd.classes.length / 32) + 1];
        this.jmd = jmd;
    }

    public boolean isCacheble() {
        return cacheble;
    }

    /**
     * set the bit for the supplied ClassMetaData and its subclasses.
     */
    public void addPlus(ClassMetaData cmd) {
        if ((bitSet[(cmd.index / 32)] & (1 << (cmd.index % 32))) == 0) {
            bitSet[(cmd.index / 32)] |= (1 << (cmd.index % 32));
            if (cmd.cacheStrategy == MDStatics.CACHE_STRATEGY_NO) cacheble = false;
            size++;
        }
        if (cmd.pcSubclasses != null) {
            final ClassMetaData[] subs = cmd.pcSubclasses;
            for (int k = subs.length - 1; k >= 0; k--) {
                addPlus(subs[k]);
            }
        }
    }

    /**
     * set the bit for the supplied ClassMetaData.
     */
    public boolean add(ClassMetaData cmd) {
        if ((bitSet[(cmd.index / 32)] & (1 << (cmd.index % 32))) == 0) {
            bitSet[(cmd.index / 32)] |= (1 << (cmd.index % 32));
            if (cmd.cacheStrategy == MDStatics.CACHE_STRATEGY_NO) cacheble = false;
            size++;
            return true;
        }
        return false;
    }

    public boolean remove(ClassMetaData cmd) {
        if ((bitSet[(cmd.index / 32)] & (1 << (cmd.index % 32))) == 0) {
            bitSet[(cmd.index / 32)] ^= (1 << (cmd.index % 32));
            size--;
            return true;
        }
        return false;
    }

    /**
     * Return a ClassMetaData array of the classes which index's has been set set on the bitSet.
     */
    public ClassMetaData[] toArray() {
        if (size == 0) return EMPTY_CMDS;
        final ClassMetaData[] cmds = new ClassMetaData[size];
        int count = 0;
        for (int i = 0; i < bitSet.length; i++) {
            int val = bitSet[i];
            for (int bit = 0; bit < 32; bit++) {
                if ((val & (1 << bit)) != 0) {
                    cmds[count++] = jmd.classes[bit + (i * 32)];
                    if (count == size) return cmds;
                }
            }
        }
        return cmds;
    }

    /**
     * Return a int[] array of the class indexes set on the bitSet.
     */
    public int[] getIndexes() {
        if (size == 0) return null;
        final int[] indexes = new int[size];
        int count = 0;
        for (int i = 0; i < bitSet.length; i++) {
            int val = bitSet[i];
            for (int bit = 0; bit < 32; bit++) {
                if ((val & (1 << bit)) != 0) {
                    indexes[count++] = bit + (i * 32);
                    if (count == size) return indexes;
                }
            }
        }
        return indexes;
    }

    /**
     * Return a ClassMetaData array if all the classes in the bitSet is cacheble.
     */
    public ClassMetaData[] toArrayIfCacheble() {
        if (!cacheble) return null;
        if (size == 0) return null;
        final ClassMetaData[] cmds = new ClassMetaData[size];
        int count = 0;
        for (int i = 0; i < bitSet.length; i++) {
            int val = bitSet[i];
            for (int bit = 0; bit < 32; bit++) {
                if ((val & (1 << bit)) != 0) {
                    ClassMetaData cmd = jmd.classes[bit + (i * 32)];
                    if (cmd.cacheStrategy == MDStatics.CACHE_STRATEGY_NO) return null;
                    cmds[count++] = cmd;
                    if (count == size) return cmds;
                }
            }
        }
        return cmds;
    }

    public void clear() {
        for (int i = bitSet.length - 1; i >= 0; i--) {
            bitSet[i] = 0;
        }
        size = 0;
    }

    public boolean contains(ClassMetaData cmd) {
        return (bitSet[(cmd.index / 32)] & (1 << (cmd.index % 32))) != 0;
    }

    public boolean containsAny(int[] bits) {
        return CmdBitSet.containsAny(bitSet, bits);
    }

    public static boolean contains(ClassMetaData cmd, int[] bits) {
        return (bits[(cmd.index / 32)] & (1 << (cmd.index % 32))) != 0;
    }

    public boolean contains(ClassMetaData[] cmds) {
        for (int i = cmds.length - 1; i >= 0; i--) {
            if (contains(cmds[i])) return true;
        }
        return false;
    }

    public static boolean contains(ClassMetaData[] cmds, int[] bits) {
        for (int i = cmds.length - 1; i >= 0; i--) {
            if (contains(cmds[i], bits)) return true;
        }
        return false;
    }

    public static int[] createFor(ClassMetaData[] cmds, ModelMetaData jdm) {
        final CmdBitSet cmdBits = new CmdBitSet(jdm);
        if (cmds != null) {
            for (int i = 0; i < cmds.length; i++) {
                cmdBits.add(cmds[i]);
            }
        }
        return cmdBits.getBits();
    }

    /**
     * This will check if the supplied src int[] contain any of the bits of the
     * data array.
     */
    public static boolean containsAny(int[] src, int[] data) {
        for (int i = 0; i < src.length; i++) {
            if ((src[i] & data[i]) != 0) return true;
        }
        return false;
    }

    /**
     * This returns the actual int[] used by the instance.
     * DO NO MODIFY IT.
     */
    public int[] getBits() {
        return bitSet;
    }
}
