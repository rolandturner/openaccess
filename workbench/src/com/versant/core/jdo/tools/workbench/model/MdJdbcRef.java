
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

import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.jdbc.metadata.JdbcColumn;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>A foreign key reference from one table to the table for a target class or
 * a polymorphic reference to any class or a set of classes. This class
 * manages the elements making up the reference and the associated
 * JdbcColumn(s) from the runtime meta data if available.</p>
 *
 * @keep-all
 */
public class MdJdbcRef implements GraphColumnRef {

    private static final GraphColumn[] EMPTY_COLS = new GraphColumn[0];

    private MdElement container;
    private GraphTable srcTable;
    private GraphTable destTable;
    private MdClass target;
    private MdDataStore srcStore;
    private MdPackage srcPackage;
    private boolean oneToMany;
    private boolean ignoreJoin;

    private MdColumn classIdCol = new MdColumn();
    private MdColumn[] refCols = new MdColumn[0];
    private List refExts;

    public MdJdbcRef() {
    }

    /**
     * Initialize this reference. This will find all of the appropriate
     * elements and hook them up to the JdbcColumn's (if any) using
     * ColumnElementWrappers.
     *
     * @param container To hold the elements generated (if any)
     * @param srcTable  The source table (i.e. the table with the fks)
     * @param target    The class being referenced by the fks or null if polyref
     * @param destTable The target table (i.e. referenced table ) null if polyref
     * @param srcStore  The datastore of the source table
     * @param jdbcCols  The columns from the runtime meta data (null if n/a)
     */
    public void init(MdElement container, GraphTable srcTable, MdClass target,
            GraphTable destTable, MdDataStore srcStore, MdPackage srcPackage,
            JdbcColumn[] jdbcCols) {
        this.container = container;
        this.srcTable = srcTable;
        this.destTable = destTable;
        this.target = target;
        this.srcStore = srcStore;
        this.srcPackage = srcPackage;
        if (isPolyRef()) {
            initPolyRef(jdbcCols);
        } else {
            initForeignKeyRef(jdbcCols);
        }
    }

    private void initForeignKeyRef(JdbcColumn[] jdbcCols) {
        if(target == null){
            return;
        }
        MdField[] pkfields = target.getAppIdentityFields();
        if (pkfields == null) { // datastore identity
            resizeRefCols(1);
            refCols[0].init(srcStore, container, get(jdbcCols, 0), srcTable);
        } else {
            List extlist = new ArrayList(
                    XmlUtils.findExtensions(container,
                            JdoExtensionKeys.JDBC_REF));
            ArrayList toLink = new ArrayList();
            resizeRefCols(pkfields.length);
            for (int i = 0; i < pkfields.length; i++) {
                MdField pkf = pkfields[i];
                String fname = pkf.getName();
                MdElement e = findJdbcRef(extlist, fname);
                if (e != null) {
                    extlist.remove(e);
                    e.setEmptyIfExtValue(true);
                } else {
                    e = XmlUtils.createExtension(JdoExtensionKeys.JDBC_REF,
                            fname, container, true);
                }
                refCols[i].init(srcStore, e, get(jdbcCols, i), srcTable);
                refCols[i].setHelp("Foreign key reference for " +
                        pkfields[i].getShortQName());
                toLink.add(e);
            }
            if (!extlist.isEmpty()) {
                for (Iterator i = extlist.iterator(); i.hasNext();) {
                    MdElement e = (MdElement)i.next();
                    error("Removing invalid jdbc-ref element: '" +
                            e.getAttributeValue("value") + "'");
                    e.getParent().removeContent(e);
                }
                extlist.clear();
            }
            MdElement.link(toLink);
        }
    }

    private static JdbcColumn get(JdbcColumn[] cols, int i) {
        if (cols == null || i >= cols.length) return null;
        return cols[i];
    }

    private void initPolyRef(JdbcColumn[] jdbcCols) {

        // class-id column
        classIdCol.init(srcStore,
                XmlUtils.findOrCreateExtension(container,
                        JdoExtensionKeys.JDBC_CLASS_ID),
                get(jdbcCols, 0), srcTable);
        classIdCol.setHelp("Identifier for type of referenced instance");

        // reference column(s)
        refExts = new ArrayList(XmlUtils.findExtensions(container,
                JdoExtensionKeys.JDBC_REF));
        if (refExts.isEmpty()) {
            // add an empty ref extension for each ref column
            int n = jdbcCols == null ? 1 : jdbcCols.length - 1;
            for (int i = 0; i < n; i++) {
                refExts.add(XmlUtils.createExtension(JdoExtensionKeys.JDBC_REF,
                        container));
            }
        }
        resizeRefCols(refExts.size());
        for (int i = 0; i < refCols.length; i++) {
            MdElement e = (MdElement)refExts.get(i);
            JdbcColumn rc;
            if (jdbcCols != null && i + 1 < jdbcCols.length) {
                rc = jdbcCols[i + 1];
            } else {
                rc = null;
            }
            refCols[i].init(srcStore, e, rc, srcTable);
            refCols[i].setHelp("Primary key of referenced instance");
        }
        MdElement.link(refExts);
    }

    /**
     * Get the classid column wrapper if this is a polyref, otherwise null.
     */
    public MdColumn getClassIdCol() {
        return isPolyRef() ? null : classIdCol;
    }

    /**
     * Get the foreign key column wrappers.
     */
    public MdColumn[] getRefCols() {
        return refCols;
    }

    public GraphTable getSrcTable() {
        return srcTable;
    }

    public GraphTable getDestTable() {
        return destTable;
    }

    /**
     * Add the column wrappers to a table. The classid col is first
     * if this is a polyref.
     */
    public void addColsToTable(MdTable table) {
        if (isPolyRef()) {
            classIdCol.setTable(table);
            table.addCol(classIdCol);
        }
        for (int i = 0; i < refCols.length; i++) {
            refCols[i].setTable(table);
            table.addCol(refCols[i]);
        }
    }

    /**
     * Make sure refCols is n long.
     */
    private void resizeRefCols(int n) {
        if (refCols.length < n) {    // grow
            MdColumn[] a = new MdColumn[n];
            if (refCols.length > 0) {
                System.arraycopy(refCols, 0, a, 0,
                        refCols.length);
            }
            for (int i = refCols.length; i < n; i++) {
                a[i] = new MdColumn();
            }
            refCols = a;
        } else if (refCols.length > n) { // shrink
            MdColumn[] a = new MdColumn[n];
            System.arraycopy(refCols, 0, a, 0, n);
            refCols = a;
        }
    }

    private MdElement findJdbcRef(List extlist, String fname) {
        for (Iterator i = extlist.iterator(); i.hasNext();) {
            MdElement e = (MdElement)i.next();
            String v = e.getAttributeValue("value");
            if (v != null && v.equals(fname)) {
                return e;
            }
        }
        return null;
    }

    private void error(String msg) {
        if (srcStore != null) {
            MdProject project = srcStore.getProject();
            if (project != null) {
                Logger logger = project.getLogger();
                if (logger != null) {
                    logger.error(msg);
                }
            }
        }
    }

    /**
     * Return true if srcTable has the same name as destTable i.e this
     * is a self reference.
     */
    public boolean isSelfRef() {
        if (srcTable == null || destTable == null) return false;
        String a = srcTable.getName();
        return a == null ? false : a.equals(destTable.getName());
    }

    /**
     * Return true if this is a polymorphic reference.
     */
    public boolean isPolyRef() {
        return srcTable != null && target == null;
    }

    public GraphColumn[] getSrcColumns() {
        return refCols == null ? EMPTY_COLS : refCols;
    }

    public GraphColumn[] getDestColumns() {
        if (srcTable == null || destTable == null) return EMPTY_COLS;
        return isSelfRef() ? srcTable.getPkCols() : destTable.getPkCols();
    }

    public String getComment() {
        StringBuffer s = new StringBuffer();
        if (!ignoreJoin) {
            String join = getJdbcUseJoinStr();
            if ("no".equals(join)) {
                s.append("Do not join when prefetching data.");
            } else {
                if (join == null) {
                    join = "OUTER JOIN";
                } else {
                    join = join.toUpperCase() + " JOIN";
                }
                s.append("Use ");
                s.append(join);
                s.append(" when prefetching data.");
            }
            s.append(' ');
        }
        String con = getJdbcConstraintStr();
        if (JdoExtension.NO_VALUE.equals(con)) {
            s.append("Do not generate foreign key constraint.");
        } else if (con == null) {
            s.append("Generate foreign key constraint with automatic name.");
        } else {
            s.append("Generate foreign key constraint named '" + con + "'.");
        }
        return s.toString();
    }

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(boolean oneToMany) {
        this.oneToMany = oneToMany;
    }

    public String toString() {
        if (ignoreJoin) {
            return "";
        } else {
            String join = getJdbcUseJoinStr();
            if (join == null) {
                join = "OUTER";
            } else if (join.equals("no")) {
                join = "NO JOIN";
            } else {
                join = join.toUpperCase();
            }
            return join;
        }
    }

    public void setJdbcUseJoinStr(String s) {
        XmlUtils.setExtension(container, JdoExtensionKeys.JDBC_USE_JOIN, s);
    }

    public String getJdbcUseJoinStr() {
        return XmlUtils.getExtension(container, JdoExtensionKeys.JDBC_USE_JOIN);
    }

    public String getJdbcConstraintStr() {
        return XmlUtils.getExtension(container,
                JdoExtensionKeys.JDBC_CONSTRAINT);
    }

    public void setJdbcConstraintStr(String s) {
        XmlUtils.setExtension(container, JdoExtensionKeys.JDBC_CONSTRAINT, s);
    }

    /**
     * Add a new reference column if this is a polyref.
     */
    public void addRefColumn() throws Exception {
        if (!isPolyRef()) return;
        MdElement e = XmlUtils.createExtension(JdoExtensionKeys.JDBC_REF,
                container);
        refExts.add(e);
        MdElement.link(refExts);
        MdColumn c = new MdColumn();
        c.init(srcStore, e, null, srcTable);
        resizeRefCols(refCols.length + 1);
        refCols[refCols.length - 1] = c;
        XmlUtils.makeDirty(container);
    }

    /**
     * Remove the last reference column if this is a polyref. If there is only
     * one it will be added back in init effectively reverting to
     * default settings.
     */
    public boolean removeRefColumn() throws Exception {
        if (!isPolyRef()) return false;
        return true;
    }

    /**
     * Get all of our nested valid-class elements wrapped in
     * ValidClassWrapper instances in a List.
     */
    public List getValidClasses() {
        ArrayList a = new ArrayList();
        List l = XmlUtils.findExtensions(container,
                JdoExtensionKeys.VALID_CLASS);
        for (Iterator i = l.iterator(); i.hasNext();) {
            a.add(new ValidClassWrapper((MdElement)i.next()));
        }
        return a;
    }

    /**
     * Create a nested valid-class element.
     */
    public void addValidClass(MdClass c) {
        String name = c.getMdPackage() == srcPackage ? c.getName() : c.getQName();
        MdElement e = XmlUtils.createExtension(JdoExtensionKeys.VALID_CLASS,
                container);
        e.setAttribute("value", name);
        XmlUtils.makeDirty(container);
    }

    /**
     * Write the names of our columns into the meta data. This is a NOP if
     * the information is not available (i.e. the meta data has errors).
     */
    public void writeMappingsToMetaData() {
        if (isPolyRef()) {
            classIdCol.writeNameToMetaData();
        }
        for (int i = 0; i < refCols.length; i++) {
            refCols[i].writeNameToMetaData();
        }
    }

    public boolean isIgnoreJoin() {
        return ignoreJoin;
    }

    /**
     * Do not show join settings for this reference. This is used for
     * references where the join is not configurable (e.g. subclass to
     * superclass when using vertical inheritance).
     */
    public void setIgnoreJoin(boolean ignoreJoin) {
        this.ignoreJoin = ignoreJoin;
    }

    /**
     * Wrapper for a valid-class element.
     */
    public class ValidClassWrapper {

        private MdElement element;

        public ValidClassWrapper(MdElement element) {
            this.element = element;
        }

        private void updateEmpty() {
            boolean changed = false;
            if (XmlUtils.isEmpty(element)) {
                if (element.getParent() != null) {
                    container.removeContent(element);
                    changed = true;
                }
            } else {
                if (element.getParent() == null) {
                    container.addContent(element);
                    changed = true;
                }
            }
            if (changed) XmlUtils.makeDirty(container);
        }

        public String getClassNameStr() {
            String s = element.getAttributeValue("value");
            if (s == null) return null;
            int i = s.indexOf('=');
            if (i < 0) return s;
            return s.substring(0, i);
        }

        public String getClassIdStr() {
            String s = element.getAttributeValue("value");
            if (s == null) return null;
            int i = s.indexOf('=');
            if (i < 0) return null;
            return s.substring(i + 1);
        }

        public void setClassIdStr(String id) {
            String name = getClassNameStr();
            if (name == null) return;
            String v;
            if (id != null && id.length() > 0) {
                v = name + "=" + id;
            } else {
                v = name;
            }
            element.setAttribute("value", v);
            XmlUtils.makeDirty(element);
        }

        /**
         * Empty this mapping and remove its element from its parent.
         */
        public void clear() {
            element.removeAttribute("value");
            updateEmpty();
        }

        public MdValue getClassName() {
            MdClassValue v = new MdClassValue(getClassNameStr(), srcPackage);
            v.setColor(Color.black);
            return v;
        }

        public MdClass getMdClass() {
            String name = getClassNameStr();
            if (name != null) {
                MdClass c = srcPackage.findClass(name);
                if (c != null) return c;
            }
            return null;
        }

        public MdValue getClassId() {
            String id = getClassIdStr();
            if (id != null && id.length() == 0) id = null;
            MdValue v = new MdValue(id);
            String name = getClassNameStr();
            if (name != null) {
                MdClass c = srcPackage.findClass(name);
                if (c != null) v.setDefText(c.getDefJdbcClassId());
            }
            return v;
        }

        public void setClassId(MdValue v) {
            setClassIdStr(v.getText());
        }
    }
}
