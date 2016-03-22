
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
package com.versant.core.jdo.tools.eclipse;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action class to set JDO Genie nature on a javaproject.
 * 
 */
public class AddJDONature implements IObjectActionDelegate, VersantStatics {
    private IJavaProject currentProject = null;

    public void setActivePart(IAction iAction, IWorkbenchPart iWorkbenchPart) {
    }

    public void run(IAction iAction) {
        try {
            Utils.addJDONature(currentProject);
        } catch (Exception e) {
            VersantPlugin.log(e, "Error adding JDO nature");
        }
    }


    public void selectionChanged(IAction iAction, ISelection iSelection) {
    	currentProject = Utils.findSelectedJavaProject(iSelection);
    }
}
