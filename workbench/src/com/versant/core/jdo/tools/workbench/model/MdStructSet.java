
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

import com.versant.core.util.BeanUtils;
import com.versant.core.common.BindingSupportImpl;

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * A collection of public field names and values for a struct. The fields can
 * be resolved to values using the Ant tokens of the project. If there is
 * a 'public static final String HELP_fieldname' constant for a field then
 * it is used as help text.
 */
public class MdStructSet {

    private List fieldList = new ArrayList();
    private HashMap values = new HashMap();
    private Object bean;
    private MdProject project;
    private String propertyPrefix;

    public MdStructSet(MdProject project) {
        this.project = project;
    }

    public void init(Object bean, Properties props) throws Exception {
        this.bean = bean;
        fieldList.clear();
        values.clear();
        propertyPrefix = null;
        if (bean != null) {
            HashMap helpMap = new HashMap();
            Class beanCls = /*CHFC*/bean.getClass()/*RIGHTPAR*/;
            Field[] a = beanCls.getFields();
            for (int i = 0; i < a.length; i++) {
                Field f = a[i];
                String name = f.getName();
                int m = f.getModifiers();
                if (Modifier.isStatic(m) && Modifier.isFinal(m)
                        && f.getType() == /*CHFC*/String.class/*RIGHTPAR*/) {
                    if (name.startsWith("HELP_")) {
                        helpMap.put(name.substring(5), f.get(null));
                    } else if ("PROPERTY_PREFIX".equals(name)) {
                        propertyPrefix = (String)f.get(null);
                    }
                }
            }
            if (propertyPrefix == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        bean.getClass().getName() + " has no 'public " +
                        "static final String PROPERTY_PREFIX' field");
            }
            for (int i = 0; i < a.length; i++) {
                Field f = a[i];
                int m = f.getModifiers();
                if (Modifier.isFinal(m) || Modifier.isStatic(m)
                        || !BeanUtils.isSupportedFieldType(f.getType())) {
                    continue;
                }
                String name = f.getName();
                FieldWrapper p = new FieldWrapper(f, (String)helpMap.get(name));
                Object def = f.get(bean);
                if (def != null) {
                    p.setDef(String.valueOf(def));
                }
                updatePickList(p);
                fieldList.add(p);
                String value = props.getProperty(p.getFullName());
                if (value != null) {
                    values.put(p.getFullName(), value);
                }
            }
        }
    }

    private void updatePickList(FieldWrapper p) {
        Class t = p.field.getType();
        if (t == /*CHFC*/Boolean.TYPE/*RIGHTPAR*/) {
            p.setPickList(PickLists.BOOLEAN);
            return;
        } else if (t != /*CHFC*/String.class/*RIGHTPAR*/) {
            return;
        }
        String s = p.getHelp();
        int i = s.lastIndexOf('{');
        int j = s.lastIndexOf('}');
        if (i >= j) return;
        ArrayList a = new ArrayList();
        StringTokenizer st = new StringTokenizer(s.substring(i + 1, j), ",",
                false);
        for (; st.hasMoreTokens();) a.add(st.nextToken());
        p.setPickList(a);
        p.setHelp(s.substring(0, i));
    }

    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    public void setPropertyPrefix(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    public void parseProperties(Properties p) {
        if (propertyPrefix == null) {
            throw new IllegalStateException("propertyPrefix not set");
        }
        Set all = p.keySet();
        String prefix = propertyPrefix + '.';
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            String s = (String)iter.next();
            if (s.startsWith(prefix)) {
                String newKey = s.substring(prefix.length());
                values.put(newKey, p.getProperty(s));
            }
        }
    }

    public Object getBean() {
        return bean;
    }

    public List getFieldList() {
        return fieldList;
    }

    public HashMap getDefaultValues() {
        int size = fieldList.size();
        HashMap map = new HashMap();
        for (int i = 0; i < size; i++) {
            FieldWrapper fieldWrapper = (FieldWrapper)fieldList.get(i);
            map.put(fieldWrapper.getName(), fieldWrapper.def);
        }
        return map;
    }

    public HashMap getValues() {
        return values;
    }

    public void clear() {
        values.clear();
    }

    public void saveProps(PropertySaver pr) {
        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            FieldWrapper f = (FieldWrapper)fieldList.get(i);
            pr.add(f.getFullName(), f.getValueStr(), f.getDef());
        }
    }

    /**
     * A wrapper for a field.
     */
    public class FieldWrapper {

        private Field field;
        private List pickList;
        private String def;
        private String help;
        private String fullname;

        public FieldWrapper(Field field, String help) {
            this.field = field;
            this.help = help;
            this.fullname = propertyPrefix + field.getName();
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
            return v;
        }

        public void setValue(MdValue v) throws Exception {
            String s = v.getText();
            if (s == null) {
                values.remove(fullname);
            } else {
                values.put(fullname, s);
            }
        }

        public String getValueStr() {
            return (String)values.get(fullname);
        }

        public String getValueTokenReplaced() {
            return project.resolveToken(getValueStr());
        }

        public void setDef(String def) {
            this.def = def;
        }

        public String getDef() {
            return def;
        }

        public String getName() {
            return field.getName();
        }

        public void setHelp(String help) {
            this.help = help;
        }

        public String getHelp() {
            return help;
        }

        public String getFullName() {
            return fullname;
        }
    }

}

