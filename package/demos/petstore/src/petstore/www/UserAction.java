
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

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jdo.PersistenceManager;

import petstore.model.CustomerDelegate;
import petstore.www.RequestUtils;
import petstore.www.RequestProcessor;

/**
 */
public class UserAction extends DispatchAction {


    public ActionForward login(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        DynaActionForm f = (DynaActionForm)form;
        CustomerDelegate customer = RequestUtils.getCustomer(request);
        customer.setUserCustomer((String)f.get("login"),
                (String)f.get("password"));

        response.sendRedirect((String)f.get("requestURL"));

        return null;

    }

    public ActionForward logout(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PersistenceManager pm = RequestUtils.getPersistenceManager(request);
        pm.close();
        request.getSession().invalidate();

        response.sendRedirect("index.jsp");

        return null;

    }


    public ActionForward showLoginPage(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        DynaActionForm f = (DynaActionForm)form;
        f.set("requestURL",(String) request.getAttribute(RequestProcessor.ORIGINAL_URL));
        return mapping.findForward("loginPage");

    }


    public ActionForward createUserCustomer(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        CustomerDelegate customer = RequestUtils.getCustomer(request);
        DynaActionForm f = (DynaActionForm)form;
        customer.createUserCustomer((String)f.get("login"),(String)f.get("password"));

        return mapping.findForward("editCustomer");

    }
}
