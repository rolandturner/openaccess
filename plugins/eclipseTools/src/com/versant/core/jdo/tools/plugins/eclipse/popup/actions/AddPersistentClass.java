
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
package com.versant.core.jdo.tools.plugins.eclipse.popup.actions;

import com.versant.core.jdo.tools.plugins.eclipse.VOAProjectControler;
import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author dirk
 */
public class AddPersistentClass implements IObjectActionDelegate {

    private IProject iProject;
	private String className;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			VOAProjectControler controler = VOAProjectControler.getInstance(iProject);
			controler.addClass(className);
		} catch (Exception e) {
			VOAToolsPlugin.log(e, "Could not make class '"+className+"' persistent.");
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setISelection(selection);
		VOAProjectControler controler;
		MdProject mdProject;
		boolean enabled;
		try {
			controler = VOAProjectControler.getInstance(iProject);
			mdProject = controler.getMdProject();
			enabled = mdProject.findClassOrInterface(className) == null;
		} catch (Exception e) {
			enabled = false;
		}
		action.setEnabled(enabled);
	}

    public void setISelection(ISelection iSelection){
    	className = null;
    	iProject = null;
        IProject proj = null;
        if (iSelection instanceof StructuredSelection) {
            StructuredSelection sS = (StructuredSelection)iSelection;
            Object el = sS.getFirstElement();
            if (el instanceof IJavaElement) {
                IJavaElement javaElement = ((IJavaElement)el);
				proj = javaElement.getJavaProject().getProject();
	    		IType selectedClass = (IType)javaElement.getAncestor(IJavaElement.TYPE);
	    		if(selectedClass != null){
	    			className = selectedClass.getFullyQualifiedName();
	    		}
            }
        }
        if(proj != null){
            iProject = proj;
        }
    }


}
