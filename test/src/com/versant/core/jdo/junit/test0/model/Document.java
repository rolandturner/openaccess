
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
package com.versant.core.jdo.junit.test0.model;

import java.io.Serializable;

/**
 * @keep-all
 */
public class Document {

    private String title;
    private int id;
    private String text;
    private CompositePart part;  // <-> CompositePart.documentation

    public Document() {
    }

    public Document(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CompositePart getPart() {
        return part;
    }

    public void setPart(CompositePart part) {
        this.part = part;
    }

    public final static class Id implements Serializable {

        public int id;

        public Id() {
        }

        public Id(String idString) {
            id = Integer.parseInt(idString);
        }

        public int hashCode() {
            return id;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Id) {
                Id other = (Id)obj;
                if (other.id == id) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "" + id;
        }
    }

}

 
