
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

import com.versant.core.util.StringListParser;
import com.versant.core.util.StringList;

/**
 * User defined base metric.
 * @keep-all
 */
public class MdUserBaseMetric {

    private String name;
    private String displayName;
    private String category;
    private String description;
    private int calc = CALC_RAW;
    private int decimals;

    public static final int CALC_RAW = 1;
    public static final int CALC_AVERAGE = 2;
    public static final int CALC_DELTA = 3;
    public static final int CALC_DELTA_PER_SECOND = 4;

    public MdUserBaseMetric() {
    }

    public MdUserBaseMetric(String s) {
        loadSettings(s);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCalc() {
        return calc;
    }

    public void setCalc(int calc) {
        this.calc = calc;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String saveSettings() {
        StringList l = new StringList();
        l.append(name);
        l.appendQuoted(displayName);
        l.appendQuoted(category);
        l.appendQuoted(description);
        l.append(calc);
        l.append(decimals);
        return l.toString();
    }

    public void loadSettings(String s) {
        StringListParser lp = new StringListParser(s);
        name = lp.nextString();
        displayName = lp.nextQuotedString();
        category = lp.nextQuotedString();
        description = lp.nextQuotedString();
        calc = lp.nextInt();
        decimals = lp.nextInt();
    }

    public String toString() {
        return displayName;
    }

}

