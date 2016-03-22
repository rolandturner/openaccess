
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
package com.versant.core.jdbc.sql.diff;

import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcIndex;
import com.versant.core.jdbc.metadata.JdbcConstraint;
import com.versant.core.jdbc.sql.SqlDriver;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;

/**
 * The DiffUtil class gets the diffs of a JdbcTable
 * @keep-all
 */
public class DiffUtil {


    private static final String PRODUCT = "JDOGenie";



    private static SqlDriver driver = null;

    public static TableDiff checkTable(SqlDriver sqlDriver, JdbcTable ourTable, JdbcTable dbTable, ControlParams checks) {
        driver = sqlDriver;
        TableDiff tableDiff = new TableDiff(ourTable, dbTable);
        //check table
        if (dbTable == null) {
            tableDiff.setMissingTable(true);
            if (!checks.checkColumnsOnly()) {  // if the table is missing we also have to index and constraints
                // check index's
                doIndex(tableDiff, ourTable, dbTable, checks);
                // check constraints's
                doConstraint(tableDiff, ourTable, dbTable, checks);
            }
            return tableDiff;
        }

        //check cols
        doCols(tableDiff, ourTable, dbTable, checks);

        if (!checks.checkColumnsOnly()) {
            // check pks
            doPK(tableDiff, ourTable, dbTable, checks);

            // check index's
            doIndex(tableDiff, ourTable, dbTable, checks);

            // check constraints's
            doConstraint(tableDiff, ourTable, dbTable, checks);
        }
        driver = null;
        if (tableDiff.hasErrors()) {
            return tableDiff;
        } else {
            return null;
        }
    }

    private static void doCols(TableDiff tableDiff, JdbcTable ourTable, JdbcTable dbTable, ControlParams checks) {
        HashMap marks = new HashMap();
        if (ourTable.cols != null) {

            for (int i = 0; i < ourTable.cols.length; i++) {
                JdbcColumn ourCol = ourTable.cols[i];
                // check if our column is in there
                JdbcColumn dbCol = null;
                if (dbTable.cols != null) {
                    for (int j = 0; j < dbTable.cols.length; j++) {
                        JdbcColumn col = dbTable.cols[j];
                        if (ourCol.name.equalsIgnoreCase(col.name)) {
                            marks.put(new Integer(j), null);
                            dbCol = col;
                            break;
                        }
                    }
                }
                if (!ourCol.shared){
                    ColumnDiff diff = checkColumn(ourCol, dbCol, checks);
                    if (diff != null) {
                        tableDiff.getColDiffs().add(diff);
                    }
                }
            }
        }
        if (checks.checkExtraColumns){
            if (dbTable.cols != null) {   // check for extra column
                for (int i = 0; i < dbTable.cols.length; i++) {
                    if (!marks.containsKey(new Integer(i))) {
                        // we have a extra column
                        ColumnDiff diff = new ColumnDiff(null, dbTable.cols[i]);
                        diff.setExtraCol(true);
                        tableDiff.getColDiffs().add(diff);
                    }
                }
            }
        }
    }

    private static void doPK(TableDiff tableDiff, JdbcTable ourTable, JdbcTable dbTable, ControlParams checks) {

        if (checks.isCheckPK()) {
            if (ourTable.pk != null && dbTable.pk == null) {
                PKDiff diff = new PKDiff(null, null);
                diff.setMissingPK(true);
                tableDiff.getPkDiffs().add(diff);
            } else {
                HashMap marks = new HashMap();
                if (ourTable.pk != null) {
                    for (int i = 0; i < ourTable.pk.length; i++) {
                        JdbcColumn ourCol = ourTable.pk[i];
                        // check if our column is in there
                        JdbcColumn dbCol = null;
                        if (dbTable.pk != null) {
                            for (int j = 0; j < dbTable.pk.length; j++) {
                                JdbcColumn col = dbTable.pk[j];
                                if (col != null){
                                    if (ourCol.name.equalsIgnoreCase(col.name)) {
                                        marks.put(new Integer(j), null);
                                        dbCol = col;
                                        break;
                                    }
                                }
                            }
                        }
                        PKDiff diff = checkPK(ourCol, dbCol, checks);
                        if (diff != null) {
                            tableDiff.getPkDiffs().add(diff);
                        }
                    }
                }
                if (dbTable.pk != null) {
                    for (int i = 0; i < dbTable.pk.length; i++) {
                        if (!marks.containsKey(new Integer(i))) {
                            // we have a extra column
                            if (dbTable.pk[i] != null){
                                PKDiff diff = new PKDiff(null, dbTable.pk[i]);
                                diff.setExtraPKCol(true);
                                tableDiff.getPkDiffs().add(diff);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void doIndex(TableDiff tableDiff, JdbcTable ourTable, JdbcTable dbTable, ControlParams checks) {

        if (checks.isCheckIndex()) {
            HashMap marks = new HashMap();
            if (ourTable.indexes != null) {
                for (int i = 0; i < ourTable.indexes.length; i++) {
                    JdbcIndex ourIndex = ourTable.indexes[i];
                    // check if our column is in there
                    HashMap possibleIndex = new HashMap();
                    if (dbTable != null) {
                        if (dbTable.indexes != null) {
                            for (int j = 0; j < dbTable.indexes.length; j++) {
                                JdbcIndex index = dbTable.indexes[j];
                                possibleIndex.put(new Integer(j), index);
                            }
                        }
                    }
                    IndexDiff diff = null;
                    JdbcIndex closeDbIndex = null;
                    Integer closeKey = null;
                    boolean found = false;
                    Set keys = possibleIndex.keySet();
                    for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
                        Integer key = (Integer) iterator.next();
                        JdbcIndex dbIndex = (JdbcIndex) possibleIndex.get(key);
                        diff = checkIndex(ourIndex, dbIndex, checks);
                        if (ourIndex.name.equalsIgnoreCase(dbIndex.name)) {
                            closeDbIndex = dbIndex;
                            closeKey = key;
                        }
                        if (diff == null) {
                            found = true;
                            marks.put(key, null);
                        }
                    }

                    if (!found) {
                        diff = checkIndex(ourIndex, closeDbIndex, checks);
                        tableDiff.getIndexDiffs().add(diff);
                        if (closeKey != null) {
                            marks.put(closeKey, null);
                        }
                    }

                }
            }
            if (dbTable != null) {     // extra index's
                if (dbTable.indexes != null) {
                    for (int i = 0; i < dbTable.indexes.length; i++) {
                        if (!marks.containsKey(new Integer(i))) {
                            // we have a extra column
                            IndexDiff diff = new IndexDiff(null, dbTable.indexes[i]);
                            diff.setExtraIndex(true);
                            tableDiff.getIndexDiffs().add(diff);
                        }
                    }
                }
            }
        }
    }

    private static void doConstraint(TableDiff tableDiff, JdbcTable ourTable, JdbcTable dbTable, ControlParams checks) {
        if (checks.isCheckConstraint()) {
            HashMap marks = new HashMap();
            if (ourTable.constraints != null) {
                for (int i = 0; i < ourTable.constraints.length; i++) {
                    JdbcConstraint ourConstraint = ourTable.constraints[i];
                    // check if our column is in there
                    HashMap possibleConstraint = new HashMap();
                    if (dbTable != null) {
                        if (dbTable.constraints != null) {
                            for (int j = 0; j < dbTable.constraints.length; j++) {
                                JdbcConstraint constraint = dbTable.constraints[j];
                                try {
                                    if (ourConstraint.src.name.equalsIgnoreCase(constraint.src.name) &&
                                            (ourConstraint.dest.name.equalsIgnoreCase(constraint.dest.name))) {
                                        possibleConstraint.put(new Integer(j), constraint);
                                    }
                                } catch (Exception e) {}
                            }
                        }
                    }
                    ConstraintDiff diff = null;
                    JdbcConstraint closeDbConstraint = null;
                    Integer closeKey = null;
                    boolean found = false;
                    Set keys = possibleConstraint.keySet();
                    for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
                        Integer key = (Integer) iterator.next();
                        JdbcConstraint dbConstraint = (JdbcConstraint)possibleConstraint.get(key);
                        diff = checkConstraint(ourConstraint, dbConstraint, checks);
                        if (ourConstraint.name.equalsIgnoreCase(dbConstraint.name)) {
                            closeDbConstraint = dbConstraint;
                            closeKey = key;
                        }
                        if (diff == null) {
                            found = true;
                            marks.put(key,null);

                        }
                    }

                    if (!found){
                        diff = checkConstraint(ourConstraint, closeDbConstraint, checks);
                        tableDiff.getConstraintDiffs().add(diff);
                        if (closeKey != null){
                            marks.put(closeKey, null);
                        }
                    }
                }
            }
            if (dbTable != null) {  // extra constraints
                if (dbTable.constraints != null) {
                    for (int i = 0; i < dbTable.constraints.length; i++) {
                        if (!marks.containsKey(new Integer(i))) {
                            // we have a extra column
                            if (dbTable.constraints[i] != null){
                                ConstraintDiff diff = new ConstraintDiff(null, dbTable.constraints[i]);
                                diff.setExtraConstraint(true);
                                diff.setDrop(true);
                                tableDiff.getConstraintDiffs().add(diff);
                            }
                        }
                    }
                }
            }
        }
    }



    private static ColumnDiff checkColumn(JdbcColumn ourCol, JdbcColumn dbCol, ControlParams checks) {
        ColumnDiff diff = new ColumnDiff(ourCol, dbCol);
        if (dbCol == null) {
            diff.setMissingCol(true);

        } else {// the db col names are the same
            if (checks.isCheckType()) {
                if (!driver.checkType(ourCol, dbCol)) {
                    diff.setTypeDiff(true);
                }
            }
            if (checks.isCheckLength()) {
                if (!driver.checkLenght(ourCol, dbCol)) {
                    diff.setLenghtDiff(true);
                }
            }
            if (checks.isCheckScale()) {
                if (!driver.checkScale(ourCol, dbCol)) {
                    diff.setScaleDiff(true);
                }
            }
            if (checks.isCheckNulls()) {
                if (!driver.checkNulls(ourCol, dbCol)) {
                    diff.setNullDiff(true);
                }
            }
        }

        if (diff.hasErrors()) {
            return diff;
        } else {
            return null;
        }

    }

    private static PKDiff checkPK(JdbcColumn ourCol, JdbcColumn dbCol, ControlParams checks) {
        PKDiff diff = new PKDiff(ourCol, dbCol);
        if (dbCol == null) {
            diff.setMissingPKCol(true);
        }
        if (diff.hasErrors()) {
            return diff;
        } else {
            return null;
        }
    }

    private static IndexDiff checkIndex(JdbcIndex ourIndex, JdbcIndex dbIndex, ControlParams checks) {
        IndexDiff diff = new IndexDiff(ourIndex, dbIndex);
        if (dbIndex == null) {
            diff.setMissingIndex(true);
        } else {
            //check cols
            HashMap marks = new HashMap();
            if (ourIndex.cols != null) {
                for (int i = 0; i < ourIndex.cols.length; i++) {
                    JdbcColumn ourCol = ourIndex.cols[i];
                    // check if our column is in there
                    JdbcColumn dbCol = null;
                    if (dbIndex.cols != null) {
                        for (int j = 0; j < dbIndex.cols.length; j++) {
                            JdbcColumn col = dbIndex.cols[j];
                            if (ourCol.name.equalsIgnoreCase(col.name)) {
                                marks.put(new Integer(j), null);
                                dbCol = col;
                                break;
                            }
                        }
                    }
                    if (dbCol == null) {
                        diff.setMissingCol(true);
                    }
                }
            }
            if (dbIndex != null && ourIndex != null){
                if (dbIndex.unique != ourIndex.unique){
                    diff.setUniqueness(true);
                }
            }

            if (dbIndex.cols != null) {
                for (int i = 0; i < dbIndex.cols.length; i++) {
                    if (!marks.containsKey(new Integer(i))) {
                        // we have a extra column in our Constraint
                        diff.setExtraCol(true);
                    }
                }
            }
        }
        if (diff.hasErrors()) {
            return diff;
        } else {
            return null;
        }
    }

    public static ConstraintDiff checkConstraint(JdbcConstraint ourConstraint, JdbcConstraint dbConstraint,
                                             ControlParams checks) {
        ConstraintDiff diff = new ConstraintDiff(ourConstraint, dbConstraint);
        if (dbConstraint == null) {
            diff.setMissingConstraint(true);
        } else {
            //check cols
            HashMap marks = new HashMap();
            if (ourConstraint.srcCols != null) {
                for (int i = 0; i < ourConstraint.srcCols.length; i++) {
                    JdbcColumn ourCol = ourConstraint.srcCols[i];
                    // check if our column is in there
                    JdbcColumn dbCol = null;
                    if (dbConstraint.srcCols != null) {
                        for (int j = 0; j < dbConstraint.srcCols.length; j++) {
                            JdbcColumn col = dbConstraint.srcCols[j];
                            if (col != null){
                                if (ourCol.name.equalsIgnoreCase(col.name)) {
                                    marks.put(new Integer(j), null);
                                    dbCol = col;
                                    break;
                                }
                            }
                        }
                    }
                    if (dbCol == null) {
                        diff.setMissingCol(true);
                        diff.setDrop(true);
                    }
                }
            }
            if (dbConstraint.srcCols != null) {
                for (int i = 0; i < dbConstraint.srcCols.length; i++) {
                    if (!marks.containsKey(new Integer(i))) {
                        // we have a extra column in our Constraint
                        diff.setExtraCol(true);
                        diff.setDrop(true);
                    }
                }
            }
        }

        if (diff.hasErrors()) {
            return diff;
        } else {
            return null;
        }
    }


    /**
     * reports the errors
     * @param diffList
     * @param out
     */
    public static void reportErrors(ArrayList diffList, PrintWriter out) {
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff) iter.next();
            if (tableDiff.hasRealErrors()){
                printError(out, tableDiff.getOurTable().name);
            }
            if (tableDiff.isMissingTable()) {
                printErrorMsg(out, "Table '" + tableDiff.getOurTable().name + "' does not exist.");
            }
            ArrayList colList = tableDiff.getColDiffs();
            for (Iterator iterator = colList.iterator(); iterator.hasNext();) {
                ColumnDiff diff = (ColumnDiff) iterator.next();
                if (diff.isExtraCol()) {
                    printErrorMsg(out, "Column '" + diff.getDbCol().name + "' is not known to "+PRODUCT);
                }

                if (diff.isLenghtDiff()) {
                    printErrorMsg(out,
                            "Column '" + diff.getOurCol().name + "' length is " + diff.getDbCol().length + ", it should be " + diff.getOurCol()
                            .length);
                }

                if (diff.isMissingCol()) {
                    printErrorMsg(out, "Column '" + diff.getOurCol().name + "' does not exist.");
                }

                if (diff.isNullDiff()) {
                    printErrorMsg(out, "Column '" + diff.getOurCol().name + "' null value is " + (diff.getDbCol()
                            .nulls ?
                            "'NULL'" : "'NOT NULL'") + ", it should be " + (diff.getOurCol().nulls ?
                            "'NULL'" : "'NOT NULL'"));
                }

                if (diff.isScaleDiff()) {
                    printErrorMsg(out,
                            "Column '" + diff.getOurCol().name + "' scale is " + diff.getDbCol().scale + ", it should be " + diff.getOurCol()
                            .scale);

                }

                if (diff.isTypeDiff()) {
                    printErrorMsg(out,
                            "Column '" + diff.getOurCol().name + "' type is " + diff.getDbCol().sqlType + ", it should be " + diff.getOurCol()
                            .sqlType);
                }
            }
            ArrayList pkList = tableDiff.getPkDiffs();
            for (Iterator iterator = pkList.iterator(); iterator.hasNext();) {
                PKDiff diff = (PKDiff) iterator.next();
                //
                if (diff.isMissingPK()){
                    printErrorMsg(out, "Primary key '"+ tableDiff.getOurTable().pkConstraintName +"' on table '" + tableDiff.getOurTable().name+ "' does not exist.");
                } else {
                    if (diff.isMissingPKCol()) {
                        printErrorMsg(out, "Primary key '"+ tableDiff.getOurTable().pkConstraintName +"' has a missing column '" + diff.getOurCol().name + "'.");
                    }
                    if (diff.isExtraPKCol()) {
                        printErrorMsg(out, "Primary key '"+ tableDiff.getOurTable().pkConstraintName +"' has a extra column '" + diff.getDbCol().name + "' that is not known to "+PRODUCT);
                    }
                }
            }
            ArrayList indexList = tableDiff.getIndexDiffs();
            for (Iterator iterator = indexList.iterator(); iterator.hasNext();) {
                IndexDiff diff = (IndexDiff) iterator.next();
                //
                if (diff.isMissingIndex()) {
                    printErrorMsg(out, "Index '" + diff.getOurIndex().name + "' does not exist.");
                }
                if (diff.isExtraIndex()) {
                    printErrorMsg(out, "Index '" + diff.getDbIndex().name + "' is not known to "+PRODUCT);
                }
                if (diff.isExtraCol()) {
                    printErrorMsg(out, "Index '" + diff.getOurIndex().name + "' has extra columns not known to "+PRODUCT);
                }
                if (diff.isMissingCol()) {
                    printErrorMsg(out, "Index '" + diff.getOurIndex().name + "' has missing columns");
                }
                if (diff.isUniqueness()) {
                    JdbcIndex ourIndex = diff.getOurIndex();
                    if (ourIndex.unique){
                        printErrorMsg(out, "Index '" + diff.getOurIndex().name + "' is unique, but database is not");
                    } else {
                        printErrorMsg(out, "Index '" + diff.getOurIndex().name + "' is not unique, but database is");
                    }

                }
            }
            ArrayList constraintList = tableDiff.getConstraintDiffs();
            for (Iterator iterator = constraintList.iterator(); iterator.hasNext();) {
                ConstraintDiff diff = (ConstraintDiff) iterator.next();
                //
                if (diff.isMissingConstraint()) {
                    printErrorMsg(out, "Constraint '" + diff.getOurConstraint().name + "' does not exist.");
                }
                if (diff.isExtraConstraint()) {
                    printErrorMsg(out, "Constraint '" + diff.getDbConstraint().name + "' is not known to "+PRODUCT);
                }
                if (diff.isExtraCol()) {
                    printErrorMsg(out, "Constraint '" + diff.getOurConstraint().name + "' has extra columns not known to "+PRODUCT);
                }
                if (diff.isMissingCol()) {
                    printErrorMsg(out, "Constraint '" + diff.getOurConstraint().name + "' has missing columns.");
                }
            }
        }
    }

    private static void printError(PrintWriter out, String tableName) {
        out.print("\nTable ");
        out.print(tableName);
        out.println(" : FAIL");
    }

    private static void printErrorMsg(PrintWriter out, String error) {
        out.print("    ");
        out.println(error);
    }



}
