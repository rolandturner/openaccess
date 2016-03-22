
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

import com.versant.core.jdo.tools.enhancer.Enhancer;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FilterSet;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EnhancerBuilder extends IncrementalProjectBuilder
        implements VersantStatics{
    public static String BUILDER_ID = "Versant.EnhancerBuilder";

    protected IProject[] build(int i, Map map, IProgressMonitor iProgressMonitor) throws CoreException {
        if (ECLIPSE_DEBUG) {
            System.out.println("-- EnhancerBuilder.build --");
            if (i == AUTO_BUILD) {
                System.out.println("--- DOING AUTO_BUILD ---");
            } else if (i == INCREMENTAL_BUILD) {
                System.out.println("--- DOING INCREMENTAL_BUILD ---");
            } else if (i == FULL_BUILD) {
                System.out.println("--- DOING FULL_BUILD ---");
            }
        }

        if (i != FULL_BUILD && getDelta(getProject()) == null) {
            if (ECLIPSE_DEBUG) {
                System.out.println("Leaving builder because delta is null");
            }
            return null;
        }

        try {
            enhanceImp(getProject());
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR,
                    "Versant OpenAccess", 0, e.getMessage(), e));
        }
        return null;
    }

    public static void enhance(IProject project) throws Exception {
        if (ECLIPSE_DEBUG) System.out.println("Doing explicit enhance");
        enhanceImp(project);
    }

    private static void enhanceImp(IProject project) throws Exception {
        if (ECLIPSE_DEBUG) System.out.println("EnhancerBuilder.enhanceImp");

        if (!project.exists() || !project.isOpen()) return;

        IJavaProject jProject = JavaCore.create(project);

        boolean copyProjectFile = Utils.getBooleanProp(VOAConfigStruct.PAGE_ID, PROP_COPY_PROJECT_FILE, project, DEFAULT_COPY_PROJECT_FILE);

        String absPropFilePath = Utils.getAbsProjectFileName(project);
        if (absPropFilePath == null) {
            //the propfile is not specified
            throw new Exception("Enhancement failed: No Versant OpenAccess project file specified." +
                    "\nRefer to 'Project|Properties|Versant OpenAccess Properties'");
        }
        File configFile = new File(absPropFilePath);
        if (!configFile.exists()) {
            throw new Exception("Enhancement failed: Versant OpenAccess project file does not exist." +
                    "\nSpecified file '" + configFile + "'" +
                    "\nRefer to 'Project|Properties|Versant OpenAccess Properties'");
        }

        String tokenFileName = Utils.getAbsTokenFileName(project);
        File tokenFile = null;
        if (tokenFileName != null) {
            tokenFile = new File(tokenFileName);
            if (!tokenFile.exists()) {
                tokenFile = null;
            }
        }
        IPath output = jProject.getOutputLocation().removeFirstSegments(1);

        //This is where the enhanced classes wil end up.
        File outFile = new File(project.getLocation().toFile(), output.toFile().getPath());

        ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            EnhancerClassLoader enhancerClassLoader
                    = new EnhancerClassLoader(EnhancerBuilder.class.getClassLoader(), jProject);
            Enhancer enhancer = new Enhancer();

            if (copyProjectFile) {
                copyConfigFile(configFile, outFile, tokenFile);
                configFile = new File(outFile, configFile.getName());
                if (!configFile.exists()) {
                    throw new RuntimeException("The new config file does not exist");
                }
            }
            enhancer.setClassLoader(enhancerClassLoader);
            enhancer.setPropertiesFile(configFile);
            enhancer.setOutputDir(outFile);

            enhancer.enhance();
        } finally {
            Thread.currentThread().setContextClassLoader(ctxClassLoader);
        }
    }

    private static String getAbsFileName(boolean relativePaths, String propValue, IProject project) throws CoreException {
        String absFilePath = getProp(VOAConfigStruct.PAGE_ID, propValue, project);
        if (absFilePath == null || absFilePath.equals("")) {
            return null;
        }

        if (relativePaths) {
            //must convert the relative paths to abs ones
            absFilePath = VOAConfigStruct.toAbsFilePath(absFilePath, project);
        }
        return absFilePath;
    }

    private static void copyConfigFile(File configFile, File outFile, File tokenFile) {
        Copy copy = new Copy();
        Project project = new Project();
        copy.setProject(project);

        if (tokenFile != null) {
            copy.setFiltering(true);
            FilterSet filterSet = copy.createFilterSet();
            filterSet.readFiltersFromFile(tokenFile);
        }

        copy.setFile(configFile);
        copy.setTodir(outFile);
        copy.setOverwrite(true);

        copy.execute();
    }

    private static String getProp(String pageID, String propName, IProject project) throws CoreException {
        return project.getPersistentProperty(new QualifiedName(pageID, propName));
    }

    public static void setAsDefaultBuilder() throws CoreException {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            IProject project = projects[i];
            if (!project.exists() || !project.isOpen()) continue;
            setAsDefaultBuilderImp(project);
        }
    }

    public static void setAsDefaultBuilderImp(IProject project) throws CoreException {
        IProjectDescription desp = project.getDescription();
        ICommand[] commands = desp.getBuildSpec();
        boolean found = false;

        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(BUILDER_ID)) {
                found = true;
                break;
            }
        }

        if (!found) {
            ICommand command = desp.newCommand();
            command.setBuilderName(BUILDER_ID);
            ICommand[] newCommands = new ICommand[commands.length + 1];

            // Add it after the other builders.
            System.arraycopy(commands, 0, newCommands, 0, commands.length);
            newCommands[commands.length] = command;
            desp.setBuildSpec(newCommands);
            project.setDescription(desp, null);
        }
    }

    public static void removeEnhancerBuilder(IProject project) throws CoreException {
        IProjectDescription desp = project.getDescription();
        ICommand[] commands = desp.getBuildSpec();

        boolean found = false;
        int index = -1;

        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(BUILDER_ID)) {
                index = i;
                found = true;
                break;
            }
        }

        if (found) {
            List list = new ArrayList(commands.length);
            for (int i = 0; i < commands.length; i++) {
                list.add(commands[i]);
            }
            list.remove(index);
            commands = new ICommand[commands.length - 1];
            list.toArray(commands);
            desp.setBuildSpec(commands);
            project.setDescription(desp, null);
        }

        if (isEnhancerBuilderSet(project)) {
            throw new RuntimeException("This is supposed to have unset the autobuild");
        }
    }

    public static boolean isEnhancerBuilderSet(IProject project) {
        try {
            ICommand[] commands = project.getDescription().getBuildSpec();
            for (int i = 0; i < commands.length; ++i) {
                if (commands[i].getBuilderName().equals(BUILDER_ID)) {
                    return true;
                }
            }
            return false;
        } catch (CoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static File[] findJDOGenieConfigFiles(IProject project) {
        File file = project.getLocation().toFile();
        List configFiles = new ArrayList();
        findJDOGenieConfigFilesImp(file, configFiles);
        System.out.println("\n\n\n --- configFiles = " + configFiles);

        File[] files = new File[configFiles.size()];
        configFiles.toArray(files);
        return files;
    }

    private static void findJDOGenieConfigFilesImp(File file, List configFiles) {
        String[] files = file.list();
        for (int i = 0; i < files.length; i++) {
            String s = files[i];
            if (s.endsWith("jdogenie")) {
                if (ECLIPSE_DEBUG) System.out.println("Found property file '" + s + "'");
                File f = new File(file, s);
                if (!f.isDirectory()) configFiles.add(f);
            } else {
                File f = new File(file, s);
                if (f.isDirectory()) {
                    findJDOGenieConfigFilesImp(f, configFiles);
                }
            }
        }
    }

}
