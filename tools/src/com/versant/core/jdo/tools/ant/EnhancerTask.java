
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
package com.versant.core.jdo.tools.ant;

import java.util.*;
import java.io.File;


import com.versant.core.common.config.ConfigParser;
import com.versant.core.jdo.tools.enhancer.Enhancer;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.BuildException;


/**
 * @keep-all
 *
 */
public class EnhancerTask extends JdoTaskBase {

    protected String configFilename = DEFAULT_CONFIG_FILENAME;
    private File dir;
    private List awareList = new ArrayList();
    private List queryList = new ArrayList();
	private boolean genHyper;
	private File hyperdriveDir;
	private File hyperSrcDir;
	private boolean detach = true;
	private boolean makeFieldsPrivate;


    public EnhancerTask() {}

    public void addPersistentaware(PCAwareSet aware){
        awareList.add(aware);
    }

    public void addQueries(QuerySet queries) {
        queryList.add(queries);
    }

    public void addPersistentAwareFileset(FileSet fileset) {
        throw new BuildException("DEPRECATED - The <persistentAwareFileset> tag has been deprecated.\n" +
                                 "use:\n" +
                                 "    <persistentaware dir=\"${dir.compile}\">\n"+
                                 "         <package name=\"za.co.hemtech.bla.*\"/>\n" +
                                 "    </persistentaware>\n");
    }

    public void setConfig(String config) {
        configFilename = config;
    }

    public void setOutputDir(File dir) {
        this.dir = dir;
    }

	public void setGenHyper(boolean s){
		this.genHyper = s;
	}

    public void setHyperdriveDir(File hyperdriveDir) {
        this.hyperdriveDir = hyperdriveDir;
    }

    public void setDetach(boolean detach) {
        this.detach = detach;
    }

    public void setmakeFieldsPrivate(boolean s) {
        this.makeFieldsPrivate = s;
    }

    public void setHyperSrcDir(File hyperSrcDir) {
        this.hyperSrcDir = hyperSrcDir;
    }


    public void execute() throws BuildException {
        try {
            executeImp();
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
            }
            throw e;
        }
    }
    
    private void executeImp() throws BuildException {

        HashSet classes = new HashSet();
        HashSet queries = new HashSet();

        if (!awareList.isEmpty()){
            for (Iterator iterator = awareList.iterator(); iterator.hasNext();) {
                PCAwareSet awareSet = (PCAwareSet) iterator.next();
                classes.addAll(awareSet.getAwareClasses(super.project));
            }
        }

        if (!queryList.isEmpty()) {
            for (Iterator iterator = queryList.iterator(); iterator.hasNext();) {
                QuerySet querySet = (QuerySet) iterator.next();
                queries.addAll(querySet.getQueryClasses(super.project));
            }
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(getClassLoader());
        enhancer.setDetached(detach);
        enhancer.setMakeFieldsPrivate(makeFieldsPrivate);
        enhancer.setGenHyper(genHyper);
        if (hyperSrcDir != null){
            enhancer.setGenSrc(true);
            enhancer.setSrcOutDir(hyperSrcDir);
        }
        enhancer.setPropertiesResourceName(configFilename);
        enhancer.setOutputDir(dir);
        enhancer.setPCAwareFiles(classes);
        enhancer.setHyperdriveDir(hyperdriveDir);
//        enhancer.setQueriesFiles(queries);
        try {
            enhancer.enhance();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
}
