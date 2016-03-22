
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
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import petstore.www.*;
import petstore.model.CustomerDelegate;
import petstore.db.Customer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.improve.struts.taglib.layout.util.FormUtils;

/**
 */
public class CustomerAction extends DispatchAction {


    public ActionForward editCustomer(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {

        FormUtils.setFormDisplayMode(request, form, FormUtils.EDIT_MODE);
        return displCustomer(mapping, form, request, response);
    }

    public ActionForward showCustomer(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {

        FormUtils.setFormDisplayMode(request, form, FormUtils.INSPECT_MODE);
        return displCustomer(mapping, form, request, response);
    }

    ActionForward displCustomer(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {
        CustomerForm f = (CustomerForm)form;
        Customer c = RequestUtils.getCustomer(request).getCustomer();
        populateForm(f, c);
        RequestUtils.setCountries(request);
        RequestUtils.setCreditCards(request);

        return mapping.findForward("customerPage");
    }

    public ActionForward updateCustomer(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        final CustomerForm f = (CustomerForm)form;
        CustomerDelegate customerDelegate = RequestUtils.getCustomer(request);
        Customer c = customerDelegate.getCustomer();
        populateCustomer(f, c);
        customerDelegate.saveCustomer(c);
        return showCustomer(mapping, form, request, response);
    }


    private void populateForm(CustomerForm f, Customer c) {
        ContactForm cf = f.getContact();
        ActionFormUtils.populateContactForm(cf, c.getAccount().getContactInfo());

        f.setCardNumber(c.getAccount().getCreditCard().getCardNumber());
        f.setCardType(c.getAccount().getCreditCard().getCardType());

        if (c.getAccount().getCreditCard().getExpiryDate() != null) {
            DateFormat df = new SimpleDateFormat("MM");
            f.setExpiryMonth(df.format(c.getAccount().getCreditCard().getExpiryDate()));
            df = new SimpleDateFormat("yyyy");
            f.setExpiryYear(df.format(c.getAccount().getCreditCard().getExpiryDate()));
        }
    }


    private void populateCustomer(CustomerForm f, Customer c) throws Exception {
        ContactForm cf = f.getContact();

        ActionFormUtils.populateContactInfo(cf,c.getAccount().getContactInfo());

        c.getAccount().getCreditCard().setCardNumber(f.getCardNumber());
        c.getAccount().getCreditCard().setCardType(f.getCardType());


        String mmStr = f.getExpiryMonth();
        String yyStr = f.getExpiryYear();
        if ((mmStr != null ) && (yyStr != null)) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            Date expDate = df.parse("01-" + mmStr + "-" + yyStr);
            c.getAccount().getCreditCard().setExpiryDate(expDate);
        }
    }
}
