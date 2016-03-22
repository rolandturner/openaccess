
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
package com.versant.core.server;

import java.io.Serializable;

/**
 * Information about a datastore. Currently this is not public and this info
 * is only used by the unit tests. When this class has been cleaned up a bit
 * we can make this public.
 */
public class DataStoreInfo implements Serializable {

    private String name;
    private String dataStoreType;
    private boolean autoIncSupported;
    private boolean scrollableResultSetSupported;
    private String selectForUpdate;
    private boolean jdbc;
    private int majorVersion;
    private boolean preparedStatementPoolingOK;
    private int inheritance;
    private int defaultClassId;
    private boolean dataSource;

    public DataStoreInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the type of datastore (vds, oracle, informix, informixse etc.).
     */
    public String getDataStoreType() {
        return dataStoreType;
    }

    public void setDataStoreType(String dataStoreType) {
        this.dataStoreType = dataStoreType;
    }

    public boolean isAutoIncSupported() {
        return autoIncSupported;
    }

    public void setAutoIncSupported(boolean autoIncSupported) {
        this.autoIncSupported = autoIncSupported;
    }

    public boolean isScrollableResultSetSupported() {
        return scrollableResultSetSupported;
    }

    public void setScrollableResultSetSupported(
            boolean scrollableResultSetSupported) {
        this.scrollableResultSetSupported = scrollableResultSetSupported;
    }

    public String getSelectForUpdate() {
        return selectForUpdate;
    }

    public void setSelectForUpdate(String selectForUpdate) {
        this.selectForUpdate = selectForUpdate;
    }

    public boolean isJdbc() {
        return jdbc;
    }

    public void setJdbc(boolean jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isDataSource() {
        return dataSource;
    }

    public void setDataSource(boolean dataSource) {
        this.dataSource = dataSource;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public boolean isPreparedStatementPoolingOK() {
        return preparedStatementPoolingOK;
    }

    public void setPreparedStatementPoolingOK(
            boolean preparedStatementPoolingOK) {
        this.preparedStatementPoolingOK = preparedStatementPoolingOK;
    }

    public int getInheritance() {
        return inheritance;
    }

    public void setInheritance(int inheritance) {
        this.inheritance = inheritance;
    }

    public int getDefaultClassId() {
        return defaultClassId;
    }

    public void setDefaultClassId(int defaultClassId) {
        this.defaultClassId = defaultClassId;
    }

}
