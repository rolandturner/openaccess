
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
package com.versant.core.util;

import java.util.Comparator;

/** 
 * Quicksort implementation for sorting arrays. Unlike the merge sort in
 * java.utils.Collections this one does not create any objects. There are
 * two implementations, one for arrays of Comparable's and another that
 * uses a comparator.
 */
public class Quicksort {

    private Quicksort() {
    }

    /**
     * Sort the first size entries in a.
     */
    public static void quicksort(Object[] a, int size) {
        quicksort(a, 0, size - 1);
    }

    /**
     * Sort the entries in a between left and right inclusive.
     */
    public static void quicksort(Object[] a, int left, int right) {
        int size = right - left + 1;
        switch (size) {
            case 0:
            case 1:
                break;
            case 2:
                if (compare(a[left], a[right]) > 0) swap(a, left, right);
                break;
            case 3:
                if (compare(a[left], a[right - 1]) > 0) swap(a, left, right - 1);
                if (compare(a[left], a[right]) > 0) swap(a, left, right);
                if (compare(a[left + 1], a[right]) > 0) swap(a, left + 1, right);
                break;
            default:
                int median = median(a, left, right);
                int partition = partition(a, left, right, median);
                quicksort(a, left, partition - 1);
                quicksort(a, partition + 1, right);
        }
    }

    private static int compare(Object a, Object b) {
        if (a == null) {
            return b == null ? 0 : -1;
        } else if (b == null) {
            return +1;
        } else {
            return ((Comparable)a).compareTo(b);
        }
    }

    private static void swap(Object[] a, int left, int right) {
        Object t = a[left];
        a[left] = a[right];
        a[right] = t;
    }

    private static int median(Object[] a, int left, int right) {
        int center = (left + right) / 2;
        if (compare(a[left], a[center]) > 0) swap(a, left, center);
        if (compare(a[left], a[right]) > 0) swap(a, left, right);
        if (compare(a[center], a[right]) > 0) swap(a, center, right);
        swap(a, center, right - 1);
        return right - 1;
    }

    private static int partition(Object[] a, int left, int right, int pivotIndex) {
        int leftIndex = left;
        int rightIndex = right - 1;
        while (true) {
            while (compare(a[++leftIndex], a[pivotIndex]) < 0);
            while (compare(a[--rightIndex], a[pivotIndex]) > 0);
            if (leftIndex >= rightIndex) {
                break; // pointers cross so partition done
            } else {
                swap(a, leftIndex, rightIndex);
            }
        }
        swap(a, leftIndex, right - 1);         // restore pivot
        return leftIndex;                 // return pivot location
    }

    /**
     * Sort the first size entries in a.
     */
    public static void quicksort(Object[] a, int size, Comparator c) {
        quicksort(a, 0, size - 1, c);
    }

    /**
     * Sort the entries in a between left and right inclusive.
     */
    public static void quicksort(Object[] a, int left, int right, Comparator c) {
        int size = right - left + 1;
        switch (size) {
            case 0:
            case 1:
                break;
            case 2:
                if (c.compare(a[left], a[right]) > 0) swap(a, left, right);
                break;
            case 3:
                if (c.compare(a[left], a[right - 1]) > 0) swap(a, left, right - 1);
                if (c.compare(a[left], a[right]) > 0) swap(a, left, right);
                if (c.compare(a[left + 1], a[right]) > 0) swap(a, left + 1, right);
                break;
            default:
                int median = median(a, left, right, c);
                int partition = partition(a, left, right, median, c);
                quicksort(a, left, partition - 1, c);
                quicksort(a, partition + 1, right, c);
        }
    }

    private static int median(Object[] a, int left, int right, Comparator c) {
        int center = (left + right) / 2;
        if (c.compare(a[left], a[center]) > 0) swap(a, left, center);
        if (c.compare(a[left], a[right]) > 0) swap(a, left, right);
        if (c.compare(a[center], a[right]) > 0) swap(a, center, right);
        swap(a, center, right - 1);
        return right - 1;
    }

    private static int partition(Object[] a, int left, int right,
            int pivotIndex, Comparator c) {
        int leftIndex = left;
        int rightIndex = right - 1;
        while (true) {
            while (c.compare(a[++leftIndex], a[pivotIndex]) < 0);
            while (c.compare(a[--rightIndex], a[pivotIndex]) > 0);
            if (leftIndex >= rightIndex) {
                break; // pointers cross so partition done
            } else {
                swap(a, leftIndex, rightIndex);
            }
        }
        swap(a, leftIndex, right - 1);         // restore pivot
        return leftIndex;                 // return pivot location
    }

}
