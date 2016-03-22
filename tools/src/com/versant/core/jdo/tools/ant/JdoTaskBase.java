
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



import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.taskdefs.MatchingTask;


import com.versant.core.metadata.ModelMetaData;
import com.versant.core.logging.LogEventStore;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageManagerFactoryBuilder;



/**
 * Base class for tasks that need jdo meta data.
 */
public class JdoTaskBase

        extends MatchingTask
 {

    public static String DEFAULT_CONFIG_FILENAME = "versant.properties";
    protected String configFilename = DEFAULT_CONFIG_FILENAME;

    protected ModelMetaData metaData;
    protected LogEventStore pes;
    protected StorageManagerFactory smf;
    protected StorageManagerFactory innermostSmf;
    protected ClassLoader taskClassLoader;


    protected Path classpath;



    public void setConfig(String config) {
        configFilename = config;
    }

    public String getConfig() {
        return configFilename;
    }

    public void setProject(String config) {
        setConfig(config);
    }

    public void log(String msg) {
        if (project == null) {
            System.out.println(msg);
        }

        else {
            super.log(msg);
        }

    }

    /**
     * Set the classpath to be used for this compilation.
     */

    public void setClasspath(Path classpath) {
        if (classpath == null) {
            this.classpath = classpath;
        } else {
            classpath.append(classpath);
        }
    }



    protected ClassLoader getClassLoader() {

        if (taskClassLoader != null) return taskClassLoader;
        ClassLoader cl = getClass().getClassLoader();
        if (cl == null) {
            cl = ClassHelper.get().getSystemClassLoader();
        }
        if (classpath == null || project == null) {
            taskClassLoader = cl;
        } else {
            taskClassLoader = new AntClassLoader(cl, project, classpath, true);
        }
        return taskClassLoader;


    }

    /**
     * Creates a nested classpath element.
     */

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }


    /**
     * Adds a reference to a CLASSPATH defined elsewhere.
     */

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }


    /**
     * Gets the classpath.
     */

    public Path getClasspath() {
        return classpath;
    }




    /**
     * Initialize this task. This will load all the meta data and create
     * and initialize data stores etc.
     */
    public void execute() 
	{
        // parse the config
        ConfigParser p = getConfigParser();

        Thread.currentThread().setContextClassLoader(getClassLoader());
        
        ConfigInfo config = null;
        try {
        	config = p.parseResource(configFilename, getClassLoader());
        	config.validate();
        } catch (Exception e) {
    		Properties props = new Properties();
    		config = p.parse(props);
        }



/*
        // create metadata from roots AND names
        MetaDataParser mp = new MetaDataParser();
        JdoRoot[] extra = mp.parse(config.jdoResources, loader);
        JdoRoot[] combined = new JdoRoot[extra.length +
                config.jdoMetaData.length];
        System.arraycopy(config.jdoMetaData, 0, combined, 0,
                config.jdoMetaData.length);
        System.arraycopy(extra, 0, combined, config.jdoMetaData.length,
                extra.length);
        config.jdoMetaData = combined;
*/

        StorageManagerFactoryBuilder b = getStorageManagerFactoryBuilder();
        b.setConfig(config);

        b.setLoader(getClassLoader());

        b.setOnlyMetaData(!isCreateConnectionPool());
        b.setFullInit(false);
        b.setIgnoreConFactoryProperties(true);
        smf = b.createStorageManagerFactory();
        for (innermostSmf = smf; ;) {
            StorageManagerFactory next =
                    innermostSmf.getInnerStorageManagerFactory();
            if (next == null) break;
            innermostSmf = next;
        }
        pes = b.getLogEventStore();
    }
    
    protected ConfigParser getConfigParser() {
        ConfigParser p = null;

        try {
        	Class c = Class.forName("com.versant.core.ejb.ConfigParser");
        	p = (ConfigParser)c.newInstance();
        } catch (Throwable e) {
        	p = new ConfigParser();
        }


        return p;
    }

    protected StorageManagerFactoryBuilder getStorageManagerFactoryBuilder() {
        StorageManagerFactoryBuilder b = null;

        try {
        	Class c = Class.forName("com.versant.core.ejb.StorageManagerFactoryBuilder");
        	b = (StorageManagerFactoryBuilder)c.newInstance();
        } catch (Throwable e) {
         	b = new StorageManagerFactoryBuilder();
        }


        return b;
    }

    /**
     * Override this to return false if a task does not require actual
     * datastore connections.
     */
    protected boolean isCreateConnectionPool() {
        return true;
    }

    /**
     * Initialize the data stores. This must be called if the stores are
     * to be used for anything other than meta data generation.
     */
    protected void initDataStores() {
        //dataStore.init();
    }

    public ModelMetaData getMetaData() {
        return metaData;
    }

    public StorageManagerFactory getSmf() {
        return smf;
    }

    public StorageManagerFactory getInnermostSmf() {
        return innermostSmf;
    }

    public LogEventStore getPerfEventStore() {
        return pes;
    }

    public void throwBuildException(String str) {
        throwBuildException(str, null);
    }

    public void throwBuildException(String str, Throwable e) {
        
        throw new BuildException(str, e);
        

    }
}
