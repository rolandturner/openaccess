
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

/**
 * Base class for containers that need to be able to sort themselves using
 * the quicksort algorithm. Subclasses just need to implement compare
 * and swap to make themselves sortable. The size field must be set to
 * the number of entries in the container when sort is called.
 *
 * @see #compare
 * @see #swap
 * @see #sort
 * @see #size
 *
 */
public abstract class SortableBase {

    /**
     * The number of entries in the container.
     */
    protected int size;

    /**
     * Sort the entries.
     */
    public void sort() {
        quicksort(0, size - 1);
    }

    public void sort(int min, int max){
        quicksort(min,max);
    }
    /**
     * Compare entries at and a and b. Return 0 if equal, less than 0
     * if a is less than b or greater than 0 if a is greater than b.
     */
    protected abstract int compare(int a, int b);

    /**
     * Swap entries.
     */
    protected abstract void swap(int index1, int index2);

    private final void quicksort(final int  left,final int right) {
        final int size = right - left + 1;
        if (size <= 3) {
            manualSort(left, right, size);
        } else {
            final int median = median(left, right);
            final int partition = partition(left, right, median);
            quicksort(left, partition - 1);
            quicksort(partition + 1, right);
        }
    }

    /**
     * This will sort up to 3 entries.
     */
    private final void manualSort(final int left, final int right,final int size) {
        if (size <= 1) return;
        if (size == 2) {
            if (compare(left, right) > 0) swap(left, right);
        } else {
            if (compare(left, right - 1) > 0) swap(left, right - 1);
            if (compare(left, right) > 0) swap(left, right);
            if (compare(left + 1, right) > 0) swap(left + 1, right);
        }

    }

    private final int median(final int left,final int right) {
        final int center = (left + right) / 2;
        if (compare(left, center) > 0) swap(left, center);
        if (compare(left, right) > 0) swap(left, right);
        if (compare(center, right) > 0) swap(center, right);
        swap(center, right - 1);
        return right - 1;
    }

    private final int partition(final int left,final int right,final int pivotIndex) {
        int leftIndex = left;
        int rightIndex = right - 1;
        while (true) {
            while (compare(++leftIndex, pivotIndex) < 0);
            while (compare(--rightIndex, pivotIndex) > 0);
            if (leftIndex >= rightIndex) {
                break; // pointers cross so partition done
            } else {
                swap(leftIndex, rightIndex);
            }
        }
        swap(leftIndex, right - 1);         // restore pivot
        return leftIndex;                 // return pivot location
    }

}

