
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

import com.versant.core.jdo.tools.plugins.eclipse.dialogs.WBDialog;
import com.versant.core.jdo.tools.workbench.DialogOpener;
import com.versant.core.jdo.tools.workbench.DialogOpenerProvider;
import com.versant.core.jdo.tools.workbench.WorkbenchDialogRoot;
import org.eclipse.swt.widgets.Shell;
import sun.awt.AppContext;

import java.awt.*;

public class DialogOpenerProviderPanel extends Panel implements DialogOpenerProvider{

	protected Shell shell;
	protected AppContext showAppContext;

	private Object lock = new Object();
	
	
	public DialogOpenerProviderPanel(Shell shell){
		this.shell = shell;
		setLayout(new BorderLayout());
	}

//	public DialogOpener createDialogOpener() {
//		return getDialogOpener(this);
//	}
//
//    private DialogOpener getDialogOpener(final Component c) {
//        return new DialogOpener() {
//            WorkbenchDialog dialog;
//            public void openDialog(WorkbenchDialogRoot root, boolean modal) {
//                dialog = createDialog(c, modal);
//                dialog.setContentPane(root);
//                dialog.setTitle(root.getTitle());
//                dialog.pack();
//                dialog.setLocationRelativeTo(dialog.getParent());
//                if(modal){
//            		final Runnable run = new Runnable() {
//            			public void run() {
//        					try {
//								lock.wait();
//							} catch (InterruptedException e) {
//								//Do Nothing
//							}
//            			}
//            		};
//        			shell.getDisplay().asyncExec(run);
//                }
//                dialog.setVisible(true);
//                lock.notifyAll();
//            }
//
//            public void dispose() {
//                dialog.dispose();
//            }
//        };
//    }
//
//    private WorkbenchDialog createDialog(Component component, boolean modal) {
//        WorkbenchDialog dialog;
//        if (component == null) {
//            dialog = new WorkbenchDialog((Frame)null, modal);
//        } else {
//            Window win = null;
//            if (component instanceof Window) {
//                win = (Window)component;
//            } else {
//                win = SwingUtilities.getWindowAncestor(component);
//            }
//            if (win instanceof Frame) {
//                dialog = new WorkbenchDialog((Frame)win, modal);
//            } else if (win instanceof Dialog) {
//                dialog = new WorkbenchDialog((Dialog)win, modal);
//            } else {
//                dialog = new WorkbenchDialog((Frame)null, modal);
//            }
//        }
//        return dialog;
//    }

	public DialogOpener createDialogOpener() {
        return new DialogOpener() {
        	WBDialog dialog = new WBDialog();
            public void openDialog(final WorkbenchDialogRoot root, boolean modal) {
                dialog.openDialog(shell, root, modal);
            }

            public void dispose() {
                dialog.dispose(shell);
            }
        };
	}
}
