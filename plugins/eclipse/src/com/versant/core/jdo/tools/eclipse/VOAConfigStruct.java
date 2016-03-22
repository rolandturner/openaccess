
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import java.io.File;
import java.io.IOException;


public class VOAConfigStruct implements VersantStatics{

	static final String PAGE_ID = "Versant.JDOGenieId";
	private IProject project;
	private boolean doAutoBuild = false;
	private String projectFilePath = "";
	private String tokenFilePath = "";
	private boolean storePathsRel;
	private boolean copyProjectFile;
    
    public VOAConfigStruct(IProject project){
    	this.project = project;
    }
    
    public void load() throws CoreException {
        doAutoBuild = EnhancerBuilder.isEnhancerBuilderSet(project);
        projectFilePath = getProp(PROP_CONFIG_FILE, DEFAULT_PROJECT_FILE);
        tokenFilePath = getProp(PROP_TOKEN_FILE, "");
        storePathsRel = getBooleanProp(PROP_REL_PATH, DEFAULT_RELATIVE_PATHS);
        copyProjectFile = getBooleanProp(EnhancerBuilder.PROP_COPY_PROJECT_FILE, DEFAULT_COPY_PROJECT_FILE);
    }

    public boolean isDirty() throws CoreException {
        VOAConfigStruct loaded = new VOAConfigStruct(project);
        loaded.load();
        if ((doAutoBuild != loaded.doAutoBuild)
            || !projectFilePath.equals(loaded.projectFilePath)
            || !tokenFilePath.equals(loaded.tokenFilePath)
            || (storePathsRel != loaded.storePathsRel)
            || (copyProjectFile != loaded.copyProjectFile)) {
            return true;
        }
        return false;
    }

    private void dump() {
        System.out.println("\n\n --- VersantPropertyPage$ConfigStruct.dump");
        System.out.println("-- doAutoBuild = " + doAutoBuild);
        System.out.println("-- projectFilePath = " + projectFilePath);
        System.out.println("-- tokenFilePath = " + tokenFilePath);
        System.out.println("-- storePathsRel = " + storePathsRel);
        System.out.println("-- copyProjectFile = " + copyProjectFile);
        System.out.println("--- VersantPropertyPage$ConfigStruct.dump end\n\n");
    }

	
    public void store() {
        try {

            if (doAutoBuild) {
                EnhancerBuilder.setAsDefaultBuilderImp(project);
            } else {
                EnhancerBuilder.removeEnhancerBuilder(project);
            }

            storeProp(EnhancerBuilder.PROP_REL_PATH, toBoolString(storePathsRel));
            storeProp(EnhancerBuilder.PROP_COPY_PROJECT_FILE, toBoolString(copyProjectFile));
            if (storePathsRel) {
                //must convert the path to a relative one
                storeProp(EnhancerBuilder.PROP_CONFIG_FILE,
                    toRelFilePath(projectFilePath, project));
                storeProp(EnhancerBuilder.PROP_TOKEN_FILE,
                        toRelFilePath(tokenFilePath, project));
            } else {
                //store as is.
                storeProp(EnhancerBuilder.PROP_CONFIG_FILE, projectFilePath);
                storeProp(EnhancerBuilder.PROP_TOKEN_FILE, tokenFilePath);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

	private static String toAbsolutePath(String path, String dir) {
        path = path.replace('\\', '/');
        if (path.startsWith("/") || path.indexOf(':') >= 0) return path;
        dir = dir.replace('\\', '/');
        if (dir.endsWith("/")) dir = dir.substring(0, dir.length() - 1);
        int dirlen = dir.length();
        int dirpos = dirlen;
        int pathpos = 0;
        for (;;) {
            int i = path.indexOf("../", pathpos);
            if (i != pathpos) break;
            dirpos = dir.lastIndexOf('/', dirpos - 1);
            if (dirpos < 0) return path;
            pathpos += 3;
        }
        File f;
        if (dirpos == dirlen) {
            f = new File(dir, path);
        } else {
            f = new File(dir.substring(0, dirpos), path.substring(pathpos));
        }
        return f.toString();
    }

    /**
     * Check for null or empty String value.
     */
    private static boolean checkString(String absFilePath) {
        if (absFilePath == null || absFilePath.trim().equals("")) {
            return false;
        }
        return true;
    }

    static String toAbsFilePath(String relFilePath, IResource project) {
        try {
            if (!checkString(relFilePath)) {
                return "";
            }

            String absPath =
                toAbsolutePath(relFilePath, project.getLocation().toFile().getCanonicalFile().toString());
            if (ECLIPSE_DEBUG) System.out.println("absPath = " + absPath);
            return absPath;
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return "";
    }

    private String toRelFilePath(String absFilePath, IProject project) {
        try {
            if (!checkString(absFilePath)) {
                return "";
            }

            String relPath =
                Utils.toRelativePath(new File(absFilePath).getCanonicalFile(), project.getLocation().toFile().getCanonicalFile());
            if (ECLIPSE_DEBUG) System.out.println("relPath = " + relPath);
            return relPath;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return "";
        }

    }

    private void storeProp(String name, String value) throws CoreException {
        storeProp(project, name, value);
    }

    static void storeProp(IProject proj, String name, String value) throws CoreException {
        QualifiedName qualifiedName = new QualifiedName(PAGE_ID, name);
        proj.setPersistentProperty(qualifiedName, value);
    }

    private String getProp(String name, String defValue) throws CoreException{
        String val = project.getPersistentProperty(new QualifiedName(PAGE_ID, name));
        if (val == null) {
            return defValue;
        }
        return val;
    }

    private String getProp(String name) throws CoreException {
        String val = project.getPersistentProperty(new QualifiedName(PAGE_ID, name));
        if (val == null) {
            return "";
        }
        return val;
    }

    private boolean getBooleanProp(String name, boolean defValue) throws CoreException{
        String val = getProp(name);
        if (val != null && !val.equals("")) {
            return Boolean.valueOf(val).booleanValue();
        }
        return defValue;
    }

    private String toBoolString(boolean b) {
        return b ? "true" : "false";
    }


	public void setDoAutoBuild(boolean doAutoBuild) {
		this.doAutoBuild = doAutoBuild;
	}

	public boolean isDoAutoBuild() {
		return doAutoBuild;
	}

	public void setProjectFilePath(String projectFilePath) {
		this.projectFilePath = projectFilePath;
	}

	public String getProjectFilePath() {
		return projectFilePath;
	}

	public void setTokenFilePath(String tokenFilePath) {
		this.tokenFilePath = tokenFilePath;
	}

	public String getTokenFilePath() {
		return tokenFilePath;
	}

	public void setStorePathsRel(boolean storePathsRel) {
		this.storePathsRel = storePathsRel;
	}

	public boolean isStorePathsRel() {
		return storePathsRel;
	}

	public void setCopyProjectFile(boolean copyProjectFile) {
		this.copyProjectFile = copyProjectFile;
	}

	public boolean isCopyProjectFile() {
		return copyProjectFile;
	}
}
