
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

import com.versant.core.common.Debug;
import com.versant.core.util.BeanUtils;
import com.versant.core.util.IntObjectHashMap;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is one of our vendor extensions with the key string converted to
 * an int constant for easy processing.
 */
public final class JdoExtension extends JdoElement implements JdoExtensionKeys {

    /**
     * This is the special value used to indicate that constraints, indexes
     * and so on should not be generated.
     */
    public static final String NO_VALUE = "{no}";

    public static final String NAME_VALUE = "{name}";
    public static final String FULLNAME_VALUE = "{fullname}";
    public static final String HASH_VALUE = "{hash}";

    public int key;
    public String value;
    public JdoExtension[] nested;
    public JdoElement parent;

    private static final Map STR_KEY_MAP = new HashMap();
    private static final IntObjectHashMap KEY_STR_MAP = new IntObjectHashMap();
    private static final HashMap PRIMITIVE_TYPE_MAP = new HashMap(17);

    static {
        // find all the valid keys and add them to the maps
        Class cls = /*CHFC*/JdoExtensionKeys.class/*RIGHTPAR*/;
        Field[] a = cls.getFields();
        for (int i = a.length - 1; i >= 0; i--) {
            Field f = a[i];
            String key = f.getName().replace('_', '-').toLowerCase();

            try {
                int v = f.getInt(null);
                STR_KEY_MAP.put(key, new Integer(v));
                KEY_STR_MAP.put(v, key);
            } catch (IllegalAccessException e) {
                throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
            }
        }

        // build map of primitive types
        PRIMITIVE_TYPE_MAP.put("boolean", Boolean.TYPE);
        PRIMITIVE_TYPE_MAP.put("byte", Byte.TYPE);
        PRIMITIVE_TYPE_MAP.put("char", Character.TYPE);
        PRIMITIVE_TYPE_MAP.put("short", Short.TYPE);
        PRIMITIVE_TYPE_MAP.put("int", Integer.TYPE);
        PRIMITIVE_TYPE_MAP.put("long", Long.TYPE);
        PRIMITIVE_TYPE_MAP.put("float", Float.TYPE);
        PRIMITIVE_TYPE_MAP.put("double", Double.TYPE);
    }

    public JdoExtension createCopy(JdoElement pe) {
        JdoExtension copy = new JdoExtension();
        copy.key = key;
        copy.value = value;

        if (nested != null) {
            copy.nested = new JdoExtension[nested.length];
            for (int i = 0; i < nested.length; i++) {
                JdoExtension jdoExtension = nested[i];
                copy.nested[i] = jdoExtension.createCopy(copy);
            }
        }
        copy.parent = pe;
        return copy;
    }

    public JdoElement getParent() {
        return parent;
    }

    /**
     * Get information for this element to be used in building up a
     * context string.
     *
     * @see #getContext
     */
    public String getSubContext() {
        if (value == null) return toKeyString(key);
        return toKeyString(key) + "=\"" + value + '"';
    }

    /**
     * Is this a common extension? This method must be kept in sync with the
     * constants.
     *
     * @see JdoExtensionKeys
     */
    public boolean isCommon() {
        return key < 100;
    }

    /**
     * Is this a jdbc extension? This method must be kept in sync with the
     * constants.
     *
     * @see JdoExtensionKeys
     */
    public boolean isJdbc() {
        return key >= 100 && key <= 199;
    }

    /**
     * Convert a String key value into an int constant.
     *
     * @return Key constant or Integer.MIN_VALUE if not valid
     * @see JdoExtensionKeys
     */
    public static int parseKey(String key) {
        Integer v = (Integer)STR_KEY_MAP.get(key);
        return v == null ? Integer.MIN_VALUE : v.intValue();
    }

    /**
     * Convert ant int key value into a String.
     *
     * @see JdoExtensionKeys
     */
    public static String toKeyString(int key) {
        return (String)KEY_STR_MAP.get(key);
    }

    public String toString() {
        return getSubContext();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        if (nested != null) {
            for (int i = 0; i < nested.length; i++) {
                nested[i].dump(out, indent + "  ");
            }
        }
    }

    /**
     * Get the value of an extension that must be a String.
     *
     * @throws javax.jdo.JDOFatalUserException if the value is null
     */
    public String getString() {
        if (value == null) {
            throw BindingSupportImpl.getInstance().runtime("Expected 'value' attribute for " +
                    this + "\n" + getContext());
        }
        return value;
    }

    /**
     * Is the value of this extension the special NO_VALUE ({no})?
     */
    public boolean isNoValue() {
        return value != null && value.equals(NO_VALUE);
    }

    /**
     * Get the value of an extension that must be a boolean. A null value
     * is true.
     *
     * @throws javax.jdo.JDOFatalUserException if the value is invalid
     */
    public boolean getBoolean() {
        if (value == null || value.equals("true")) return true;
        if (value.equals("false")) return false;
        throw BindingSupportImpl.getInstance().runtime("Expected 'true' or 'false' for " +
                this + "\n" + getContext());
    }

    /**
     * Get the value of an extension that must be an int.
     *
     * @throws javax.jdo.JDOFatalUserException if the value is invalid
     */
    public int getInt() {
        try {
            if (value != null) return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // ignore
        }
        throw BindingSupportImpl.getInstance().runtime("Expected integer value for " +
                this + "\n" + getContext());
    }

    /**
     * Get the value of an extension that must be one of a set of enumerated
     * String's. Each valid String must have an int entry in map. The
     * corresponding int is returned.
     *
     * @throws javax.jdo.JDOFatalUserException if the value is not in map
     */
    public int getEnum(Map map) {
        int ans;
        if (value == null) {
            ans = Integer.MIN_VALUE;
        } else {
            ans = ((Integer)map.get(value)).intValue();
        }
        if (ans != Integer.MIN_VALUE) return ans;
        // build a nice exception message and throw it
        ArrayList a = new ArrayList(map.size());
        for (Iterator i = map.keySet().iterator(); i.hasNext();) a.add(i.next());
        Collections.sort(a);
        StringBuffer s = new StringBuffer();
        if (value == null) {
            s.append("No value attribute");
        } else {
            s.append("Invalid value attribute '");
            s.append(value);
            s.append('\'');
        }
        s.append(": Expected ");
        int n = a.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                if (i < n - 1)
                    s.append(", ");
                else
                    s.append(" or ");
            }
            s.append('\'');
            s.append(a.get(i));
            s.append('\'');
        }
        s.append('\n');
        s.append(getContext());
        throw BindingSupportImpl.getInstance().runtime(s.toString());
    }

    /**
     * Get the value of an extension that must be the fully qualified name
     * of a class or a primitive type. The class is loaded using the supplied
     * classloader.
     */
    public Class getType(ClassLoader loader) {
        String qname = getString();
        try {
            return BeanUtils.loadClass(qname, true, loader);
        } catch (ClassNotFoundException e) {
            throw BindingSupportImpl.getInstance().runtime("Class " + qname + " not found\n" +
                    getContext(), e);
        }
    }

    /**
     * Get the value of an extension that must be the fully qualified name
     * of a class assignable from the supplied class. The class is loaded
     * using the supplied classloader.
     */
    public Class getType(ClassLoader loader, Class requiredType) {
        Class t = getType(loader);
        if (requiredType.isAssignableFrom(t)) return t;
        throw BindingSupportImpl.getInstance().runtime("Class " + t.getName() + " is not a " +
                requiredType.getName() + "\n" +
                getContext());
    }

    /**
     * Create a HashMap of properties from our nested property extensions.
     * If there are none an empty map is returned.
     */
    public HashMap getPropertyMap() {
        return getPropertyMap(new HashMap(17));
    }

    /**
     * Add propertiies to the supplied HashMap.
     */
    public HashMap getPropertyMap(HashMap m) {
        if (nested != null) {
            int n = nested.length;
            for (int i = 0; i < n; i++) {
                JdoExtension e = nested[i];
                if (e.key != PROPERTY) {
                    throw BindingSupportImpl.getInstance().runtime("Expected property extension: " + e + "\n" +
                            e.getContext());
                }
                String v = nested[i].getString();
                int pos = v.indexOf('=');
                if (pos < 0) {
                    throw BindingSupportImpl.getInstance().runtime("Invalid value attribute, expected 'key=value': '" +
                            v + "'\n" + e.getContext());
                }
                String key = v.substring(0, pos);
                v = v.substring(pos + 1);
                m.put(key, v);
            }
        }
        return m;
    }

    /**
     * Find an extension from an array of extensions.
     */
    public static JdoExtension find(int key, JdoExtension[] a) {
        if (a == null) return null;
        int n = a.length;
        for (int i = 0; i < n; i++) if (a[i].key == key) return a[i];
        return null;
    }

    /**
     * Create the extension if it does not exist, else update it if overwrite is true
     * @param key
     * @param value
     * @param overwrite
     */
    public void findCreate(int key, String value, boolean overwrite) {
        if (nested == null) {
            nested = new JdoExtension[] {createChild(key, value)};
            return;
        }

        JdoExtension ext = find(key, nested);

        if (ext == null) {
            addExtension(createChild(key, value));
        } else if (overwrite) {
            ext.value = value;
        }
    }

    private JdoExtension createChild(int key, String value) {
        JdoExtension e = new JdoExtension();
        e.key = key;
        e.value = value;
        e.parent = this;
        return e;
    }

    private void addExtension(JdoExtension e) {
        JdoExtension[] tmp = new JdoExtension[nested.length + 1];
        System.arraycopy(nested, 0, tmp, 0, nested.length);
        tmp[nested.length] = e;
        nested = tmp;
    }

    /**
     * Find an extension from an array of extensions.
     */
    public static JdoExtension find(int key, JdoElement[] a) {
        if (a == null) return null;
        int n = a.length;
        for (int i = 0; i < n; i++) {
            if (a[i] instanceof JdoExtension) {
                if (((JdoExtension)a[i]).key == key) return (JdoExtension) a[i];
            }
        }
        return null;
    }

    public static JdoExtension find(int key, String value, JdoExtension[] a) {
        if (a == null) return null;
        int n = a.length;
        for (int i = 0; i < n; i++) if (a[i].key == key && a[i].value.equals(value)) return a[i];
        return null;
    }

    /**
     * Does the any of the nested jdoExtension contain the spec key
     *
     * @param key The key to search for.
     * @return
     */
    public boolean contains(int key) {
        if (nested != null) {
            for (int i = 0; i < nested.length; i++) {
                JdoExtension jdoExtension = nested[i];
                if (jdoExtension.key == key) return true;
            }
        }
        return false;
    }

    /**
     * Update the 'to' extension with the 'from' extensions. If there is
     * elements in 'from' that is not in 'to' then add them. If the element is there
     * then update it value and recursively synchronize its nested elements.
     *
     */
    public static JdoExtension[] synchronize(JdoExtension[] from, JdoExtension[] to) {
        if (from == null) return to;
        if (to == null) return from;

        ArrayList newExts = new ArrayList(to.length);
        for (int i = 0; i < from.length; i++) {
            boolean found = false;
            JdoExtension fromElement = from[i];

            for (int j = 0; j < to.length; j++) {
                JdoExtension toElement = to[j];
                if (fromElement.key == toElement.key) {
                    switch (fromElement.key) {
                        case JdoExtension.FIELD:
                            if (fromElement.value != null
                                    && fromElement.value.equals(toElement.value)) {
                                toElement.nested = JdoExtension.synchronize(fromElement.nested, toElement.nested);
                            }
                            break;
                        case JdoExtension.JDBC_COLUMN:
                            toElement.nested =
                                    JdoExtension.synchronize(fromElement.nested,
                                            toElement.nested);
                            break;
                        case JdoExtension.JDBC_COLUMN_NAME:
                            toElement.value = fromElement.value;
                            break;
                    }
                    found = true;
                    newExts.add(toElement);
                    break;
                }
            }

            if (!found) {
                newExts.add(fromElement);
            }
        }

        JdoExtension[] result = new JdoExtension[newExts.size()];
        newExts.toArray(result);
        return result;
    }

    public static void synchronize3(JdoExtension[] from, JdoExtension[] to,
            Set exclude, boolean errorOnExclude) {
        if (from == null) return;
        if (to == null) return;

        for (int i = 0; i < from.length; i++) {
            JdoExtension fromElement = from[i];
            for (int j = 0; j < to.length; j++) {
                JdoExtension toElement = to[j];
                if (fromElement.key == toElement.key) {
                    if (exclude.contains(toKeyString(fromElement.key))) {
                        if (errorOnExclude) {
                            throw BindingSupportImpl.getInstance().invalidOperation("");
                        }
                        return;
                    }
                    if (fromElement.value != null) {
                        if (!fromElement.value.equals(toElement.value)) {
                            toElement.value = fromElement.value;
                        }
                    }
                    synchronize3(fromElement.nested, toElement.nested, exclude, errorOnExclude);
                }
            }
        }
    }

    public static JdoExtension[] synchronize4(JdoExtension[] from, JdoExtension[] to, Set ignore) {
        if (from == null) return to;

        List toAdd = new ArrayList();
        for (int i = 0; i < from.length; i++) {
            JdoExtension fromElement = from[i];
            if (!ignore.isEmpty()
                    && ignore.contains(JdoExtension.toKeyString(fromElement.key))) {
                continue;
            }
            boolean found = false;
            if (to != null) {
                for (int j = 0; j < to.length; j++) {
                    JdoExtension toElement = to[j];
                    if (fromElement.key == toElement.key) {
                        found = true;
                        if (fromElement.value != null) {
                            if (!fromElement.value.equals(toElement.value)) {
                                toElement.value = fromElement.value;
                            }
                        }
                        toElement.nested = synchronize4(fromElement.nested, toElement.nested, ignore);
                    }
                }
            }
            if (!found) {
                toAdd.add(fromElement);
            }
        }
        if (!toAdd.isEmpty()) {
            if (to != null) {
                for (int i = 0; i < to.length; i++) {
                    toAdd.add(to[i]);
                }
            }
            JdoExtension tmp[] = new JdoExtension[toAdd.size()];
            toAdd.toArray(tmp);
            return tmp;
        }
        return to;
    }

    public boolean isFieldAttribute() {
        if (key == JdoExtensionKeys.DEFAULT_FETCH_GROUP
                || key == JdoExtensionKeys.EMBEDDED
                || key == JdoExtensionKeys.NULL_VALUE) {
            return true;
        }
        return false;
    }

    public static void clearKey(int key, int level, JdoExtension[] exts) {
        if (exts != null) {
            for (int k = 0; k < exts.length; k++) {
                JdoExtension ext1 = exts[k];
                if (ext1.key == key) {
                    ext1.value = null;
                    break;
                } else if (ext1.nested != null) {
                    for (int l = 0; l < ext1.nested.length; l++) {
                        JdoExtension ext2 = ext1.nested[l];
                        if (ext2.key == key) {
                            ext2.key = -1;
                        } else if (ext2.nested != null) {
                            for (int i = 0; i < ext2.nested.length; i++) {
                                JdoExtension ext3 = ext2.nested[i];
                                if (ext3.key == key) {
                                    ext3.key = -1;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

