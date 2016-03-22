
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

/**
 * A event from a MdProject.
 *
 * @see MdProject#addMdProjectListener
 */
public class MdProjectEvent extends MdEvent {

    private Object arg;

    /**
     * The projects dirty flag has changed.
     */
    public static final int ID_DIRTY_FLAG = 1;

    /**
     * New parsed meta data is available. This means that the meta data has
     * just been parsed or the JDO engine has been started.
     *
     * @see MdProject#getJdoMetaData
     * @see MdProject#getJdbcStorageManagerFactory
     */
    public static final int ID_PARSED_META_DATA = 2;

    /**
     * One of the data stores has changed.
     *
     * @see MdProject#getDataStoreList
     */
    public static final int ID_DATA_STORE_CHANGED = 3;

    /**
     * The JDO engine has been started.
     */
    public static final int ID_ENGINE_STARTED = 4;

    /**
     * The JDO engine has been stopped.
     */
    public static final int ID_ENGINE_STOPPED = 5;

    /**
     * New parsed database meta data is available. This means that the database has
     * just been parsed.
     *
     * @see MdProject#getDatabaseMetaData
     */
    public static final int ID_PARSED_DATABASE = 6;

    /**
     * One or more classes have been removed from the project. The arg
     * property contains a List of removed classes.
     */
    public static final int ID_CLASSES_REMOVED = 7;

    public MdProjectEvent(MdProject source, int flags) {
        super(source, source, null);
        this.flags = flags;
    }

    public Object getArg() {
        return arg;
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    public MdProject getProject() {
        return (MdProject)source;
    }

    public static String toIdString(int id) {
        switch (id) {
            case ID_DIRTY_FLAG:
                return "ID_DIRTY_FLAG";
            case ID_PARSED_META_DATA:
                return "ID_PARSED_META_DATA";
            case ID_DATA_STORE_CHANGED:
                return "ID_DATA_STORE_CHANGED";
            case ID_ENGINE_STARTED:
                return "ID_ENGINE_STARTED";
            case ID_ENGINE_STOPPED:
                return "ID_ENGINE_STOPPED";
            case ID_PARSED_DATABASE:
                return "ID_PARSED_DATABASE";
            case ID_CLASSES_REMOVED:
                return "ID_CLASSES_REMOVED";
        }
        return "UNKNOWN(" + id + ")";
    }

    public String toString() {
        return "MdProjectEvent " + toIdString(flags) + " " + source + " arg " + arg;
    }
}

