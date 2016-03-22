
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

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Statement;
import java.util.*;

/**
 * This is a util class to assist with java.beans persistence for JDO Genie
 * SCOs. Call register with an Encoder to instal persistence delegates to
 * handle all the JDO Genie SCO classes.
 *
 * @see #register(java.beans.Encoder)
 * @see java.beans.Encoder
 * @see java.beans.XMLEncoder
 */
public class PersistenceDelegateManager {

    /**
     * Set persistence delegates on provided encoder for JDO Genie SCO instances.
     */
    public static void register(Encoder encoder) {
        encoder.setPersistenceDelegate(com.versant.core.jdo.sco.Date.class,
                new SCOPersistenceDelegate(java.util.Date.class));

        encoder.setPersistenceDelegate(SCOList.class, new SCOListPD(ArrayList.class));
        encoder.setPersistenceDelegate(SCOArrayList.class, new SCOListPD(ArrayList.class));
        encoder.setPersistenceDelegate(SCOVector.class, new SCOListPD(Vector.class));

        encoder.setPersistenceDelegate(SCOHashMap.class, new SCOMapPD(HashMap.class));
        encoder.setPersistenceDelegate(SCOHashtable.class, new SCOMapPD(Hashtable.class));
        encoder.setPersistenceDelegate(SCOTreeMap.class, new SCOMapPD(TreeMap.class));
        encoder.setPersistenceDelegate(SCOTreeSet.class, new SCOMapPD(TreeSet.class));

        encoder.setPersistenceDelegate(SCOHashSet.class, new SCOCollectionPD(HashSet.class));
        encoder.setPersistenceDelegate(SCOLinkedList.class, new SCOCollectionPD(LinkedList.class));
    }

    private static void invokeStatement(Object instance, String methodName,
                                        Object[] args, Encoder out) {
        out.writeStatement(new Statement(instance, methodName, args));
    }

    private static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    public static class SCOPersistenceDelegate extends DefaultPersistenceDelegate {

        Class javaType;

        public SCOPersistenceDelegate(Class javaType) {
            this.javaType = javaType;
        }

        protected Expression instantiate(Object oldInstance, Encoder out) {

            return new Expression(oldInstance,
                    javaType,
                    "new",
                    new Object[]{});
        }

        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            if (oldInstance != null && newInstance != null) {
                return true;
            }
            return super.mutatesTo(oldInstance, newInstance);
        }
    }

    public static class SCOCollectionPD extends SCOPersistenceDelegate {

        public SCOCollectionPD(Class javaType) {
            super(javaType);
        }

        protected void initialize(Class type, Object oldInstance,
                                  Object newInstance, Encoder out) {
            java.util.Collection oldO = (java.util.Collection) oldInstance;
            java.util.Collection newO = (java.util.Collection) newInstance;

            if (newO.size() != 0) {
                PersistenceDelegateManager.invokeStatement(oldInstance, "clear", new Object[]{}, out);
            }
            for (Iterator i = oldO.iterator(); i.hasNext();) {
                PersistenceDelegateManager.invokeStatement(oldInstance, "add", new Object[]{
                    i.next()}, out);
            }
        }
    }

    public static class SCOListPD extends SCOPersistenceDelegate {

        public SCOListPD(Class javaType) {
            super(javaType);
        }

        protected void initialize(Class type, Object oldInstance,
                                  Object newInstance, Encoder out) {

            java.util.List oldO = (java.util.List) oldInstance;
            java.util.List newO = (java.util.List) newInstance;
            int oldSize = oldO.size();
            int newSize = (newO == null) ? 0 : newO.size();
            if (oldSize < newSize) {
                PersistenceDelegateManager.invokeStatement(oldInstance, "clear", new Object[]{}, out);
                newSize = 0;
            }
            for (int i = 0; i < newSize; i++) {
                Object index = new Integer(i);

                Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{
                    index});
                Expression newGetExp = new Expression(newInstance, "get", new Object[]{
                    index});
                try {
                    Object oldValue = oldGetExp.getValue();
                    Object newValue = newGetExp.getValue();
                    out.writeExpression(oldGetExp);
                    if (!PersistenceDelegateManager.equals(newValue, out.get(oldValue))) {
                        PersistenceDelegateManager.invokeStatement(oldInstance, "set", new Object[]{
                            index, oldValue}, out);
                    }
                } catch (Exception e) {
                    out.getExceptionListener().exceptionThrown(e);
                }
            }
            for (int i = newSize; i < oldSize; i++) {
                PersistenceDelegateManager.invokeStatement(oldInstance, "add", new Object[]{
                    oldO.get(i)}, out);
            }
        }

    }

    public static class SCOMapPD extends SCOPersistenceDelegate {

        public SCOMapPD(Class javaType) {
            super(javaType);
        }

        protected void initialize(Class type, Object oldInstance,
                                  Object newInstance, Encoder out) {
            java.util.Map oldMap = (java.util.Map) oldInstance;
            java.util.Map newMap = (java.util.Map) newInstance;
            // Remove the new elements.
            // Do this first otherwise we undo the adding work.
            if (newMap != null) {
                java.util.Iterator newKeys = newMap.keySet().iterator();
                while (newKeys.hasNext()) {
                    Object newKey = newKeys.next();
                    // PENDING: This "key" is not in the right environment.
                    if (!oldMap.containsKey(newKey)) {
                        PersistenceDelegateManager.invokeStatement(oldInstance, "remove", new Object[]{
                            newKey}, out);
                    }
                }
            }
            // Add the new elements.
            java.util.Iterator oldKeys = oldMap.keySet().iterator();
            while (oldKeys.hasNext()) {
                Object oldKey = oldKeys.next();

                Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{
                    oldKey});
                // Pending: should use newKey.
                Expression newGetExp = new Expression(newInstance, "get", new Object[]{
                    oldKey});
                try {
                    Object oldValue = oldGetExp.getValue();
                    Object newValue = newGetExp.getValue();
                    out.writeExpression(oldGetExp);
                    if (!PersistenceDelegateManager.equals(newValue, out.get(oldValue))) {
                        PersistenceDelegateManager.invokeStatement(oldInstance, "put", new Object[]{
                            oldKey, oldValue}, out);
                    }
                } catch (Exception e) {
                    out.getExceptionListener().exceptionThrown(e);
                }
            }
        }
    }
}
