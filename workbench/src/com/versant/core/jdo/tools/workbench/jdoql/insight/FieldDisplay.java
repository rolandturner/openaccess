
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
package com.versant.core.jdo.tools.workbench.jdoql.insight;

import java.util.Hashtable;

/**
 * @keep-all
 */
public class FieldDisplay implements Comparable{
    public static int BIGGEST_LENGHT;
    public static final int SPACE_LENGHT = 4;
    public String name;
    public String type;
    public String methodSig;
    public FieldDisplay(String name , String type, String methodSig) {
        this.name = name;
        this.type = type;
        this.methodSig = methodSig;
    }

    public String getName() {
        return name;
    }

    public FieldDisplay getDisplay(){
        return this;
    }

    public String getDisplayName() {
        if (methodSig != null){
            return (replace(name,"()","("+methodSig+")")+pad()+type);
        } else {
            return name+pad()+type;
        }
    }

    private int getNameLenght(){
        if (methodSig != null){
            return replace(name,"()","("+methodSig+")").length();
        } else {
            return name.length();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return name;
    }

    public int compareTo(Object a) {
        return this.toString().compareTo(a.toString());
    }

    private int getLenght(){
        return getNameLenght() + getType().length();
    }

    public void setLenght(){
        if (BIGGEST_LENGHT < getLenght()){
            BIGGEST_LENGHT = getLenght();
        }
    }

    private String replace(String text, String repl, String with) {
        if (text == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer(text.length());
        int start = 0, end = 0;
        while ( (end = text.indexOf(repl, start)) != -1 ) {
            buf.append(text.substring(start, end)).append(with);
            start = end + repl.length();
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    private String pad() {
        int lenght = (BIGGEST_LENGHT - getLenght()) + SPACE_LENGHT;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < lenght; i++) {
            buf.append(" ");
        }
        return buf.toString();
    }


    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        return name.equals(((FieldDisplay)obj).name);
    }
}

