
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
package com.versant.core.metadata.parser;

import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.common.Debug;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Class element from a .jdo file.
 */
public final class JdoClass extends JdoElement implements MDStatics {

    public String name;
    public int identityType;
    public String objectIdClass;
    public boolean requiresExtent;
    public String pcSuperclass;
    public JdoPackage parent;
    
    public Class javaClass, entityListener;
    public Method[] listenerCallbackMap, callbackMap = null;
    
    /** Whether this class was generated using an xml mapping file. */
    public boolean xmlGenerated = false;
    public boolean isXmlGenerated() {return xmlGenerated;}

    /** The field and extension elements in order or null if none. */
    public JdoElement[] elements;

    /** The query elements or null if none. */
    public ArrayList queries;
    /**
     * This can be used to override the need for a objectIdClass for appid instances
     */
    public boolean objectIdClasssRequired = true;

    public JdoElement getParent() { return parent; }

    /**
     * Get information for this element to be used in building up a
     * context string.
     * @see #getContext
     */
    public String getSubContext() {
        return "class[" + name + "]";
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("class[");
        s.append(name);
        s.append("] identityType=");
        s.append(MDStaticUtils.toIdentityTypeString(identityType));
        s.append(" objectIdClass=");
        s.append(objectIdClass);
        s.append(" requiresExtent=");
        s.append(requiresExtent);
        s.append(" pcSuperclass=");
        s.append(pcSuperclass);
        return s.toString();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                Object o = elements[i];
                if (o instanceof JdoField) {
                    ((JdoField)o).dump(out, indent + "  ");
                } else if (o instanceof JdoExtension) {
                    ((JdoExtension)o).dump(out, indent + "  ");
                } else if (o instanceof JdoQuery) {
                    ((JdoQuery) o).dump(out, indent + "  ");
                } else {
                    out.println("unknown " + o);
                }
            }
        }
        if (queries != null) {
            for (Iterator i = queries.iterator(); i.hasNext(); ) {
                ((JdoQuery)i.next()).dump(out, indent + "  ");
            }
        }
    }

    /**
     * Get the fully qualified name of this class.
     */
    public String getQName() {
        return getQName(name);
    }

    private String getQName(String n) {
        if (n == null) return null;
        int i = n.indexOf('.');
        if (i >= 0 || parent.name.length() == 0) return n;
        return parent.name + '.' + n;
    }

    /**
     * Get the fully qualified name of our PC super class or null if none.
     */
    public String getPCSuperClassQName() {
        return getQName(pcSuperclass);
    }

    /**
     * Get the fully qualified name of our objectid-class or null if none.
     */
    public String getObjectIdClassQName() {
        return getQName(objectIdClass);
    }

    /**
     * Does this class has a JDBC_KEY_GENERATOR set on it?
     */
    public boolean hasKeyGen(){
        if (elements != null) {
            JdoExtension ext = null;
            for (int i = 0; i < elements.length; i++) {
                Object o = elements[i];
                if (o instanceof JdoExtension) {
                    ext = (JdoExtension)o;
                    if (ext.key == JdoExtension.JDBC_KEY_GENERATOR) {
                        return true;
                    } else if (ext.contains(JdoExtension.JDBC_KEY_GENERATOR)){
                        return true;
                    }

                }
            }
        }
        return false;
    }

    /**
     * Add a JdoQuery to this class. This is called when queries declared
     * in a separate .jdoquery resource file are moved to the main JdoClass
     * definition and when queries are originally parsed. The q.parent field
     * must have already been set.
     */
    public void addJdoQuery(JdoQuery q) {
        // Note that this code must not change q.parent to reference this
        // class. The parent link is used to construct parsing error messages
        // and so must lead back to the original JdoRoot for the resource the
        // query was declared in.
        if (queries == null) queries = new ArrayList();
        queries.add(q);
    }

    public int getInheritance(Map enumMap) {
        if (elements != null) {
            JdoExtension ext = null;
            for (int i = 0; i < elements.length; i++) {
                Object o = elements[i];
                if (o instanceof JdoExtension) {
                    ext = (JdoExtension)o;
                    if (ext.key == JdoExtension.JDBC_INHERITANCE) {
                        if (ext.value == null) return -1;
                        return ext.getEnum(enumMap);
                    }
                }
            }
        }
        return -1;
    }

    public void addElement(JdoElement jdoElement) {
        if (elements == null) {
            elements = new JdoElement[] {jdoElement,};
        } else {
            JdoElement[] tmp = new JdoElement[elements.length + 1];
            System.arraycopy(elements, 0, tmp, 0, elements.length);
            tmp[elements.length] = jdoElement;
            elements = tmp;
        }

    }

    /**
     * If this class has a single pk field the name will be returned, else we
     * will return null.
     */
    public String getSinglePKField() {
        String name = null;
        if (elements != null) {
            JdoField field = null;
            for (int i = 0; i < elements.length; i++) {
                Object o = elements[i];
                if (o instanceof JdoField) {
                    field = (JdoField) o;
                    if (field.primaryKey) {
                        if (name == null) {
                            name = field.name;
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return name;
    }
}
