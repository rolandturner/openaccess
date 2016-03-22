
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

import com.versant.core.jdo.tools.eclipse.Utils;
import com.versant.core.jdo.tools.eclipse.VersantStatics;
import com.versant.core.jdo.tools.workbench.model.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class VOAProjectControler {
	
	private static Map iMap = new HashMap();
	private static Map mdMap = new HashMap();
    private static boolean isReloading = false;
    private static VOAEclipseLogger logger = new VOAEclipseLogger();
    private IProject iProject;
    private MdProject mdProject;
	private Timer compileMetaDataTimer;
	
	private VOAProjectControler() throws Exception {
    }

	public static VOAProjectControler getInstance(IProject iProject) throws Exception{
		boolean hasNature = hasVOANature(iProject);
		if(hasNature){
			VOAProjectControler controler = (VOAProjectControler) iMap.get(iProject);
			if(controler == null){
		    	System.setProperty("OPENACCESS_HOME", ".");
		    	controler = new VOAProjectControler();
				controler.setSelectedProject(iProject);
				iMap.put(iProject, controler);
				mdMap.put(controler.mdProject, controler);
			}
			return controler;
		}else{
			removeInstance(iProject);
			return null;
		}
	}

	public static void removeInstance(IProject iProject){
		if(iProject == null){
			return;
		}
		iMap.remove(iProject);
		Iterator it = mdMap.values().iterator();
		while(it.hasNext()){
			VOAProjectControler controler = (VOAProjectControler)it.next();
			if(controler.iProject.equals(iProject)){
				it.remove();
			}
		}
	}

	public static VOAProjectControler getInstance(MdProject iProject){
		return (VOAProjectControler) mdMap.get(iProject);
	}

    private void setSelectedProject(IProject proj) throws Exception {
    	iProject = proj;
    	String newProject = Utils.getAbsProjectFileName(iProject);
    	if(newProject != null && newProject.length() > 0){
    		logger.quite = true;
    		try{
	    		mdProject = new MdProject(logger, new File(newProject));
                mdProject.setDisableHyperdriveInWorkbench(true);
	    		mdProject.load();
				setMdProjectDefaults();
				mdProject.setDirty(false);
				try{
					iProject.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor(){
						public void done() {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
						    		try{
						    			isReloading = true;
						    			mdProject.reloadJDOFiles();
						    			mdProject.reload();
						    		}catch(Exception x){
						    			// Do Nothing
						    		}finally{
						    			isReloading = false;
						    		}
								}
							});
						}
					});
				}catch(Throwable x){
					// Do Nothing (just trying)
				}
				compileMetaDataTimer = new Timer(200, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
		        		try{
		        			if(!mdProject.isMetaDataUpToDate()){
		        				mdProject.compileMetaData(true, true);
		        			}
		        		}catch(Exception x){
		        			// Do Nothing
		        		}
					}
				}); 
				compileMetaDataTimer.setRepeats(false);
				mdProject.addMdChangeListener(new MdChangeListener() {
					public void metaDataChanged(MdChangeEvent e) {
		    	        if (e.getFlags() == MdChangeEvent.FLAG_CLASSES_CHANGED) {
		    	        	recompile();
		    	        }
					}
				});
				mdProject.addMdProjectListener(new MdProjectListener() {
					public void projectChanged(MdProjectEvent ev) {
						if (ev.getFlags() != MdProjectEvent.ID_PARSED_META_DATA) {
		    	        	recompile();
						}
					}
				});
    		}finally{
    			logger.quite = false;
    		}
    	}else{
    		throw new Exception("No VAO project found");
    	}
    }

    public MdProject getMdProject() throws Exception {
		setMdProjectDefaults();
		return mdProject;
	}

	/**
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private void setMdProjectDefaults() throws JavaModelException, CoreException {
		IJavaProject iJavaProject = JavaModelManager.getJavaModelManager().getJavaModel().findJavaProject(iProject);
		File source = Utils.getSrcFile(iProject);
		List cp = mdProject.getClassPathList();
		cp.clear();
		if(source != null){
			mdProject.setSrcDir(source);
			cp.add(source);
		}
		String[] paths = JavaRuntime.computeDefaultRuntimeClassPath(iJavaProject);
		for(int x=0; x < paths.length; x++){
			cp.add(new File(paths[x]));
		}
		mdProject.setAntDisabled(true);
	}

    public IProject getIProject(){
		return iProject;
	}

    public ISelection getISelection(IWorkbenchPart targetPart) {
    	return targetPart.getSite().getSelectionProvider().getSelection();
    }

    public String getClassName(ISelection iSelection){
        if (iSelection instanceof StructuredSelection) {
            StructuredSelection sS = (StructuredSelection)iSelection;
            Object el = sS.getFirstElement();
            if (el instanceof IJavaElement) {
                IJavaElement javaElement = ((IJavaElement)el);
                IType type = (IType)javaElement.getAncestor(IJavaElement.TYPE);
                if(type != null){
                	return type.getFullyQualifiedName();
                }
            }
        }
        return null;
    }

    public String getFieldName(ISelection iSelection){
        if (iSelection instanceof StructuredSelection) {
            StructuredSelection sS = (StructuredSelection)iSelection;
            Object el = sS.getFirstElement();
            if (el instanceof IJavaElement) {
                IJavaElement javaElement = ((IJavaElement)el);
                IType type = (IType)javaElement.getAncestor(IJavaElement.FIELD);
                if(type != null){
                	return type.getFullyQualifiedName();
                }
            }
        }
        return null;
    }

	public void recompile() {
		compileMetaDataTimer.restart();
	}

	public void reloadClasses() throws Exception {
		isReloading = true;
		try{
			mdProject.reload();
		}finally{
			isReloading = false;
		}
	}
	
	public static boolean isDirty(){
		Iterator it = mdMap.keySet().iterator();
		while(it.hasNext()){
			MdProject mdProject = (MdProject)it.next();
			if(mdProject.isDirty()){
				return true;
			}
		}
		return false;
	}
	
	public static void save() throws Exception{
		Iterator it = mdMap.keySet().iterator();
		while(it.hasNext()){
			MdProject mdProject = (MdProject)it.next();
			if(mdProject.isDirty()){
				mdProject.setAntDisabled(true);
				VOAProjectControler controler = (VOAProjectControler)mdMap.get(mdProject);
				controler.setMdProjectDefaults();
				controler.getMdProject().save(mdProject.getFile());
			}
		}
	}

	public static boolean hasVOANature(IProject iProject) {
		if(iProject == null){
			return false;
		}
		try {
			return iProject.hasNature(VersantStatics.ID_JDGENIE_NATURE);
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * @return
	 */
	public static boolean isReloading() {
		return isReloading;
	}

    public void addClass(String className) throws Exception {
    	if(className != null){
            try {
                if (mdProject.findClassOrInterface(className) != null) return;
                Class cls = mdProject.loadClass(className);
                if (cls == null) {
                	mdProject.getLogger().error("Unable to load class: " + className);
                	return;
                }
                int pi = className.lastIndexOf('.');
                if (pi < 0) {
                	mdProject.getLogger().error("Classes in the default package cannot be persistent: " + className);
                	return;
                }
                String pname = className.substring(0, pi);
                List packages = mdProject.findPackages(pname);
                MdPackage pkg = null;
                int np = packages.size();
                if (np == 0) {
                    List jdoFileList = mdProject.getJdoFileList();
                    MdJdoFile jf;
                    if(jdoFileList.size() > 0){
                        jf = (MdJdoFile) jdoFileList.get(0);
                    }else{
                    	mdProject.getLogger().error(
                                "No .jdo files found.");
                        return;
                    }
                    pkg = new MdPackage(jf, pname);
                    jf.addPackage(pkg);
                    mdProject.getLogger().info("Created package " + pname + " in " +
                            jf.getResourceName());
                } else if (np > 1) {
                	mdProject.getLogger().error(
                            "Package in more than one .jdo file: " + pname);
                    return;
                } else {
                    pkg = (MdPackage)packages.get(0);
                }
                MdClassOrInterface c;
                if (cls.isInterface()) {
                    c = new MdInterface(mdProject, pkg, className.substring(pi + 1));
                } else {
                    c = new MdClass(mdProject, pkg, className.substring(pi + 1));
                }
                MdDataStore ds = mdProject.getDataStore();
                c.setMdDataStore(ds);
                pkg.addClassOrInterface(c);
                mdProject.getLogger().info("Added " + cls + " in " +
                        pkg.getJdoFile().getResourceName());
            } finally {
            	mdProject.syncAllClassesAndInterfaces();
            }
            List allClasses = mdProject.getDataStore().getClasses();
            if (MdUtils.fillPCSuperclassAttr(allClasses)) {
            	mdProject.syncAllClassesAndInterfaces();
            }
    	}
    }

    public void removeClass(String className){
    	if(className != null){
        	MdClassOrInterface c = mdProject.findClassOrInterface(className);
        	if (c == null) return;
            try {
                MdPackage p = c.getMdPackage();
                p.removeClassOrInterface(c);
                mdProject.getLogger().info("Removed " + c.getQName());
                if (p.getClassList().isEmpty()) {
                	try{
                		p.getJdoFile().removePackage(p);
                        mdProject.getLogger().info("Removed empty package " + p.getName());
                	}catch(Exception x){}
                }
                ArrayList arg = new ArrayList();
                arg.add(c);
                mdProject.fireProjectEvent(MdProjectEvent.ID_CLASSES_REMOVED, arg);
            } finally {
                mdProject.syncAllClassesAndInterfaces();
            }
        }
    }

	public void addField(String className, String fieldName) throws NoSuchFieldException {
		MdClass mdClass = mdProject.findClass(className);
		if(mdClass != null){
			mdClass.addField(fieldName);
		}
	}

	public void removeField(String className, String fieldName) {
		MdClass mdClass = mdProject.findClass(className);
		if(mdClass != null){
			mdClass.removeField(fieldName);
		}
	}
}
