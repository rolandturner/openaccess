
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

import com.versant.core.metadata.FetchGroup;
import com.versant.core.common.State;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.server.PersistGraph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * State's used by the {@link JdbcStorageManager} must implement this.
 */
public interface JdbcState {

    /**
     * Populate this State from the given ResultSet. The firstCol parameter
     * specifies the column index of the first column to read from rs. All
     * persistent pass 1 fields in the fetch group must be read in order.
     */
    public void copyPass1Fields(ResultSet rs, FetchGroup fetchGroup,
            int firstCol) throws SQLException;

    public void copyPass1Fields(ResultSet rs, JdbcField[] fields);

    /**
     * Set parameters on a PrepareStatement from this State. The firstParam
     * parameter specifies the column index of the first parameter to set.
     * Entries in fieldNos that are less than 0 should be skipped.
     *
     * @param firstFieldNo The index of the first field to set
     * @param lastFieldNo  The index of the last field to set + 1
     * @param tableNo      Set fields with table == jdbcClass.allTables[tableNo]
     * @return the index of the last param set + 1
     */
    public int setParams(PreparedStatement ps, int[] fieldNos,
            int firstFieldNo, int lastFieldNo, int firstParam,
            PersistGraph pGraph, int tableNo) throws SQLException;

    /**
     * Set parameters on a PrepareStatement from this State for fields that
     * are not null and that are included when doing changed optimistic locking.
     * The firstParam parameter specifies the column index of the first
     * parameter. This will not be called for classes that are not stored by
     * the JdbcDataStore or that do not use changed optimistic locking.
     * Entries in fieldNos that are less than 0 should be skipped.
     *
     * @param firstFieldNo The index of the first field to set
     * @param lastFieldNo  The index of the last field to set + 1
     * @param tableNo
     * @return the index of the last param set + 1
     */
    public int setParamsChangedAndNotNull(PreparedStatement ps, int[] fieldNos,
            int firstFieldNo, int lastFieldNo, int firstParam,
            PersistGraph pGraph, int tableNo) throws SQLException;

    /**
     * Set parameters on a PrepareStatement from the optimistic locking field
     * for the class for this State. The firstParam parameter specifies the
     * column index of the first parameter to set.
     *
     * @return the index of the last param set + 1
     * @throws javax.jdo.JDOFatalInternalException
     *          if there is no such field
     * @see com.versant.core.jdbc.metadata.JdbcClass#optimisticLockingField
     */
    public int setOptimisticLockingParams(PreparedStatement ps, int firstParam)
            throws SQLException;

    /**
     * Call the set(rs,...) method on each of the converters for the first
     * numFieldNos entries in stateFieldNos. This is used to handle Oracle
     * style LOB columns.
     *
     * @param firstCol The first column in rs to use
     * @see com.versant.core.jdbc.JdbcConverter#set
     */
    public void setOracleStyleLOBs(ResultSet rs, int[] stateFieldNos,
            int numFieldNos, int firstCol) throws SQLException;

    /**
     * Does this State contain exactly the same null fields as the supplied
     * State? A null field is a field that is filled in mask but that is
     * null or not filled in this state. This must always return true for
     * classes that do not use changed optimistic locking or that are not
     * stored by the JdbcDataStore.
     *
     * @param state State to compare to (will be for same class)
     * @param mask  State providing the filled states to check
     */
    public boolean hasSameNullFields(State state, State mask);

}

