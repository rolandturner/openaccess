
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
package com.versant.core.jdbc;

import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoElement;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.MetaDataBuilder;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ClassIdTranslator;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.util.IntObjectHashMap;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This will analyze meta data for a reference to any PC class from an
 * array of extensions. It is used for polyref fields and for polymorphic link
 * tables. Instances of this class may only be used once.
 */
public class JdbcPolyRefMetaDataBuilder implements JdoExtensionKeys {

    private JdbcMetaDataBuilder mdb;
    private JdoElement context;
    private String fieldName;
    private JdbcField field;

    private ArrayList colsList = new ArrayList();

    private JdbcColumn classIdCol;
    private JdbcColumn[] pkCols;
    private JdbcColumn[] cols;

    private HashSet classIdSet = new HashSet(); // to detect duplicate IDs

    private boolean stringClassIds; // is at least one class-id in map not int

    // ClassMetaData -> String for ID translation
    private Map cmdToIDString = new HashMap();
    private Map idToCmdString; // String ID -> ClassMetaData

    // contains int values of class-ids if all are ints
    private Map cmdToIDInt = new HashMap();
    private IntObjectHashMap idToCmdInt;       // int ID -> ClassMetaData

    private ClassIdTranslator classIdTranslator;

    public JdbcPolyRefMetaDataBuilder(JdbcMetaDataBuilder mdb,
            JdoElement context, String fieldName, JdoExtension[] extensions,
            JdbcField field) {
        this.mdb = mdb;
        this.context = context;
        this.fieldName = fieldName;
        this.field = field;
        process(extensions);
    }

    private void process(JdoExtension[] extensions) {
        JdoExtension classIdColExt = null;
        if (extensions != null) {
            int ne = extensions.length;
            for (int i = 0; i < ne; i++) {
                JdoExtension e = extensions[i];
                switch (e.key) {

                    case JDBC_CLASS_ID:
                        if (classIdColExt != null) {
                            throw BindingSupportImpl.getInstance().runtime(
                                "Only one jdbc-class-id extension is allowed\n" +
                                context);
                        }
                        classIdColExt = e;
                        break;

                    case JDBC_REF:
                        colsList.add(mdb.createColumn(e.nested, fieldName,
                                /*CHFC*/Integer.class/*RIGHTPAR*/));
                        break;

                    case VALID_CLASS:
                        processClassIdMapping(e);
                        break;

                    default:
                        if (e.isJdbc()) MetaDataBuilder.throwUnexpectedExtension(e);
                        break;
                }
            }
        }

        // convert all the class-id's to ints if possible
        try {
            for (Iterator i = cmdToIDString.keySet().iterator(); i.hasNext(); ) {
                ClassMetaData key = (ClassMetaData)i.next();
                String value = (String)cmdToIDString.get(key);
                cmdToIDInt.put(key, new Integer(value));
            }
        } catch (NumberFormatException e) {
            stringClassIds = true;
        }

        // get rid of empty maps and init id -> class maps
        if (cmdToIDString.size() == 0) cmdToIDString = null;
        if (cmdToIDInt.size() == 0) cmdToIDInt = null;

        if (cmdToIDInt != null) {    // build reverse map
            idToCmdInt = new IntObjectHashMap();
            for (Iterator i = cmdToIDInt.keySet().iterator(); i.hasNext(); ) {
                ClassMetaData key = (ClassMetaData)i.next();
                int value = ((Integer)cmdToIDInt.get(key)).intValue();
                idToCmdInt.put(value, key);
            }
        }

        if (cmdToIDString != null) { // build reverse map
            idToCmdString = new HashMap();
            for (Iterator i = cmdToIDString.keySet().iterator(); i.hasNext(); ) {
                ClassMetaData key = (ClassMetaData)i.next();
                String value = (String)cmdToIDString.get(key);
                idToCmdString.put(value, key);
            }
        }

        // create the translator
        classIdTranslator = new ClassIdTranslator(field.fmd.classMetaData.jmd,
                stringClassIds, cmdToIDString, idToCmdString,
                cmdToIDInt, idToCmdInt);
        classIdTranslator.setMessage("field " + field.fmd.getQName());

        // make sure all possible classes have the same number of primary
        // key columns if any valid-class elements were used and also get
        // the primary key of the first class
        JdbcColumn[] firstPk = null;
        List cl = classIdTranslator.getClassList();
        int n = cl.size();
        if (n > 0) {
            ClassMetaData first = (ClassMetaData)cl.get(0);
            firstPk = ((JdbcClass)first.storeClass).table.pk;
            for (int i = 1; i < n; i++) {
                ClassMetaData cmd = (ClassMetaData)cl.get(i);
                int len = ((JdbcClass)cmd.storeClass).table.pk.length;
                if (len != firstPk.length) {
                    throw BindingSupportImpl.getInstance().runtime(
                        "All valid classes must have the same number of " +
                        "primary key columns\n" +
                        first + " has " + firstPk.length + "\n" +
                        cmd + " has " + len + "\n" +
                        field.fmd.jdoField.getContext());
                }
            }
        }

        // generate the classIdCol
        classIdCol = mdb.createColumn(classIdColExt == null ? null : classIdColExt.nested,
                fieldName, stringClassIds ? /*CHFC*/String.class/*RIGHTPAR*/ : /*CHFC*/Integer.class/*RIGHTPAR*/);

        // generate primary key column(s) if none specified
        if (colsList.isEmpty()) {
            if (firstPk != null) {
                for (int i = 0; i < firstPk.length; i++) {
                    JdbcColumn c = mdb.createColumn(null, firstPk[i]);
                    c.pk = false;
                    colsList.add(c);
                }
            } else {
                colsList.add(mdb.createColumn(null, fieldName, /*CHFC*/Integer.class/*RIGHTPAR*/));
            }
        } else if (firstPk != null && colsList.size() != firstPk.length) {
            throw BindingSupportImpl.getInstance().runtime(
                "Mismatched reference column(s): " +
                colsList.size() + " reference column(s) defined but " +
                "valid classes have " + firstPk.length +
                " primary key column(s)\n" +
                field.fmd.jdoField.getContext());
        }

        pkCols = new JdbcColumn[colsList.size()];
        colsList.toArray(pkCols);
        colsList.add(0, classIdCol);
        cols = new JdbcColumn[colsList.size()];
        colsList.toArray(cols);
    }

    public JdbcColumn getClassIdCol() {
        return classIdCol;
    }

    public JdbcColumn[] getPkCols() {
        return pkCols;
    }

    public JdbcColumn[] getCols() {
        return cols;
    }

    public ArrayList getColsList() {
        return colsList;
    }

    /**
     * Get a translator for the class-id's of this field.
     */
    public ClassIdTranslator getClassIdTranslator() {
        return classIdTranslator;
    }

    private void processClassIdMapping(JdoExtension e) {
        String value = e.getString();
        int i = value.indexOf('=');
        String cname = i < 0 ? value : value.substring(0, i);
        String id = i < 0 ? null : value.substring(i + 1);
        ClassMetaData key = mdb.getJmd().getClassMetaData(
                field.fmd.classMetaData, cname);
        if (key == null) {
            throw BindingSupportImpl.getInstance().runtime("No persistent class found for '" +
                cname + "': " + e + "\n" + e.getContext());
        }
        if (cmdToIDString.containsKey(key)) {
            throw BindingSupportImpl.getInstance().runtime("Duplicate class in mapping '" +
                cname + "': " + e + "\n" + e.getContext());
        }
        if (id == null || id.length() == 0) id = Integer.toString(key.classId);
        if (classIdSet.contains(id)) {
            throw BindingSupportImpl.getInstance().runtime("Duplicate class-id in mapping '" +
                id + "': " + e + "\n" + e.getContext());
        }
        cmdToIDString.put(key, id);
        classIdSet.add(id);
    }

}
