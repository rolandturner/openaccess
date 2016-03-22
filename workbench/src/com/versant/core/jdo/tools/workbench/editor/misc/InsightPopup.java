
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
package com.versant.core.jdo.tools.workbench.editor.misc;


import javax.swing.*;
import java.awt.*;

/**
 * @keep-all
 */
public class InsightPopup extends JPopupMenu {

//    static Config cfg = Config.getInstance(InsightPopup.class);

    private SqlInsightBody body;

    public InsightPopup() {
        try {
            init();
        } catch (Exception e) {
            // do nothing
        }
    }

    private void init() throws Exception {
        this.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        this.setRequestFocusEnabled(false);
    }

    public SqlInsightBody getBody() {
        return body;
    }

    public void setBody(SqlInsightBody body) {
        int ind = getComponentIndex(this.body);

        if (ind != -1) {
            remove(this.body);
        }
        this.body = body;
        this.add(this.body);

    }

    /**
     * Set the index of the selected object in our data list.
     */
    public void setSelectedIndex(int index) {
        body.setSelectedIndex(index);
    }

    /**
     * Get the index of the selected object in our data list.
     */
    public int getSelectedIndex() {
        return body.getSelectedIndex();
    }

    /**
     * Move the selected value in the list up by one.
     */
    public void moveUp() {
        body.moveUp();
    }

    /**
     * Move the selected value in the list down by one.
     */
    public void moveDown() {
        body.moveDown();
    }

    /**
     * Move the selected value in the list up by one page.
     */
    public void movePageUp() {
        body.movePageUp();
    }

    /**
     * Move the selected value in the list down by one page.
     */
    public void movePageDown() {
        body.movePageDown();
    }

    /**
     * Get the size of our data list.
     */
    public int getListSize() {
        return body.getListSize();
    }

    /**
     * Get the selected value in the list.
     */
    public Object getSelectedValue() {
        return body.getSelectedValue();
    }

    /**
     * Jumps to the entry in the list that most resembles String word and
     * highlights it.
     */
    public void setInsightWord(String word) {
        body.setInsightWord(word);
    }
}
