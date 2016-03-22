
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
package com.versant.core.jdo.tools.workbench.model;

import java.util.List;
import java.util.Iterator;
import java.awt.*;

/**
 * The value for a property of an meta data object. This includes information
 * about where the value came from, an icon for it, a list of valid values
 * and a default value. This makes it easy to paint and edit these.
 * @keep-all
 */
public class MdValue implements Comparable 
   
{

    protected String text;
    protected String defText;
    protected List pickList;  // of String
    protected boolean onlyFromPickList = true;
    protected boolean caseSensitive = true;
    protected boolean warningOnError = false;
    protected Color defaultColor;
    protected Color color = Color.blue;
    protected boolean readOnly;

    /**
     * Text of NA value.
     */
    public static final String NAS = "-";

    /**
     * Value to indicate that a field is not applicable and may not be
     * edited.
     */
    public static final MdValue NA = new MdValue(NAS);

    static {
        NA.color = Color.gray;
        NA.readOnly = true;
    }

    public MdValue() {
    }

    public MdValue(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List getPickList() {
        return pickList;
    }

    public void setPickList(List pickList) {
        this.pickList = pickList;
    }

    public boolean isOnlyFromPickList() {
        return onlyFromPickList;
    }

    public void setOnlyFromPickList(boolean onlyFromPickList) {
        this.onlyFromPickList = onlyFromPickList;
    }

    public String getDefText() {
        return defText;
    }

    public void setDefText(String defText) {
        this.defText = defText;
    }

    public boolean isValid() {
        if (!onlyFromPickList || pickList == null) {
            return true;
        }
        // if we want the default to be repainted, then we must uncomment this section
        /*if (!caseSensitive && warningOnError){
            if (text != null){
                return containsCaseInsensitive(pickList, text);
            } else if (defText != null){
                return containsCaseInsensitive(pickList, defText);
            } else {
                return true;
            }
        }*/
        return text == null || (caseSensitive? pickList.contains(text) : containsCaseInsensitive(pickList,text));
    }

    private boolean containsCaseInsensitive(List pickList, String text){
        int index = -1;
        for (Iterator iter = pickList.iterator(); iter.hasNext();) {
            String pickStr = (String) iter.next();
            index = pickStr.indexOf(' ');
            if (index != -1){
                pickStr = pickStr.substring(0,index);
            }
            if (pickStr.equalsIgnoreCase(text)){
                return true;
            }
        }
        return false;

    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean equals(Object o) {
        if (o instanceof MdValue) {
            String t = ((MdValue)o).text;
            if (text == null && t == null) return true;
            if (text == null || t == null) return false;
            if (caseSensitive){
                return text.equals(t);
            } else {
                return text.equalsIgnoreCase(t);
            }
        } else {
            return false;
        }
    }

    public String toString() {
        if (text == null) return defText;
        else return text;
    }

    public int hashCode() {
        if (text == null) return 0;
        else return text.hashCode();
    }

    /**
     * Order MdValue's so NA is last.
     */
    public int compareTo(Object o) {
        MdValue v = (MdValue)o;
        if (this == NA) {
            if (v == NA) return 0;
            else return 1;
        } else {
            if (v == NA) return -1;
            String s = toString();
            String vs = v.toString();
            if (s == null) {
                if (vs == null) return 0;
                return -1;
            } else {
                if (vs == null) return 1;
                return s.compareTo(vs);
            }
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isWarningOnError() {
        return warningOnError;
    }

    public void setWarningOnError(boolean warningOnError) {
        this.warningOnError = warningOnError;
    }

    /**
     * This is called by MdValueEditor to format each entry in the pick list.
     * This gives subclasses an opportunity to change the way the pick list
     * is displayed.
     */
    public String formatPickListEntry(String s) {
        return s;
    }

    /**
     * This is called by MdValueEditor when an entry in the pick list is
     * selected to turn it into a String. This gives subclasses an
     * opportunity to change the way the pick list is displayed.
     */
    public String parsePickListEntry(String s) {
        return s;
    }

    /**
     * Get the default text as a bool ('true' is true, anything else is false).
     */
    public boolean getDefBool() {
        return defText != null && defText.equals("true");
    }

    /**
     * Get the text as a bool ('true' is true, anything else is false).
     */
    public boolean getBool() {
        return text != null && text.equals("true");
    }

}

