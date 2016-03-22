
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 */
public class RemoveJDONature implements IObjectActionDelegate {
    private IJavaProject currentProject;

    public void setActivePart(IAction iAction, IWorkbenchPart iWorkbenchPart) {
    }

    public void run(IAction iAction) {
        removeJDONature(currentProject);
    }

	public static void removeJDONature(IJavaProject currentProject) {
		try {
            IProject proj = currentProject.getProject();

            Utils.removeJDOGenieNature(proj);
            EnhancerBuilder.removeEnhancerBuilder(currentProject.getProject());

            proj.refreshLocal(IResource.DEPTH_INFINITE, null);
            // as both decorators share the same visibility rules, this will work
        } catch (Exception e) {
            Shell shell = new Shell();
            MessageDialog.openInformation(
                shell,
                "Versant OpenAccess Plug-in",
                "Cannot remove JDO nature:\n" +
                Utils.getStatusMessages(e));
        }
	}

    public void selectionChanged(IAction iAction, ISelection iSelection) {
        currentProject = Utils.findSelectedJavaProject(iSelection);
    }
}
