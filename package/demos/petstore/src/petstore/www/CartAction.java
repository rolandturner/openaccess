
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

import org.apache.struts.action.*;
import org.apache.struts.actions.DispatchAction;
import org.apache.commons.beanutils.PropertyUtils;
import petstore.model.CartDelegate;
import petstore.model.CartVO;
import petstore.model.CartItem;
import petstore.www.RequestUtils;
import petstore.www.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CartAction extends DispatchAction {


    public static final String CART_KEY = CartAction.class + ".cart";


    public ActionForward addItem(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String itemId = request.getParameter(Constants.ITEM_PRM);
        CartDelegate cart = RequestUtils.getCart(request);
        cart.addItem(itemId, 1);


        return showCart(mapping, form, request, response);

    }

    public ActionForward removeItem(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String itemId = request.getParameter(Constants.ITEM_PRM);
        CartDelegate cart = RequestUtils.getCart(request);
        cart.deleteItem(itemId);

        return showCart(mapping, form, request, response);

    }


    public ActionForward updateCart(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        System.out.println(">>>> CartAction.updateCart ");
        CartForm f = (CartForm) form;
        CartDelegate cart = RequestUtils.getCart(request);
        CartItem[] item = f.getCartItem();
        for (int i=0; i< item.length; i++) {
            cart.updateItemQuantity(item[i].getItemId(), item[i].getQuantity());
        }

        return showCart(mapping, form, request, response);

    }

    public ActionForward showCart(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        System.out.println(">>>> CartAction.showCart ");
        CartForm f = (CartForm) form;
        CartDelegate cart = RequestUtils.getCart(request);
        CartVO cartVO = cart.getCart();
        f.setCartItem((CartItem[])cartVO.getCart().toArray(new CartItem[cartVO.getCart().size()]));
        request.setAttribute(CART_KEY, cartVO);

        return mapping.findForward("cartPage");
    }


}
