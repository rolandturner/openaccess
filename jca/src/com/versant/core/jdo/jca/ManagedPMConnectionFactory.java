
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
package com.versant.core.jdo.jca;

import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.jdo.VersantPMFInternal;
import com.versant.core.jdo.PersistenceManagerFactoryImp;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Implementation of {@link ManagedConnectionFactory}.
 * This is the <managedconnectionfactory-class> in the jca
 */
public class ManagedPMConnectionFactory
        implements Serializable, ManagedConnectionFactory{

    private TxUtils txUtils;
    private boolean forceConnectionMatching = false;
    private VersantPMFInternal pmf;
    private ClassLoader loader = null;
    private String propertiesResource = "versant.properties";
    private PrintWriter logWriter = null;
    /**
     * Class version identifies the app deployment
     */
    private Class appClass = null;
    private String version = "1.0";
    public boolean remote = false;
    private String versantPropsString = "";
    private Properties versantProps = new Properties();


    public ManagedPMConnectionFactory() { }

    public PMRequestInfo createRequestInfo() {
        return new PMRequestInfo();
    }

    TxUtils getTxUtils() {
        if (txUtils == null){
            txUtils = new TxUtils("");
        }
        return txUtils;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return this.logWriter;
    }

    public void setLogWriter(PrintWriter printwriter)
            throws ResourceException {
        this.logWriter = printwriter;
    }

    public Object createConnectionFactory(ConnectionManager cm)
            throws ResourceException {
        return new PMConnectionFactory(this, cm);
    }

    public Object createConnectionFactory()
            throws ResourceException {
        throw new ResourceException("ConnectionManager required");
    }

    /**
     * Determine if the pmf should be restarted. This is determined by the classloader
     * of the current appClass.
     */
    private boolean shouldRestart(ClassLoader loader)
            throws ClassNotFoundException {
        if (pmf == null) return false;
        if (remote) return false;
        final Class cls = appClass;
        return cls != loader.loadClass(cls.getName());
    }

    public boolean shouldRestart()
            throws ClassNotFoundException {
        return shouldRestart(loader);
    }

    public ManagedConnection matchManagedConnections(Set mcs, Subject
            subject, ConnectionRequestInfo cri) throws ResourceException {
        Transaction tx = null;
        try {
            tx = getTxUtils().getTransactionManager().getTransaction();
        } catch (SystemException e) {
            e.printStackTrace(System.out);
            throw new ResourceException("Unable to get Transaction > " + e.getMessage() + "\n");
        }

        ManagedPMConnection match = null;
        for (Iterator iterator = mcs.iterator(); iterator.hasNext();) {
            ManagedPMConnection con = (ManagedPMConnection) iterator.next();
            Transaction conTx = con.getCurrentTransaction();
            if (tx == null && conTx == null) {
                match = con;
                break;
            }
            if (tx != null && conTx == null) {
                match = con;
                break;
            }
            if (tx != null && tx.equals(conTx)) {
                match = con;
                break;
            }
        }
        if (match != null && tx != null) {
            match.setCurrentTransaction(tx);
        }
        return match;
    }

    public ManagedConnection createManagedConnection(Subject
            subject, ConnectionRequestInfo cri) throws ResourceException {
        try {
//            PMRequestInfo info = cri != null ? (PMRequestInfo) cri : new PMRequestInfo();
            PMRequestInfo info;
            if (cri == null) {
                info = new PMRequestInfo();
            } else {
                info = new PMRequestInfo((PMRequestInfo) cri);
            }
            ManagedPMConnection connection = new ManagedPMConnection(this, info);
            connection.setLogWriter(logWriter);
            return connection;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new ResourceException("Unable to create connection > " + e.getMessage() + "\n");
        }
    }

    /**
     * Return the pmf. This might return null if the pmf can not be created at this stage.
     * e.g. if the propertiesResource file can not be loaded.
     */
    public VersantPersistenceManagerFactory getPMF(boolean lazy) throws Exception {
        loader = Thread.currentThread().getContextClassLoader();
        if (pmf == null || shouldRestart(loader)) {
            return startServer(loader, lazy);
        }
        return pmf;
    }

    /**
     * Try to obtain a PMF. This will only happen if the propertiesResource file is available.
     */
    private VersantPersistenceManagerFactory startServer(ClassLoader loader,
            boolean lazy) throws ResourceException {
        Properties props = loadProperties(loader, lazy);
        if (lazy && props == null) {
            return null;
        }

        if (pmf != null) {
            try {
                pmf.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pmf = new PersistenceManagerFactoryImp(props, loader);
//        pmf = (VersantPMFInternal)JDOHelper.getPersistenceManagerFactory(
//                props, loader);
        if (pmf.getClass().getName().indexOf(".Remote") > 0) {
            remote = true;
        } else {
            try {
                appClass = pmf.getJDOMetaData().classes[0].cls;
            } catch (Exception e) {
                throw new ResourceException(
                        "Unable to get persistent class metadata. " +
                        "Make sure you have persistence classes in your " +
                        "application.");
            }
            remote = false;
        }
        return pmf;
    }

    /**
     * If the primary datasource returns already enlisted connections.
     */
    public boolean isEnlistedDataSource() throws Exception {
        getPMF(false);
        return pmf.isEnlistedDataSource();
    }

    private Properties loadProperties(ClassLoader loader, boolean lazy)
            throws ResourceException {
        Properties localProps = new Properties();
        InputStream is = loader.getResourceAsStream(propertiesResource);
        if (is == null) {
            String msg = "Unable to load resource '" + propertiesResource +
                "'. Make sure this file is included in your " +
                "application archive";
            if (lazy) {
                System.out.println(msg);
                return null;
            } else {
                throw new ResourceException(msg);
            }
        }

        try {
            localProps.load(is);
        } catch (IOException e) {
            throw new ResourceException("Unable to read '" +
                    propertiesResource + "' cause: " + e.getMessage());
        }

        //update from the user set props.
        localProps.putAll(versantProps);
        return localProps;
    }

    public int hashCode() {
        return 17;
    }

    public boolean equals(Object other) {
        return this == other;
    }

    /**
     * Specify the name of the propertiesResource to load.
     */ 
    public void setPropertiesResource(String res) {
        this.propertiesResource = res.startsWith("/") ? res.substring(1) : res;
    }

    public String getPropertiesResource() {
        return propertiesResource;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The ForceConnectionMatching as set from the ra.xml.
     */
    public boolean getForceConnectionMatching() {
        return forceConnectionMatching;
    }

    /**
     * The ForceConnectionMatching as set from the ra.xml.
     */
    public void setForceConnectionMatching(boolean forceConnectionMatching) {
        this.forceConnectionMatching = forceConnectionMatching;
    }
    
    /**
     * The ForceConnectionMatching as set from the ra.xml.
     */
    public void setForceConnectionMatching(Boolean forceConnectionMatching) {
        this.forceConnectionMatching = forceConnectionMatching.booleanValue();
    }

    public String getVersion() {
        return version;
    }

    /**
     * The property string as set from the ra.xml.
     */
    public String getProperties() {
        return versantPropsString;
    }

    /**
     * The property string as set from the ra.xml.
     */
    public void setProperties(String props) {
        this.versantPropsString = props;
        try {
            versantProps.load(new ByteArrayInputStream(props.getBytes("8859_1")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
