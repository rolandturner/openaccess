
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
import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.MdProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public abstract class BaseContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	protected IProject[] projects;
	private MdProjectListener projectListener1;
	private MdProjectListener projectListener2;
	
	public BaseContentProvider(IProject[] projects, MdProjectListener projectListener) {
		projectListener1 = projectListener;
		setIProjects(projects);
	}

	public void setMdProjectListener(MdProjectListener projectListener){
		removeListeners();
		this.projectListener2 = projectListener;
		addListeners();
	}
	
	public void setIProjects(IProject[] projects) {
		removeListeners();
		this.projects = projects;
		addListeners();
	}

	private void removeListeners() {
		if(projects == null){
			return;
		}
		for(int x = projects.length-1; x >= 0; x--){
			IProject iProject = projects[x];
			MdProject mdProject = null;
			try {
				mdProject = getMdProject(iProject);
				if(mdProject != null){
					mdProject.removeMdProjectListener(projectListener1);
					if(projectListener2 != null){
						mdProject.removeMdProjectListener(projectListener2);
					}
				}
			} catch (Exception e) {
				// Do Nothing
			}
		}
	}

	private void addListeners() {
		if(projects == null){
			return;
		}
		for(int x = projects.length-1; x >= 0; x--){
			IProject iProject = projects[x];
			MdProject mdProject = null;
			try {
				mdProject = getMdProject(iProject);
				if(mdProject != null){
					mdProject.addMdProjectListener(projectListener1);
					if(projectListener2 != null){
						mdProject.addMdProjectListener(projectListener2);
					}
				}
			} catch (Exception e) {
				// Do Nothing
			}
		}
	}
	
	public void reload(){
		for(int x = projects.length-1; x >= 0; x--){
			IProject iProject = projects[x];
			VOAProjectControler controler;
			try{
				controler = VOAProjectControler.getInstance(iProject);
				controler.reloadClasses();
			}catch(Exception ex){
				// Do Nothing
			}
		}
	}
	
	public void recompile() {
		for(int x = projects.length-1; x >= 0; x--){
			IProject iProject = projects[x];
			VOAProjectControler controler;
			try{
				controler = VOAProjectControler.getInstance(iProject);
				controler.recompile();
			}catch(Exception ex){
				// Do Nothing
			}
		}
	}

	protected MdProject getMdProject(IProject project) throws Exception {
		VOAProjectControler controler;
		try{
			controler = VOAProjectControler.getInstance(project);
		}catch(Exception x){
			controler = null;
		}
		if(controler != null){
			return controler.getMdProject();
		}
		return null;
	}

	protected IProject getIProject(MdProject project){
		if(project != null){
			VOAProjectControler controler = VOAProjectControler.getInstance(project);
			if(controler != null){
				return controler.getIProject();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		removeListeners();
		projects = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
