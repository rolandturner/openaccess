
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

import com.versant.core.jdo.tools.plugins.eclipse.DialogOpenerProviderPanel;
import com.versant.core.jdo.tools.workbench.WorkbenchDialogRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.awt.*;

public class WBDialog {

	private Shell shell;
	private VOAToolkit toolkit = new VOAToolkit();
	
	public WBDialog() {
	}

	private void setContentPane(Shell parent, final WorkbenchDialogRoot rootPane, boolean modal) {
		int style = SWT.TITLE | SWT.RESIZE | SWT.BORDER;
		if(modal){
			style = style | SWT.SYSTEM_MODAL;
		}
		shell = new Shell(parent, style);
		shell.setLayout(new FillLayout());
		shell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent e) {
				try {
					rootPane.activated();
				} catch (Exception e1) {
				}
			}

			public void shellClosed(ShellEvent e) {
				toolkit.continueThread();
			}
		});	
		DialogOpenerProviderPanel opener = new DialogOpenerProviderPanel(shell){
		    public Container getParent() {
		    	return toolkit.getContainer();
		    }
		};
		Composite awtComposite = new Composite(shell, SWT.EMBEDDED | SWT.FILL);
		Frame awtFrame = SWT_AWT.new_Frame(awtComposite);
		awtFrame.setLayout(new BorderLayout());
		awtFrame.add(opener, BorderLayout.CENTER);
		opener.add(rootPane, BorderLayout.CENTER);
		shell.setText(rootPane.getTitle());
		Dimension d = opener.getPreferredSize();
		awtFrame.setSize(d.width, d.height);
//		shell.pack();
		shell.setVisible(true);
	}

	public void openDialog(final Shell parent, final WorkbenchDialogRoot rootPane, final boolean modal) {
		final Runnable run = new Runnable() {
			public void run() {
				setContentPane(parent, rootPane, modal);
			}
		};
		if (parent != null && !parent.isDisposed()) {
			parent.getDisplay().asyncExec(run);
			if(modal){
				toolkit.blockThread();
			}
		}
	}

    public void dispose(Shell parent) {
		toolkit.continueThread();
		final Runnable run = new Runnable() {
			public void run() {
        		if (shell != null && !shell.isDisposed()) {
        			shell.dispose();
        		}
			}
		};
		if (parent != null && !parent.isDisposed()) {
			parent.getDisplay().syncExec(run);
		}
    }
}
//
//	private Shell shell;
//	private VOAToolkit toolkit = new VOAToolkit();
//	
//	public WBDialog() {
//	}
//
//	private void setContentPane(Shell parent, WorkbenchDialogRoot rootPane, boolean modal) {
//		int style = SWT.DIALOG_TRIM | SWT.RESIZE;
//		if(modal){
//			style |= SWT.APPLICATION_MODAL;
//		}
//		shell = new Shell(parent, style);
//		shell.setLayout(new FillLayout());
//		DialogOpenerProviderPanel opener = new DialogOpenerProviderPanel(shell){
//		    public Container getParent() {
//		    	return toolkit.getContainer();
//		    }
//		};
//		Composite awtComposite = new Composite(shell, SWT.EMBEDDED | SWT.FILL);
//		Frame awtFrame = SWT_AWT.new_Frame(awtComposite);
//		awtFrame.setLayout(new BorderLayout());
//		awtFrame.add(opener, BorderLayout.CENTER);
//		opener.add(rootPane, BorderLayout.CENTER);
//		shell.setText(rootPane.getTitle());
//		Dimension d = opener.getPreferredSize();
//		shell.setSize(d.width, d.height);
////		shell.pack();
//	}
//
//	public void openDialog(final Shell parent, final WorkbenchDialogRoot rootPane, final boolean modal) {
//		final Runnable run = new Runnable() {
//			public void run() {
//				try{
//					setContentPane(parent, rootPane, modal);
//					shell.setVisible(true);
//				}finally{
//					toolkit.continueThread();
//				}
//			}
//		};
//		if (parent != null && !parent.isDisposed()) {
//			parent.getDisplay().asyncExec(run);
//			toolkit.blockThread();
//		}
//	}
//
//    public void dispose(Shell parent) {
//		final Runnable run = new Runnable() {
//			public void run() {
//        		if (shell != null && !shell.isDisposed()) {
//        			shell.dispose();
//        		}
//			}
//		};
//		if (parent != null && !parent.isDisposed()) {
//			parent.getDisplay().syncExec(run);
//		}
//    }
//}
