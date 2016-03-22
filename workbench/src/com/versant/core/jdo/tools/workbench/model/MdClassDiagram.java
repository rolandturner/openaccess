
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

import com.versant.core.common.config.ConfigParser;
import com.versant.core.util.StringList;
import com.versant.core.util.StringListParser;
import com.versant.core.util.StringList;

import java.util.*;
import java.io.IOException;
import java.awt.*;

/**
 * Information about a class diagram that is stored in the .jdogenie
 * project file and extra tempory info used by ClassGraph.
 */
public class MdClassDiagram {

    private MdProject project;
    private ArrayList linesFromLoad;
    private Map infoMap = new HashMap();
    // MdClass -> ClassInfo, GraphTable -> TableInfo
    private boolean dirty;
    private Settings settings = new Settings();
    private int legendX = 600, legendY = 460;

    private static final String LEGEND = ".legend";

    public MdClassDiagram(MdProject project) {
        this.project = project;
    }

    /**
     * Copy constructor.
     */
    public MdClassDiagram(MdClassDiagram s) {
        project = s.project;
        settings.fillFrom(s.settings);
        settings.setName("Copy of " + s.settings.getName());
        legendX = s.legendX;
        legendY = s.legendY;
        if (s.linesFromLoad != null) {
            linesFromLoad = (ArrayList)s.linesFromLoad.clone();
        } else {
            for (Iterator i = s.infoMap.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry)i.next();
                Object o = e.getValue();
                if (o instanceof ClassInfo) {
                    o = new ClassInfo((ClassInfo)o);
                } else {
                    o = new TableInfo((TableInfo)o);
                }
                infoMap.put(e.getKey(), o);
            }
        }
    }

    public MdProject getProject() {
        return project;
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Add class to diagram.
     *
     * @throws IllegalArgumentException if any are already in diagram
     */
    public ClassInfo addClass(MdClass mdClass) {
        if (infoMap.containsKey(mdClass)) {
            throw new IllegalArgumentException("Class already in diagram: " +
                    mdClass);
        }
        ClassInfo ci = new ClassInfo(mdClass);
        ci.setOrder(infoMap.size());
        infoMap.put(mdClass, ci);
        makeDirty();
        return ci;
    }

    /**
     * Remove a class from the diagram.
     *
     * @throws IllegalArgumentException if any are not in diagram
     */
    public ClassInfo removeClass(MdClass mdClass) {
        ClassInfo ans = (ClassInfo)infoMap.remove(mdClass);
        makeDirty();
        return ans;
    }

    /**
     * Add table to diagram. This is a NOP and returns TableInfo if the table
     * is already in the diagram. Otherwise a new TableInfo instance is
     * returned.
     */
    public TableInfo addTable(GraphTable table) {
        TableInfo ti = (TableInfo)infoMap.get(table);
        if (ti == null) {
            ti = new TableInfo(table);
            ti.setOrder(infoMap.size());
            infoMap.put(table, ti);
            makeDirty();
        }
        return ti;
    }

    /**
     * Remove a table from the diagram.
     */
    public TableInfo removeTable(GraphTable table) {
        TableInfo ans = (TableInfo)infoMap.remove(table);
        makeDirty();
        return ans;
    }

    /**
     * Remove all tables and classes from the diagram.
     */
    public void clear() {
        infoMap.clear();
        makeDirty();
    }

    /**
     * Save everything to p.
     */
    public void save(PropertySaver p, String base) throws IOException {
        finishLoad();
        p.add(base + ConfigParser.DIAGRAM_NAME, settings.getName());
        StringList list = new StringList();
        list.append(legendX);
        list.append(legendY);
        p.add(base + LEGEND, list.toString());
        settings.save(p, base);
        ArrayList a = new ArrayList(infoMap.values());
        Collections.sort(a);
        int n = a.size();
        p.add(base + ConfigParser.DIAGRAM_CLASS + ConfigParser.DIAGRAM_COUNT,
                n);
        for (int i = 0; i < n; i++) {
            ClassInfo ci = (ClassInfo)a.get(i);
            list.reset();
            list.append(ci.getMdClass().getQName());
            ci.save(list);
            p.add(base + ConfigParser.DIAGRAM_CLASS + i, list.toString());
        }
    }

    /**
     * Load everything from p. This does not lookup the classes as they
     * may not be available in the project yet. It keeps the property
     * info for each class for finishLoad.
     *
     * @see #finishLoad
     */
    public void load(Properties p, String base) {
        settings.setName(p.getProperty(base + ConfigParser.DIAGRAM_NAME));
        StringListParser ps = new StringListParser(
                p.getProperty(base + LEGEND, "0,0"));
        legendX = ps.nextInt();
        legendY = ps.nextInt();
        settings.load(p, base);
        infoMap.clear();
        String cbase = base + ConfigParser.DIAGRAM_CLASS;
        int n = getInt(p, cbase + ConfigParser.DIAGRAM_COUNT, 50);
        linesFromLoad = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            String line = p.getProperty(cbase + i);
            if (line != null) linesFromLoad.add(line);
        }
    }

    /**
     * Finish the loading process. This cannot be done at load time as
     * the project will not have initialized its classes yet. This is a NOP
     * if load has already been finished.
     *
     * @see #load
     */
    public void finishLoad() {
        if (linesFromLoad == null) return;
        infoMap.clear();
        int n = linesFromLoad.size();
        StringListParser parser = new StringListParser();
        for (int i = 0; i < n; i++) {
            parser.setString((String)linesFromLoad.get(i));
            String qname = parser.nextString();
            MdClass mdc = project.findClass(qname);
            if (mdc == null) continue;
            ClassInfo ci = new ClassInfo(mdc);
            ci.load(parser);
            infoMap.put(ci.getMdClass(), ci);
        }
        linesFromLoad = null;
    }

    private static int getInt(Properties p, String s, int def) {
        String v = p.getProperty(s);
        if (v == null) return def;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public String getName() {
        return settings.getName();
    }

    public void setName(String name) {
        settings.setName(name);
        makeDirty();
    }

    public int getLegendY() {
        return legendY;
    }

    public void setLegendY(int legendY) {
        this.legendY = legendY;
    }

    public int getLegendX() {
        return legendX;
    }

    public void setLegendX(int legendX) {
        this.legendX = legendX;
    }

    public ClassInfo findClassInfo(MdClass mdClass) {
        return (ClassInfo)infoMap.get(mdClass);
    }

    public ClassInfo findClassInfo(String qname) {
        MdClass mdc = project.findClass(qname);
        if (mdc == null) return null;
        return findClassInfo(mdc);
    }

    public TableInfo findTableInfo(GraphTable table) {
        return (TableInfo)infoMap.get(table);
    }

    /**
     * Get Info's for all the things on the diagram sorted according to
     * their order properties.
     */
    public Collection getInfos() {
        ArrayList a = new ArrayList(infoMap.values());
        Collections.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                Info a = (Info)o1;
                Info b = (Info)o2;
                return a.order - b.order;
            }
        });
        return a;
    }

    /**
     * Get all ClassInfo's.
     */
    public Iterator getClassInfos() {
        ArrayList a = new ArrayList();
        for (Iterator i = infoMap.values().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof ClassInfo) a.add(o);
        }
        return a.iterator();
    }

    /**
     * Get all the MdClass'es.
     */
    public Set getClasses() {
        HashSet a = new HashSet();
        for (Iterator i = infoMap.keySet().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof MdClass) a.add(o);
        }
        return a;
    }

    private void makeDirty() {
        dirty = true;
    }

    public void clearDirty() {
        dirty = false;
    }

    public String toString() {
        return super.toString() + " " + getName();
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * Dump debugging info to System.out.
     */
    public void dump() {
        System.out.println(this);
        if (linesFromLoad != null) {
            System.out.println("finishLoad not called yet");
            int n = linesFromLoad.size();
            for (int i = 0; i < n; i++) {
                System.out.println(linesFromLoad.get(i));
            }
        } else {
            for (Iterator i = getClassInfos(); i.hasNext();) {
                System.out.println(i.next());
            }
        }
        System.out.println("---");
    }

    /**
     * Info on something we keep on the diagram. This makes the diagram dirty
     * when its properties change.
     */
    public abstract class Info implements Comparable {

        private int x, y;
        private int order;
        private boolean under;

        public Info() {
        }

        /**
         * Copy constructor.
         */
        public Info(Info s) {
            x = s.x;
            y = s.y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
            makeDirty();
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
            makeDirty();
        }

        public Rectangle getRect() {
            return new Rectangle(x, y, 0, 0);
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public boolean isUnder() {
            return under;
        }

        /**
         * Place under previous info when doing autolayout on the graph
         * instead of to the right.
         */
        public void setUnder(boolean under) {
            this.under = under;
        }

        public void save(StringList l) {
            l.append(x);
            l.append(y);
        }

        public void load(StringListParser p) {
            x = p.nextInt();
            y = p.nextInt();
        }

        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    /**
     * Info we keep about a class on the diagram. This makes the diagram dirty
     * when its properties change.
     */
    public class ClassInfo extends Info {

        private MdClass mdClass;

        public ClassInfo(MdClass mdClass) {
            this.mdClass = mdClass;
        }

        /**
         * Copy constructor.
         */
        public ClassInfo(ClassInfo s) {
            super(s);
            mdClass = s.mdClass;
        }

        public MdClass getMdClass() {
            return mdClass;
        }

        public String toString() {
            return mdClass.getName() + super.toString();
        }

        public int compareTo(Object o) {
            ClassInfo a = (ClassInfo)o;
            return mdClass.getQName().compareTo(a.mdClass.getQName());
        }
    }

    /**
     * Info we keep about a table on the diagram. This makes the diagram dirty
     * when its properties change.
     */
    public class TableInfo extends Info {

        private GraphTable table;

        public TableInfo(GraphTable table) {
            this.table = table;
        }

        /**
         * Copy constructor.
         */
        public TableInfo(TableInfo s) {
            super(s);
            table = s.table;
        }

        public GraphTable getTable() {
            return table;
        }

        public String toString() {
            return table.getName() + super.toString();
        }

        public int compareTo(Object o) {
            TableInfo a = (TableInfo)o;
            return table.getName().compareTo(a.table.getName());
        }
    }

    /**
     * General settings for a diagram.
     */
    public static class Settings {

        private String name;
        private int style = 13;
        private boolean showLegend = true;
        private boolean printHeader = true;

        private boolean className = true;
        private boolean classPackage;
        private boolean classTable = true;
        private boolean classTableFirst;
        private boolean classPseudoFields;
        private boolean classSimpleFields = true;
        private boolean classCache;
        private boolean classDeleteOrphans = true;
        private boolean classDatastore;
        private boolean classClassID;
        private boolean classJDBCClassID;
        private boolean classDoNotCreateTable;
        private boolean classKeyGenerator;
        private boolean classOptLocking;
        private boolean classUseJoin;
        private boolean classJdoFile;
        private boolean classObjectIdClass;

        private boolean fieldName = true;
        private boolean fieldType = true;
        private boolean fieldColumnName;
        private boolean fieldJDBCType;
        private boolean fieldSQLType;
        private boolean fieldShowLinkTables;

        private static final String KEY_GENERAL = "general";
        private static final String KEY_CLASS = "class";
        private static final String KEY_FIELD = "field";

        // redefine constants here for GUI framework statics painter and editor
        public static final int STYLE_ORTHOGONAL = 11;
        public static final int STYLE_QUADRATIC = 12;
        public static final int STYLE_BEZIER = 13;

        public Settings() {
        }

        public Settings(Settings src) {
            fillFrom(src);
        }

        /**
         * Fill all our fields from s.
         */
        public void fillFrom(Settings s) {
            name = s.name;
            style = s.style;
            showLegend = s.showLegend;
            printHeader = s.printHeader;

            className = s.className;
            classPackage = s.classPackage;
            classTable = s.classTable;
            classTableFirst = s.classTableFirst;
            classPseudoFields = s.classPseudoFields;
            classSimpleFields = s.classSimpleFields;
            classCache = s.classCache;
            classDeleteOrphans = s.classDeleteOrphans;
            classDatastore = s.classDatastore;
            classClassID = s.classClassID;
            classJDBCClassID = s.classJDBCClassID;
            classDoNotCreateTable = s.classDoNotCreateTable;
            classKeyGenerator = s.classKeyGenerator;
            classOptLocking = s.classOptLocking;
            classUseJoin = s.classUseJoin;
            classJdoFile = s.classJdoFile;
            classObjectIdClass = s.classObjectIdClass;

            fieldName = s.fieldName;
            fieldType = s.fieldType;
            fieldColumnName = s.fieldColumnName;
            fieldJDBCType = s.fieldJDBCType;
            fieldSQLType = s.fieldSQLType;
            fieldShowLinkTables = s.fieldShowLinkTables;
        }

        private String encodeGeneral() {
            StringList l = new StringList();
            l.append(style);
            l.append(showLegend);
            l.append(printHeader);
            return l.toString();
        }

        private void decodeGeneral(String s) {
            if (s == null || s.length() == 0) return;
            StringListParser p = new StringListParser(s);
            try {
                style = p.nextInt();
                showLegend = p.nextBoolean();
                printHeader = p.nextBoolean();
            } catch (IllegalStateException e) {
                System.out.println(e.toString());
            }
        }

        private String encodeClass() {
            StringList l = new StringList();
            l.append(className);
            l.append(classPackage);
            l.append(classTable);
            l.append(classTableFirst);
            l.append(classPseudoFields);
            l.append(classSimpleFields);
            l.append(classCache);
            l.append(classDeleteOrphans);
            l.append(classDatastore);
            l.append(classClassID);
            l.append(classJDBCClassID);
            l.append(classDoNotCreateTable);
            l.append(classKeyGenerator);
            l.append(classOptLocking);
            l.append(classUseJoin);
            l.append(classJdoFile);
            l.append(classObjectIdClass);
            return l.toString();
        }

        private void decodeClass(String s) {
            if (s == null || s.length() == 0) return;
            StringListParser p = new StringListParser(s);
            try {
                className = p.nextBoolean();
                classPackage = p.nextBoolean();
                classTable = p.nextBoolean();
                classTableFirst = p.nextBoolean();
                classPseudoFields = p.nextBoolean();
                classSimpleFields = p.nextBoolean();
                classCache = p.nextBoolean();
                classDeleteOrphans = p.nextBoolean();
                classDatastore = p.nextBoolean();
                classClassID = p.nextBoolean();
                classJDBCClassID = p.nextBoolean();
                classDoNotCreateTable = p.nextBoolean();
                classKeyGenerator = p.nextBoolean();
                classOptLocking = p.nextBoolean();
                classUseJoin = p.nextBoolean();
                classJdoFile = p.nextBoolean();
                classObjectIdClass = p.nextBoolean();
            } catch (IllegalStateException e) {
                System.out.println(e.toString());
            }
        }

        private String encodeField() {
            StringList l = new StringList();
            l.append(fieldName);
            l.append(fieldType);
            l.append(fieldColumnName);
            l.append(fieldJDBCType);
            l.append(fieldSQLType);
            l.append(fieldShowLinkTables);
            return l.toString();
        }

        private void decodeField(String s) {
            if (s == null || s.length() == 0) return;
            StringListParser p = new StringListParser(s);
            try {
                fieldName = p.nextBoolean();
                fieldType = p.nextBoolean();
                fieldColumnName = p.nextBoolean();
                fieldJDBCType = p.nextBoolean();
                fieldSQLType = p.nextBoolean();
                fieldShowLinkTables = p.nextBoolean();
            } catch (IllegalStateException e) {
                System.out.println(e.toString());
            }
        }

        public void save(PropertySaver p, String base) throws IOException {
            base = base + ".";
            p.add(base + KEY_GENERAL, encodeGeneral());
            p.add(base + KEY_CLASS, encodeClass());
            p.add(base + KEY_FIELD, encodeField());
        }

        public void load(Properties p, String base) {
            base = base + ".";
            decodeGeneral(p.getProperty(base + KEY_GENERAL));
            decodeClass(p.getProperty(base + KEY_CLASS));
            decodeField(p.getProperty(base + KEY_FIELD));
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStyle() {
            return style;
        }

        public void setStyle(int style) {
            this.style = style;
        }

        public boolean isPrintHeader() {
            return printHeader;
        }

        public void setPrintHeader(boolean printHeader) {
            this.printHeader = printHeader;
        }

        public boolean isClassName() {
            return className;
        }

        public void setClassName(boolean className) {
            this.className = className;
        }

        public boolean isClassPackage() {
            return classPackage;
        }

        public void setClassPackage(boolean classPackage) {
            this.classPackage = classPackage;
        }

        public boolean isClassTable() {
            return classTable;
        }

        public void setClassTable(boolean classTable) {
            this.classTable = classTable;
        }

        public boolean isClassTableFirst() {
            return classTableFirst;
        }

        public void setClassTableFirst(boolean classTableFirst) {
            this.classTableFirst = classTableFirst;
        }

        public boolean isClassPseudoFields() {
            return classPseudoFields;
        }

        public void setClassPseudoFields(boolean classPseudoFields) {
            this.classPseudoFields = classPseudoFields;
        }

        public boolean isClassSimpleFields() {
            return classSimpleFields;
        }

        public void setClassSimpleFields(boolean classSimpleFields) {
            this.classSimpleFields = classSimpleFields;
        }

        public boolean isFieldName() {
            return fieldName;
        }

        public void setFieldName(boolean fieldName) {
            this.fieldName = fieldName;
        }

        public boolean isFieldType() {
            return fieldType;
        }

        public void setFieldType(boolean fieldType) {
            this.fieldType = fieldType;
        }

        public boolean isFieldColumnName() {
            return fieldColumnName;
        }

        public void setFieldColumnName(boolean fieldColumnName) {
            this.fieldColumnName = fieldColumnName;
        }

        public boolean isFieldJDBCType() {
            return fieldJDBCType;
        }

        public void setFieldJDBCType(boolean fieldJDBCType) {
            this.fieldJDBCType = fieldJDBCType;
        }

        public boolean isFieldSQLType() {
            return fieldSQLType;
        }

        public void setFieldSQLType(boolean fieldSQLType) {
            this.fieldSQLType = fieldSQLType;
        }

        public boolean isFieldShowLinkTables() {
            return fieldShowLinkTables;
        }

        public void setFieldShowLinkTables(boolean fieldShowLinkTables) {
            this.fieldShowLinkTables = fieldShowLinkTables;
        }

        public boolean isClassCache() {
            return classCache;
        }

        public void setClassCache(boolean classCache) {
            this.classCache = classCache;
        }

        public boolean isClassDeleteOrphans() {
            return classDeleteOrphans;
        }

        public void setClassDeleteOrphans(boolean classDeleteOrphans) {
            this.classDeleteOrphans = classDeleteOrphans;
        }

        public boolean isClassDatastore() {
            return classDatastore;
        }

        public void setClassDatastore(boolean classDatastore) {
            this.classDatastore = classDatastore;
        }

        public boolean isClassClassID() {
            return classClassID;
        }

        public void setClassClassID(boolean classClassID) {
            this.classClassID = classClassID;
        }

        public boolean isClassJDBCClassID() {
            return classJDBCClassID;
        }

        public void setClassJDBCClassID(boolean classJDBCClassID) {
            this.classJDBCClassID = classJDBCClassID;
        }

        public boolean isClassDoNotCreateTable() {
            return classDoNotCreateTable;
        }

        public void setClassDoNotCreateTable(boolean classDoNotCreateTable) {
            this.classDoNotCreateTable = classDoNotCreateTable;
        }

        public boolean isClassKeyGenerator() {
            return classKeyGenerator;
        }

        public void setClassKeyGenerator(boolean classKeyGenerator) {
            this.classKeyGenerator = classKeyGenerator;
        }

        public boolean isClassOptLocking() {
            return classOptLocking;
        }

        public void setClassOptLocking(boolean classOptLocking) {
            this.classOptLocking = classOptLocking;
        }

        public boolean isClassUseJoin() {
            return classUseJoin;
        }

        public void setClassUseJoin(boolean classUseJoin) {
            this.classUseJoin = classUseJoin;
        }

        public boolean isClassJdoFile() {
            return classJdoFile;
        }

        public void setClassJdoFile(boolean classJdoFile) {
            this.classJdoFile = classJdoFile;
        }

        public boolean isClassObjectIdClass() {
            return classObjectIdClass;
        }

        public void setClassObjectIdClass(boolean classObjectIdClass) {
            this.classObjectIdClass = classObjectIdClass;
        }

        /**
         * Are any of the extra class info flags set?
         */
        public boolean isAnyClassExtraSet() {
            return classCache
                    || classDeleteOrphans
                    || classDatastore
                    || classClassID
                    || classJDBCClassID
                    || classDoNotCreateTable
                    || classKeyGenerator
                    || classOptLocking
                    || classUseJoin
                    || classJdoFile
                    || classObjectIdClass;
        }

        /**
         * How many of the field flags are on?
         */
        public int countFieldFlags() {
            int t = 0;
            if (fieldName) ++t;
            if (fieldType) ++t;
            if (fieldColumnName) ++t;
            if (fieldSQLType) ++t;
            return t;
        }

        public boolean isShowLegend() {
            return showLegend;
        }

        public void setShowLegend(boolean showLegend) {
            this.showLegend = showLegend;
        }

    }
}
