
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
package com.versant.core.jdo.tools.workbench;

public interface DialogOpener {

    void openDialog(WorkbenchDialogRoot c, boolean modal);
    void dispose();
//    dialogOpener.setResizable(true);
//    dialogOpener.setTitle(form.getTitle());
//} else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
}
