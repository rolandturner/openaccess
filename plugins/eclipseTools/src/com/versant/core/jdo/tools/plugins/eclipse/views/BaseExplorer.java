
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

import com.versant.core.jdo.tools.eclipse.EnhanceAction;
import com.versant.core.jdo.tools.eclipse.RemoveJDONature;
import com.versant.core.jdo.tools.eclipse.Utils;
import com.versant.core.jdo.tools.plugins.eclipse.VOAProjectControler;
import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.plugins.eclipse.dialogs.VOAAddNatureDialog;
import com.versant.core.jdo.tools.workbench.model.MdClass;
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.MdProjectEvent;
import com.versant.core.jdo.tools.workbench.model.MdProjectListener;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Arrays;


public abstract class BaseExplorer extends ViewPart implements MdProjectListener{
	
	protected TreeViewer viewer;
	private BaseContentProvider provider;
	private boolean bringToTop = true;
	private boolean internal = false;
	private DrillDownAdapter drillDownAdapter;
	private Action actAddJDONature;
	private Action actRemoveJDONature;
	private Action actEnhance;
	private Action actAddClass;

	/**
	 * The constructor.
	 */
	public BaseExplorer() throws Exception{
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		try{
			viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			viewer.addSelectionChangedListener(new ISelectionChangedListener(){
				public void selectionChanged(SelectionChangedEvent event){
					setSelection(event.getSelection(), bringToTop);
				}
			});
			drillDownAdapter = new DrillDownAdapter(viewer);
			IProject[] projects = findIProject();
			provider = createProvider(projects);
			viewer.setLabelProvider(new ViewLabelProvider());
			viewer.setContentProvider(provider);
			viewer.setInput(getViewSite());
			viewer.getControl().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.F5){
						refreshTree(false, true);
					}
				}
			});
			viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
			viewer.setComparer(new MdComparer());
			makeActions();
			hookContextMenu();
			contributeToActionBars();
		}catch(Exception x){
			x.printStackTrace();
		}
	}

	abstract BaseContentProvider createProvider(IProject[] projects);
	
	private IProject[] findIProject() {
		Object input= getSite().getPage().getInput();
		if (input instanceof IWorkspace) { 
			return ((IWorkspace)input).getRoot().getProjects();
		} else if (input instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot)input;
			return root.getProjects();
		} else if (input instanceof IContainer) {
			return ((IContainer)input).getWorkspace().getRoot().getProjects();
		}
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BaseExplorer.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object element= ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (viewer.isExpandable(element)) {
					viewer.setExpandedState(element, !viewer.getExpandedState(element));
				}
			}
		});
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void fillLocalPullDown(IMenuManager manager) {
	}


	protected void fillContextMenu(IMenuManager manager) {
		boolean isProject = false;
		if(viewer != null){
			IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
			if(selection != null){
                IProject iProject = getSelectedIProjectInTree();
                if(VOAProjectControler.hasVOANature(iProject)){
                   manager.add(actEnhance);
                   manager.add(actAddClass);
                   manager.add(actRemoveJDONature);
                }else{
                   manager.add(actAddJDONature);
                }
                Object obj = selection.getFirstElement();
                isProject = obj instanceof IProject;
                if (obj instanceof MdClass){
					final MdClass mdClass = (MdClass) obj;
					final MdProject mdProject = mdClass.getMdProject();
					Action removeClass = new Action() {
						public void run() {
							VOAProjectControler controler = VOAProjectControler.getInstance(mdProject);
							if(controler != null){
								controler.removeClass(mdClass.getQName());
							}
						}
					};
					removeClass.setText("Remove Class");
					removeClass.setToolTipText("Make this class not persistent.");
//					removeClass.setImageDescriptor(VersantPlugin.getDefault().getImageDescriptor("AddClass16.gif"));
					manager.add(removeClass);
				}
			}
		}
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		JavaPlugin.createStandardGroups(manager);
		if(isProject){
			manager.add(new Separator());
			manager.add(new PropertyDialogAction(getSite().getShell(), viewer));
		}
	}

	protected IProject getSelectedIProject(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if(selection != null){
			Object obj = selection.getFirstElement();
			if(obj instanceof IProject){
			 	return (IProject)obj;
			}
		}
		return null;
	}

	protected IProject getSelectedIProjectInTree(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if(selection != null){
			Object obj = selection.getFirstElement();
			while(obj != null){
				if(obj instanceof IProject){
				 	return (IProject)obj;
				}
				obj = provider.getParent(obj);
			}
		}
		return null;
	}
	
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actAddClass);
//		manager.add(actAddJDONature);
//		manager.add(action2);
//		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	protected void makeActions() {
		actAddJDONature = new Action() {
			public void run() {
				IProject iProject = getSelectedIProject();
				if(iProject != null){
					try{
						VOAAddNatureDialog wizard= new VOAAddNatureDialog(iProject);
						WizardDialog dialog= new WizardDialog(getSite().getShell(), wizard);
						PixelConverter converter= new PixelConverter(JavaPlugin.getActiveWorkbenchShell());
						
						dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));
						dialog.create();
						int res= dialog.open();
						if(res == Window.OK){
							IJavaProject iJavaProject = JavaModelManager.getJavaModelManager().getJavaModel().findJavaProject(iProject);
							Utils.addJDONature(iJavaProject);
						}
						notifyResult(res == Window.OK);
					}catch(Exception x){
						VOAToolsPlugin.log(x, "Problems opening Mapping editor.");
					}
					refreshTree(false, true);
				}
			}
		};
		actAddJDONature.setText("Add VOA Nature");
		actAddJDONature.setToolTipText("Add VOA Nature");
		
		actRemoveJDONature = new Action() {
			public void run() {
				IProject iProject = getSelectedIProject();
				if(iProject != null){
					IJavaProject iJavaProject = JavaModelManager.getJavaModelManager().getJavaModel().findJavaProject(iProject);
					RemoveJDONature.removeJDONature(iJavaProject);
					VOAProjectControler.removeInstance(iProject);
					refreshTree(false, true);
				}
			}
		};
		actRemoveJDONature.setText("Remove VOA Nature");
		actRemoveJDONature.setToolTipText("Remove VOA Nature");

		actEnhance = new Action() {
			public void run() {
				EnhanceAction.enhance(getSelectedIProject());
			}
		};
		actEnhance.setText("Enhance Classes");
		actEnhance.setToolTipText("Enhance Classes");
		
		actAddClass = new Action() {
			public void run() {
				try {
					IProject iProject = getSelectedIProjectInTree();
					if (iProject == null) {
						return;
					}	

					IJavaProject iJavaProject = JavaModelManager.getJavaModelManager().getJavaModel().findJavaProject(iProject);
					IPackageFragmentRoot[] roots = iJavaProject.getAllPackageFragmentRoots();
					ArrayList elementList = new ArrayList();
					for(int x=0; x < roots.length; x++){
						IPackageFragmentRoot root = roots[x];
						if(root.getKind() == IPackageFragmentRoot.K_SOURCE){
							elementList.addAll(Arrays.asList(root.getChildren()));
						}
					}
					IJavaElement[] elements = new IJavaElement[elementList.size()];
					elementList.toArray(elements);
					IJavaSearchScope scope= SearchEngine.createJavaSearchScope(elements);
                    SelectionDialog dialog = JavaUI.createTypeDialog(getSite().getShell(), getSite().getWorkbenchWindow(), scope, IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, true);
					dialog.setTitle("Make Class Persistent");
					dialog.setMessage("Please select a class to make persistent"); 
		
					if (dialog.open() == Window.OK) {
						VOAProjectControler controler = VOAProjectControler.getInstance(iProject);
						Object[] results = dialog.getResult();
                        for (int i = 0; i < results.length; i++) {
                            IType itype = (IType) results[i];
                            controler.addClass(itype.getFullyQualifiedName());
                        }
					}
				} catch (Exception e) {
					VOAToolsPlugin.log(e, "Problems adding a persistent class.");
				}finally{
					refreshTree(false, true);
				}
			}
		};
		actAddClass.setText("Make Class Persistent");
		actAddClass.setImageDescriptor(VOAToolsPlugin.imageDescriptorFromPlugin("Versant","icons/class16.png"));
		actAddClass.setToolTipText("Make  a class in you project persistent.");
	}

	protected MdProject getMdProject(IProject project) throws Exception {
		VOAProjectControler controler = VOAProjectControler.getInstance(project);
		if(controler != null){
			return controler.getMdProject();
		}
		return null;
	}

	public abstract void setSelection(ISelection selection, boolean bringToTop);
	
	void refreshTree(final boolean toTop, final boolean data) {
		final Runnable run = new Runnable() {
			public void run() {
				Control ctrl= viewer.getControl();
				if (ctrl != null && !ctrl.isDisposed() && ctrl.isVisible()){
					if(data && provider != null){
						provider.setIProjects(findIProject());
					}
					try{
						internal = true;
						viewer.getTree().setRedraw(false);
						bringToTop = toTop;
						ISelection selection = viewer.getSelection();
						Object[] exp = viewer.getExpandedElements();
						viewer.refresh(true);
						for(int x=0; x < exp.length; x++){
							viewer.setExpandedState(exp, true);
						}
						viewer.setSelection(selection, true);
					}finally{
						internal = false;
						bringToTop = true;
						viewer.getTree().setRedraw(true);
						viewer.getTree().redraw();
					}
				}
			}
		};
		Control ctrl= viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			try {
				ctrl.getDisplay().asyncExec(run); 
			} catch (RuntimeException e) {
				VOAToolsPlugin.log(e); 
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if(provider != null){
			provider.setIProjects(findIProject());
			provider.reload();
//			provider.recompile();
		}
//		refreshTree(false, false);
		viewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see com.versant.core.jdo.tools.workbench.model.MdProjectListener#projectChanged(com.versant.core.jdo.tools.workbench.model.MdProjectEvent)
	 */
	public void projectChanged(MdProjectEvent ev) {
        if (!internal && ev.getFlags() == MdProjectEvent.ID_PARSED_META_DATA) {
    		refreshTree(false, false);
		}
	}
}
