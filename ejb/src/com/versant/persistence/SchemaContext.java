
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
/**
 * @author Rick George
 * @version 1.0, 9/4/05
 */
package com.versant.persistence;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

/**
 * The <code>SchemaContext</code> class provides a mechanism for
 * marshalling and unmarshalling <code>T</code>
 * objects to and from files and input/output streams based
 * on the javax.persistence xml schema.
 */
public class SchemaContext<T> implements ValidationEventHandler {
	private String schema;
	private ClassLoader loader;
    
    public SchemaContext(String schema, ClassLoader loader) {
    	this.schema = schema;
    	this.loader = loader;
    }
    
    /**
     * Unmarshal a mapping.xml file and return a newly constructed
     * <code>T</code> object.
     * @param filename the name of the xml file to unmarshal.
     * @return the unmarsalled <code>T</code>xml file
     * @throws XmlException
     */
    public T unmarshal(String filename) 
    							throws XmlException {
        InputStream in = null; 
    	T result = null;
    	try {
			in = new FileInputStream(filename);
			result = this.unmarshal(in);
		}
    	catch (FileNotFoundException e) {
    		throw new XmlException();
		}
		finally {
			this.close(in);
		}  	
    	return result;
    }
    
    /**
     * Return a <code>T</code> object from an input stream.
     * @param in the stream to use for unmarshalling.
     * @return the resulting <code>T</code> content from
     * the given input stream.
     * @throws XmlException
     */
    public T unmarshal(InputStream in) 
 								throws XmlException {
    	createContext();
    	T result = null;
    	try {
        	// create a Marshaller and marshal to out
        	Unmarshaller u = jc.createUnmarshaller();
        	u.setEventHandler(this);          
        	result = (T)u.unmarshal(in);
		}
		catch (JAXBException e) {
			throw new XmlException(e);
		}
		return result;
    }
    
    /**
     * Closes an input or output stream used for marshalling
     * or unmarshalling.
     * @param stream the stream to close.
     * @throws XmlException
     */
    private void close(Closeable stream) 
    							throws XmlException {
    	try {
    		if (stream != null) {
    			stream.close();
    		}
		} catch (IOException e) {
			throw new XmlException(e);
		}
    }
    
    /**
     * Marshal an <code>T</code> object to an
     * output file.
     * @param content the <code>T</code> object to marshal.
     * @param filename the file to marshal the <code>
     * T</code> object to.
     * @throws XmlException
     */
    public void marshal(T content, String filename) 
    						throws XmlException {
    	OutputStream out = null;
    	try {
			out = new PrintStream(filename);
			this.marshal(content, out);
		} catch (FileNotFoundException e) {
			throw new XmlException(e);
		}
		finally {
			this.close(out);
		}
    }
    
    /**
     * Marshal an <code>T</code> object to an
     * output stream.
     * @param content the <code>T</code> object to marshal.
     * @param filename the file to marshal the <code>
     * T</code> object to.
     * @throws XmlException
     */
    public void marshal(T content, OutputStream out) 
    							throws XmlException {
    	createContext();
		try {
			// create a Marshaller and marshal to out
	        Marshaller m = jc.createMarshaller();
	        m.setEventHandler(this);          
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	        m.marshal(content, out);
		} catch (JAXBException e) {
			throw new XmlException(e);
		}
    }

    private JAXBContext jc = null;
    
    /**
     * Create a new context for marshalling and unmarshalling
     * <code>T</code> objects.
     * @throws XmlException
     */
    private void createContext() throws XmlException {
    	if (jc == null) {
    		try {
    			jc = JAXBContext.newInstance(schema, loader);
    		}
    		catch (JAXBException e) {
    			throw new XmlException(e);
    		}
    	}
    }
    
    private List<String> errors = new ArrayList<String>();

    /**
     * Handle validation errors and add to list.
     */
	public boolean handleEvent(ValidationEvent ve) {
        if (ve.getSeverity() != ValidationEvent.WARNING) {
            ValidationEventLocator vel = ve.getLocator();
            String message = "Line:Col[" + 
            			vel.getLineNumber() + ":" + 
            			vel.getColumnNumber() + "]:" + 
            			ve.getMessage();
            errors.add(message);
        }
		return true;
	}
	
	public List<String> getErrors() {
		return errors;
	}

}
