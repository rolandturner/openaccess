
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
package customtypes.model;

/**
 * A Contact in an address book.
 * $Id: Contact.java,v 1.1 2005/03/08 08:31:42 david Exp $
 */
public class Contact {

    private String name;
    private String email;
    private PhoneNumber phone;
    private PngImage pngImage;

    public Contact() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public void setPhone(PhoneNumber phone) {
        this.phone = phone;
    }

    public PngImage getPngImage() {
        return pngImage;
    }

    public void setPngImage(PngImage pngImage) {
        this.pngImage = pngImage;
    }

    public String toString() {
        return name;
    }
}

