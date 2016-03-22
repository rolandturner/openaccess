
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
 * Interface for logging messages.
 *
 * @see com.versant.core.jdo.tools.workbench.MessageForm
 */
public interface Logger {

    public void error(String message);

    public void error(Throwable t);

    public void error(String message, Throwable t);

    public void warn(String message);

    public void warn(Throwable t);

    public void info(String message);

    public void status(String message);
}

