
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
package com.versant.core.jdo.tools.workbench.ant;

import javax.jdo.JDOFatalUserException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class runs ant build targets using Runtime.exec in the build file dir.
 *
 * @keep-all
 */
public class AntRunner {

    private ArrayList targetsList = new ArrayList();
    private File buildFile;
    private Throwable buildFileError;
    private String antCommand;
    private String antArgs;

    private Process process;
    private int exitCode;
    private ArrayList targetsObjectList;
    private String possibleEnhanceTarget;
    private String[] cmd;

    public AntRunner() {
    }

    public String getAntCommand() {
        return antCommand;
    }

    public void setAntCommand(String antCommand) {
        this.antCommand = antCommand;
    }

    public String getAntArgs() {
        return antArgs;
    }

    public void setAntArgs(String antArgs) {
        this.antArgs = antArgs;
    }

    /**
     * Set and parse the build file.
     *
     * @throws JDOFatalUserException if invalid or does not exist
     */
    public void setBuildFile(File buildFile) throws JDOFatalUserException {
        buildFileError = null;
        try {
            setBuildFileImp(buildFile);
        } catch (JDOFatalUserException e) {
            buildFileError = e;
            throw e;
        }
    }

    private void setBuildFileImp(File buildFile) throws JDOFatalUserException {
        this.buildFile = buildFile;
        targetsList.clear();
        possibleEnhanceTarget = null;
        if (buildFile.exists()) {
            AntBuildParser p = new AntBuildParser();
            p.parse(buildFile);
            targetsObjectList = p.getTargetList();
            for (Iterator iter = targetsObjectList.iterator();
                 iter.hasNext();) {
                Target target = (Target)iter.next();
                targetsList.add(target.getName());
            }
            Collections.sort(targetsList);
            possibleEnhanceTarget = p.getEnhanceTargetName();
        } else {
            throw new JDOFatalUserException("File '" + buildFile +
                    "' does not exist");
        }
    }

    public File getBuildFile() {
        return buildFile;
    }

    /**
     * Do we have a sucessfully parsed build file? This will be false if
     * the file does not exist or contains errors.
     */
    public boolean isBuildFileOk() {
        return buildFileError == null;
    }

    /**
     * Has the build file been set and does it exist?
     */
    public boolean isBuildFileExists() {
        return buildFile != null && buildFile.exists();
    }

    /**
     * Get the build file error or null if ok.
     */
    public Throwable getBuildFileError() {
        return buildFileError;
    }

    /**
     * Reparse the build file.
     *
     * @throws JDOFatalUserException if invalid
     */
    public void reloadBuildFile() throws JDOFatalUserException {
        setBuildFile(buildFile);
    }

    /**
     * Sorted ArrayList of all the names of all targets in the build file.
     */
    public ArrayList getTargetsList() {
        return targetsList;
    }

    public ArrayList getTargetsObjectList() {
        return targetsObjectList;
    }

    /**
     * Runs an ant build target using Runtime.exec.
     */
    public Process run(String target) throws Exception {
        stop();
        ArrayList a = new ArrayList();
        a.add(antCommand);
        if (antArgs != null) a.add(antArgs);
        if (buildFile != null) {
            a.add("-f");
            a.add(buildFile.toString());
        }
        if (target != null) a.add(target);
        setCmd(new String[a.size()]);
        a.toArray(getCmd());
        process = Runtime.getRuntime().exec(getCmd(), null,
                buildFile == null ? null : buildFile.getParentFile());
        return process;
    }

    /**
     * Is an Ant build currently busy?
     */
    public boolean isBusy() {
        if (process == null) return false;
        try {
            exitCode = process.exitValue();
        } catch (IllegalThreadStateException e) {
            return true;
        }
        cleanup();
        return false;
    }

    /**
     * Get the exit code of the last run.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Stop the current Ant build if any. This waits until the build has
     * stopped before returning. NOP if no build is in progress.
     */
    public void stop() {
        if (process == null) return;
        process.destroy();
        for (; ;) {
            try {
                exitCode = process.waitFor();
                break;
            } catch (InterruptedException e) {
                // ignore
            }
        }
        cleanup();
    }

    private void cleanup() {
        process = null;
    }

    public String getPossibleEnhanceTarget() {
        return possibleEnhanceTarget;
    }

    public String[] getCmd() {
        return cmd;
    }

    public void setCmd(String[] cmd) {
        this.cmd = cmd;
    }
}
