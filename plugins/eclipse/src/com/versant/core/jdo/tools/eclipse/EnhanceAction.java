
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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Action class to invoke a explicit JDO Genie enhance for a selected project.
 */
public class EnhanceAction implements IWorkbenchWindowActionDelegate {
    private IProject selectedProject;

    public void dispose() {
    }

    public void init(IWorkbenchWindow iWorkbenchWindow) {
    }

    public void run(IAction iAction) {
        enhance(selectedProject);
    }

	/**
	 * 
	 */
	public static void enhance(IProject selectedProject) {
		if (selectedProject == null) return;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Cursor waitCursor = new Cursor(window.getShell().getDisplay(), SWT.CURSOR_WAIT);
		try {
            EnhancerBuilder.enhance(selectedProject);
        } catch (Exception e) {
            Shell shell = new Shell();
			MessageDialog.openInformation(
				shell,
				"Versant OpenAccess Plug-in",
				"Enhancer:\n" +
				 Utils.getStatusMessages(e));
        } finally {
            window.getShell().setCursor(null);
			waitCursor.dispose();
        }
	}

    public void selectionChanged(IAction iAction, ISelection iSelection) {
        selectedProject = Utils.findSelectedProject(iSelection);
    }


}
