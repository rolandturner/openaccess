
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

import com.versant.core.jdo.tools.workbench.model.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.internal.ViewSite;

import java.util.ArrayList;

public class DDLContentProvider extends BaseContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	public DDLContentProvider(IProject[] projects, MdProjectListener projectListener) {
		super(projects, projectListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if(inputElement == null || inputElement instanceof ViewSite){
			return projects;
		}else if(inputElement instanceof IProject){
			ArrayList list = new ArrayList();
			IProject iProject = (IProject)inputElement;
			MdProject mdProject = null;
			try {
				mdProject = getMdProject(iProject);
			} catch (Exception e) {}
			if(mdProject != null){
				MdDataStore ds = mdProject.getDataStore();
				if(ds != null && ds.isJDBC()){
					list.addAll(mdProject.getPackages());
//					JDOMetaData jmd = mdProject.getJdoMetaData();
//					if(jmd != null){
//				        JdbcTable[] keyGenTables = ((JdbcMetaData)jmd.jdbcMetaData).keyGenTables;
//				        if (keyGenTables != null) {
//				            for (int i = 0; i < keyGenTables.length; i++) {
//				                JdbcTable keyGenTable = keyGenTables[i];
//				                if (keyGenTable != null) {
//				                	list.add(keyGenTable);
//				                }
//				            }
//				        }
//					}
				}
			}
			return list.toArray();
		}else if(inputElement instanceof MdPackage) {
			return ((MdPackage)inputElement).getClassList().toArray();
		}else if(inputElement instanceof MdClass) {
			MdClass mdClass = (MdClass)inputElement;
			return mdClass.getTableFieldList().toArray();
		}
		return new Object[]{};
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if(element instanceof MdPackage) {
			MdPackage mdPackage = (MdPackage)element;
			return getIProject(mdPackage.getJdoFile().getProject());
		}else if(element instanceof MdClass) {
			return ((MdClass)element).getMdPackage();
		}else if(element instanceof MdField) {
			return ((MdField)element).getMdClass();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if(element == null || element instanceof IProject){
			MdProject mdProject;
			try {
				mdProject = getMdProject((IProject)element);
				return !mdProject.getPackages().isEmpty();
			} catch (Exception e) {
				return false;
			}
		}else if(element instanceof MdPackage) {
			return !((MdPackage)element).getClassList().isEmpty();
		}else if(element instanceof MdClass) {
			return !((MdClass)element).getTableFieldList().isEmpty();
		}else if(element instanceof MdField) {
			return false;
		}
		return false;
	}
}
