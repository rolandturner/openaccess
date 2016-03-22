
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

import com.versant.core.common.Debug;

import com.versant.core.common.BindingSupportImpl;

/**
 * A Stack implementation used by QueryResultIterator to hold values returned for query.
 */
public class Stack {

    /**
     * The underlying array used for storing the data.
     */
    public Object[] m_baseArray;
    private int size;
    private int currentIndex;

    /**
     * Default constructor.
     */
    public Stack() {
    }

    /**
     * Pop a value from the stack.
     * 
     * @return value from top of stack
     * @throws ArrayIndexOutOfBoundsException on attempt to pop empty stack
     */
    public Object pop() {
        if (Debug.DEBUG) {
            if (size <= 0) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds
                        ("Attempt to pop empty stack");
            }
        }
        size--;
        return m_baseArray[currentIndex++];
    }

    /**
     * Pop multiple values from the stack.
     * 
     * @param count number of values to pop from stack (must be strictly
     *              positive)
     * @throws ArrayIndexOutOfBoundsException on attempt to pop past end of
     *                                        stack
     */
    public void pop(int count) {
        if (Debug.DEBUG) {
            if (count > size) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(
                        "Attempt to pop past end of stack");
            }
            if (count <= 0) {
                throw BindingSupportImpl.getInstance().illegalArgument(
                        "Count must be greater than 0");
            }
        }
        currentIndex += count;
        size = size - count;
    }

    public void add(Object[] data, int amountToAdd) {
        if (m_baseArray == null || size == 0) {
            /**
             * No data so just replace with supplied array
             */
            m_baseArray = data;
            size = amountToAdd;
            currentIndex = 0;
        } else {
            /**
             * There is current data so copy in.
             */
            if ((m_baseArray.length - size) < amountToAdd) {
                Object[] tmpArray = new Object[m_baseArray.length + amountToAdd];
                System.arraycopy(m_baseArray, 0, tmpArray, 0, size);
                System.arraycopy(data, 0, tmpArray, size - 1, amountToAdd);
                m_baseArray = tmpArray;
                size += amountToAdd;
            }
            //array copy the new data and update the entry count.
            System.arraycopy(data, 0, m_baseArray, size - 1, amountToAdd);
            size += amountToAdd;
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        size = 0;
        currentIndex = 0;
    }

    public void close() {
        m_baseArray = null;
        size = 0;
        currentIndex = 0;
    }
}
