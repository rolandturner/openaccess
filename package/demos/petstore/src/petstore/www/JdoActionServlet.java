
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
package petstore.www;

import org.apache.struts.action.ActionServlet;

import javax.servlet.ServletException;

import petstore.model.JDOSupport;

/**
 * Extend the Struts ActionServlet to initialize the JDO Genie server.
 */
public class JdoActionServlet extends ActionServlet {

    public void init() throws ServletException {
        try {
            JDOSupport.init();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        super.init();
    }

}

 
