
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
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.common.VersantFieldMetaData;

import javax.jdo.spi.PersistenceCapable;
import java.io.*;

/**
 * @keep-all
 */
public class Date extends java.util.Date implements SCODate {

    private transient PersistenceCapable owner;
    private final transient VersantFieldMetaData fmd;
    private transient VersantStateManager stateManager;

    /**
     * Creates a <code>Date</code> object that represents the given time
     * in milliseconds.
     *
     * @param date the number of milliseconds
     */
    public Date(PersistenceCapable owner,
                VersantStateManager stateManager, VersantFieldMetaData fmd,
                long date) {
        super(date);
        this.owner = owner;
        this.stateManager = stateManager;
        this.fmd = fmd;
    }

    /**
     * Sets the <tt>Date</tt> object to represent a point in time that is
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT.
     *
     * @param time the number of milliseconds.
     * @see java.util.Date
     */
    public void setTime(long time) {
        this.makeDirty();
        super.setTime(time);
    }

    /**
     * Creates and returns a copy of this object.
     * <p/>
     * Mutable Second Class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     */
    public Object clone() {
        Object obj = super.clone();
        if (obj instanceof VersantSimpleSCO) {
            ((VersantSimpleSCO) obj).makeTransient();
        }
        return obj;
    }

    /** -----------Depricated Methods------------------*/

    /**
     * Sets the year of this <tt>Date</tt> object to be the specified
     * value plus 1900.
     *
     * @param year the year value.
     * @see java.util.Calendar
     * @see java.util.Date
     * @deprecated As of JDK version 1.1,
     *             replaced by <code>Calendar.set(Calendar.YEAR, year + 1900)</code>.
     */
    public void setYear(int year) {
        this.makeDirty();
        super.setYear(year);
    }

    /**
     * Sets the month of this date to the specified value.
     *
     * @param month the month value between 0-11.
     * @see java.util.Calendar
     * @see java.util.Date
     * @deprecated As of JDK version 1.1,
     *             replaced by <code>Calendar.set(Calendar.MONTH, int month)</code>.
     */
    public void setMonth(int month) {
        this.makeDirty();
        super.setMonth(month);
    }

    /**
     * Sets the day of the month of this <tt>Date</tt> object to the
     * specified value.
     *
     * @param date the day of the month value between 1-31.
     * @see java.util.Calendar
     * @see java.util.Date
     * @deprecated As of JDK version 1.1,
     *             replaced by <code>Calendar.set(Calendar.DAY_OF_MONTH, int date)</code>.
     */
    public void setDate(int date) {
        this.makeDirty();
        super.setDate(date);
    }

    /**
     * Sets the hour of this <tt>Date</tt> object to the specified value.
     *
     * @param hours the hour value.
     * @see java.util.Calendar
     * @see java.util.Date
     * @deprecated As of JDK version 1.1,
     *             replaced by <code>Calendar.set(Calendar.HOUR_OF_DAY, int hours)</code>.
     */
    public void setHours(int hours) {
        this.makeDirty();
        super.setHours(hours);
    }

    /**
     * Sets the minutes of this <tt>Date</tt> object to the specified value.
     *
     * @param minutes the value of the minutes.
     * @see java.util.Calendar
     * @see java.util.Date
     * @deprecated As of JDK version 1.1,
     *             replaced by <code>Calendar.set(Calendar.MINUTE, int minutes)</code>.
     */
    public void setMinutes(int minutes) {
        this.makeDirty();
        super.setMinutes(minutes);
    }

    /**
     * Sets the seconds of this <tt>Date</tt> to the specified value.
     *
     * @param seconds the seconds value.
     * @see java.util.Calendar
     * @see java.util.Date
     * @deprecated As of JDK version 1.1,
     *             replaced by <code>Calendar.set(Calendar.SECOND, int seconds)</code>.
     */
    public void setSeconds(int seconds) {
        this.makeDirty();
        super.setSeconds(seconds);
    }

    /** ---------------- internal methods ------------------- */

    /**
     * Sets the <tt>Date</tt> object without notification of the Owner
     * field. Used internaly to populate date from DB
     *
     * @param time the number of milliseconds.
     * @see java.util.Date
     */
    public void setTimeInternal(long time) {
        super.setTime(time);
    }

    /**
     * Nullifies references to the owner Object and Field
     */
    public void makeTransient() {
        this.owner = null;
        this.stateManager = null;
    }

    /**
     * Returns the owner object of the SCO instance
     *
     * @return owner object
     */
    public Object getOwner() {
        return this.owner;
    }

    /**
     * Marks object dirty
     */
    public void makeDirty() {
        if (stateManager != null) {
            stateManager.makeDirty(owner, fmd.getManagedFieldNo());
        }
    }

    public void reset() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(super.getTime());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.setTime(in.readLong());
    }

    public static void main(String[] args) throws Exception {
        Date d = new Date(null, null, null, System.currentTimeMillis());
        final long time = System.currentTimeMillis() - 5000;
        d.setTime(time);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(d);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        Object o = in.readObject();
        Debug.OUT.println("####### o.type = " + o.getClass().getName());
        Debug.OUT.println("####### equal = " + d.equals(o));
    }

}

