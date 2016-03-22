
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
package com.versant.core.jdo.tools.plugins.eclipse.dialogs;

import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class VOAAddNatureDialog extends Wizard {
	private VOAAddNaturePage page;
	private IProject iProject;

	/**
	 * Constructor for VOAAddNatureDialog.
	 */
	public VOAAddNatureDialog(IProject iProject) {
		super();
		this.iProject = iProject;
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(VOAToolsPlugin.imageDescriptorFromPlugin("Versant","icons/voa32.png"));
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new VOAAddNaturePage(iProject);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		page.store();
		return true;
	}
}
