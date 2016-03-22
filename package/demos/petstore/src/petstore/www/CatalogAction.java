
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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import petstore.model.CatalogDelegate;
import petstore.model.Page;
import petstore.db.Item;
import petstore.www.RequestUtils;
import petstore.www.Constants;


/**
 */
public class CatalogAction extends DispatchAction {


    public static final String CATEGORY_PAGE_KEY = CatalogAction.class + ".categoryPage";
    public static final String PRODUCT_PAGE_KEY = CatalogAction.class + ".productPage";
    public static final String ITEM_PAGE_KEY = CatalogAction.class + ".itemPage";
    public static final String ITEM_KEY = CatalogAction.class + ".item";
    public static final String ITEM_OID_KEY = CatalogAction.class + ".itemOID";

    public static int MAX_CATEGORY_CNT = 10;

    public ActionForward populateCatalog(ActionMapping mapping, ActionForm form,
                                         HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        CatalogDelegate catalog = RequestUtils.getCatalog(request);
        catalog.populateCatalog();

        return showCatalog(mapping, form, request, response);

    }


    public ActionForward showCatalog(ActionMapping mapping, ActionForm form,
                                     HttpServletRequest request, HttpServletResponse response)
            throws Exception {



        return (mapping.findForward("catalogPage"));

    }


    public ActionForward listCategories(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        int start = 0;
        int count = 10;


        CatalogDelegate catalog = RequestUtils.getCatalog(request);
        Page page = null;
        page = catalog.getCategories(start, count);
        request.setAttribute(CATEGORY_PAGE_KEY, page);

        return (mapping.findForward("categoryPage"));

    }

    /*
    public ActionForward listProducts(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        PageForm pageForm = (PageForm) form;
        int start = 0;
        int count = 10;

        try {
            start = Integer.parseInt(pageForm.getStart());
            count = Integer.parseInt(pageForm.getCount());
        } catch (NumberFormatException e) {
        }
        CatalogDelegate catalog = RequestUtils.getCatalog(request);
        Page page = catalog.getProducts(pageForm.getParent(), start, count);
        pageForm.setPage(page);

        return (mapping.findForward("productPage"));
    }
    */

    public ActionForward listItems(ActionMapping mapping, ActionForm form,
                                   HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PageForm pageForm = (PageForm) form;
        int start = 0;
        int count = 10;

        try {
            start = Integer.parseInt(pageForm.getStart());
            count = Integer.parseInt(pageForm.getCount());
        } catch (NumberFormatException e) {
        }
        CatalogDelegate catalog = RequestUtils.getCatalog(request);
        Page page = catalog.getItems(pageForm.getParent(), start, count);
        pageForm.setPage(page);

        return (mapping.findForward("itemPage"));
    }


    public ActionForward showItem(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String itemId = request.getParameter(Constants.ITEM_PRM);

        CatalogDelegate catalog = RequestUtils.getCatalog(request);
        Item item = catalog.getItem(itemId);
        request.setAttribute(ITEM_KEY, item);
        request.setAttribute(ITEM_OID_KEY, catalog.getStringObjectID(item));
        return (mapping.findForward("itemDetailsPage"));
    }


}
