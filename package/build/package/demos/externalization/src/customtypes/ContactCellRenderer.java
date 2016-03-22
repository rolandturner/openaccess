
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
package customtypes;

import customtypes.model.Contact;

import javax.swing.*;
import java.awt.*;

/**
 * This paints Contact's on lists with their pictures.
 */
public class ContactCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Contact c = (Contact)value;
        setIcon(c.getPngImage());
        setText("<html><body>" +
                c.getName() + "<br>" +
                c.getEmail() + "<br>" +
                c.getPhone() +
                "</body></html>");
        return this;
    }

}

