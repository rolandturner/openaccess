
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 */
public class VersantPropertyPage extends PropertyPage
        implements VersantStatics {
    private IProject project;
    private VOAConfigComposite config;

    public Control createContents(Composite composite) {
        this.project = (IProject)getElement();
    	this.config = new VOAConfigComposite(this, project, composite);
        return config;
    }

    protected void performDefaults() {
    	config.setToDefaults();
    }

    protected void performApply() {
    	if(performOk()){
            super.performApply();
        }
    }

    public boolean performOk() {
        if (config != null && config.validate()) {
        	config.store();
        	config.resetFromStore();
            return true;
        }
        return false;
    }

    public boolean okToLeave() {
        return config==null ? true : config.validate();
    }
}
