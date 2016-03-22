
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
/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2005/02/18
 * Time: 11:32:51
 * To change this template use File | Settings | File Templates.
 */
package com.versant.core.jdo.tools.workbench;

public class WorkbenchPanelHelperManager {
    private static WorkbenchPanelHelperManager ourInstance = new WorkbenchPanelHelperManager();

    private WorkbenchPanelHelper workbenchPanelHelper;

    public static WorkbenchPanelHelperManager getInstance() {
        return ourInstance;
    }

    private WorkbenchPanelHelperManager() {
    }

    public WorkbenchPanelHelper getWorkbenchPanelHelper() {
        return workbenchPanelHelper;
    }

    public void setWorkbenchPanelHelper(WorkbenchPanelHelper workbenchPanelHelper) {
        this.workbenchPanelHelper = workbenchPanelHelper;
    }
}
