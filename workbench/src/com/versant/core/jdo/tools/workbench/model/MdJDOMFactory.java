
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

import org.jdom.*;
import org.jdom.DefaultJDOMFactory;

/**
 * Singleton that extends the DefaultJDOMFactory to create MdDocument's
 * and MdElement's instead of Document's and Element's.
 *
 * @keep-all
 */
public class MdJDOMFactory extends DefaultJDOMFactory {

    private static MdJDOMFactory instance = new MdJDOMFactory();

    public static MdJDOMFactory getInstance() {
        return instance;
    }

    private MdJDOMFactory() {
    }

    public Document document(Element rootElement, DocType docType) {
        return new MdDocument(rootElement, docType);
    }

    public Document document(Element rootElement) {
        return new MdDocument(rootElement);
    }

    public Element element(String name, Namespace namespace) {
        return new MdElement(name, namespace);
    }

    public Element element(String name) {
        return new MdElement(name);
    }

    public Element element(String name, String uri) {
        return new MdElement(name, uri);
    }

    public Element element(String name, String prefix, String uri) {
        return new MdElement(name, prefix, uri);
    }

}

