
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
package com.versant.core.jdo.tools.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Utils class for Eclipse plugin
 */
public class Utils implements VersantStatics {
    
    public static IProject findSelectedProject(ISelection iSelection) {
        IProject proj = null;
        if (iSelection instanceof StructuredSelection) {
            StructuredSelection sS = (StructuredSelection)iSelection;
            Object el = sS.getFirstElement();
            if (el instanceof IJavaProject) {
                proj = ((IJavaProject)el).getProject();
            } else if (el instanceof IResource) {
                proj = ((IResource)el).getProject();
            } else if (el instanceof IJavaElement) {
                proj = ((IJavaElement)el).getJavaProject().getProject();
            }
        }
        return proj;
    }

    public static IJavaProject findSelectedJavaProject(ISelection selection) {
    	IJavaProject currentProject = null;
    	if (selection != null) {
    		if (selection instanceof IStructuredSelection) {
    			IStructuredSelection ss = (IStructuredSelection)selection;
    			Object obj = ss.getFirstElement();
    			if (obj instanceof IJavaProject) {
    				currentProject = (IJavaProject)obj;
    			}
    		}
    	}
    	return currentProject;
    }

    public static String getStatusMessages(Exception e) {
    	String msg = e.getMessage();
    	if (e instanceof CoreException) {
    		CoreException ce = (CoreException)e;
			IStatus status = ce.getStatus();
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++)
				msg += "\n" + children[i].getMessage();
			System.err.println(msg);
			ce.printStackTrace(System.err);
    	}
    	return msg;
    }

    /**
     * The dir where plugin is installed.
     */
    public static String getJDOGenieHomePath() {
        return Platform.getPluginRegistry().getPluginDescriptor("Versant").getInstallURL().toExternalForm();
    }

    public static void addJDOGenieCPContainer(IJavaProject jProject) {
        String path = getJDOGenieHomePath();
        try {
            IClasspathEntry libEntryGenie = JavaCore.newLibraryEntry(
                new Path(Platform.asLocalURL(new URL(path + "lib/openaccess.jar")).getPath()), null, null);
            IClasspathEntry libEntryJTA = JavaCore.newLibraryEntry(
                new Path(Platform.asLocalURL(new URL(path + "lib/jta.jar")).getPath()), null, null);

            IClasspathEntry[] entries = new IClasspathEntry[] {libEntryGenie, libEntryJTA};

            JDOClasspathContainer container = new JDOClasspathContainer(entries);

            JavaCore.setClasspathContainer(container.getPath(),
                new IJavaProject[] {jProject },
                new IClasspathContainer[] {container},
                null);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Add the 'JDO_CONTAINER' container variable to the project's build path.
     * This variable is resolved from {@link JDOClasspathContainerInitializer}.
     *
     * @see #addJDOGenieCPContainer(org.eclipse.jdt.core.IJavaProject)
     * @see JDOClasspathContainerInitializer
     * @see JDOClasspathContainer
     *
     * @param jProject
     * @throws JavaModelException
     */
    public static void addCPVarTo(IJavaProject jProject) throws JavaModelException {
        //create the container variable
        IClasspathEntry varEntry = JavaCore.newContainerEntry(new Path("JDO_CONTAINER"));

        IClasspathEntry[] oldclasspath = jProject.getRawClasspath();
        IClasspathEntry[] newclasspath =
            new IClasspathEntry[oldclasspath.length + 1];

        for (int i = 0; i < oldclasspath.length; i++) {
            IClasspathEntry iClasspathEntry = oldclasspath[i];
            if (iClasspathEntry.equals(varEntry)) {
                if (ECLIPSE_DEBUG) System.out.println("-- JDO_CONTAINER already added to cp");
                return;
            }
        }

        for (int i = 0; i < oldclasspath.length; i++) {
            newclasspath[i] = oldclasspath[i];
        }

        newclasspath[newclasspath.length - 1] = varEntry;
        jProject.setRawClasspath(newclasspath, null);
    }

    public static void addJDOGenieJars(IProject project) {
        String path = getJDOGenieHomePath();
        if (path == null) return;

        try {
            IClasspathEntry libEntryGenie = JavaCore.newLibraryEntry(
                new Path(path + "lib/openaccess.jar"),null, null, false);
            IClasspathEntry libEntryJTA = JavaCore.newLibraryEntry(
                new Path(path + "lib/jta.jar"),null, null, false);
            IJavaProject jProject = JavaCore.create(project);

            IClasspathEntry[] currentCPs = jProject.getRawClasspath();
            java.util.List l = new ArrayList();
            for (int i = 0; i < currentCPs.length; i++) {
                IClasspathEntry currentCP = currentCPs[i];
                l.add(currentCP);
            }

            addIfNotContains(l, libEntryGenie);
            addIfNotContains(l, libEntryJTA);

            IClasspathEntry[] newCPs = new IClasspathEntry[l.size()];
            l.toArray(newCPs);

            jProject.setRawClasspath(newCPs, null);
        } catch (JavaModelException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void addIfNotContains(java.util.List l, IClasspathEntry aEntry) {
        if (!l.contains(aEntry)) {
            l.add(aEntry);
        }
    }

    public static void removeJDOGenieNature(IProject proj) throws CoreException {
        IProjectDescription description = proj.getDescription();
        
        String[] natures = description.getNatureIds();
        String[] newNatures = new String[natures.length - 1];
        for(int i = 0; i < natures.length; i++) {
            if (!natures[i].equals(ID_JDGENIE_NATURE))
                newNatures[i] = natures[i];
        }
        description.setNatureIds(newNatures);
        proj.setDescription(description, null);
        // refresh project so user sees changes
        proj.refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    /**
     * Add JDOGenie nature to the project.
     */
    public static void addJDOGenieNature(IProject proj) throws CoreException {
        IProjectDescription description = proj.getDescription();
        String[] natures = description.getNatureIds();
        for (int i = 0; i < natures.length; i++) {
            String nature = natures[i];
            if (nature.equals(ID_JDGENIE_NATURE)) {
                //already added so leave
                return;
            }
        }

        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        // must prefix with plugin id!!!
        newNatures[natures.length] = ID_JDGENIE_NATURE;
        description.setNatureIds(newNatures);
        proj.setDescription(description, null);
    }

    public static void dumpClassH(Class baseClass, String indent) {
        System.out.println(indent + " -> " + baseClass.getName());
        Class scls = baseClass.getSuperclass();
        if (scls != null) dumpClassH(scls, indent + indent);
    }
    
    public static String getAbsProjectFileName(IResource project) throws CoreException {
    	return getAbsFileName(PROP_CONFIG_FILE, project);
    }

    public static String getAbsTokenFileName(IResource project) throws CoreException {
    	return getAbsFileName(PROP_TOKEN_FILE, project);
    }

    private static String getAbsFileName(String fileName, IResource project) throws CoreException {
    	if(project == null){
    		return null;
    	}
        boolean relativePaths = getBooleanProp(VOAConfigStruct.PAGE_ID, PROP_REL_PATH, project, DEFAULT_RELATIVE_PATHS);
        String absFilePath = getProp(VOAConfigStruct.PAGE_ID, fileName, project);
        if (absFilePath == null || absFilePath.equals("")) {
            return null;
        }

        if (relativePaths) {
            //must convert the relative paths to abs ones
            absFilePath = VOAConfigStruct.toAbsFilePath(absFilePath, project);
        }
        return absFilePath;
    }

    private static String getProp(String pageID, String propName, IResource project) throws CoreException {
        return project.getPersistentProperty(new QualifiedName(pageID, propName));
    }

    public static boolean getBooleanProp(String pageID, String propName, IResource project, boolean defValue) throws CoreException {
        String val = getProp(pageID, propName, project);
        if (val != null && !val.equals("")) {
            return Boolean.valueOf(val).booleanValue();
        }
        return defValue;
    }

    public static String toRelativePath(File fPath, File fDir) {
        String path = fPath.getAbsolutePath();
        String dir = fDir.getAbsolutePath();
        path = path.replace('\\', '/');
        dir = dir.replace('\\', '/');
        if (!dir.endsWith("/")) dir += "/";
        int dirlen = dir.length();
        int pathlen = path.length();
        int pos = 0;
        for (; pos < dirlen && pos < pathlen; pos++) {
            char a = path.charAt(pos);
            char b = dir.charAt(pos);
            if (a != b) { // go back to last slash
                int i = path.lastIndexOf('/', pos);
                if (i >= 0) pos = i + 1;
                break;
            }
        }
        if (pos == 0) return path;
        if (pos == dirlen || pos == dirlen - 1) return path.substring(pos);
        StringBuffer ans = new StringBuffer(pathlen + dirlen);
        for (int p = pos; ; ) {
            int i = dir.indexOf('/', p);
            if (i < 0) break;
            ans.append("../");
            p = i + 1;
        }
        ans.append(path.substring(pos));
        return ans.toString();
    }
    
    public static File getSrcFile(IProject iProject) throws JavaModelException{
		File source = null;
		IPath path = getSrcPath(iProject);
		if(path != null){
			source = path.toFile();
		}
		return source;
    }
    
    public static IPath getSrcPath(IProject iProject) throws JavaModelException{
		IJavaProject iJavaProject = JavaModelManager.getJavaModelManager().getJavaModel().findJavaProject(iProject);
		IPackageFragmentRoot[] roots = iJavaProject.getAllPackageFragmentRoots();
		IPath rawLocation = iProject.getLocation();
		for(int x=0; x < roots.length; x++){
			IPackageFragmentRoot root = roots[x];
			if(root.getKind() == IPackageFragmentRoot.K_SOURCE){
				return rawLocation.append(root.getPath().removeFirstSegments(1));
			}
		}
		return null;
    }

    /**
	 *
	 */
	public static void addJDONature(IJavaProject currentProject) throws Exception{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Cursor waitCursor = new Cursor(window.getShell().getDisplay(), SWT.CURSOR_WAIT);
		try {
			window.getShell().setCursor(waitCursor);
			((ApplicationWindow)window).setStatus("Adding JDO Nature...");
			IProject proj = currentProject.getProject();
            addJDOGenieNature(proj);
            ((ApplicationWindow)window).setStatus("Adding JDO libraries");
            addJDOGenieCPContainer(currentProject);
            addCPVarTo(currentProject);
            ((ApplicationWindow)window).setStatus("Finished adding JDO Nature");

            VOAConfigStruct.storeProp(proj, PROP_REL_PATH, "true");
			proj.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (Exception e) {
			Shell shell = new Shell();
			MessageDialog.openInformation(
				shell,
				"JDO Ui Plug-in",
				"Cannot add JDO nature:\n" +
				 getStatusMessages(e));
            throw e;
		} finally {
			window.getShell().setCursor(null);
			waitCursor.dispose();
		}
	}
}
