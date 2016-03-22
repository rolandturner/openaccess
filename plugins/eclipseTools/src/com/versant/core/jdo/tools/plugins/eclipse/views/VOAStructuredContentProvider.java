
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
import java.util.List;

public class VOAStructuredContentProvider extends BaseContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	public VOAStructuredContentProvider(IProject[] projects, MdProjectListener projectListener) {
		super(projects, projectListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if(inputElement == null || inputElement instanceof ViewSite){
			return projects;
		}else if(inputElement instanceof IProject){
			IProject iProject = (IProject)inputElement;
			MdProject mdProject = null;
			try {
				mdProject = getMdProject(iProject);
			} catch (Exception e) {}
			if(mdProject != null){
				return mdProject.getPackages().toArray();
			}
		}else if(inputElement instanceof MdPackage) {
			MdPackage mdPackage = (MdPackage)inputElement;
			ArrayList list = new ArrayList();
			list.addAll(mdPackage.getClassList());
			list.addAll(mdPackage.getInterfaceList());
			return list.toArray();
		}else if(inputElement instanceof MdClass) {
			return ((MdClass)inputElement).getTreeFieldList().toArray();
        }else if(inputElement instanceof List) {
            return ((List)inputElement).toArray();
        } else if (inputElement instanceof MdField) {
            MdField mdField = (MdField) inputElement;
            if(mdField.isEmbeddedRef()){
                List embeddedFields = mdField.getEmbeddedFields();
                if (embeddedFields != null) {
                    return embeddedFields.toArray();
                }
            }
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
		}else if(element instanceof MdClassOrInterface) {
			return ((MdClassOrInterface)element).getMdPackage();
		}else if(element instanceof MdEmbeddedField) {
			return ((MdEmbeddedField)element).getParentField();
		}else if(element instanceof MdField) {
			return ((MdField)element).getMdClass();
        }else if(element instanceof List) {
            List list = (List)element;
            if (!list.isEmpty()) {
                return ((MdField)list.get(0)).getMdClass();
            }
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
			MdPackage mdPackage = (MdPackage)element;
			return !mdPackage.getClassList().isEmpty() || !mdPackage.getInterfaceList().isEmpty();
		}else if(element instanceof MdClass) {
			return !((MdClass)element).getTreeFieldList().isEmpty();
        } else if (element instanceof MdField) {
            MdField mdField = (MdField) element;
            if(mdField.isEmbeddedRef()){
                List embeddedFields = mdField.getEmbeddedFields();
                return embeddedFields != null;
            }
		}else if(element instanceof MdField) {
			return false;
        }else if(element instanceof List) {
            return !((List)element).isEmpty();
		}
		return false;
	}
}
