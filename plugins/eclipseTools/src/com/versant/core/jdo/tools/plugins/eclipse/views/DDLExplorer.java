
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

import com.versant.core.jdo.tools.plugins.eclipse.VOAProjectControler;
import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.plugins.eclipse.dialogs.WriterOutputDialog;
import com.versant.core.jdo.tools.plugins.eclipse.editor.DDLEditor;
import com.versant.core.jdo.tools.plugins.eclipse.editor.VOAMappingEditorInput;
import com.versant.core.jdo.tools.workbench.model.MdDataStore;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.MdProjectProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileInPlaceEditorInput;

import java.io.*;

public class DDLExplorer extends BaseExplorer {

	private Action recreateSchema;
//	private Action checkSchema;

	public DDLExplorer() throws Exception {
		super();
	}

	public static final String ID = "com.versant.core.jdo.tools.plugins.eclipse.views.DDLExplorer";

	BaseContentProvider createProvider(IProject[] projects) {
		return new DDLContentProvider(projects, this);
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
				if(editor instanceof DDLEditor){
					page.reuseEditor((DDLEditor)editor, new VOAMappingEditorInput(obj));
					if(bringToTop){
						page.bringToTop(editor);
					}
					return;
				}
			}
			getViewSite().getWorkbenchWindow().getActivePage().openEditor(new VOAMappingEditorInput(obj), "com.versant.core.jdo.tools.plugins.eclipse.editor.DDLEditor");
		}catch(Exception x){
			VOAToolsPlugin.log(x, "Problems opening DDL editor.");
		}
	}

	protected void fillContextMenu(IMenuManager manager) {
		recreateSchema.setEnabled(false);
//		checkSchema.setEnabled(false);
		if(viewer != null){
			IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
			if(selection != null){
				Object obj = selection.getFirstElement();
				if(obj instanceof IProject){
				 	IProject iProject = (IProject)obj;
				 	if(VOAProjectControler.hasVOANature(iProject)){
						manager.add(recreateSchema);
						recreateSchema.setEnabled(true);
//						manager.add(checkSchema);
//						checkSchema.setEnabled(true);
				 	}
				}
			}
		}
		super.fillContextMenu(manager);
	}

	protected void makeActions() {
		super.makeActions();
		recreateSchema = new Action() {
			public void run() {
				try{
					recreateSchema();
				}catch(Exception x){
					VOAToolsPlugin.log(x, "Problems recreating tables.");
				}
			}
		};
		recreateSchema.setId("VOArecreateSchema");
		recreateSchema.setImageDescriptor(VOAToolsPlugin.imageDescriptorFromPlugin("Versant","icons/DatabaseSchema16.png"));
		recreateSchema.setText("Recreate Schema");
		recreateSchema.setToolTipText("Drop all the tables and create them again.");

//		checkSchema = new Action() {
//			public void run() {
//				try{
//					checkSchema();
//				}catch(Exception x){
//					VOAToolsPlugin.log(x, "Problems checking the schema.");
//				}
//			}
//		};
//		checkSchema.setId("VOAcheckSchema");
//		checkSchema.setImageDescriptor(VOAToolsPlugin.imageDescriptorFromPlugin("Versant","icons/DatabaseSchema16.png"));
//		checkSchema.setText("Check Schema");
//		checkSchema.setToolTipText("Check the schema to see if the db is up to date.");
}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(recreateSchema);
//		manager.add(checkSchema);
//		manager.add(action2);
	}

    /**
     * Recreate the database schema.
     */
    public void recreateSchema() throws Exception {
		Object obj = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
		if(obj instanceof IProject){
			try{
				obj = getMdProject((IProject)obj);
			}catch(Exception e){
				obj = null;
			}
		}
		if(obj instanceof MdProjectProvider){
			MdProject mdProject = ((MdProjectProvider)obj).getMdProject();
			final MdDataStore ds = mdProject.getDataStore();
			String url = ds.getUrl();
			String message = 
              "Are you sure you want to DROP and RECREATE all tables\n" +
              "for data store '" + ds.getName() + "'?\n\n" +
              "ALL DATA WILL BE LOST!\n\n" +
              "URL: " + url;
			if(MessageDialog.openConfirm(getSite().getShell(),"DROP all tables", message)){
				final WriterOutputDialog dialog = new WriterOutputDialog(getSite().getShell(), "Recreating tables", "Dropping and recreating tables...");
		        final Writer out = dialog.getWriter();
		        final PrintWriter pout = new PrintWriter(out, true);
		        final Exception[] ex = new Exception[1];
		        Thread thread = new Thread(){
		        	public void run(){
				        try {
							ds.recreateSchema(pout, false, dialog);
						} catch (Exception e) {
                            e.printStackTrace(pout);
							ex[0] = e;
						}finally{
							try {out.close();} catch (IOException e1) {}
//							dialog.close();
						}
		        	}
		        };
		        thread.start();
		        dialog.open();
		        if(ex[0] != null){
		        	throw ex[0];
		        }
			}
		}
    }

    /**
     * Recreate the database schema.
     */
    public void checkSchema() throws Exception {

        StringWriter error = new StringWriter(10240);
        PrintWriter perror = new PrintWriter(error, false);

        ByteArrayOutputStream fix = new ByteArrayOutputStream(10240);
        PrintWriter pfix = new PrintWriter(fix, false);
        IProject iProject = getSelectedIProject();
        MdProject mdProject = getMdProject(iProject);
        MdDataStore ds = mdProject.getDataStore();
        if (ds.checkSchema(perror, pfix)) {
                perror.close();
                pfix.close();
            MessageDialog.openInformation(getSite().getShell(), "Schema correct.", "The schema is correct.");
        } else {
            perror.close();
            pfix.close();
        	IFile sql = iProject.getFile("new.sql");
        	if(sql.exists())
        		sql.setContents(new ByteArrayInputStream(fix.toByteArray()), true, true, new NullProgressMonitor());
        	else
            	sql.create(new ByteArrayInputStream(fix.toByteArray()), true, new NullProgressMonitor());
    		try{
    			IWorkbenchPage page = getViewSite().getWorkbenchWindow().getActivePage();
    			FileInPlaceEditorInput editorInput = new FileInPlaceEditorInput(sql);
    			IEditorPart part = IDE.openEditor(page, sql, true);
//    			part.
//    			IEditorPart part = page.findEditor(editorInput);
//    			OpenFileAction act = new OpenFileAction(page);
//    			act.selectionChanged()
//    			Resour
//    			page.openEditor(editorInput, part.);
//    			int length = parts.length;
//    			for(int x = 0; x < length; x++){
//    				IEditorPart editor = parts[x].getEditor(false);
//    				if(editor instanceof DDLEditor){
//    					page.reuseEditor((DDLEditor)editor, new VOAMappingEditorInput(obj));
//    					page.bringToTop(editor);
//    					return;
//    				}
//    			}
//    			page.openEditor(new VOAMappingEditorInput(obj), "com.versant.core.jdo.tools.plugins.eclipse.editor.DDLEditor");
    		}catch(Exception x){
    			VOAToolsPlugin.log(x, "Problems opening DDL editor.");
    		}
//            showEditor(true, true);
//            if (dirty) {
//                if (okToAbandon()) {
//                    writeFile();
//                    dirty = false;
//                }
//            }
//            currFileName = null;
//            tempFileName = null;
//            text.setText(error.toString());
//            text.setCaretPosition(0);
//            fixes.setText(fix.toString());
//            fixes.setCaretPosition(0);
//            fixes.discard();
//            dirty = true;
//            getToolkit().beep();
            mdProject.getLogger().error(
                    "Database (" + ds.getUrl() + ") has schema errors!");
        }

    }
}
