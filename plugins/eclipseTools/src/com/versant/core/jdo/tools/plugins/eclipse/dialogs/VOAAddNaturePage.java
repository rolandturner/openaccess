
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

import com.versant.core.jdo.tools.eclipse.VOAConfigComposite;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file Path. The page will only accept file Path without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class VOAAddNaturePage extends WizardPage {

	private IProject project;
    private VOAConfigComposite config;

	public VOAAddNaturePage(IProject project) {
		super("wizardPage");
		setTitle("Select VOA Project file.");
		setDescription("Select VOA Project file.");
		this.project = project;
	}

	public void createControl(Composite parent) {
    	this.config = new VOAConfigComposite(this, project, parent);
    	setControl(config);
	}

	public void store() {
		config.store();
	}
	
	public boolean isPageComplete(){
		return config.validate();
	}
}
