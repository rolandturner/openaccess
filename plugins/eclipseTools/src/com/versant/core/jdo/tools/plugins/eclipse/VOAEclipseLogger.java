
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
package com.versant.core.jdo.tools.plugins.eclipse;

import com.versant.core.jdo.tools.workbench.Utils;
import com.versant.core.jdo.tools.workbench.model.ExceptionListenerManager;
import com.versant.core.jdo.tools.workbench.model.Logger;
import org.eclipse.core.runtime.Status;
import za.co.hemtech.gui.ExceptionListener;

public class VOAEclipseLogger implements Logger, ExceptionListener{
	
	public VOAEclipseLogger(){
        ExceptionListenerManager.setExceptionListenerManager(this, null);
        Utils.setLogger(this);
	}
	boolean quite = true;
	public void error(String message) {
		if(quite) return;
		VOAToolsPlugin.log(null, message);
	}

	public void error(Throwable t) {
		if(quite) return;
		VOAToolsPlugin.log(t);
	}

	public void error(String message, Throwable t) {
		if(quite) return;
		VOAToolsPlugin.log(t, message);
	}

	public void warn(String message) {
		if(quite) return;
		VOAToolsPlugin.log(null, message, Status.WARNING);
	}

	public void warn(Throwable t) {
		if(quite) return;
		VOAToolsPlugin.log(t, "Warning: "+t.getMessage(), Status.WARNING);

	}

	public void info(String message) {
		if(quite) return;
		VOAToolsPlugin.log(null, message, Status.INFO);
	}

	public void status(String message) {
		if(quite) return;
		VOAToolsPlugin.log(null, message, Status.OK);
	}

	/* (non-Javadoc)
	 * @see za.co.hemtech.gui.ExceptionListener#handleException(java.lang.Object, java.lang.Throwable)
	 */
	public boolean handleException(Object source, Throwable x) {
		error(x);
		return true;
	}
}
