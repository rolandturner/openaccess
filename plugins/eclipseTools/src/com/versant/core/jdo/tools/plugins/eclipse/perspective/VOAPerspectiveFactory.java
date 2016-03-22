
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
package com.versant.core.jdo.tools.plugins.eclipse.perspective;

import com.versant.core.jdo.tools.plugins.eclipse.views.DDLExplorer;
import com.versant.core.jdo.tools.plugins.eclipse.views.PersistenceBrowser;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class VOAPerspectiveFactory implements IPerspectiveFactory {
	public static final String ID = "com.versant.core.jdo.tools.plugins.eclipse.perspective.VOAPerspectiveFactory";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); 
		layout.addPerspectiveShortcut(JavaUI.ID_PERSPECTIVE);

 		String editorArea = layout.getEditorArea();

 		IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea); 
		folder.addView(PersistenceBrowser.ID);
		folder.addView(DDLExplorer.ID);
		folder.addPlaceholder(JavaUI.ID_PACKAGES);
		folder.addPlaceholder(IPageLayout.ID_RES_NAV);

		IFolderLayout bottom= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		bottom.addView("org.eclipse.pde.runtime.LogView"); 
		bottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);

		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
	}
}
