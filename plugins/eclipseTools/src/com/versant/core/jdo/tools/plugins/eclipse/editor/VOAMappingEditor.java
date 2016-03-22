
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
package com.versant.core.jdo.tools.plugins.eclipse.editor;

import com.versant.core.jdo.tools.plugins.eclipse.DialogOpenerProviderPanel;
import com.versant.core.jdo.tools.plugins.eclipse.VOAProjectControler;
import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.workbench.WorkbenchPanelHelper;
import com.versant.core.jdo.tools.workbench.WorkbenchPanelHelperManager;
import com.versant.core.jdo.tools.workbench.model.MdProjectProviderManager;
import com.versant.core.jdo.tools.workbench.tree.MappingPanel;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.EditorPart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VOAMappingEditor extends EditorPart implements IReusableEditor, WorkbenchPanelHelper{

	private MappingPanel mappingPanel = new MappingPanel(this);
	private Frame awtFrame;
	private Composite awtComposite;
	private Timer timer;

	public VOAMappingEditor() throws Exception{
		super();
        MdProjectProviderManager.setProjectProvider(mappingPanel, null);
        WorkbenchPanelHelperManager.getInstance().setWorkbenchPanelHelper(this);
		final Runnable dirty = new Runnable() {
			public void run() {
		        firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
			}
		};
        timer = new Timer(500, new ActionListener() {
        	boolean isDirty = false;
			public void actionPerformed(ActionEvent action) {
				boolean newDirty = isDirty();
				if(isDirty == newDirty)return;
				isDirty = newDirty;
				if (awtComposite != null && !awtComposite.isDisposed()) {
					try {
						awtComposite.getDisplay().asyncExec(dirty); 
					} catch (RuntimeException e) {
						VOAToolsPlugin.log(e); 
					}
				}
			}
		});
        timer.start();
	}
	
	public void dispose() {
		super.dispose();
		awtComposite.dispose();
		awtFrame.dispose();
		timer.stop();
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		this.awtComposite = new Composite(parent, SWT.EMBEDDED);
		awtFrame = SWT_AWT.new_Frame(awtComposite);
		awtFrame.setLayout(new BorderLayout());
		DialogOpenerProviderPanel panel = new DialogOpenerProviderPanel(getSite().getShell());
		JRootPane rootPane = new JRootPane();
		rootPane.getContentPane().add(new JScrollPane(mappingPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		awtFrame.add(panel, BorderLayout.CENTER);
		panel.add(rootPane, BorderLayout.CENTER);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		try{
			VOAProjectControler.save();
		}catch(Exception x){
			VOAToolsPlugin.log(x, "Problem saving mapping!");
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
//		try{
//			site.getPage().addSelectionListener(new ISelectionListener(){
//				public void selectionChanged(IWorkbenchPart part, ISelection iSelection){
//					try {
//						Object o = WBControler.getInstance().getSelectedVOA(iSelection);
//						mappingPanel.valueChanged(o);
//			            mappingPanel.setSize(scrollPane.getViewportBorderBounds().width, mappingPanel.getSize().height);
//						awtFrame.repaint();
//					} catch (Throwable e) {
//						Shell shell = new Shell();
//						MessageDialog.openError(
//								shell,
//								"Error",
//								e.getMessage());
//
//						e.printStackTrace();
//					}
//				}
//			});
//		}catch(Exception e){
////			label.setText(e.getClass().getName()+ e.getMessage());
//		}
	}

	public void setInput(IEditorInput input){
		super.setInput(input);
		final Object value = ((VOAMappingEditorInput)input).getObject();
		if (value != null){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					mappingPanel.valueChanged(value);
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
//		return true;
		return VOAProjectControler.isDirty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if(awtComposite != null){
			awtComposite.setFocus();
		}
	}

	/* (non-Javadoc)
	 * @see com.versant.core.jdo.tools.workbench.WorkbenchPanelHelper#getMainFrame()
	 */
	public Frame getMainFrame() {
//		return PlatformUI.getWorkbench().addWindowListener();
		return awtFrame;
	}

	/* (non-Javadoc)
	 * @see com.versant.core.jdo.tools.workbench.WorkbenchPanelHelper#isReloadingClasses()
	 */
	public boolean isReloadingClasses() {
		return VOAProjectControler.isReloading();
	}
}
