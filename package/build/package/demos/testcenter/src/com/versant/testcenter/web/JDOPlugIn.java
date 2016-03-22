
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
package com.versant.testcenter.web;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import com.versant.testcenter.service.Context;
import com.versant.testcenter.service.TestCenterService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.jdo.PersistenceManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This Struts plugin obtains {@link javax.jdo.PersistenceManagerFactory}. The persistence manager
 * factory is configured using servlet init parameters. This allows for configuration by the
 * servlet engine without changing the war file. The following parameters are expected:
 * <p/>
 * main.db - database type
 * main.driver - JDBC driver class name<br>
 * main.url - JDBC connection url<br>
 * main.user - JDBC user
 * main.password - JDBC password
 * <p/>
 * The rest of the Versant configuration is loaded from  /WEB-INF/versant.properties
 * <p/>
 * The reference to {@link javax.jdo.PersistenceManagerFactory} is stored as a static attribute of {@link Context}.
 * <p/>
 * The plugin also insures that database has been populated with essential data
 */
public class JDOPlugIn implements PlugIn {

    /**
     * Servlet shutdown. Perform {@link Context#shutdown}
     */
    public void destroy() {
        Context.shutdown();
    }

    /**
     * Initialization sequence.
     */
    public void init(ActionServlet servlet, ModuleConfig config)
            throws ServletException {

        ServletContext servletContext = servlet.getServletContext();

        // for some reason Tomcat 5.5 seems to ignore the load-on-startup
        // order so we have to use this horrible hack to wait for the
        // PMFServerServlet to finish loading
        PersistenceManagerFactory pmf = null;
        for (int c = 0; c < 120; c++) {
            pmf = (PersistenceManagerFactory)servletContext.getAttribute("pmf");
            if (pmf != null) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        if (pmf == null) {
            throw new ServletException("pmf is null");
        }

        try {
            Context.initialize(pmf);
            initializeData();
        } catch (Exception e) {
            /* if ServletException is thrown during deploy, container will try to
             * redeploy the app every time it is requested. If there are still exceptions
             * during redeploy they will be shown to the client. If we were to throw UnavailableExeption
             * it will prevent container from trying to reload the servlet */
            throw new ServletException(e);
        }
    }

    /**
     * Make sure that database is populated with essential data
     */
    private void initializeData() {
        try {
            TestCenterService.beginTxn();
            TestCenterService.createDefaultAdministrator();
            TestCenterService.createExamCategories();
            TestCenterService.commitTxn();
        } finally {
            // make sure that context is released as this is not part of
            // http request processing
            Context ctx = Context.getContext();
            ctx.close();
        }
    }

}

