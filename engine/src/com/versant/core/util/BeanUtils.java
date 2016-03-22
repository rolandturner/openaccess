
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

import com.versant.core.util.classhelper.ClassHelper;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.PrintStream;
import java.io.IOException;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;

/**
 * Static utility methods for working with java beans.
 */
public class BeanUtils {

    private BeanUtils() {
    }

    private static final HashMap PRIMITIVE_NAME_MAP = new HashMap(17);

    static {
        PRIMITIVE_NAME_MAP.put(Long.TYPE.getName(), /*CHFC*/Long.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Integer.TYPE.getName(), /*CHFC*/Integer.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Short.TYPE.getName(), /*CHFC*/Short.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Byte.TYPE.getName(), /*CHFC*/Byte.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Boolean.TYPE.getName(), /*CHFC*/Boolean.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Character.TYPE.getName(), /*CHFC*/Character.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Float.TYPE.getName(), /*CHFC*/Float.TYPE/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Double.TYPE.getName(), /*CHFC*/Double.TYPE/*RIGHTPAR*/);


        PRIMITIVE_NAME_MAP.put(Long.TYPE.getName() + "[]", /*CHFC*/Long[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Integer.TYPE.getName() + "[]", /*CHFC*/Integer[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Short.TYPE.getName() + "[]", /*CHFC*/Short[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Byte.TYPE.getName() + "[]", /*CHFC*/Byte[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Boolean.TYPE.getName() + "[]", /*CHFC*/Boolean[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Character.TYPE.getName() + "[]", /*CHFC*/Character[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Float.TYPE.getName() + "[]", /*CHFC*/Float[].class/*RIGHTPAR*/);
        PRIMITIVE_NAME_MAP.put(Double.TYPE.getName() + "[]", /*CHFC*/Double[].class/*RIGHTPAR*/);

    }

    private static final Class[] STRING_ARGS = new Class[]{/*CHFC*/String.class/*RIGHTPAR*/};
    private static final Class[] INT_ARGS = new Class[]{/*CHFC*/Integer.TYPE/*RIGHTPAR*/};
    private static final Class[] BOOLEAN_ARGS = new Class[]{/*CHFC*/Boolean.TYPE/*RIGHTPAR*/};

    private static final String DEFAULT_PROP_FILE = "versant.properties";

    /**
     * Find and set all properties or public fields on bean from key/value
     * pairs in props. Only int, String and boolean properties and fields may
     * be set. This is a NOP if props or bean is null.
     *
     * @exception IllegalArgumentException if any are invalid
     */
    public static void setProperties(Object bean, Map props)
            throws IllegalArgumentException {
        if (props == null || bean == null) return;
        Class cls = /*CHFC*/bean.getClass()/*RIGHTPAR*/;
        Object[] args = new Object[1];
        for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            String prop = (String)e.getKey();
            String value = (String)e.getValue();
            Field f = findField(prop, cls, value, args);
            if (f != null) {
                try {
                    f.set(bean, args);
                } catch (Throwable x) {
                    if (x instanceof InvocationTargetException) {
                        x = ((InvocationTargetException)x).getTargetException();
                    }
                    throw BindingSupportImpl.getInstance().illegalArgument(
                            x.getClass() + ": " + x.getMessage());
                }
            }
            Method m = findSetMethod(prop, cls, value, args);
            try {
                m.invoke(bean, args);
            } catch (IllegalArgumentException x) {
                throw x;
            } catch (Throwable x) {
                if (x instanceof InvocationTargetException) {
                    x = ((InvocationTargetException)x).getTargetException();
                }
                throw BindingSupportImpl.getInstance().illegalArgument(
                        x.getClass() + ": " + x.getMessage());
            }
        }
    }

    /**
     * Set a property on bean. Only int, String and boolean properties may be
     * set.
     * @exception IllegalArgumentException if invalid
     */
    public static void setProperty(Object bean, String prop, String value)
            throws IllegalArgumentException {
        Class cls = /*CHFC*/bean.getClass()/*RIGHTPAR*/;
        Object[] args = new Object[1];
        Method m = findSetMethod(prop, cls, value, args);
        try {
            m.invoke(bean, args);
        } catch (IllegalArgumentException x) {
            throw x;
        } catch (Throwable x) {
            if (x instanceof InvocationTargetException) {
                x = ((InvocationTargetException)x).getTargetException();
            }
            throw BindingSupportImpl.getInstance().illegalArgument(
                    x.getClass() + ": " + x.getMessage());
        }
    }

    private static Field findField(String prop, Class cls, String value,
            Object[] args) {
        try {
            Field f = cls.getField(prop);
            Class t = f.getType();
            if (t == /*CHFC*/Integer.TYPE/*RIGHTPAR*/ || t == /*CHFC*/Integer.class/*RIGHTPAR*/) {
                args[0] = toInteger(value, prop);
            } else if (t == /*CHFC*/Boolean.TYPE/*RIGHTPAR*/ || t == /*CHFC*/Boolean.class/*RIGHTPAR*/) {
                args[0] = toBoolean(value, prop);
            } else if (t == /*CHFC*/String.class/*RIGHTPAR*/) {
                args[0] = value;
            } else {
                throw BindingSupportImpl.getInstance().internal(
                        "Unsupported field type: " + f);
            }
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Method findSetMethod(String prop, Class cls, String value,
            Object[] args) {
        String name = "set" + Character.toUpperCase(prop.charAt(0)) +
            prop.substring(1);
        Method m;
        try {
            try {
                m = cls.getMethod(name, STRING_ARGS);
                args[0] = value;
            } catch (NoSuchMethodException x) {
                try {
                    m = cls.getMethod(name, INT_ARGS);
                    args[0] = toInteger(value, prop);
                } catch (NoSuchMethodException xx) {
                    m = cls.getMethod(name, BOOLEAN_ARGS);
                    args[0] = toBoolean(value, prop);
                }
            }
        } catch (NoSuchMethodException x) {
            String pos = getClosestProperty(prop,cls);
            String all = getAllPropsFormatted(cls);
            throw BindingSupportImpl.getInstance().illegalArgument(
                "An invalid property '"+ prop +"' was set on " +
                    cls.getName() + ". " +
                (pos != null ?
                    ("The property closely matches '" + pos + "'. ") : "")+
                (all != null ?
                    ("All possible properties are: " +all) : ""));
        }
        return m;
    }

    private static Boolean toBoolean(String value, String prop) {
        if (value.equals("true")) {
            return Boolean.TRUE;
        } else if (value.equals("false")) {
            return Boolean.FALSE;
        } else {
            throw BindingSupportImpl.getInstance().illegalArgument(
                "Expected true or false value for '" +
                prop + "' got '" + value + "'");
        }
    }

    private static Integer toInteger(String value, String prop) {
        try {
            return new Integer(value);
        } catch (NumberFormatException xx) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                "Expected int value for '" + prop + "' got '" + value + "'");
        }
    }

    /**
     * Process a list of command line arguments in [-propertyname value]+
     * format setting each on the bean.
     * @param args Command line arguments as passed to a main method
     * @param expected Names of allowed properties
     * @exception IllegalArgumentException if args are invalid
     */
    public static void setCommandLineArgs(Object bean, String[] args,
            String[] expected) throws IllegalArgumentException {

        int n = args.length;
        Object[] v = new Object[1];
        for (int i = 0; i < n; i++) {
            String s = args[i];
            if (!s.startsWith("-")) {
                throw BindingSupportImpl.getInstance().illegalArgument(
                        "Invalid argument: " + s);
            }
            String prop = s.substring(1);
            int j;
            for (j = expected.length - 1; j >= 0; j--) {
                if (expected[j].equals(prop)) break;
            }
            if (j < 0) {
                throw BindingSupportImpl.getInstance().illegalArgument(
                        "Invalid argument: " + prop);
            }
            if (++i == n) {
                throw BindingSupportImpl.getInstance().illegalArgument(
                        "Expected value for " + prop);
            }
            String value = args[i];
            Method m = findSetMethod(prop, /*CHFC*/bean.getClass()/*RIGHTPAR*/, value, v);
            try {
                m.invoke(bean, v);
            } catch (IllegalArgumentException x) {
                throw x;
            } catch (Throwable x) {
                if (x instanceof InvocationTargetException) {
                    x = ((InvocationTargetException)x).getTargetException();
                }
                throw BindingSupportImpl.getInstance().illegalArgument(
                    "Invalid value for " + prop + ": " +
                    x.getClass().getName() + ": " +
                    x.getMessage());
            }
        }
    }

    /**
     * Add any properties from a semicolon delimited String ps to props.
     */
    public static void parseProperties(String ps, Properties props) {
        StringTokenizer t = new StringTokenizer(ps, "=", false);
        for (;;) {
            String key;
            try {
                key = t.nextToken("=");
                if (key.startsWith(";")) key = key.substring(1);
            } catch (NoSuchElementException e) {
                break;
            }
            try {
                String value = t.nextToken(";").substring(1);
                props.put(key, value);
            } catch (NoSuchElementException e) {
                throw BindingSupportImpl.getInstance().runtime(
                        "Expected semicolon delimited property=value pairs: '" +
                        ps + "'");
            }
        }
    }

    /**
     * Parse semicolon delimited properties from props and set them on bean.
     * This can handle String, int and boolean properties.
     * @exception IllegalArgumentException on errors
     */
    public static void parseProperties(String props, Object bean) {
        if (props == null || props.length() == 0) return;
        Class cls = /*CHFC*/bean.getClass()/*RIGHTPAR*/;
        Object[] args = new Object[1];
        int last = 0;
        for (;;) {
            int i = props.indexOf('=', last);
            if (i < 0) {
                throw BindingSupportImpl.getInstance().illegalArgument(
                    "Expected property name at position " + last + ": " + props);
            }
            String key = props.substring(last, i);
            int j = props.indexOf(';', ++i);
            String value;
            if (j < 0) value = props.substring(i);
            else value = props.substring(i, j);
            Method m = findSetMethod(key, cls, value, args);
            try {
                m.invoke(bean, args);
            } catch (Exception e) {
                Throwable t;
                if (e instanceof InvocationTargetException) {
                    t = ((InvocationTargetException)e).getTargetException();
                } else {
                    t = e;
                }
                throw BindingSupportImpl.getInstance().runtime(
                    "Error setting property '" + key + "' to '" + value + "'",
                    t);
            }
            if (j < 0) break;
            last = j + 1;
        }
    }

    /**
     * Convert a primitive name (int, byte etc.) to a class or null if the
     * name is not a primitive.
     */
    public static Class toClass(String primitiveName) {
        return (Class)PRIMITIVE_NAME_MAP.get(primitiveName);
    }

    /**
     * Load the class with name using loaded. The name may be a primitive
     * (int, byte etc) or a single dimensional array (e.g. int[]).
     */
    public static Class loadClass(String name, boolean initialize,
            ClassLoader loader) throws ClassNotFoundException {
        Class ans = toClass(name);
        if (ans == null) {
            int i = name.indexOf("[]");
            if (i >= 0) {
                name = "[L" + name.substring(0, i);
            }
            ans = ClassHelper.get().classForName(name, initialize, loader);
        }
        return ans;
    }

    /**
     * Create a new instance of cname using loader. It must be an instance of
     * mustBe.
     */
    public static Object newInstance(String cname, ClassLoader loader, Class mustBe) {
        try {
            Class cls = ClassHelper.get().classForName(cname, true, loader);
            if (!mustBe.isAssignableFrom(cls)) {
                throw BindingSupportImpl.getInstance().runtime(
                        cname + " is not a " + mustBe.getName());
            }
            return cls.newInstance();
        } catch (Exception e) {
        	if( BindingSupportImpl.getInstance().isOwnException(e) )
        	{
        		throw (RuntimeException)e;	
        	}
            throw BindingSupportImpl.getInstance().runtime(
                    "Unable to create instance of " + cname +  ": " +
                    e.getMessage(), e);
        }
    }

    /**
     * Get the value of a property of bean.
     */
    public static Object getPropertyValue(Object bean, String property)
            throws Exception {
        Class cls = /*CHFC*/bean.getClass()/*RIGHTPAR*/;
        property = Character.toUpperCase(property.charAt(0)) +
                property.substring(1);
        Method m;
        try {
            m = cls.getMethod("get" + property, null);
        } catch (NoSuchMethodException e) {
            m = cls.getMethod("is" + property, null);
        }
        return m.invoke(bean, null);
    }

    /**
     * Fill o by introspecting all of its public non-static, non-final fields
     * that are int, Integer, boolean, Boolean or String and looking for
     * properties named prefix + fieldname to populate them.
     */
    public static void fillFields(Object o, String prefix, Map props,
            Set fieldsToIgnore) {
        Class cls = /*CHFC*/o.getClass()/*RIGHTPAR*/;
        Field[] a = cls.getFields();
        for (int i = 0; i < a.length; i++) {
            Field f = a[i];
            int m = f.getModifiers();
            if (Modifier.isFinal(m) || Modifier.isStatic(m)
                    || !isSupportedFieldType(f.getType())) {
                continue;
            }
            String name = f.getName();
            if (fieldsToIgnore != null && fieldsToIgnore.contains(name)) {
                continue;
            }
            String prop = prefix + name;
            String value = (String)props.get(prop);
            if (value == null) {
                continue;
            }
            setFieldValue(f, o, value, prop);
        }
    }

    public static boolean isSupportedFieldType(Class t) {
        return t == /*CHFC*/Integer.TYPE/*RIGHTPAR*/ || t == /*CHFC*/Integer.class/*RIGHTPAR*/
            || t == /*CHFC*/Boolean.TYPE/*RIGHTPAR*/ || t == /*CHFC*/Boolean.class/*RIGHTPAR*/
            || t == /*CHFC*/String.class/*RIGHTPAR*/;
    }

    /**
     * Set field f on o converting value to the correct type for the field.
     * Fields of int, Integer, boolean, Boolean and String are suppported.
     */
    private static void setFieldValue(Field f, Object o, String value,
            String name) {
        Class t = f.getType();
        Object arg;
        if (t == /*CHFC*/Integer.TYPE/*RIGHTPAR*/ || t == /*CHFC*/Integer.class/*RIGHTPAR*/) {
            arg = toInteger(value, name);
        } else if (t == /*CHFC*/Boolean.TYPE/*RIGHTPAR*/ || t == /*CHFC*/Boolean.class/*RIGHTPAR*/) {
            arg = toBoolean(value, name);
        } else if (t == /*CHFC*/String.class/*RIGHTPAR*/) {
            arg = value;
        } else {
            throw BindingSupportImpl.getInstance().illegalArgument(
                    "Unsupported field type: " + f);
        }
        try {
            f.set(o, arg);
        } catch (Throwable x) {
            if (x instanceof InvocationTargetException) {
                x = ((InvocationTargetException)x).getTargetException();
            }
            throw BindingSupportImpl.getInstance().illegalArgument(
                    x.getClass() + ": " + x.getMessage());
        }
    }

    /**
     * The result of a call to processCmdline.
     */
    public static class CmdLineResult {
        public HashSet fieldsFilled = new HashSet();
        public Properties properties;
    }

    /**
     * <p>Process command line arguments. Arguments starting with a dash are
     * expected to match the names of public fields in o (like
     * {@link #fillFields}) with non-boolean fields requiring an argument.
     * If a boolean field does not have an argument then it is set to true.</p>
     *
     * <p>If hasProps is true then there is an implicit -p arguement to
     * specify the resource name or filename of a properties file to load and
     * return. The default name is versant.properties. The argument is first
     * treated as a file and if this fails then as a resource. System
     * properties starting with "versant." or "javax.jdo.option." are added
     * after the properties are loaded. Finally command line arguments of the
     * form key=value are added i.e. they override any previous value.</p>
     *
     * <p>Errors are printed to System.err followed by usage information and
     * an IllegalArgumentException is thrown.</p>
     *
     * <p>If one of the args is /? or -help or --help then the usage is printed
     * and an IllegalArgumentException is thrown.</p>
     *
     * @return The fieldsFilled set contains the names of all fields that were
     * set from the command line. If hasProps is true then the properties field
     * on the result holds all properties with overrides already applied
     * otherwise it is null.
     */
    public static CmdLineResult processCmdLine(String[] args, ClassLoader loader,
            Object o, String[] requiredFields, String toolName,
            String toolDescription, boolean hasProps) {
        try {
            CmdLineResult res = new CmdLineResult();
            Properties overrides = hasProps ? new Properties() : null;
            String propFileName = DEFAULT_PROP_FILE;
            for (int i = 0; i < args.length; ) {
                String arg = args[i++];
                if (arg.equals("-p")) {
                    if (i >= args.length) {
                        throw BindingSupportImpl.getInstance().illegalArgument(
                                 "Expected file or resource name for -p");
                    }
                    propFileName = args[i++];
                } else if (arg.equals("/?") || arg.equals("-help")
                        || arg.equals("--help")) {
                    throw BindingSupportImpl.getInstance().illegalArgument("");
                } else if (arg.startsWith("-")) {
                    String key = arg.substring(1);
                    if (key.length() == 0) {
                        throw BindingSupportImpl.getInstance().illegalArgument(
                                "Expected name of argument after '-'");
                    }
                    try {
                        Class tmp = /*CHFC*/o.getClass()/*RIGHTPAR*/;
                        Field f = tmp.getField(key);
                        Class t = f.getType();
                        int m = f.getModifiers();
                        if (Modifier.isFinal(m) || Modifier.isStatic(m)
                                || !isSupportedFieldType(t)) {
                            throw new NoSuchFieldException();
                        }
                        String value = i < args.length ? args[i] : null;
                        if (value.startsWith("-")) {
                            value = null;
                        }
                        if (value == null) {
                            if (t == /*CHFC*/Boolean.class/*RIGHTPAR*/ || t == /*CHFC*/Boolean.TYPE/*RIGHTPAR*/) {
                                value = "true";
                            } else {
                                throw BindingSupportImpl.getInstance().illegalArgument(
                                        "Expected value for " + arg);
                            }
                        } else {
                            ++i;
                        }
                        setFieldValue(f, o, value, arg);
                        res.fieldsFilled.add(f.getName());
                    } catch (NoSuchFieldException e) {
                        throw BindingSupportImpl.getInstance().illegalArgument(
                                "Unknown option: " + arg);
                    }
                } else {
                    int pos = hasProps ? arg.indexOf('=') : -1;
                    if (pos <= 0) {
                        throw BindingSupportImpl.getInstance().illegalArgument(
                                "Invalid argument: " + arg);
                    }
                    String key = arg.substring(0, i);
                    String value = arg.substring(i + 1);
                    overrides.put(key, value);
                }
            }
            if (hasProps) {
                Properties p;
                try {
                    p = PropertiesLoader.loadProperties(propFileName);
                    if (p == null) {
                        p = PropertiesLoader.loadProperties(loader, propFileName);
                        if (p == null) {
                            throw BindingSupportImpl.getInstance().illegalArgument(
                                    "File or resource not found: '" +
                                    propFileName + "'");
                        }
                    }
                } catch (IOException e) {
                    throw BindingSupportImpl.getInstance().illegalArgument(
                            "Error loading " + propFileName);
                }
                Properties sp = System.getProperties();
                for (Iterator i = sp.keySet().iterator(); i.hasNext(); ) {
                    String key = (String)i.next();
                    if (key.startsWith("versant.")
                            || key.startsWith("javax.jdo.option.")) {
                        String value = sp.getProperty(key);
                        System.err.println("Using system property: " +
                                key + "=" + value);
                        p.put(key, value);
                    }
                }
                for (Iterator i = overrides.keySet().iterator(); i.hasNext(); ) {
                    String key = (String)i.next();
                    String value = overrides.getProperty(key);
                    System.err.println("Command line override: " + key + "=" + value);
                    p.put(key, value);
                }
                res.properties = p;
            }
            return res;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println();
            printUsage(o, requiredFields, toolName, toolDescription, hasProps,
                    System.err);
            throw e;
        }
    }

    /**
     * <p>Print command line usage information by introspecting o to find all
     * of its public non-static, non-final fields that are int, Integer,
     * boolean, Boolean or String. For each field a public static final
     * String field named HELP_fieldname is used to provided a description
     * of the field if present. The values of the fields in o are assumed
     * to be the defaults.</p>
     *
     * <p>If hasProps is true the information on how to specify key=value
     * properties is also printed.</p>
     */
    public static void printUsage(Object o, String[] requiredFields,
            String toolName, String toolDescription, boolean hasProps,
            PrintStream out) {
        Class tmp = /*CHFC*/o.getClass()/*RIGHTPAR*/;
        Field[] all = tmp.getFields();

        // find all of the help text
        HashMap helpMap = new HashMap();
        for (int i = 0; i < all.length; i++) {
            Field f = all[i];
            int m = f.getModifiers();
            if (!(Modifier.isFinal(m) && Modifier.isStatic(m))
                    || f.getType() != /*CHFC*/String.class/*RIGHTPAR*/) {
                continue;
            }
            String name = f.getName();
            if (name.startsWith("HELP_")) {
                try {
                    helpMap.put(name.substring(5), f.get(null));
                } catch (IllegalAccessException e) {
                    throw BindingSupportImpl.getInstance().internal(
                            e.toString(), e);
                }
            }
        }

        // map all the options to their default values
        HashMap defaults = new HashMap();
        ArrayList options = new ArrayList();
        HashMap fields = new HashMap();
        int longestOp = 0;
        if (hasProps) {
            String s = "-p " + DEFAULT_PROP_FILE;
            longestOp = s.length();
        }
        for (int i = 0; i < all.length; i++) {
            Field f = all[i];
            int m = f.getModifiers();
            if (Modifier.isFinal(m) || Modifier.isStatic(m)
                    || !isSupportedFieldType(f.getType())) {
                continue;
            }
            String op = f.getName();
            options.add(op);
            fields.put(op, f);
            String def;
            try {
                Object v = f.get(o);
                def = v == null ? null : v.toString();
            } catch (IllegalAccessException e) {
                throw BindingSupportImpl.getInstance().internal(
                        e.toString(), e);
            }
            String s = "-" + op;
            if (def != null) {
                defaults.put(op, def);
                s = s + " " + def;
            }
            if (s.length() > longestOp) {
                longestOp = s.length();
            }
        }

        // now print everything
        out.println("Versant Open Access " + Debug.VERSION + " " + toolName);
        out.println("Usage: " + toolName +
                (hasProps ? " -p <property file or resource>" : "") +
                " [OPTION] ... " +
                (hasProps ? " [property=value] ..." : ""));
        out.println(toolDescription);
        out.println();
        HashSet requiredSet = new HashSet();
        int rfc = requiredFields == null ? 0 : requiredFields.length;
        if (rfc > 0) {
            requiredSet.addAll(Arrays.asList(requiredFields));
            out.println("Required arguements:");
            for (int i = 0; i < requiredFields.length; i++) {
                printOption(requiredFields[i], fields, null, helpMap,
                        longestOp, out);
            }
        }
        int n = options.size();
        if (n - rfc > 0 || hasProps) {
            out.println("Optional arguements with default values:");
            if (hasProps) {
                printOption("p", DEFAULT_PROP_FILE,
                        "Name of file or resource to load properties from",
                        longestOp, out);
            }
            for (int i = 0; i < n; i++) {
                String op = (String)options.get(i);
                if (!requiredSet.contains(op)) {
                    printOption(op, fields, defaults, helpMap, longestOp, out);
                }
            }
            out.println();
        }
        if (hasProps) {
            out.println("Use property=value pairs to override properties " +
                    "including properties set from System properties.");
            out.println();
        }
    }

    private static void printOption(String op, HashMap fields,
            HashMap defaults, HashMap helpMap, int longestOp, PrintStream out) {
        Field f = (Field)fields.get(op);
        String def = defaults == null ? null : (String)defaults.get(op);
        String help = (String)helpMap.get(op);
        if (help == null) {
            Class t = f.getType();
            if (t == /*CHFC*/Integer.class/*RIGHTPAR*/ || t == /*CHFC*/Integer.TYPE/*RIGHTPAR*/) {
                help = "int";
            } else if (t == /*CHFC*/Boolean.class/*RIGHTPAR*/ || t == /*CHFC*/Boolean.TYPE/*RIGHTPAR*/) {
                help = "true|false";
            } else if (t == /*CHFC*/String.class/*RIGHTPAR*/) {
                help = "string";
            } else {
                help = "...";
            }
        }
        printOption(op, def, help, longestOp, out);
    }

    private static void printOption(String op, String def, String help,
            int longestOp, PrintStream out) {
        StringBuffer b = new StringBuffer();
        b.append('-');
        b.append(op);
        if (def != null) {
            b.append(' ');
            b.append(def);
        }
        for (int j = longestOp - b.length() + 2; j > 0; j--) {
            b.append(' ');
        }
        b.append(help);
        out.println(b.toString());
    }

    /**
     * Get the closest property that can be set to this s, or return null, if
     * they are too diffrent.
     */
    private static String getClosestProperty(String s, Class cls) {
        Method[] methods = cls.getMethods();
        int j = 0;
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.startsWith("set") && methodName.length() > 3) {
                Class params[] = methods[i].getParameterTypes();
                if (params.length != 0 && !params[0].isArray()) {
                    j++;
                }
            }

        }
        if (j == 0) {
            return null;
        }
        String[] targets = new String[j];
        j = 0;
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.startsWith("set") && methodName.length() > 3) {
                Class params[] = methods[i].getParameterTypes();
                if (params.length != 0 && !params[0].isArray()) {
                    targets[j] = Character.toLowerCase(methodName.charAt(3)) +
                            (methodName.length() > 4 ? methodName.substring(4) :
                            "");
                    j++;
                }
            }
        }

        return getClosest(s,targets,0.25f);
    }

    /**
     * Does this class contain this property
     */
    private static boolean containsProperties(Class cls, String prop){
        return getAllProperties(cls).contains(prop);
    }

    /**
     * Get all the valid properties for this class.
     */
    public static Set getAllProperties(Class cls) {
        Method[] methods = cls.getMethods();
        Set set = new HashSet();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.startsWith("set") && methodName.length() > 3) {
                String prop = Character.toLowerCase(methodName.charAt(3)) +
                        (methodName.length() > 4 ? methodName.substring(4) : "");
                set.add(prop);
            }

        }

        Field fields[] = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            String prop = Character.toLowerCase(fieldName.charAt(0)) +
                    (fieldName.length() > 1 ? fieldName.substring(1) : "");

            set.add(prop);
        }
        return set;

    }

    /**
     * format the properties.
     */
    private static String getAllPropsFormatted(Class cls) {
        Set set = getAllProperties(cls);
        if (set.isEmpty()){
            return null;
        } else {
            ArrayList list = new ArrayList(set);
            Collections.sort(list);
            StringBuffer buff = new StringBuffer();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                String name = (String) iter.next();

                buff.append(name);
                buff.append(';');
            }
            return buff.toString();
        }
    }

    /**
     * This method uses a Levenshtein Distance algorithm to find the closest
     * match.
     */
    private static String getClosest(String s, String[] targets, float diffs) {
        if (s == null || targets == null) {
            return null;
        }

        HashMap map = new HashMap();
        for (int i = 0; i < targets.length; i++) {
            map.put(targets[i], targets[i].toUpperCase());
        }
        s = s.toUpperCase();
        int n = s.length(); // length of s

        float srcLenght = (float) n;

        float currentWeight = 1;
        String currentString = null;
        Set set = map.keySet();
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            String origianal = (String) iter.next();
            String t = (String) map.get(origianal);


            int m = t.length(); // length of t


            int p[] = new int[n + 1]; //'previous' cost array, horizontally
            int d[] = new int[n + 1]; // cost array, horizontally
            int _d[]; //placeholder to assist in swapping p and d

            // indexes into strings s and t
            int i; // iterates through s
            int j; // iterates through t

            char t_j; // jth character of t

            int cost; // cost

            for (i = 0; i <= n; i++) {
                p[i] = i;
            }

            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                d[0] = j;

                for (i = 1; i <= n; i++) {
                    cost = s.charAt(i - 1) == t_j ? 0 : 1;
                    // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                    d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
                }

                // copy current distance counts to 'previous row' distance counts
                _d = p;
                p = d;
                d = _d;
            }

            // our last action in the above loop was to switch d and p, so p now
            // actually has the most recent cost counts
            float diff = ((float) p[n]) / srcLenght;

            if (currentWeight > diff) {
                currentWeight = diff;
                currentString = origianal;
            }
        }

        if (currentWeight <= diffs) {
            return currentString;
        } else {
            return null;
        }
    }
}
