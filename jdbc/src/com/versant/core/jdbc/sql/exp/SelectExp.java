
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
package com.versant.core.jdbc.sql.exp;

import com.versant.core.common.Debug;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdo.query.*;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.query.JDOQLNodeToSqlExp;
import com.versant.core.jdbc.fetch.FetchSpec;
import com.versant.core.util.CharBuf;

import java.util.Map;

import com.versant.core.common.BindingSupportImpl;

/**
 * A 'select ... from ... where ... order by ...' expression.
 */
public class SelectExp extends LeafExp {
    public static final ColumnStruct[] EMPTY_COLUMS_STRUCT = new ColumnStruct[0];

    /**
     * Linked list of expressions to select (null if none).
     */
    public SqlExp selectList;
    /**
     * Is this a 'select distinct'?
     */
    public boolean distinct;
    /**
     * Is this a 'select for update'?
     */
    public boolean forUpdate;
    /**
     * The table to select from.
     */
    public JdbcTable table;
    /**
     * The field that was navigated to get to this table (null if none).
     */
    public JdbcField jdbcField;
    /**
     * The variable this expression belongs to (null if none).
     */
    public VarNode var;
    /**
     * The alias assigned to our table (null if none).
     */
    public String alias;
    /**
     * The 'join' expression for a sub select (null if none). This will end
     * up in the where clause but is kept separately so nested sub selects can
     * be converted into joins easily.
     */
    public SqlExp subSelectJoinExp;
    /**
     * The where clause expression (null if none).
     */
    public SqlExp whereExp;
    /**
     * Linked list of expressions to order by (null if none).
     */
    public SqlExp orderByList;
    public SqlExp resultList;
    public SqlExp groupByList;
    public HavingExp havingExp;
    /**
     * Is table involved in an outer join as the outer table?
     */
    public boolean outer;
    /**
     * Linked list of joins (null if none). Note that the selectExp for
     * each entry in the list may have its own joinList and so on
     * recusively (i.e. this is actually a tree).
     */
    public Join joinList;

    /**
     * Index of the first column in the select list as of the last call to
     * appendSQL.
     */
    public int selectListStartIndex;
    /**
     * Index of the last charcter of the first column in the select list + 1
     * as of the last call to appendSQL.
     */
    public int selectListFirstColEndIndex;
    /**
     * Index of the last column in the select list + 1 as of the last call
     * to appendSQL.
     */
    public int selectListEndIndex;
    /**
     * Index of the start of the "order by ..." clause as of the last call to
     * appendSQL.
     */
    public int orderByStartIndex;
    /**
     * Index of the end of the "order by ..." clause + 1 as of the last call to
     * appendSQL.
     */
    public int orderByEndIndex;

    public int selectListCountBeforeAggregate;

    public FetchSpec fetchSpec;

    private static final char ALIAS_PREPEND_CONSTANT = 'x';

    private boolean done;

    public SelectExp() {
    }

    public SqlExp createInstance() {
        return new SelectExp();
    }

    public SqlExp getClone(SqlExp clone, Map cloneMap) {
        SelectExp cst = (SelectExp)clone;
        super.getClone(cst, cloneMap);

        if (selectList != null) cst.selectList = createClone(selectList, cloneMap);
        cst.distinct = distinct;
        cst.forUpdate = forUpdate;
        cst.table = table;
        cst.jdbcField = jdbcField;
        cst.var = var;
        cst.alias = alias;
        if (subSelectJoinExp != null) cst.subSelectJoinExp = createClone(subSelectJoinExp, cloneMap);
        if (whereExp != null) cst.whereExp = createClone(whereExp, cloneMap);
        if (orderByList != null) cst.orderByList = createClone(orderByList, cloneMap);
        cst.outer = outer;
        if (joinList != null) cst.joinList = joinList.getClone(cloneMap);
        cst.selectListStartIndex = selectListStartIndex;
        cst.selectListEndIndex = selectListEndIndex;
        cst.orderByStartIndex = orderByStartIndex;
        cst.orderByEndIndex = orderByEndIndex;

        return cst;
    }

    public String toString() {
        return super.toString() + (outer ? " OUTER " : " ") +
                (table == null ? " NO_TABLE ": table.name) +
                (alias == null ? "": "aliased to " + alias) +
                (jdbcField != null ? " " + jdbcField : "") +
                (var != null ? " " + var : "");
    }

    /**
     * Count the number of columns in the select list and all joined to lists.
     */
    public int getSelectListColumnCount() {
        if (selectList == null) return 0;
        int count = 0;
        for (SqlExp se = selectList; se != null; se = se.getNext()) {
            count++;
        }
        count += countSelect(joinList);
        return count;
    }

    /**
     * Dump debugging info to System.out.
     */
    public void dump(String indent) {
        Debug.OUT.println(indent + this);
        String is = indent + "  ";
        if (selectList != null) {
            Debug.OUT.println(indent + "selectList");
            selectList.dumpList(is);
        }
        if (joinList != null) {
            Debug.OUT.println(indent + "joinList");
            joinList.dumpList(is);
        }
        if (subSelectJoinExp != null) {
            Debug.OUT.println(indent + "subSelectJoinExp");
            subSelectJoinExp.dump(is);
        }
        if (whereExp != null) {
            Debug.OUT.println(indent + "whereExp");
            whereExp.dump(is);
        }
        if (orderByList != null) {
            Debug.OUT.println(indent + "orderByList");
            orderByList.dumpList(is);
        }
    }

    /**
     * Add a Join to end of the joinList.
     */
    public void addJoin(Join join) {
        if (joinList == null) {
            joinList = join;
        } else {
            for (Join j = joinList; ;) {
                Join next = j.next;
                if (next == null) {
                    j.next = join;
                    return;
                }
                j = next;
            }
        }
    }

    /**
     * Add a Join to end of the joinList. This is an attempt to not add uncess.
     * joins.
     */
    public void addJoinMerge(Join aJoin) {
        if (joinList == null) {
            joinList = aJoin;
        } else {
            getTailJoin(joinList).next = aJoin;
            addJoinImp(joinList, aJoin, aJoin);
        }
    }

    private static Join getPrevJoin(Join joinList, Join aJoin) {
        if (joinList.next == null) {
            throw BindingSupportImpl.getInstance().internal("");
        }

        for (Join j = joinList;;) {
            if (j.next == aJoin) {
                return j;
            }
            j = j.next;
            if (j == null) return null;
        }
    }

    private static Join getTailJoin(Join joinList) {
        for (Join j = joinList;;) {
            if (j.next == null) return j;
            j = j.next;
        }
    }

    private static void addJoinImp(Join currentJoin, Join aJoinToAdd, Join origJoin) {
        Join j = currentJoin;
        Join restartJoin = null;
        Join stopJoin = getTailJoin(currentJoin);
        for (;j != null;) {
            if (aJoinToAdd == null) {
                break;
            }
            if (Join.isCurrentEqaul(j, aJoinToAdd)) {
                /**
                 * Update the whereExp of the origJoin with the new selectExp
                 */
                if (origJoin.selectExp.whereExp != null) {
                    origJoin.selectExp.whereExp.replaceSelectExpRef(origJoin.selectExp,
                            j.selectExp);
                }

                Join subCurrentJoin = j.selectExp.joinList;
                Join subJoinToAdd = aJoinToAdd.selectExp.joinList;

                if (subCurrentJoin == null && subJoinToAdd == null) {
                } else if (subJoinToAdd == null) {
                } else if (subCurrentJoin == null) {
                    //just set it
                    j.selectExp.joinList = aJoinToAdd.selectExp.joinList;
                    ((JoinExp)aJoinToAdd.selectExp.joinList.exp).setLeftTable(j.selectExp);
                } else {
                    addJoinImp(subJoinToAdd, subCurrentJoin, origJoin);
                }
                Join tmpJoin = aJoinToAdd.next;
                /**
                 * Fix up the lList
                 */
                getPrevJoin(currentJoin, aJoinToAdd).next = aJoinToAdd.next;
                aJoinToAdd.next = null;

                aJoinToAdd = tmpJoin;
                j = j.next;
            } else {
                if (j.next == stopJoin) {
                    //no match found so move to next aJoinToAdd and to currentJoin
                    aJoinToAdd = aJoinToAdd.next;
                    j = restartJoin;
                } else {
                    j = j.next;
                }
            }

        }
    }

    /**
     * Add a join to the end of the join list.
     */
    public Join addJoin(JdbcColumn[] leftCols, JdbcColumn[] rightCols,
            SelectExp right) {
        if (Debug.DEBUG) {
            System.out.println("SelectExp.addJoin from " + table.name + " to " + right.table.name);
            String join;
            if (right.outer) {
                join = "OUTER";
            } else {
                join = "INNER";
            }
            System.out.println("--- SelectExp.addJoin from "
                    + leftCols[0].table.name + " -- " + join + " --> "
                    + right.table.name);
        }
        Join j = new Join();
        j.exp = createJoinExp(leftCols, rightCols, right);
        j.selectExp = right;
        addJoin(j);

        return j;
    }

    /**
     * Search our join list for a join for the field. This is not recursive
     * i.e. it only finds joins in our join list. Returns null if not found.
     * @see #findJoinRec(com.versant.core.jdbc.metadata.JdbcField)
     */
    public Join findJoin(JdbcField jdbcField) {
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp.jdbcField == jdbcField) return j;
        }
        return null;
    }

    /**
     * Find a join by table and refField.
     */
    public Join findJoin(JdbcTable table, JdbcField jdbcField) {
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp.jdbcField == jdbcField
                    && j.selectExp.table == table) return j;
        }
        return null;
    }

    public SelectExp findTableRecursive(JdbcTable t, JdbcField field) {
        if (t == table && jdbcField == field) return this;
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp.table == t && j.selectExp.jdbcField == field) return j.selectExp;
        }

        for (Join j = joinList; j != null; j = j.next) {
            SelectExp se = j.selectExp.findTableRecursive(t, field);
            if (se != null) return se;
        }
        return null;
    }

    /**
     * Recursively search our join list for a join for the field. Returns null
     * if not found.
     * @see #findJoin(com.versant.core.jdbc.metadata.JdbcField)
     */
    public Join findJoinRec(JdbcField jdbcField) {
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp.jdbcField == jdbcField) return j;
            Join ans = j.selectExp.findJoinRec(jdbcField);
            if (ans != null) return ans;
        }
        return null;
    }

    /**
     * Search our join list for a join to the expression. This is not recursive
     * i.e. it only finds joins in our join list. Returns null if not found.
     */
    public Join findJoin(SelectExp se) {
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp == se) return j;
        }
        return null;
    }

    /**
     * If our table is t then return this. Otherwise search our join list
     * for a join for the table. This is not recursive i.e. it only finds
     * tables in our join list. Returns null if not found.
     */
    public SelectExp findTable(JdbcTable t) {
        if (t == table) {
            return this;
        }
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp.table == t) {
                return j.selectExp;
            }
        }
        return null;
    }

    public SelectExp findTableRecursive(JdbcTable t) {
        if (t == table) return this;
        for (Join j = joinList; j != null; j = j.next) {
            if (j.selectExp.table == t) return j.selectExp;
        }

        for (Join j = joinList; j != null; j = j.next) {
            SelectExp se = j.selectExp.findTable(t);
            if (se != null) return se;
        }
        return null;
    }

    /**
     * Create an expression to join us to right.
     */
    public SqlExp createJoinExp(JdbcColumn[] leftCols,
            JdbcColumn[] rightCols, SelectExp right) {
        if (leftCols.length == 1) {
            return new JoinExp(leftCols[0], this,
                    rightCols[0], right);
        } else {
            JoinExp first = new JoinExp(leftCols[0], this, rightCols[0], right);
            int nc = leftCols.length;
            SqlExp j = first;
            for (int i = 1; i < nc; i++) {
                j = j.setNext(new JoinExp(leftCols[i], this, rightCols[i], right));
            }
            return new AndJoinExp(first);
        }
    }

    public Join getLastJoin() {
        for (Join j = joinList; j != null; j = j.next) {
            if (j.next == null) {
                return j;
            }
        }
        return null;
    }

    /**
     * Create an aliases for any tables we may have if we do not already have
     * an alias.
     */
    public int createAlias(int index) {
        if (alias == null) {
            if (index < 26) {
                alias = new String(new char[]{(char)(index + 'a')});
            } else {
                alias = "t" + index;
            }
            ++index;
        }

        if (!done) {
            done = true;
            for (Join j = joinList; j != null; j = j.next) {
                index = j.createAlias(index);
            }
            for (SqlExp e = whereExp; e != null; e = e.getNext()) {
                index = e.createAlias(index);
            }
        }
        return index;
    }

    /**
     * Replace any references to old with nw. This is used when redundant
     * joins are removed.
     */
    public void replaceSelectExpRef(SelectExp old, SelectExp nw) {
        if (whereExp != null) whereExp.replaceSelectExpRef(old, nw);
        if (subSelectJoinExp != null) subSelectJoinExp.replaceSelectExpRef(old, nw);
    }

    /**
     * Normalize this node i.e. transform it into its simplist possible form.
     * This will turn sub selects into joins and so on.
     */
    public SqlExp normalize(SqlDriver driver, SelectExp sel, boolean convertExists) {
        for (Join j = joinList; j != null; j = j.next) {
            SelectExp se = j.selectExp;
            se.normalize(driver, sel, convertExists);
            if (convertExists && se.whereExp != null) {
                j.findDeepestJoin().appendJoinExp(se.whereExp);
                se.whereExp = null;
            }
        }

        if (whereExp == null) return null;
        SqlExp r = whereExp.normalize(driver, this, convertExists);
        if (r != null) whereExp = r;

        int cj = whereExp.getConvertToJoin();
        if (cj == SqlExp.YES || (convertExists && cj >= SqlExp.YES_DISTINCT)) {
            // convert to join
            boolean not = convertExists && cj == SqlExp.YES_DISTINCT_NOT;
            SelectExp sub;
            if (not) {
                sub = (SelectExp)(whereExp.childList.childList);
            } else {
                sub = (SelectExp)(whereExp.childList);
            }
			if ( sub != null ) {
				Join j = new Join();
				j.selectExp = sub;
				j.exp = sub.subSelectJoinExp;
				sub.subSelectJoinExp = null;
				if (sub.whereExp != null) { // add to join clause
					Join jj = j;
					for (;;jj = jj.selectExp.joinList) {
						if (jj.selectExp.joinList == null) break;
					}
					jj.appendJoinExp(sub.whereExp);
					sub.whereExp = null;
				}
				addJoin(j);
			}
            if (not) {
                if (sub != null) {
                    sub.outer = true;
                    whereExp = sub.getOuterJoinNotMatchedExp();
                }
            } else {
                whereExp = null;
            }
            if (cj >= SqlExp.YES_DISTINCT) distinct = true;
            return null;
        }
        return null;
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s      Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        // put in the select list (ours and all of our join tables)
        boolean putOrderColsInSelect = driver.isPutOrderColsInSelect();
        boolean selectListAppendedWithOrderByExp = false;

        //Create a ColumnStruct[] for all the fields in the 'order by' exp.
        ColumnStruct[] orderByStruct = createColumnStruct(orderByList);

        int start = s.size();
        if (distinct) s.append("SELECT DISTINCT ");
        else s.append("SELECT ");
        selectListStartIndex = s.size();
        int colCount = appendFirstSelect(selectList, driver, s);
        colCount += appendSelect(joinList, driver, s);
        if (Debug.DEBUG) {
            if (colCount != getSelectListColumnCount()) {
                throw BindingSupportImpl.getInstance().internal("");
            }
        }

        if (orderByList != null) {
            if (putOrderColsInSelect || (groupByList != null && driver.putOrderColsInGroupBy())) {
                selectListAppendedWithOrderByExp = appendSelectForOrderBy(orderByList,
                        driver, s, orderByStruct);
            }
        }

        finishSelectList(s, start);
        selectListEndIndex = s.size();

        // put in the from list
        s.append(" FROM ");
        driver.appendSqlFrom(table, alias, s);
        appendFrom(joinList, driver, s);

        // put all the expressions for the where clause into a list
        SqlExp list = new SqlExp();
        SqlExp pos = list;
        if (subSelectJoinExp != null) pos = pos.setNext(subSelectJoinExp);
        if (!driver.isAnsiJoinSyntax()) {
            findWhereExp(findWhereJoinExp(pos));
        } else {
            findWhereExp(pos);
        }

        // build a single expression from this list (joins exps with and)
        SqlExp exp = merge(list.getNext());
        if (exp != null) {
            s.append(" WHERE ");
            exp.appendSQL(driver, s, null);
        }

        exp = groupByList;
        int gIndex = 1;
        if (exp != null) {
            boolean thisDone = false;
            s.append(" GROUP BY ");
            for (; ;) {
                if (gIndex > 1) {
                    s.append(',');
                    s.append(' ');
                }
                if (exp instanceof GroupByThisExp) {
                    if (!thisDone) {
                        thisDone = true;
                        appendFirstSelect(selectList, driver, s, selectListCountBeforeAggregate);
                        appendSelect(joinList, driver, s);
                    }
                } else {
                    // The group by list can contain ColumnExp and OrderExp
                    // expressions. The OrderExp's just wrap a ColumnExp and
                    // add a descending indicator.
                    ColumnExp ce;
                    if (exp instanceof ColumnExp) {
                        ce = (ColumnExp)exp;
                    } else {
                        ce = (ColumnExp)exp.childList;
                    }

                    if (ce.isAliasedColumn()) {
                        exp.appendSQL(driver, s, null);
                    } else if (driver.useColumnIndexForGroupBy()) {
                        int i = findIndexInSelectList(selectList, ce.selectExp, ce.col.name);
                        if (i >= 0) {
                            s.append(i);
                        } else {
                            exp.appendSQL(driver, s, null);
                        }
                    } else {
                        exp.appendSQL(driver, s, null);
                    }
                }
                gIndex++;
                if ((exp = exp.getNext()) == null) break;
            }
        }

        //update the groupBy expression with fields that was added to the selectlist
        if (selectListAppendedWithOrderByExp && groupByList != null) {
            appendGroupBy(s, orderByStruct, gIndex == 1, driver);
        }

        exp = havingExp;
        if (exp != null) {
            havingExp.appendSQL(driver, s, null);
        }

        // put in the order by list
        exp = orderByList;
        if (exp != null) {
            orderByStartIndex = s.size();
            s.append(" ORDER BY ");
            if (driver.isUseIndexesForOrderCols()) {
                int index = 1;
                boolean first = true;
                for (; ;) {
                    if (first) {
                        first = false;
                    } else {
                        s.append(',');
                        s.append(' ');
                    }
                    // The order by list can contain ColumnExp and OrderExp
                    // expressions. The OrderExp's just wrap a ColumnExp and
                    // add a descending indicator.
                    ColumnExp ce;
                    if (exp instanceof ColumnExp) {
                        ce = (ColumnExp)exp;
                    } else {
                        ce = (ColumnExp)exp.childList;
                    }

                    if (ce.isAliasedColumn()) {
                        exp.appendSQL(driver, s, null);
                    } else {
                        ColumnStruct colStruct = orderByStruct[index - 1];
                        int i = colStruct.indexInSelectList;
                        if (i > 0) {
                            s.append(i);
                            if (ce != exp) { // exp is OrderExp as it is not ColumnExp
                                OrderExp oe = (OrderExp)exp;
                                if (oe.isDesc()) s.append(" DESC");
                            }
                        } else {
                            //if appended to selectList
                            if (colStruct.columnNme != null) {
                                if (driver.useColAliasForAddedCols()) {
                                    s.append(colStruct.alias);
                                } else {
                                    s.append(colStruct.columnNme);
                                }

                                if (ce != exp) { // exp is OrderExp as it is not ColumnExp
                                    OrderExp oe = (OrderExp)exp;
                                    if (oe.isDesc()) s.append(" DESC");
                                }
                            } else {
                                exp.appendSQL(driver, s, null);
                            }
                        }
                    }
                    index++;
                    if ((exp = exp.getNext()) == null) break;
                }
            } else {
                //use the column name
                int index = 1;
                boolean first = true;
                for (; exp != null; exp = exp.getNext()) {
                    if (first) {
                        first = false;
                    } else {
                        s.append(',');
                        s.append(' ');
                    }
                    ColumnExp ce;
                    if (exp instanceof ColumnExp) {
                        ce = (ColumnExp)exp;
                    } else {
                        ce = (ColumnExp)exp.childList;
                    }
                    if (ce.isAliasedColumn()) {
                        exp.appendSQL(driver, s, null);
                    } else {
                        ColumnStruct colStruct = orderByStruct[index - 1];
                        int i = colStruct.indexInSelectList;
                        if (i > 0) {
                            exp.appendSQL(driver, s, null);
                        } else {
                            if (selectListAppendedWithOrderByExp) {
                                if (driver.useColAliasForAddedCols()) {
                                    s.append(colStruct.alias);
                                } else {
                                    s.append(colStruct.columnNme);
                                }
                                if (ce != exp) { // exp is OrderExp as it is not ColumnExp
                                    OrderExp oe = (OrderExp)exp;
                                    if (oe.isDesc()) s.append(" DESC");
                                }
                            } else {
                                exp.appendSQL(driver, s, null);
                            }
                        }
                    }
                    index++;
                }
            }
            orderByEndIndex = s.size();
        } else {
            orderByStartIndex = orderByEndIndex = 0;
        }

        if (forUpdate && (!distinct || driver.isSelectForUpdateWithDistinctOk())) {
            char[] a = driver.getSelectForUpdate();
            if (a != null) {
                s.append(a);
                if (driver.isSelectForUpdateAppendTable()) {
                    if (alias == null) {
                        s.append(table.name);
                    } else {
                        s.append(alias);
                    }
                }
            }
        }
    }

    /**
     * Find the index of a matching entry (i.e. same column from same table)
     * in the select list (first is 1) for ce or -1 if none found.
     */
    private int findIndexInSelectList(SqlExp toSearch, SelectExp se, String name) {
        int[] indexArray = new int[2];
        findIndexInSelectListImp(toSearch, se, name, indexArray);

        if (indexArray[1] == 0) {
            //must check more
            findIndexInJoin(joinList, se, name, indexArray);
        }

        if (indexArray[1] == 1) {
            return indexArray[0] + 1;
        } else {
            return -1;
        }
    }

    private void findIndexInJoin(Join j, SelectExp exp, String name,
            int[] index) {
        for (; j != null; j = j.next) {
            SelectExp se = j.selectExp;
            findIndexInSelectListImp(se.selectList, exp, name, index);

            if (index[1] == 0) {
                findIndexInJoin(se.joinList, exp, name, index);
            }
            break;
        }
    }

    private void findIndexInSelectListImp(SqlExp se, SelectExp exp,
            String name, int[] index) {
        for (SqlExp e = se; e != null; e = e.getNext(), index[0]++) {
            if (e instanceof ColumnExp) {
                ColumnExp ce = (ColumnExp)e;
                if (ce.selectExp == exp && ce.col.name.equals(name)) {
                    index[1] = 1;
                    return;
                }
            }
        }
    }

    private static boolean isDesc(SqlExp e) {
        return e instanceof OrderExp && ((OrderExp)e).isDesc();
    }

    /**
     * Check that the select list is valid.
     */
    protected void finishSelectList(CharBuf s, int start) {
        int sz = s.size();
        int n = sz - start;
        if (n <= 7) {
            if (start == 0) {
                throw BindingSupportImpl.getInstance().internal("no columns in select list");
            }
            s.append('1');
        } else {
            s.setSize(sz - 2);   // remove the extra comma and space
        }
    }

    /**
     * Recursively append the select lists for all the joins to s using a
     * depth first traversal. NOP if j is null.
     */
    private int appendSelect(Join j, SqlDriver driver, CharBuf s) {
        int count = 0;
        for (; j != null; j = j.next) {
            SelectExp se = j.selectExp;
            count += appendSelect(se.selectList, driver, s);
            count += appendSelect(se.joinList, driver, s);
        }
        return count;
    }

    /**
     * Recursively count the colums of all joined to selectExp's selectList.
     */
    private int countSelect(Join j) {
        int count = 0;
        for (; j != null; j = j.next) {
            SelectExp se = j.selectExp;
            count += countSelect(se.selectList);
            count += countSelect(se.joinList);
        }
        return count;
    }

    /**
     * Append all the expressions in the list starting at e. NOP if e is null.
     * Follow each expression with ', '. Record the last index of
     */
    private int appendFirstSelect(SqlExp e, SqlDriver driver, CharBuf s) {
        if (e == null) return 0;
        int count = 1;
        e.appendSQL(driver, s, null);
        selectListFirstColEndIndex = s.size();
        s.append(',');
        s.append(' ');
        for (e = e.getNext(); e != null; e = e.getNext()) {
            e.appendSQL(driver, s, null);
            s.append(',');
            s.append(' ');
            count++;
        }
        return count;
    }

    private void appendFirstSelect(SqlExp e, SqlDriver driver, CharBuf s, int columnCount) {
        if (e == null) return;
        e.appendSQL(driver, s, null);
        selectListFirstColEndIndex = s.size();
        int count = 1;
        for (e = e.getNext(); e != null; e = e.getNext()) {
            if (count++ >= columnCount) break;
            s.append(',');
            s.append(' ');
            e.appendSQL(driver, s, null);
        }
    }

    /**
     * Append all the expressions in the list starting at e. NOP if e is null.
     * Follow each expression with ', '.
     */
    private int appendSelect(SqlExp e, SqlDriver driver, CharBuf s) {
        int count = 0;
        for (; e != null; e = e.getNext()) {
            count++;
            e.appendSQL(driver, s, null);
            s.append(',');
            s.append(' ');
        }
        return count;
    }

    private int countSelect(SqlExp e) {
        int count = 0;
        for (; e != null; e = e.getNext()) {
            count++;
        }
        return count;
    }

    /**
     * Append all the expressions in the list starting at e. NOP if e is null.
     * Follow each expression with an alias and ', '. This is used to add
     * columns used in the order by to the select list for databases that
     * require this e.g. informix.
     */
    private boolean appendSelectForOrderBy(SqlExp e, SqlDriver driver,
            CharBuf s, ColumnStruct[] columnStructs) {
        boolean appended = false;
        int c = 1;
        for (; e != null; e = e.getNext()) {
            ColumnStruct colStruct = columnStructs[c - 1];
            if (colStruct.indexInSelectList == -1) {
                int offset = s.size();
                if (e instanceof OrderExp) {
                    e.childList.appendSQL(driver, s, null);
                } else {
                    e.appendSQL(driver, s, null);
                }
                colStruct.columnNme = s.toString(offset, s.size() - offset);
                colStruct.alias = ALIAS_PREPEND_CONSTANT + "j" + c;

                //not in select list
                s.append(driver.getAliasPrepend());
                s.append(colStruct.alias);
                s.append(',');
                s.append(' ');
                appended = true;
            }
            c++;
        }
        return appended;
    }

    private ColumnStruct[] createColumnStruct(SqlExp e) {
        SqlExp startExp = e;
        int count = 0;
        for (; e != null; e = e.getNext()) {
            count++;
        }
        if (count == 0) return EMPTY_COLUMS_STRUCT;
        ColumnStruct[] colStructs = new ColumnStruct[count];
        count = 0;
        e = startExp;
        for (; e != null; e = e.getNext()) {
            ColumnExp ce;
            if (e instanceof ColumnExp) {
                ce = (ColumnExp)e;
            } else {
                ce = (ColumnExp)e.childList;
            }
            if (ce.isAliasedColumn()) {
                colStructs[count++] = new ColumnStruct(0);
            } else {
                colStructs[count++] = new ColumnStruct(findIndexInSelectList(selectList, ce.selectExp, ce.col.name));
            }

        }
        return colStructs;
    }

    /**
     * This is a struct to hold information about the order by columns that were added.
     */
    private class ColumnStruct {
        /**
         * The alias of the appended column.
         */
        String alias;
        /**
         * The columnName of the appended column. This field is only filled in
         * if this column was appended to the original select exp.
         */
        String columnNme;
        /**
         * The index of this column in the select list.
         */
        int indexInSelectList;

        public ColumnStruct(int indexInSelectList) {
            this.indexInSelectList = indexInSelectList;
        }
    }

    /**
     * Some drivers will automatically add fields that are in the orderBy
     * to the select exp. This method must check to see if these fields
     * are in the groupby and if not it must be added.
     */
    private void appendGroupBy(CharBuf s, ColumnStruct[] columnStructs,
                               boolean first, SqlDriver driver) {
        for (int i = 0; i < columnStructs.length; i++) {
            ColumnStruct columnStruct = columnStructs[i];
            if (columnStruct.columnNme != null) {
                if (first) {
                    first = false;
                } else {
                    s.append(',');
                }
                if (driver.useColAliasForAddedCols()) {
                    s.append(columnStruct.alias);
                } else {
                    s.append(columnStruct.columnNme);
                }
            }
        }
    }

    /**
     * Recursively append the from list entries for all the joins to s using a
     * depth first traversal. NOP if j is null.
     */
    private void appendFrom(Join j, SqlDriver driver, CharBuf s) {
        for (; j != null; j = j.next) {
            SelectExp se = j.selectExp;
//            if (j.isMerged()) continue;
            if (!j.isMerged()) {
                driver.appendSqlFromJoin(se.table, se.alias, j.exp, se.outer, s);
            }
            appendFrom(se.joinList, driver, s);
        }
    }

    /**
     * Recursively add all where expressions we can find to list and
     * return the new head of list.
     */
    private SqlExp findWhereExp(SqlExp list) {
        SqlExp e = whereExp;
        if (e != null) list = list.setNext(e);
        for (Join j = joinList; j != null; j = j.next) {
            list = j.selectExp.findWhereExp(list);
        }
        return list;
    }

    /**
     * Recursively add all join expressions we can find to list and
     * return the new head of list.
     */
    private SqlExp findWhereJoinExp(SqlExp list) {
        for (Join j = joinList; j != null; j = j.next) {
            SqlExp e = j.exp;
            if (e != null) list = list.setNext(e);
            list = j.selectExp.findWhereJoinExp(list);
        }
        return list;
    }

    /**
     * Combine a list of expressions into a single expression by anding
     * all the expressions together. This flattens nested 'and' expressions.
     */
    private SqlExp merge(SqlExp first) {
        if (first == null) return null;
        if (first.getNext() == null) return first;
        // flatten enclosed 'and' expressions by adding them to the list
        // and processing their children
        for (SqlExp pos = first; pos != null;) {
            final SqlExp e = pos.getNext();
            if (e instanceof AndExp) {
                pos = pos.setNext(e.childList);
                SqlExp f = pos;
                for (; f.getNext() != null; f = f.getNext()) ;
                f.setNext(e.getNext());
            } else {
                pos = e;
            }
        }
        return new AndExp(first);
    }

    /**
     * Set our outer flag and follow all our joins and make then outer as
     * well.
     */
    public void setOuterRec() {
        outer = true;
        for (Join j = joinList; j != null; j = j.next) {
            j.selectExp.setOuterRec();
        }
    }

    /**
     * Get an expression that is true if this join does not produce a fully
     * populated row (i.e. one or more of the outer most pk columns are null
     * indicating that an outer join was not matched).
     */
    public SqlExp getOuterJoinNotMatchedExp() {
        if (joinList == null) {
            return new IsNullExp(table.pk[0].toSqlExp(this));
        } else if (joinList.next == null) {
            return joinList.selectExp.getOuterJoinNotMatchedExp();
        } else {
            // join expressions with OrExp
            SqlExp root = joinList.selectExp.getOuterJoinNotMatchedExp();
            SqlExp p = root;
            for (Join j = joinList.next; ;) {
                p = p.setNext(j.selectExp.getOuterJoinNotMatchedExp());
                if ((j = j.next) == null) break;
            }
            return new OrExp(root);
        }
    }

    /**
     * Get an expression that is true if this join produces a fully
     * populated row (i.e. all of the outer most pk columns are not null
     * indicating that all outer joins were matched).
     */
    public SqlExp getOuterJoinMatchedExp() {
        if (joinList == null) {
            return new IsNotNullExp(table.pk[0].toSqlExp(this));
        } else if (joinList.next == null) {
            return joinList.selectExp.getOuterJoinMatchedExp();
        } else {
            // join expressions with AndExp
            SqlExp root = joinList.selectExp.getOuterJoinMatchedExp();
            SqlExp p = root;
            for (Join j = joinList.next; ;) {
                p = p.setNext(j.selectExp.getOuterJoinMatchedExp());
                if ((j = j.next) == null) break;
            }
            return new AndExp(root);
        }
    }

    /**
     * Add an order-by expression this to this select.
     */
    public SqlExp addOrderBy(OrderNode[] orders, boolean append) {
        return addOrderBy(orders, append, JDOQLNodeToSqlExp.INSTANCE);
    }

    /**
     * Add an order-by expression this to this select.
     */
    public SqlExp addOrderBy(OrderNode[] orders, boolean append,
            JDOQLNodeToSqlExp visitor) {
        // create the order by list including joins to pickup columns as needed
        int len = orders.length;
        SqlExp oePos = null;
        if (append) {
            oePos = findTailOrderByExp();
        }
        for (int i = 0; i < len; i++) {
            OrderNode on = orders[i];
            SelectExp se = this;
            Node prevNode = null;
            for (Node n = on.childList; n != null; n = n.childList) {
                if (n instanceof FieldNode) {
                    SelectExp oSe = se;
                    if (prevNode != null && prevNode instanceof FieldNavNode) {
                        JdbcField refField = (JdbcField)((FieldNavNode)prevNode).fmd.storeField;
                        JdbcField orderByField = (JdbcField)((FieldNode)n).fmd.storeField;

                        Join join = se.findJoin(orderByField.mainTable, refField);
                        if (join == null) {
                            oSe = new SelectExp();
                            oSe.table = orderByField.mainTable;
                            oSe.outer = true;
                            oSe.jdbcField = refField;

                            if (refField instanceof JdbcPolyRefField) {
                                se.addJoin(((JdbcPolyRefField)refField).refCols, oSe.table.pk, oSe);
                            } else {
                                se.addJoin(refField.mainTableCols, oSe.table.pk, oSe);
                            }

                        } else {
                            oSe = join.selectExp;
                        }

                    } else if (prevNode == null) {
                        //look to join to superTable if in different table
                        JdbcField orderByField = (JdbcField)((FieldNode)n).fmd.storeField;
                        if (orderByField.mainTable != se.table) {
                            oSe = se.findTable(orderByField.mainTable);
                            if (oSe == null) {
                                oSe = new SelectExp();
                                oSe.table = orderByField.mainTable;
                                oSe.outer = false;
                                se.addJoin(se.table.pk, oSe.table.pk, oSe);
                            }
                        }
                    }

                    oePos = addOrderExp(
                            handleOrderByFieldNode((FieldNode)n, oSe, visitor),
                            (on.order == OrderNode.ORDER_DESCENDING),
                            oePos);
                } else if (n instanceof AsValueNode) {
                    oePos = addOrderExp(new ColumnExp(((AsValueNode)n).value),
                            (on.order == OrderNode.ORDER_DESCENDING),
                            oePos);
                } else if (n instanceof ReservedFieldNode) {
                    ColumnExp[] cExps = createColumnExpForOrdering(((ReservedFieldNode)n).getTarget(), se);
                    for (int j = 0; j < cExps.length; j++) {
                        oePos = addOrderExp(cExps[j],
                                (on.order == OrderNode.ORDER_DESCENDING),
                                oePos);
                    }
                } else if (n instanceof FieldNavNode) {
                    se = handleOrderByFieldNavNode((FieldNavNode)n, se, prevNode);
                } else {
                    throw BindingSupportImpl.getInstance().internal("Invalid node in orders: " + n);
                }
                prevNode = n;
            }
        }
        return oePos;
    }

    /**
     * Insert the columns at the start of the orderByList as ascending.
     */
    public void prependOrderByForColumns(JdbcColumn[] columns) {
        SqlExp current = orderByList;

        SqlExp oePos = null;
        ColumnExp[] columnExps = new ColumnExp[columns.length];
        for (int i = 0; i < columnExps.length; i++) {
            columnExps[i] = new ColumnExp(columns[i], this, null);
            oePos = addOrderExp(columnExps[i], false, oePos);
        }

        if (current != null && oePos != null) {
            oePos.setNext(current);
        }
    }

    /**
     * Append the columns at the end of the orderByList as ascending.
     */
    public void appendOrderByForColumns(JdbcColumn[] columns) {
        SqlExp oePos = findTailOrderByExp();
        for (int i = 0; i < columns.length; i++) {
            if (containsOrderExp(columns[i])) {
                continue;
            }
            oePos = addOrderExp(new ColumnExp(columns[i], this, null), false, oePos);
        }
    }

    /**
     * Append the column at the end of the orderByList as ascending.
     */
    public void appendOrderByForColumns(JdbcColumn column) {
        if (!containsOrderExp(column)) {
            addOrderExp(new ColumnExp(column, this, null), false, findTailOrderByExp());
        }
    }


    /**
     * Append the orderExp at the end of the current orderExp.
     */
    public void appendOrderByExp(SqlExp orderExp) {
        SqlExp oePos = findTailOrderByExp();
        if (oePos == null) {
            orderByList = orderExp;
        } else {
            if (oePos != orderExp) {
                oePos.setNext(orderExp);
            }
        }
    }

    /**
     * Append the columns to the orderByList
     */
    public void appendOrderByForColumns(JdbcColumn[] columns, SelectExp se) {
        SqlExp oePos = findTailOrderByExp();
        for (int i = 0; i < columns.length; i++) {
            if (containsOrderExp(columns[i])/* || se.containsOrderExp(columns[i])*/) {
                continue;
            }
            oePos = addOrderExp(new ColumnExp(columns[i], se, null), false, oePos);
        }
    }

    public boolean containsOrderExp(JdbcColumn jdbcColumn) {
        for (SqlExp e = orderByList; e != null; e = e.getNext()) {
            if (((ColumnExp)((OrderExp)e).childList).col == jdbcColumn) return true;
        }
        return false;
    }

    /**
     * return the tail of the orderExp
     */
    private SqlExp findTailOrderByExp() {
        for (SqlExp e = orderByList; e != null; e = e.getNext()) {
            if (e.getNext() == null) return e;
        }
        return null;
    }

    public ColumnExp[] createColumnExpForOrdering(ClassMetaData target,
            SelectExp root) {
        JdbcColumn[] pk = ((JdbcClass)target.storeClass).table.pk;
        ColumnExp[] columnExps = new ColumnExp[pk.length];
        for (int i = 0; i < pk.length; i++) {
            columnExps[i] = new ColumnExp(pk[i], root, pk[i].refField);
        }
        return columnExps;
    }

    private SqlExp addOrderExp(SqlExp list, boolean desc, SqlExp oePos) {
        // add a new OrderExp to the order by list
        OrderExp oe = new OrderExp(list, desc);
        if (oePos == null) {
            orderByList = oe;
        } else {
            oePos.setNext(oe);
        }
        oePos = oe;
        return oePos;
    }

    /**
     * The idea here is to only add a join if the previous node is a FieldNavNode.
     */
    private SelectExp handleOrderByFieldNavNode(FieldNavNode nav, SelectExp se, Node prevNode) {
        if (prevNode != null && prevNode instanceof FieldNavNode) {
            //check if join is needed
            // see if there is a join to the table for the class we are navigating to
            JdbcField f = (JdbcField)((FieldNavNode)prevNode).fmd.storeField;
            Join j = se.findJoin(f);
            if (j != null) return j.selectExp;

            // no join so add one
            SelectExp next = new SelectExp();
            next.jdbcField = f;
            ClassMetaData targetClass = ((FieldNavNode)prevNode).targetClass;
            next.table = ((JdbcClass)targetClass.storeClass).table;
            if (f instanceof JdbcPolyRefField) {
                se.addJoin(((JdbcPolyRefField)f).refCols, next.table.pk, next);
            } else {
                se.addJoin(f.mainTableCols, next.table.pk, next);
            }
            next.outer = f.fmd.nullValue != MDStatics.NULL_VALUE_EXCEPTION;
            return next;
        } else {
            return se;
        }
    }

    private SqlExp handleOrderByFieldNode(FieldNode fn, SelectExp se,
            JDOQLNodeToSqlExp visitor) {
        if (!(fn.fmd.storeField instanceof JdbcSimpleField)) {
            throw BindingSupportImpl.getInstance().internal(
                    "Only simple fields may be used in an " +
                    "ordering statement: " + fn.fmd);
        }
        SqlExp list = visitor.toSqlExp(fn, se, null, 0, null);
        if (list.childList != null) {
            throw BindingSupportImpl.getInstance().internal(
                    "Only single column fields may be used in an " +
                    "ordering statement: " + fn.fmd);
        }
        return list;
    }

    /**
     * Does this select contain a join to a many table that may produce
     * multiple rows? This is used to decide on setting the distinct flag
     * if this select is converterd into an outer join.
     */
    public boolean isJoinToManyTable() {
        return jdbcField instanceof JdbcCollectionField;
    }

    /**
     * Append e to the where clause of this select. This will create a new
     * AndExp if needed.
     */
    public void appendToWhereExp(SqlExp e) {
        whereExp = SqlExp.appendWithAnd(whereExp, e);
    }

    /**
     * Append e to the subSelectJoinExp clause of this select. This will
     * create a new AndExp if needed.
     */
    public void appendToSubSelectJoinExp(SqlExp e) {
        subSelectJoinExp = SqlExp.appendWithAnd(subSelectJoinExp, e);
    }

    /**
     * Used to obtain a selectexp for a field. This will add a join to the supertable
     * if not already added.
     */
    public static SelectExp createJoinToSuperTable(SelectExp root, JdbcField jdbcField) {
        return createJoinToSuperTable(root, ((JdbcClass)jdbcField.fmd.classMetaData.storeClass).table);
    }

    /**
     * Used to obtain a selectexp for a field. This will add a join to the supertable
     * if not already added.
     */
    public static SelectExp createJoinToSuperTable(SelectExp root, SelectExp joinFromExp,
                                                   JdbcColumn[] lJoinColumns, JdbcField jdbcField) {
        if (Debug.DEBUG) {
            JdbcRefField.isSubTableOf(root.table, jdbcField.fmd.classMetaData);
        }
        if (root.table != ((JdbcClass)jdbcField.fmd.classMetaData.storeClass).table) {
            Join join = joinFromExp.findJoin(jdbcField);
            if (join == null) {
                SelectExp se = new SelectExp();
                se.outer = root.outer;
                se.table = ((JdbcClass)jdbcField.fmd.classMetaData.storeClass).table;
                joinFromExp.addJoin(lJoinColumns, se.table.pk, se);
                return se;
            } else {
                return join.selectExp;
            }
        }
        return root;
    }

    /**
     * Used to obtain a selectexp for a field. This will add a join to the supertable
     * if not already added.
     */
    public static SelectExp createJoinToSuperTable(SelectExp root, JdbcTable table) {
        // If the field is not in the table then join to the sub class table
        SelectExp se = root.findTable(table);
        if (se == null) {
            se = new SelectExp();
            se.outer = root.outer;
            se.table = table;
            root.addJoin(root.table.pk, se.table.pk, se);
        }
        return se;
    }

    public static void dumpJoinList(SelectExp joinFromExp, String val) {
        System.out.println(val + "dumping joinlist");
        Join j = joinFromExp.joinList;
        if (j == null) {
            System.out.println("-- no joins ");
            return;
        }
        for (;;) {
            System.out.println("j = " + j);
            j = j.next;
            if (j == null) break;
        }
    }

    public static void dumpJoinListRec(SelectExp joinFromExp, String indent) {
        Join j = joinFromExp.joinList;
        if (j == null) {
//            System.out.println(indent + "-- no joins --");
            return;
        }
        for (;;) {
            System.out.println(indent + j);
            dumpJoinListRec(j.selectExp, indent + "  ");
            j = j.next;
            if (j == null) break;
        }
    }
    /**
     * Look through the joins to find any equal joins that can be eliminated.
     */
    public static void mergeJoinList(Join j) {
        if (j == null) return;
        if (j.next == null) return;

        for (Join nextJoin = j.next; nextJoin != null; nextJoin = nextJoin.next) {
            if (nextJoin.isMerged()) {
                continue;
            }
            if (Join.isCurrentEqaul(j, nextJoin)) {
                nextJoin.setMergedWith(j);
            }
        }
        mergeJoinList(j.selectExp.joinList);
        mergeJoinList(j.next);
    }

}
