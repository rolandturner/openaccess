
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
import com.versant.core.jdo.tools.workbench.model.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class WriterOutputDialog extends MessageDialog implements Logger{

	private Text text;
	private Writer writer;
	private Button closeWhenDone;
	private PrintWriter printWriter;
	
	public WriterOutputDialog(Shell parentShell, String dialogTitle, String dialogMessage) {
		super(parentShell, dialogTitle, null, dialogMessage,
				MessageDialog.INFORMATION, new String[]{IDialogConstants.CLOSE_LABEL}, 0);
		writer = new Writer() {
			public void close() throws IOException {
				final Runnable run = new Runnable() {
					public void run() {
						if(closeWhenDone.getSelection()){
							WriterOutputDialog.this.close();
						}else{
							closeWhenDone.setEnabled(false);
							getButton(0).setEnabled(true);
						}
					}
				};
				if (text != null && !text.isDisposed()) {
					try {
						text.getDisplay().asyncExec(run); 
					} catch (RuntimeException e) {
						VOAToolsPlugin.log(e); 
					}
				}
			}

			public void flush() throws IOException {
			}

			public void write(char[] cbuf, int off, int len) throws IOException {
				final String add = new String(cbuf, off, len);
				final Runnable run = new Runnable() {
					public void run() {
						text.append(add);
					}
				};
				if (text != null && !text.isDisposed()) {
					try {
						text.getDisplay().asyncExec(run); 
					} catch (RuntimeException e) {
						VOAToolsPlugin.log(e); 
					}
				}
			}
		};
		printWriter = new PrintWriter(writer, true);
	}
	
	protected Control createCustomArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		text = new Text(comp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		GridData textLData = new GridData();
		textLData.grabExcessHorizontalSpace = true;
		textLData.grabExcessVerticalSpace = true;
		textLData.verticalAlignment = GridData.FILL;
		textLData.widthHint = 550;
		textLData.heightHint = 350;
		text.setLayoutData(textLData);
		closeWhenDone = new Button(comp, SWT.CHECK | SWT.LEFT);
		closeWhenDone.setText("Close window when done");
		GridData closeWhenDoneLData = new GridData();
		closeWhenDoneLData.verticalAlignment = GridData.FILL;
		closeWhenDoneLData.grabExcessHorizontalSpace = true;
		closeWhenDone.setLayoutData(closeWhenDoneLData);
		comp.layout();
		comp.pack();
		return comp;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(0).setEnabled(false);
	}

	public Writer getWriter(){
		return writer;
	}

	private void setMessage(final String message) {
		final Runnable run = new Runnable() {
			public void run() {
				messageLabel.setText(message);
			}
		};
		if (messageLabel != null && !messageLabel.isDisposed()) {
			try {
				messageLabel.getDisplay().asyncExec(run); 
			} catch (RuntimeException e) {
				VOAToolsPlugin.log(e); 
			}
		}
	}

	public void error(String message) {
		setMessage(message);
	}

	public void error(Throwable t) {
		t.printStackTrace(printWriter);
	}

	public void error(String message, Throwable t) {
		setMessage(message);
		t.printStackTrace(printWriter);
		
	}

	public void warn(String message) {
		setMessage(message);
	}

	public void warn(Throwable t) {
		t.printStackTrace(printWriter);
	}

	public void info(String message) {
		setMessage(message);
	}

	public void status(String message) {
		setMessage(message);
	}
}
