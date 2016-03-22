
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
import petstore.www.ContactForm;

public class OrderForm extends ActionForm {
    private ContactForm billInfo = new ContactForm();
    private ContactForm shipInfo = new ContactForm();

    public ContactForm getBillInfo() {
        return billInfo;
    }

    public void setBillInfo(ContactForm billInfo) {
        this.billInfo = billInfo;
    }

    public ContactForm getShipInfo() {
        return shipInfo;
    }

    public void setShipInfo(ContactForm shipInfo) {
        this.shipInfo = shipInfo;
    }
}
