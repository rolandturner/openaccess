
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
package com.versant.core.jdo.sco;

import com.versant.core.common.Debug;
import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.common.*;
import com.versant.core.util.Quicksort;

import javax.jdo.spi.PersistenceCapable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.OID;
import com.versant.core.common.OrderedCollectionDiff;
import com.versant.core.common.UnorderedCollectionDiff;
import com.versant.core.metadata.FieldMetaData;

/**
 * This is an utility class used by SCO collection and lists to calculate diffs.
 */
public class CollectionDiffUtil {

    /**
     * Compares objects based on their hashcodes. This is used to sort arrays
     * of objects that are not comparable.
     */
    private static class HashcodeComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            int a = o1.hashCode();
            int b = o2.hashCode();
            if (a < b) return -1;
            if (a > b) return +1;
            if (o1.equals(o2)) return 0;
            return -1;
        }
    }

    private static final HashcodeComparator HC_COMP = new HashcodeComparator();

    /**
     * This builds an CollectionDiff for an ordered Collection. If the
     * collection consists of PC instances then the before array must contain
     * OIDs and the data array PC instances.
     */
    public static OrderedCollectionDiff getOrderedCollectionDiff(
            VersantFieldMetaData fmd,
            PersistenceContext pm, Object[] data, int size,
            Object[] before) {

        OrderedCollectionDiff diff = new OrderedCollectionDiff(fmd);

        int beforeLen = fmd.isIncludeAllDataInDiff()
                ? 0
                : before == null ? 0 : before.length;

        // ignore nulls at the end of before
//        for (; beforeLen > 0 && before[beforeLen - 1] == null; --beforeLen) ;

        if (beforeLen == 0) {

            // everything is inserted
            diff.insertedValues = new Object[size];
            if (fmd.isElementTypePC()) {
                Object[] a = diff.insertedValues;
                for (int i = 0; i < size; i++) {
                    a[i] = pm.getInternalOID((PersistenceCapable)data[i]);
                }
            } else {
                System.arraycopy(data, 0, diff.insertedValues, 0, size);
            }
            diff.insertedIndexes = new int[size];
            for (int i = 0; i < size; i++) diff.insertedIndexes[i] = i;

        } else if (size == 0) {

            // everything is deleted
            diff.status = CollectionDiff.STATUS_NEW;

        } else {
            int[] insertedIndexes = new int[size];
            Object[] insertedValues = new Object[size];
            int[] deletedIndexes = new int[beforeLen];

            int deletedSize;
            int n = beforeLen < size ? beforeLen : size;
            int pos = 0;

            if (fmd.isElementTypePC()) {
                // PC instances must be converted into OIDs before comparing.
                for (int i = 0; i < n; i++) {
                    Object a = pm.getInternalOID((PersistenceCapable)data[i]);
                    Object b = pm.getInternalOID((PersistenceCapable)before[i]);
                    if (!equal(a, b)) {
                        deletedIndexes[pos] = i;
                        insertedIndexes[pos] = i;
                        insertedValues[pos++] = a;
                    }
                }
                deletedSize = pos;
                for (int i = beforeLen; i < size; i++) {
                    insertedIndexes[pos] = i;
                    insertedValues[pos++] =
                            pm.getInternalOID((PersistenceCapable)data[i]);
                }
            } else {
                for (int i = 0; i < n; i++) {
                    if (!equal(data[i], before[i])) {
                        deletedIndexes[pos] = i;
                        insertedIndexes[pos] = i;
                        insertedValues[pos++] = data[i];
                    }
                }
                deletedSize = pos;
                for (int i = beforeLen; i < size; i++) {
                    insertedIndexes[pos] = i;
                    insertedValues[pos++] = data[i];
                }
            }

            // delete any extra entries in before
            for (int i = size; i < beforeLen; i++) {
                deletedIndexes[deletedSize++] = i;
            }

            // Shrink arrays down to size. This can come out as soon as there
            // are sizes in OrderedCollectionDiff.
            diff.insertedIndexes = new int[pos];
            if (pos > 0) {
                System.arraycopy(insertedIndexes, 0, diff.insertedIndexes, 0,
                        pos);
            }
            diff.insertedValues = new Object[pos];
            if (pos > 0) {
                System.arraycopy(insertedValues, 0, diff.insertedValues, 0,
                        pos);
            }
            diff.deletedIndexes = new int[deletedSize];
            if (deletedSize > 0) {
                System.arraycopy(deletedIndexes, 0, diff.deletedIndexes, 0,
                        deletedSize);
            }
        }

        if (Debug.DEBUG) {
//            System.out.println("*** CollectionDiffUtil.getOrderedCollectionDiff");
//            System.out.println(" fmd.getQName() = " + fmd.getQName());
//            System.out.println(" before = " +
//                    (fmd.isElementPC() ? toOIDString(pm, before) : toString(before)));
//            System.out.println(" data = " +
//                    (fmd.isElementPC() ? toOIDString(pm, data, size) : toString(data, size)));
//            System.out.println(" diff.deletedIndexes = " + toString(diff.deletedIndexes));
//            System.out.println(" diff.insertedIndexes = " + toString(diff.insertedIndexes));
//            System.out.println(" diff.insertedValues = " +
//                    (fmd.isElementPC() ? toOIDString(pm, diff.insertedValues) : toString(diff.insertedValues)));
//            System.out.println("---");

            // make sure there are no nulls in any of the arrays if pc
            if (diff.insertedValues != null && fmd.isElementTypePC()
                    && !((FieldMetaData)fmd).ordered) {
                for (int i = 0; i < diff.insertedValues.length; i++) {
                    if (diff.insertedValues[i] == null) {
                        throw BindingSupportImpl.getInstance().internal("diff.insertedValues[" + i + "] is null for " +
                                fmd.getQName());
                    }
                }
            }
        }

        return diff;
    }

    /**
     * This builds an UnorderedCollectionDiff for an unordered Collection. If
     * the collection consists of PC instances then the before array must
     * contain OIDs and the data array PC instances.
     */
    public static UnorderedCollectionDiff getUnorderedCollectionDiff(
            VersantFieldMetaData fmd, PersistenceContext pm,
            Object[] data, int size, Object[] before) {
        UnorderedCollectionDiff diff = new UnorderedCollectionDiff(fmd);

        int beforeLen = fmd.isIncludeAllDataInDiff()
                ? 0
                : before == null ? 0 : before.length;

        // ignore nulls at the end of before
        for (; beforeLen > 0 && before[beforeLen - 1] == null; --beforeLen) ;

        if (beforeLen == 0) {
            // everything is inserted
            diff.insertedValues = new Object[size];
            if (fmd.isElementTypePC()) {
                Object[] a = diff.insertedValues;
                for (int i = 0; i < size; i++) {
                    a[i] = pm.getInternalOID((PersistenceCapable)data[i]);
                }
            } else {
                System.arraycopy(data, 0, diff.insertedValues, 0, size);
            }

        } else if (size == 0) {
            // everything is deleted
            diff.status = CollectionDiff.STATUS_NEW;
            if (fmd.getInverseFieldMetaData() != null && fmd.getInverseFieldMetaData().isFake()) {
                diff.deletedValues = new Object[beforeLen];
                if (fmd.isElementTypePC()) {
                    for (int i = 0; i < beforeLen; i++) {
                        diff.deletedValues[i] = pm.getInternalOID(
                                (PersistenceCapable)before[i]);
                    }
                } else {
                    System.arraycopy(before, 0, diff.deletedValues, 0,
                            beforeLen);
                }
            }
        } else {
            Object[] inserted = new Object[size];
            int insertedCount = 0;
            Object[] deleted = new Object[beforeLen];
            int deletedCount = 0;

            // convert to OIDs if required
            Object[] sortedData;
            Object[] sortedBefore;
            boolean pc = fmd.isElementTypePC();
            if (pc) {
                sortedData = new Object[size];
                for (int i = 0; i < size; i++) {
                    sortedData[i] = pm.getInternalOID(
                            (PersistenceCapable)data[i]);
                }
                sortedBefore = new Object[beforeLen];
                for (int i = 0; i < beforeLen; i++) {
                    sortedBefore[i] = pm.getInternalOID(
                            (PersistenceCapable)before[i]);
                }
            } else {
                sortedData = data;
                sortedBefore = before;
            }

            // sort the arrays
            Class comparableClass = /*CHFC*/Comparable.class/*RIGHTPAR*/;
            boolean directCompare = pc || comparableClass.isAssignableFrom(fmd.getElementType());
            if (directCompare) {
                Quicksort.quicksort(sortedData, size);
                Quicksort.quicksort(sortedBefore, beforeLen);
            } else {
                // The elements are not comparable so use a Comparator that
                // orders them based on HashCode. We do not allow duplicates
                // in unsorted collections so this method is ok.
                Quicksort.quicksort(sortedData, size, HC_COMP);
                Quicksort.quicksort(sortedBefore, beforeLen, HC_COMP);
            }

            // find differences between the sorted arrays
            int newPos = 0;
            int oldPos = 0;
            Object nw = sortedData[0];
            Object old = sortedBefore[0];
            for (; ;) {
                int c = (directCompare ? ((Comparable)nw).compareTo(old)
                                       : HC_COMP.compare(nw,old));
                if (c == 0) {
                    ++newPos;
                    ++oldPos; // must inc both so cannot do inc in test
                    if (newPos == size || oldPos == beforeLen) break;
                    nw = sortedData[newPos];
                    old = sortedBefore[oldPos];
                } else if (c < 0) { // inserted element
                    inserted[insertedCount++] = nw;
                    if (++newPos == size) break;
                    nw = sortedData[newPos];
                } else {
                    deleted[deletedCount++] = old;
                    if (++oldPos == beforeLen) break;
                    old = sortedBefore[oldPos];
                }
            }

            // copy in elements from side that was still going
            int n = size - newPos;
            if (n > 0) {
                System.arraycopy(sortedData, newPos, inserted, insertedCount,
                        n);
                insertedCount += n;
            }
            if ((n = beforeLen - oldPos) > 0) {
                System.arraycopy(sortedBefore, oldPos, deleted, deletedCount,
                        n);
                deletedCount += n;
            }

            // Shrink arrays down to size. This can come out as soon as there
            // are sizes in UnorderedCollectionDiff.
            diff.insertedValues = new Object[insertedCount];
            System.arraycopy(inserted, 0, diff.insertedValues, 0,
                    insertedCount);
            diff.deletedValues = new Object[deletedCount];
            System.arraycopy(deleted, 0, diff.deletedValues, 0, deletedCount);
        }

        if (Debug.DEBUG) {
//            System.out.println("*** CollectionDiffUtil.getUnorderedCollectionDiff");
//            System.out.println(" fmd.getQName() = " + fmd.getQName());
//            System.out.println(" before = " +
//                    (fmd.isElementPC() ? toOIDString(pm, before) : toString(before)));
//            System.out.println(" data = " +
//                    (fmd.isElementPC() ? toOIDString(pm, data, size) : toString(data, size)));
//            System.out.println(" diff.deletedValues = " +
//                    (fmd.isElementPC() ? toOIDString(pm, diff.deletedValues) : toString(diff.deletedValues)));
//            System.out.println(" diff.insertedValues = " +
//                    (fmd.isElementPC() ? toOIDString(pm, diff.insertedValues) : toString(diff.insertedValues)));
//            System.out.println("---");

            // make sure there are no nulls in any of the arrays
            if (diff.insertedValues != null) {
                for (int i = 0; i < diff.insertedValues.length; i++) {
                    if (diff.insertedValues[i] == null) {
                        throw BindingSupportImpl.getInstance().internal("diff.insertedValues[" + i + "] is null for " +
                                fmd.getQName());
                    }
                }
            }
            if (diff.deletedValues != null) {
                for (int i = 0; i < diff.deletedValues.length; i++) {
                    if (diff.deletedValues[i] == null) {
                        throw BindingSupportImpl.getInstance().internal("diff.deletedValues[" + i + "] is null for " +
                                fmd.getQName());
                    }
                }
            }
        }

        return diff;
    }

    private static void dump(Object[] a) {
        if (Debug.DEBUG) {
            if (a == null) {
                System.out.println("null");
            } else {
                for (int i = 0; i < a.length; i++) {
                    System.out.println("[" + i + "] = " + a[i]);
                }
                System.out.println("-");
            }
        }
    }

    /**
     * Compare a and b for equality. Considers null == null to be true.
     */
    private static boolean equal(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        } else {
            return a.equals(b);
        }
    }

    /**
     * Create a diff for a new non-SCO collection field.
     */
    public static CollectionDiff getNonSCOCollectionDiff(Collection c,
            PersistenceContext sm, VersantFieldMetaData fmd) {
        if (Debug.DEBUG) {
            if (c instanceof VersantSimpleSCO) {
                throw BindingSupportImpl.getInstance().internal("getNonSCOCollectionDiff called for SCO collection: " +
                        fmd.getQName() + " " + c);
            }
        }
        int size = c.size();
        boolean pc = fmd.isElementTypePC();
        if (fmd.isOrdered()) {
            OrderedCollectionDiff diff = new OrderedCollectionDiff(fmd);
            int[] ia = new int[size];
            diff.insertedIndexes = ia;
            if (pc) {
                OID[] a = new OID[size];
                diff.insertedValues = a;
                int pos = 0;
                for (Iterator i = c.iterator(); i.hasNext();) {
                    ia[pos] = pos;
                    a[pos++] = sm.getInternalOID((PersistenceCapable)i.next());
                }
            } else {
                diff.insertedValues = c.toArray();
                for (int i = 0; i < size; i++) ia[i] = i;
            }
            diff.status = CollectionDiff.STATUS_NEW;
            return diff;
        } else {
            UnorderedCollectionDiff diff = new UnorderedCollectionDiff(fmd);
            if (pc) {
                OID[] a = new OID[size];
                diff.insertedValues = a;
                int pos = 0;
                for (Iterator i = c.iterator(); i.hasNext();) {
                    a[pos++] = sm.getInternalOID((PersistenceCapable)i.next());
                }
            } else {
                diff.insertedValues = c.toArray();
            }
            diff.status = CollectionDiff.STATUS_NEW;
            return diff;
        }
    }

    private static String toString(Object[] a) {
        return toString(a, a == null ? 0 : a.length);
    }

    private static String toString(Object[] a, int len) {
        StringBuffer s = new StringBuffer();
        if (a == null) {
            s.append("null");
        } else {
            s.append("[");
            for (int i = 0; i < len; i++) {
                if (i > 0) s.append(", ");
                s.append(a[i]);
            }
            s.append("]");
        }
        return s.toString();
    }

    private static String toOIDString(VersantPersistenceManagerImp pm,
            Object[] a) {
        return toOIDString(pm, a, a == null ? 0 : a.length);
    }

    private static String toOIDString(VersantPersistenceManagerImp pm,
            Object[] a, int len) {
        StringBuffer s = new StringBuffer();
        if (a == null) {
            s.append("null");
        } else {
            s.append("[");
            for (int i = 0; i < len; i++) {
                if (i > 0) s.append(", ");
                Object o = a[i];
                if (o instanceof OID) {
                    s.append(o);
                } else {
                    s.append("PC:" + pm.getInternalOID((PersistenceCapable)o));
                }
            }
            s.append("]");
        }
        return s.toString();
    }

    private static String toString(int[] a) {
        return toString(a, a == null ? 0 : a.length);
    }

    private static String toString(int[] a, int len) {
        StringBuffer s = new StringBuffer();
        if (a == null) {
            s.append("null");
        } else {
            s.append("[");
            for (int i = 0; i < len; i++) {
                if (i > 0) s.append(", ");
                s.append(a[i]);
            }
            s.append("]");
        }
        return s.toString();
    }

}
