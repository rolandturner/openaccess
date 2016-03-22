
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
package util;

import javax.servlet.*;
import java.io.IOException;

/**
 * Servlet filter to close the PM after request processing.
 */
public class PMFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
            throws IOException, ServletException {
        try {
            fc.doFilter(req, res);
        } finally {
            Sys.cleanup(); // close the PM for request (if any) and rollback
        }
    }

    public void destroy() {
        Sys.shutdown(); // close all database connections + stop threads
    }

}

