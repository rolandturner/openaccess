
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
package com.versant.core.jdo.tools.workbench.model;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * A collection of properties and values for a javabean. The properties can
 * be resolved to values using the Ant tokens of the project. Only read/write
 * properties are displayed.
 */
public class MdPropertySet {

    private List propertyList = new ArrayList();
    private HashMap values = new HashMap();
    private Object bean;
    private MdProject project;
    private boolean allowTokens;

    public MdPropertySet(MdProject project, boolean allowTokens) {
        this.project = project;
        this.allowTokens = allowTokens;
    }

    public void setBean(Object bean) throws Exception {
        this.bean = bean;
        propertyList.clear();
        if (bean != null) {
            Class cls = bean.getClass();
            BeanInfo bi = Introspector.getBeanInfo(cls);
            PropertyDescriptor[] pda = bi.getPropertyDescriptors();
            int n = pda.length;
            for (int i = 0; i < n; i++) {
                PropertyDescriptor pd = pda[i];
                if (pd.getReadMethod() == null || pd.getWriteMethod() == null) continue;
                Class t = pd.getPropertyType();
                if (t != String.class && t != Integer.TYPE && t != Boolean.TYPE) continue;
                Prop p = new Prop(pd);
                updatePickList(p, pd);
                try {
                    Object o = pd.getReadMethod().invoke(bean, null);
                    if (o != null) p.setDef(o.toString());
                } catch (Exception e) {
                    error(e);
                }
                propertyList.add(p);
                try {
                    String s = p.getValueStr();
                    if (MdUtils.isStringNotEmpty(s)) p.setValueStr(s);
                } catch (Exception e) {
                    error(e);
                }
            }
            // now that we have the defaults set all properties on the bean
            n = propertyList.size();
            for (int i = 0; i < n; i++) {
                Prop p = (Prop)propertyList.get(i);
                String v = p.getValueStr();
                if (MdUtils.isStringNotEmpty(v)) {
                    try {
                        p.setValueStr(v);
                    } catch (Exception e) {
                        project.getLogger().error(e);
                    }
                }
            }
        }
    }

    private void updatePickList(Prop p, PropertyDescriptor pd) {
        Class t = pd.getPropertyType();
        if (t == Boolean.TYPE) {
            p.setPickList(PickLists.BOOLEAN);
            return;
        } else if (t != String.class) {
            return;
        }
        String s = pd.getShortDescription();
        int i = s.lastIndexOf('{');
        int j = s.lastIndexOf('}');
        if (i >= j) return;
        ArrayList a = new ArrayList();
        StringTokenizer st = new StringTokenizer(s.substring(i + 1, j), ",",
                false);
        for (; st.hasMoreTokens();) a.add(st.nextToken());
        p.setPickList(a);
        p.setDescription(s.substring(0, i));
    }

    private void error(Throwable x) {
        if (x instanceof InvocationTargetException) {
            x = ((InvocationTargetException)x).getTargetException();
        }
        project.getLogger().error(x);
    }

    public Object getBean() {
        return bean;
    }

    public List getPropertyList() {
        return propertyList;
    }

    public HashMap getDefaultValues() {
        int size = propertyList.size();
        HashMap map = new HashMap();
        for (int i = 0; i < size; i++) {
            Prop prop = (Prop)propertyList.get(i);
            map.put(prop.pd.getName(), prop.def);
        }
        return map;
    }

    public HashMap getValues() {
        return values;
    }

    public void clear() {
        values.clear();
    }

    /**
     * A wrapper for a property.
     */
    public class Prop {

        private PropertyDescriptor pd;
        private List pickList;
        private String def;
        private String description;
        private boolean readOnly;

        public Prop(PropertyDescriptor pd) {
            this.pd = pd;
            description = pd.getShortDescription();
            if (description.equals(pd.getDisplayName())) description = null;
            readOnly = pd.getWriteMethod() == null;
        }

        public void setPickList(List pickList) {
            this.pickList = pickList;
        }

        public List getPickList() {
            return pickList;
        }

        public MdValue getValue() {
            MdValue v = new MdValue(getValueStr());
            v.setPickList(pickList);
            v.setDefText(def);
            v.setReadOnly(readOnly);
            return v;
        }

        public void setValue(MdValue v) throws Exception {
            if (readOnly) {
                throw new MdVetoException("Property '" + description +
                        "' is read only");
            }
            String s = v.getText();
            String name = pd.getName();
            if (s == null) {
                values.remove(name);
            } else {
                values.put(name, s);
            }
            setValueStr(s == null ? def : s);
        }

        public String getValueStr() {
            return (String)values.get(pd.getName());
        }

        public String getValueTokenReplaced() {
            return project.resolveToken(getValueStr());
        }

        public void setValueStr(String s) throws Exception {
            Object o;
            if (s == null) {
                o = null;
            } else {
                if (allowTokens) s = project.resolveToken(s);
                Class t = pd.getPropertyType();
                if (t == String.class) {
                    o = s;
                } else if (t == Integer.TYPE) {
                    o = new Integer(s);
                } else {
                    o = Boolean.valueOf(s);
                }
            }
            pd.getWriteMethod().invoke(bean, new Object[]{o});
        }

        public String getDef() {
            return def;
        }

        public void setDef(String def) {
            this.def = def;
        }

        public String getDisplayName() {
            return pd.getDisplayName();
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}

