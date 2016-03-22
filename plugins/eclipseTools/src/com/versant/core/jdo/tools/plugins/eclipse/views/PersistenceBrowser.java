
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
package com.versant.core.jdo.tools.plugins.eclipse.views;

import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.plugins.eclipse.editor.VOAMappingEditor;
import com.versant.core.jdo.tools.plugins.eclipse.editor.VOAMappingEditorInput;
import com.versant.core.jdo.tools.workbench.model.MdProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;

public class PersistenceBrowser extends BaseExplorer implements MdProjectListener{
	
	public static final String ID = "com.versant.core.jdo.tools.plugins.eclipse.views.PersistenceBrowser";

	/**
	 * The constructor.
	 */
	public PersistenceBrowser() throws Exception{
	}

	BaseContentProvider createProvider(IProject[] projects) {
		return new VOAStructuredContentProvider(projects, this);
	}

	protected void fillContextMenu(IMenuManager manager) {
//		if(viewer != null){
//			IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
//			if(selection != null){
//				Object obj = selection.getFirstElement();
//				if (obj instanceof MdClass){
//					final MdClass mdClass = (MdClass) obj;
//					final MdProject mdProject = mdClass.getMdProject();
//					Action removeClass = new Action() {
//						public void run() {
//							VOAProjectControler controler = VOAProjectControler.getInstance(mdProject);
//							if(controler != null){
//								controler.removeClass(mdClass.getQName());
//							}
//						}
//					};
//					removeClass.setText("Remove Class");
//					removeClass.setToolTipText("Make this class not persistent.");
////					removeClass.setImageDescriptor(VersantPlugin.getDefault().getImageDescriptor("AddClass16.gif"));
//					manager.add(removeClass);
//				}
//			}
//		}
		super.fillContextMenu(manager);
	}

	public void setSelection(ISelection selection, boolean bringToTop) {
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if(obj instanceof IProject){
			try{
				obj = getMdProject((IProject)obj);
			}catch(Exception e){
				obj = null;
			}
		}
		try{
			IWorkbenchPage page = getViewSite().getWorkbenchWindow().getActivePage();
			IEditorReference[] parts = page.getEditorReferences();
			int length = parts.length;
			for(int x = 0; x < length; x++){
				IEditorPart editor = parts[x].getEditor(false);
				if(editor instanceof VOAMappingEditor){
					page.reuseEditor((VOAMappingEditor)editor, new VOAMappingEditorInput(obj));
					if(bringToTop){
						page.bringToTop(editor);
					}
					return;
				}
			}
			getViewSite().getWorkbenchWindow().getActivePage().openEditor(new VOAMappingEditorInput(obj), "com.versant.core.jdo.tools.plugins.eclipse.editor.VOAMappingEditor");
		}catch(Exception x){
			VOAToolsPlugin.log(x, "Problems opening Mapping editor.");
		}
	}
}
