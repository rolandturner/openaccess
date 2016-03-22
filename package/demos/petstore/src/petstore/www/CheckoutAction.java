
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

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.actions.DispatchAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.improve.struts.taglib.layout.util.FormUtils;
import petstore.www.ActionFormUtils;
import petstore.www.RequestUtils;
import petstore.model.*;
import petstore.db.*;

public class CheckoutAction extends DispatchAction {

    public ActionForward confirmCheckout(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {

        FormUtils.setFormDisplayMode(request, form, FormUtils.EDIT_MODE);
        Customer c = RequestUtils.getCustomer(request).getCustomer();
        OrderForm f = (OrderForm) form;
        ActionFormUtils.populateContactForm(f.getBillInfo(),c.getAccount().getContactInfo());
        ActionFormUtils.populateContactForm(f.getShipInfo(),c.getAccount().getContactInfo());
        RequestUtils.setCountries(request);
        RequestUtils.setCreditCards(request);
        return mapping.findForward("orderPage");
    }


    public ActionForward createOrder(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {


        OrderForm f = (OrderForm) form;
        Customer c = RequestUtils.getCustomer(request).getCustomer();

        ContactInfo billing = new ContactInfo();
        ActionFormUtils.populateContactInfo(f.getBillInfo(), billing);

        ContactInfo shipping = new ContactInfo();
        ActionFormUtils.populateContactInfo(f.getShipInfo(), shipping);

        CreditCard card = new CreditCard();
        card.copy(c.getAccount().getCreditCard());

        CartDelegate cartDel = RequestUtils.getCart(request);
        CartVO cart = cartDel.getCart();

        OrderDelegate ord =  RequestUtils.getOrderDelegate(request);
        String poid = ord.createOrder(c.getUser(), billing, shipping, card, cart);
        request.setAttribute(Constants.PURCHASE_ID,poid);

        cartDel.empty();

        return mapping.findForward("orderSubmittedPage");
    }
}
