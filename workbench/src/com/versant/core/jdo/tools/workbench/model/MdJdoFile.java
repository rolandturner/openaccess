
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
package com.versant.core.jdo.tools.workbench.model;

import org.jdom.Document;

import org.jdom.input.SAXBuilder;


import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import com.versant.core.metadata.parser.JdoExtensionKeys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A .jdo file. This fires ChangeEvent's on itself and its MdProject when its
 * dirty flag changes.
 */
public class MdJdoFile extends MdBase implements MdChangeListener, MdProjectProvider {

    public static final int STATUS_NOT_LOADED = 1;
    public static final int STATUS_NOT_FOUND = 2;
    public static final int STATUS_XML_ERROR = 3;
    public static final int STATUS_LOADED = 4;
    public static final int STATUS_OK = 5;

    private MdProject project;
    
    private String resourceName;
    private String resourceNameShort;

    
    private URL url;
    private MdDocument doc;
    private int status = STATUS_NOT_LOADED;
    private List packageList = new ArrayList();     // of MdPackage
    private long lastModified = -1;


    public MdJdoFile(MdProject project, String resourceName) {
        this.project = project;
        setResourceName(resourceName);
    }

    

    public boolean isDirty() {
        return doc != null && doc.isDirty();
    }

    public void setDirty(boolean dirty) {
        if (doc != null) doc.setDirty(dirty);
    }

    /**
     * Our doc's dirty flag has changed state.
     */
    public void metaDataChanged(MdChangeEvent e) {
        fireMdChangeEvent(project, null);
        if (project != null) project.jdoFileChanged();
    }

    
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
        if (resourceName.endsWith(".jdo")) {
            resourceNameShort = resourceName.substring(0,
                    resourceName.length() - 4);
        } else {
            resourceNameShort = resourceName;
        }
    }
    

    public URL getUrl() {
        return url;
    }

    public MdProject getProject() {
        return project;
    }

  
    public String toString() {
        return resourceNameShort;
    }


    /**
     * Parse the meta data and return the JDom Document.
     */
    public Document getDocument() throws Exception {
        if (doc == null) {
            status = STATUS_NOT_LOADED;
            
            SAXBuilder b = new SAXBuilder(false);
            b.setFactory(MdJDOMFactory.getInstance());
            b.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId,
                        String systemId) {
                    StringReader reader = new StringReader("");
                    return new InputSource(reader);
                }
            });
            url = getResource(resourceName);
            if (url == null) {
                status = STATUS_NOT_FOUND;
                String msg = "Resource not found on classpath: " + resourceName;
                logError(msg);
                throw new MdVetoException(msg);
            }else{
                setLastModified();
            }
            logInfo("Parsing " + url);            
 

            try {

                doc = (MdDocument)b.build(url);
                upgrade(doc);
            } catch (Exception e) {
                status = STATUS_XML_ERROR;
                logError(e);
                throw e;
            }
            status = STATUS_LOADED;
            analyze();
             
            doc.addMdChangeListener(this);
        }
        return doc;
    }

    private long getLastModified() {
        try {
            if (url.getProtocol().equals("file")) {
                String file = url.getFile();
                if(MdUtils.isStringNotEmpty(file)){
                    File f = new File(file);
                    if(f.exists()){
                        return f.lastModified();
                    }
                }
            }
        } catch (Exception e) {
            //Do Nothing
        }
        return -1;
    }

    public void setLastModified() {
        lastModified = getLastModified();
    }

    public boolean hasFileChanged() {
        if (lastModified >= 0) {
            long newLastModified = getLastModified();
            return newLastModified != lastModified;
        }
        return false;
    }

    private void logError(Exception e) {
        if (project != null) project.getLogger().error(e);
    }

    private void logError(String msg) {
        if (project != null) project.getLogger().error(msg);
    }

    private void logInfo(String msg) {
        if (project != null) project.getLogger().info(msg);
    }


    private URL getResource(String resourceName) throws Exception {    	
        ClassLoader cl = project.getProjectClassLoader();
        return cl.getResource(resourceName);     
    }
     

    /**
     * Upgrade document renaming elements to cater for changes to our meta data.
     */
    public static void upgrade(Document doc) {
        XmlUtils.renameExtension(doc.getRootElement(),
                JdoExtensionKeys.JDBC_LINK_FOREIGN_KEY,
                JdoExtensionKeys.INVERSE);
    }

    /**
     * Create a new document for us.
     */
    public void createDocument(File f) throws MalformedURLException {
        if (doc != null) throw new IllegalStateException("doc is not null");
        doc = new MdDocument(new MdElement("jdo"));
        status = STATUS_LOADED;
        analyze();
        doc.addMdChangeListener(this);
        setDirty(true);
        
        url = f.toURL();
        setLastModified();

        
	}



    public boolean hasDocument() {
        return doc != null;
    }

    /**
     * Save our document. This is a NOP if we are not dirty or do not have
     * a document.
     */
     
    public void save() throws Exception {
        if (!isDirty()) return;
        if (!url.getProtocol().equals("file")) {
            logError("Cannot save to non-file URL: " + url);
            return;
        }
        XMLOutputter outp = new XMLOutputter(XmlUtils.XML_FORMAT);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(url.getFile());
            outp.output(doc, os);
        } finally {
            if (os != null) os.close();
        }
        logInfo("Saved " + url);
        setLastModified();
        setDirty(false);
    }
    


    public int getStatus() {
        return status;
    }

    public List getPackageList() {
        return packageList;
    }

    /**
     * Find package with name or null if none.
     */
    public MdPackage findPackage(String name) {
        int len = packageList.size();
        for (int i = 0; i < len; i++) {
            MdPackage p = (MdPackage)packageList.get(i);
            String n = p.getName();
            if (n != null && n.equals(name)) return p;
        }
        return null;
    }

    public void addPackage(MdPackage p) throws Exception {
        Document d = getDocument();
        MdElement root = (MdElement)d.getRootElement();
        root.addContent(p.getElement());
        XmlUtils.makeDirty(root);
        packageList.add(p);
    }

    public void removePackage(MdPackage p) throws Exception {
        Document d = getDocument();
        MdElement root = (MdElement)d.getRootElement();
        root.removeContent(p.getElement());
        XmlUtils.makeDirty(root);
        packageList.remove(p);
    }

    public void analyze() {
        packageList.clear();
        MdElement root = (MdElement)doc.getRootElement();
		List plist = root.getChildren(ModelConstants.PACKAGE_NODE_NAME);
        for (Iterator i = plist.iterator(); i.hasNext();) {
            MdElement pe = (MdElement)i.next();
            packageList.add(new MdPackage(this, pe));
        }
	}

    public void getAllClasses(List a) {
        int n = packageList.size();
        for (int i = 0; i < n; i++) {
            MdPackage p = (MdPackage)packageList.get(i);
            a.addAll(p.getClassList());
        }
    }

    public void getAllInterfaces(List a) {
        int n = packageList.size();
        for (int i = 0; i < n; i++) {
            MdPackage p = (MdPackage)packageList.get(i);
            a.addAll(p.getInterfaceList());
        }
    }

    public void dump() {
        if (doc == null) {
            System.out.println("No doc");
        } else {
            try {
                XMLOutputter outp = new XMLOutputter(XmlUtils.XML_FORMAT);
                outp.output(doc, System.out);
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }


    public void reload() {
        doc = null;
    }




    public MdProject getMdProject() {
        return getProject();
    }
}

