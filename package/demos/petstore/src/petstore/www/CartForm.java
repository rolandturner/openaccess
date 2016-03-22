
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

import org.apache.struts.action.ActionForm;
import petstore.model.CartItem;

import java.util.Collection;

/**
 */
public class CartForm extends ActionForm {

    private CartItem[] cartItem;

    public CartItem[] getCartItem() {
        return cartItem;
    }

    public void setCartItem(CartItem[] item) {
        this.cartItem = item;
    }

}
